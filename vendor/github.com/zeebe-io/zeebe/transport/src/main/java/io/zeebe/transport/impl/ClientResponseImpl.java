/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.transport.impl;

import io.zeebe.transport.ClientResponse;
import io.zeebe.transport.RemoteAddress;
import org.agrona.DirectBuffer;

public class ClientResponseImpl implements ClientResponse {
  private final RemoteAddress remoteAddres;
  private final long requestId;
  private final DirectBuffer responseBuffer;

  public ClientResponseImpl(IncomingResponse incomingResponse, RemoteAddress remoteAddress) {
    this.remoteAddres = remoteAddress;
    this.requestId = incomingResponse.getRequestId();
    this.responseBuffer = incomingResponse.getResponseBuffer();
  }

  @Override
  public RemoteAddress getRemoteAddress() {
    return remoteAddres;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public DirectBuffer getResponseBuffer() {
    return responseBuffer;
  }
}
