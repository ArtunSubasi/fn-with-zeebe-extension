/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.gateway.impl.broker.response;

public class BrokerErrorResponse<T> extends BrokerResponse<T> {

  private final BrokerError error;

  public BrokerErrorResponse(BrokerError error) {
    super();
    this.error = error;
  }

  @Override
  public boolean isError() {
    return true;
  }

  @Override
  public BrokerError getError() {
    return error;
  }

  @Override
  public String toString() {
    return "BrokerErrorResponse{" + "error=" + error + '}';
  }
}
