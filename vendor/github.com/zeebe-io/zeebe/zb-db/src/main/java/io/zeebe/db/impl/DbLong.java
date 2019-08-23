/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.db.impl;

import static io.zeebe.db.impl.ZeebeDbConstants.ZB_DB_BYTE_ORDER;

import io.zeebe.db.DbKey;
import io.zeebe.db.DbValue;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class DbLong implements DbKey, DbValue {

  private long longValue;

  public void wrapLong(long value) {
    longValue = value;
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {
    longValue = buffer.getLong(offset, ZB_DB_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Long.BYTES;
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset, longValue, ZB_DB_BYTE_ORDER);
  }

  public long getValue() {
    return longValue;
  }
}