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
package io.zeebe.dispatcher.impl.log;

import static org.agrona.BitUtil.CACHE_LINE_LENGTH;
import static org.agrona.BitUtil.SIZE_OF_INT;

/**
 * Describes data layout in the log buffer
 *
 * <pre>
 *  +----------------------------+
 *  |        Partition 0         |
 *  +----------------------------+
 *  |        Partition 1         |
 *  +----------------------------+
 *  |        Partition 2         |
 *  +----------------------------+
 *  |   Partition Meta Data 0    |
 *  +----------------------------+
 *  |   Partition Meta Data 1    |
 *  +----------------------------+
 *  |   Partition Meta Data 2    |
 *  +----------------------------+
 *  |        Log Meta Data       |
 *  +----------------------------+
 * </pre>
 */
public class LogBufferDescriptor {

  /** The number of Partitions the log is divided into */
  public static final int PARTITION_COUNT = 3;

  /** Minimum buffer length for a Partition */
  public static final int PARTITION_MIN_LENGTH = 64 * 1024;

  // ----------------------------------------------------------
  // Partition Metadata constants

  /** A Partition which is clean or in use. */
  public static final int PARTITION_CLEAN = 0;

  /** A Partition is dirty and requires cleaning. */
  public static final int PARTITION_NEEDS_CLEANING = 1;

  /** Offset within the Partition meta data where the tail value is stored. */
  public static final int PARTITION_TAIL_COUNTER_OFFSET;

  /** Offset within the Partition meta data where current status is stored */
  public static final int PARTITION_STATUS_OFFSET;

  /** Total length of the Partition meta data buffer in bytes. */
  public static final int PARTITION_META_DATA_LENGTH;

  static {
    int offset = (CACHE_LINE_LENGTH * 2);
    PARTITION_TAIL_COUNTER_OFFSET = offset;

    offset += (CACHE_LINE_LENGTH * 2);
    PARTITION_STATUS_OFFSET = offset;

    offset += (CACHE_LINE_LENGTH * 2);
    PARTITION_META_DATA_LENGTH = offset;
  }

  // --------------------------------------------
  // log metadata constants

  /** Offset within the log meta data where the current publisher limit is stored. */
  public static final int LOG_PUBLISHER_LIMIT_OFFSET;

  /** Offset within the log meta data where the active Partition id is stored. */
  public static final int LOG_ACTIVE_PARTITION_ID_OFFSET;

  /** Offset within the log meta data where the active Partition id is stored. */
  public static final int LOG_INITIAL_PARTITION_ID_OFFSET;

  /** Offset within the log meta data which the MTU length is stored; */
  public static final int LOG_MAX_FRAME_LENGTH_OFFSET;

  static {
    int offset = 0;
    LOG_PUBLISHER_LIMIT_OFFSET = offset;
    offset += (CACHE_LINE_LENGTH * 2);

    LOG_ACTIVE_PARTITION_ID_OFFSET = offset;
    offset += (CACHE_LINE_LENGTH * 2);

    LOG_INITIAL_PARTITION_ID_OFFSET = offset;
    offset += SIZE_OF_INT;

    LOG_MAX_FRAME_LENGTH_OFFSET = offset;
    offset += SIZE_OF_INT;

    LOG_META_DATA_LENGTH = offset;
  }

  /**
   * Total length of the log meta data buffer in bytes.
   *
   * <pre>
   *   0                   1                   2                   3
   *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   *  |                       Publisher limit                         |
   *  |                                                               |
   *  +---------------------------------------------------------------+
   *  |                     Cache Line Padding                      ...
   * ...                                                              |
   *  +---------------------------------------------------------------+
   *  |                     Active Partition Id                       |
   *  +---------------------------------------------------------------+
   *  |                     Cache Line Padding                      ...
   * ...                                                              |
   *  +---------------------------------------------------------------+
   *  |                     Initial Partition Id                      |
   *  +---------------------------------------------------------------+
   *  |                       Max Frame Length                        |
   *  +---------------------------------------------------------------+
   * </pre>
   */
  public static final int LOG_META_DATA_LENGTH;

  /**
   * Calculates the required capacity for a log buffer which can hold up to the provided bytes of
   * data.
   */
  public static int requiredCapacity(int partitionSize) {
    return (PARTITION_COUNT * (partitionSize + PARTITION_META_DATA_LENGTH)) + LOG_META_DATA_LENGTH;
  }

  public static int partitionMetadataSectionOffset(int partitionSize, int partitionNumber) {
    return (PARTITION_COUNT * partitionSize) + (partitionNumber * PARTITION_META_DATA_LENGTH);
  }

  public static int partitionDataSectionOffset(int partitionSize, int partitionNumber) {
    return partitionNumber * partitionSize;
  }

  public static int logMetadataOffset(int partitionSize) {
    return (PARTITION_COUNT * partitionSize) + (PARTITION_COUNT * PARTITION_META_DATA_LENGTH);
  }
}
