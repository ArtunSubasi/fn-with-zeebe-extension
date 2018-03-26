package agent

import (
	"fmt"
	"math"
	"os"
	"strconv"
	"time"
)

type AgentConfig struct {
	MinDockerVersion   string        `json:"min_docker_version"`
	FreezeIdle         time.Duration `json:"freeze_idle_msecs"`
	EjectIdle          time.Duration `json:"eject_idle_msecs"`
	HotPoll            time.Duration `json:"hot_poll_msecs"`
	HotLauncherTimeout time.Duration `json:"hot_launcher_timeout_msecs"`
	AsyncChewPoll      time.Duration `json:"async_chew_poll_msecs"`
	MaxResponseSize    uint64        `json:"max_response_size_bytes"`
	MaxLogSize         uint64        `json:"max_log_size_bytes"`
	MaxTotalCPU        uint64        `json:"max_total_cpu_mcpus"`
	MaxTotalMemory     uint64        `json:"max_total_memory_bytes"`
	MaxFsSize          uint64        `json:"max_fs_size_mb"`
	PreForkPoolSize    uint64        `json:"pre_fork_pool_size"`
	PreForkImage       string        `json:"pre_fork_image"`
	PreForkCmd         string        `json:"pre_fork_pool_cmd"`
}

const (
	EnvFreezeIdle         = "FN_FREEZE_IDLE_MSECS"
	EnvEjectIdle          = "FN_EJECT_IDLE_MSECS"
	EnvHotPoll            = "FN_HOT_POLL_MSECS"
	EnvHotLauncherTimeout = "FN_HOT_LAUNCHER_TIMEOUT_MSECS"
	EnvAsyncChewPoll      = "FN_ASYNC_CHEW_POLL_MSECS"
	EnvMaxResponseSize    = "FN_MAX_RESPONSE_SIZE"
	EnvMaxLogSize         = "FN_MAX_LOG_SIZE_BYTES"
	EnvMaxTotalCPU        = "FN_MAX_TOTAL_CPU_MCPUS"
	EnvMaxTotalMemory     = "FN_MAX_TOTAL_MEMORY_BYTES"
	EnvMaxFsSize          = "FN_MAX_FS_SIZE_MB"
	EnvPreForkPoolSize    = "FN_EXPERIMENTAL_PREFORK_POOL_SIZE"
	EnvPreForkImage       = "FN_EXPERIMENTAL_PREFORK_IMAGE"
	EnvPreForkCmd         = "FN_EXPERIMENTAL_PREFORK_CMD"

	MaxDisabledMsecs = time.Duration(math.MaxInt64)
)

func NewAgentConfig() (*AgentConfig, error) {

	cfg := &AgentConfig{
		MinDockerVersion: "17.06.0-ce",
		MaxLogSize:       1 * 1024 * 1024,
	}

	var err error

	err = setEnvMsecs(err, EnvFreezeIdle, &cfg.FreezeIdle, 50*time.Millisecond)
	err = setEnvMsecs(err, EnvEjectIdle, &cfg.EjectIdle, 1000*time.Millisecond)
	err = setEnvMsecs(err, EnvHotPoll, &cfg.HotPoll, 200*time.Millisecond)
	err = setEnvMsecs(err, EnvHotLauncherTimeout, &cfg.HotLauncherTimeout, time.Duration(60)*time.Minute)
	err = setEnvMsecs(err, EnvAsyncChewPoll, &cfg.AsyncChewPoll, time.Duration(60)*time.Second)
	err = setEnvUint(err, EnvMaxResponseSize, &cfg.MaxResponseSize)
	err = setEnvUint(err, EnvMaxLogSize, &cfg.MaxLogSize)
	err = setEnvUint(err, EnvMaxTotalCPU, &cfg.MaxTotalCPU)
	err = setEnvUint(err, EnvMaxTotalMemory, &cfg.MaxTotalMemory)
	err = setEnvUint(err, EnvMaxFsSize, &cfg.MaxFsSize)
	err = setEnvUint(err, EnvPreForkPoolSize, &cfg.PreForkPoolSize)

	if err != nil {
		return cfg, err
	}

	cfg.PreForkImage = os.Getenv(EnvPreForkImage)
	cfg.PreForkCmd = os.Getenv(EnvPreForkCmd)

	if cfg.EjectIdle == time.Duration(0) {
		return cfg, fmt.Errorf("error %s cannot be zero", EnvEjectIdle)
	}
	if cfg.MaxLogSize > math.MaxInt64 {
		// for safety during uint64 to int conversions in Write()/Read(), etc.
		return cfg, fmt.Errorf("error invalid %s %v > %v", EnvMaxLogSize, cfg.MaxLogSize, math.MaxInt64)
	}

	return cfg, nil
}

func setEnvUint(err error, name string, dst *uint64) error {
	if err != nil {
		return err
	}
	if tmp := os.Getenv(name); tmp != "" {
		val, err := strconv.ParseUint(tmp, 10, 64)
		if err != nil {
			return fmt.Errorf("error invalid %s=%s", name, tmp)
		}
		*dst = val
	}
	return nil
}

func setEnvMsecs(err error, name string, dst *time.Duration, defaultVal time.Duration) error {
	if err != nil {
		return err
	}

	*dst = defaultVal

	if dur := os.Getenv(name); dur != "" {
		durInt, err := strconv.ParseInt(dur, 10, 64)
		if err != nil {
			return fmt.Errorf("error invalid %s=%s err=%s", name, dur, err)
		}
		// disable if negative or set to msecs specified.
		if durInt < 0 || time.Duration(durInt) >= MaxDisabledMsecs/time.Millisecond {
			*dst = MaxDisabledMsecs
		} else {
			*dst = time.Duration(durInt) * time.Millisecond
		}
	}

	return nil
}