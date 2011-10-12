// Copyright 2011 Google Inc. All rights reserved.

package com.google.appengine.api.files;

import static com.google.appengine.api.files.RecordConstants.unmaskCrc;

import com.google.appengine.api.files.RecordConstants.RecordType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

final class RecordReadChannelImpl implements RecordReadChannel {
  private Logger log = Logger.getLogger(RecordReadChannelImpl.class.getName());

  static final class RecordReadException extends Exception {
    public RecordReadException(String errorMessage) {
      super(errorMessage);
    }
  }

  private static final class Record {
    private final ByteBuffer data;
    private final RecordType type;

    public Record(RecordType type, ByteBuffer data) {
      this.type = type;
      this.data = data;
    }
    public ByteBuffer data() {
      return this.data;
    }
    public RecordType type() {
      return this.type;
    }
  }

  private final FileReadChannel input;
  private ByteBuffer blockBuffer;
  private ByteBuffer finalRecord;

  /**
   * @param input a {@link FileReadChannel} that holds Records to read from.
   */
  RecordReadChannelImpl(FileReadChannel input) {
    this.input = input;
    blockBuffer = ByteBuffer.allocate(RecordConstants.BLOCK_SIZE);
    blockBuffer.order(ByteOrder.LITTLE_ENDIAN);
    finalRecord = ByteBuffer.allocate(RecordConstants.BLOCK_SIZE);
    finalRecord.order(ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ByteBuffer readRecord() throws IOException {
    finalRecord.clear();
    RecordType lastRead = RecordType.NONE;
    while (true) {
      try {
        Record record = readPhysicalRecord();
        System.out.println("Record: " + record);
        if (record == null) {
          return null;
        }
        switch (record.type()) {
          case NONE:
            sync();
            break;
          case FULL:
            if (lastRead != RecordType.NONE) {
              throw new RecordReadException("Invalid RecordType: "
                + record.type);
            }
            System.out.println("Record position: " + record.data().position());
            System.out.println("Record limit   : " + record.data().limit());
            System.out.println("Record capacity: " + record.data().capacity());
            return record.data().slice();
          case FIRST:
            if (lastRead != RecordType.NONE) {
              throw new RecordReadException("Invalid RecordType: "
                  + record.type);
            }
            finalRecord = appendToBuffer(finalRecord, record.data());
            break;
          case MIDDLE:
            if (lastRead == RecordType.NONE) {
              throw new RecordReadException("Invalid RecordType: "
                + record.type);
            }
            finalRecord = appendToBuffer(finalRecord, record.data());
            break;
          case LAST:
            if (lastRead == RecordType.NONE) {
              throw new RecordReadException("Invalid RecordType: "
                + record.type);
            }
            finalRecord = appendToBuffer(finalRecord, record.data());
            finalRecord.flip();
            return finalRecord.slice();
          default:
            throw new RecordReadException("Invalid RecordType: " + record.type.value());
        }
        lastRead = record.type();
      } catch (RecordReadException e) {
        log.warning(e.getMessage());
        finalRecord.clear();
        sync();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long position() throws IOException {
    return input.position();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void position(long newPosition) throws IOException {
    input.position(newPosition);
  }

  /**
   * Reads the next record from the RecordIO data stream.
   *
   * @return Record data about the physical record read.
   * @throws IOException
   */
  private Record readPhysicalRecord()
          throws IOException, RecordReadException {
    int bytesToBlockEnd = (int) (RecordConstants.BLOCK_SIZE -
        (input.position() % RecordConstants.BLOCK_SIZE));

    if (bytesToBlockEnd < RecordConstants.HEADER_LENGTH) {
      return new Record(RecordType.NONE, null);
    }

    blockBuffer.clear();
    blockBuffer.limit(RecordConstants.HEADER_LENGTH);
    int bytesRead = input.read(blockBuffer);
    if (bytesRead != RecordConstants.HEADER_LENGTH) {
      return null;
    }
    blockBuffer.flip();
    int checksum = blockBuffer.getInt();
    short length = blockBuffer.getShort();
    RecordType type = RecordType.get(blockBuffer.get());
    if (length > bytesToBlockEnd || length < 0) {
      throw new RecordReadException("Length is too large:" + length);
    }

    blockBuffer.clear();
    blockBuffer.limit(length);
    bytesRead = input.read(blockBuffer);
    if (bytesRead != length) {
      return null;
    }
    if (!isValidCrc(checksum, blockBuffer, type.value())) {
      throw new RecordReadException("Checksum doesn't validate.");
    }

    blockBuffer.flip();
    return new Record(type, blockBuffer);
  }

  /**
   * Moves to the start of the next block.
   * @throws IOException
   */
  private void sync() throws IOException {
    long padLength = RecordConstants.BLOCK_SIZE -
        (input.position() % RecordConstants.BLOCK_SIZE);
    input.position(input.position() + padLength);
  }

  /**
   * Validates that the {@link Crc32c} validates.
   * @param checksum the checksum in the record.
   * @param data the {@link ByteBuffer} of the data in the record.
   * @param type the byte representing the {@link RecordType} of the record.
   * @return true if the {@link Crc32c} validates.
   */
  private static boolean isValidCrc(int checksum, ByteBuffer data, byte type) {
    Crc32c crc = new Crc32c();
    crc.update(type);
    crc.update(data.array(), 0, data.limit());

    return unmaskCrc(checksum) == crc.getValue();
  }

  /**
   * Appends a {@link ByteBuffer} to another. This may modify
   * the inputed buffer that will be appended to.
   * @param to the {@link ByteBuffer} to append to.
   * @param from the {@link ByteBuffer} to append.
   * @return the resulting appended {@link ByteBuffer}
   */
  private static ByteBuffer appendToBuffer(ByteBuffer to, ByteBuffer from) {
    if (to.remaining() < from.remaining()) {
      int capacity = to.capacity();
      while (capacity - to.position() < from.remaining()) {
        capacity *= 2;
      }
      ByteBuffer newBuffer = ByteBuffer.allocate(capacity);
      to.flip();
      newBuffer.put(to);
      to = newBuffer;
      to.order(ByteOrder.LITTLE_ENDIAN);
    }
    to.put(from);
    return to;
  }

}
