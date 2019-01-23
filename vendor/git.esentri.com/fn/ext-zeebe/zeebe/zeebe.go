package zeebe

import (
	"github.com/fnproject/fn/api/server"
	"github.com/fnproject/fn/fnext"
	"log" // TODO log as fn logs
	"time"
	"os"
	"errors"
)

// Extension for Zeebe integration
func init() {
	server.RegisterExtension(&Zeebe{})
}

type Zeebe struct {
}

func (zeebe *Zeebe) Name() string {
	return "git.esentri.com/fn/ext-zeebe/zeebe"
}

func (zeebe *Zeebe) Setup(s fnext.ExtServer) error {
	// The the extension should only be set up for the FnServer modes "Full" and "API Server"
	// At the moment, checking this is not possible because the node type is not exposed in the ExtServer or Server types.
	// This is not a big problem, because different FnServer Modes must be build separately.
	// Therefore only the API Server must be build with the Zeebe extension. All other parts must be built without the extension.
	log.Println("Zeebe integration setup!")

	loadBalancerAddr := os.Getenv("FN_LB_URL")
	if loadBalancerAddr == "" {
		return errors.New("Zeebe: The load balancer address FN_LB_URL ist not configured. The zeebe extension could not start.")
	}

	apiServerAddr := os.Getenv("FN_API_SERVER_URL")
	if apiServerAddr == "" {
		return errors.New("Zeebe: The API server address FN_API_SERVER_URL ist not configured. The zeebe extension could not start.")
	}

	// TODO in the future, the Zeebe Gateway address could be read using the apps or the functions to increase flexibility
	zeebeGatewayAddr := os.Getenv("FN_ZEEBE_GATEWAY_URL")
	if zeebeGatewayAddr == "" {
		return errors.New("Zeebe: The Zeebe Gateway address FN_ZEEBE_GATEWAY_URL ist not configured. The zeebe extension could not start.")
	}

	server := s.(*server.Server) // TODO this type assertion is hacky. ExtServer should implement the AddFnListener interface.
	jobWorkerRegistry := NewJobWorkerRegistry(loadBalancerAddr, zeebeGatewayAddr)
	server.AddFnListener(&FnListener{&jobWorkerRegistry})

	// TODO we eventually also need an App Listener. If an App gets deletes, all functions within are deleted as well.
	// All Job workers of the app have to be stopped.

	// TODO Coolness factor: register a new Endpoint using the ExtServer interface which lists all registered functions and their zeebe job types
	// so that we may show a simple UI of the job workers and their connections

	go zeebe.waitAndRegisterFunctions(&jobWorkerRegistry, apiServerAddr)
	return nil
}

func (zeebe *Zeebe) waitAndRegisterFunctions(jobWorkerRegistry *JobWorkerRegistry, apiServerAddr string) {
	// Waiting for the REST endpoints to come up before querying for functions since the Extension Setup does not have any callback such as OnServerStarted
	// TODO Get in touch with the Fn Project: Create a feature request, maybe a pull request
	time.Sleep(1 * time.Second)
	functionsWithZeebeJobType := GetFunctionsWithZeebeJobType(apiServerAddr)
	for _, fn := range functionsWithZeebeJobType {
		jobWorkerRegistry.RegisterFunctionAsWorker(fn.fnID, fn.jobType)
	}
}
