// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.files;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A @{link WritableByteChannel} for writing records to an {@link FileWriteChannel}.
 * <p>
 * The format of these records is defined by the leveldb log format:
 * http://leveldb.googlecode.com/svn/trunk/doc/log_format.txt
 * </p>
 * <p>
 * An instance of @{link RecordWriteChannel} may be obtained from the method:
 * @{link {@link FileService#openRecordWriteChannel(AppEngineFile, boolean)}.
 * </p>
 *
 */
public interface RecordWriteChannel extends WritableByteChannel {

    /**
     * Writes the data out to FileWriteChannel.
     * @see com.google.appengine.api.files.FileWriteChannel#write(ByteBuffer, String)
     */
    public int write(ByteBuffer src, String sequenceKey) throws IOException;

    /**
     * Closes the file.
     * @see com.google.appengine.api.files.FileWriteChannel#closeFinally()
     */
    public void closeFinally() throws IllegalStateException, IOException;
}
