// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appengine.api.files;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile.FileSystem;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@code AppEngineFile} represents a file in one of the Google App Engine
 * file systems.
 * <p>
 * A file has a <b>path</b> of the form {@code /<fileSystem>/<namePart> }. The
 * path consists of a <b>file system</b> which is one of several identifiers for
 * a Google App Engine file system, and a <b>name part</b> wich is an arbitrary
 * String. For example "/blobstore/Aie7uHVwtvM" is a path in which "blobstore"
 * is the file system and "Aie7uHVwtvM" is the name part.
 * <p>
 * The enum {@link FileSystem} represents the available file systems. Each file
 * system has particular attributes regarding permanence, reliability
 * availability, and cost.
 * <p>
 * In the current release of Google App Engine, {@link FileSystem#BLOBSTORE
 * BLOBSTORE} and {@link FileSystem#GS GS} are the only available file systems.
 * These file systems store files as blobs in the BlobStore and in Google Storage
 * respectively.
 * <p>
 * App Engine files may only be accessed using a particular access pattern:
 * Newly created files may be <b>appended</b> to until they are
 * <b>finalized</b>. After a file is finalized it may be read, but it may no
 * longer be written.
 * <p>
 * To create a new {@code BLOBSTORE} file use
 * {@link FileService#createNewBlobFile(String)}. This returns an instance of
 * {@code AppEngineFile} with a {@code FileSystem} of {@code BLOBSTORE}.
 *
 * <p>
 * To create a new {@code GS} file use
 * {@link FileService#createNewGSFile(GSFileOptions)}. This returns an
 * instance of {@code AppEngineFile} with a {@code FileSystem} of {@code GS}.
 * This instance cannot be used for reading. For full file lifecycle
 * example {@see FileService}
 *
 */
public class AppEngineFile implements Serializable {

  private static final String fullPathRegex = "^/([^/]+)/(.+)$";
  private static final Pattern fullPathPattern = Pattern.compile(fullPathRegex);

  /**
   * Represents the back-end storage location of a file. In the current release
   * there is only one file system available, the BlobStore.
   */
  public static enum FileSystem {
    /**
     * This file system stores files as blobs in the App Engine BlobStore. The
     * full path of a file from this file system is of the form
     * "/blobstore/<identifier>" where <identifier> is an opaque String
     * generated by the BlobStore.
     */
    BLOBSTORE("blobstore"),

    /**
     * Files in this file system use one path for writing and one path for
     * reading. The full path for a writable GS file is "/gs/<identifier>"
     * where <identifier> is an opaque String generated by Google Storage. The
     * full path for a readable GS file is "/gs/bucket/key". See comments at
     * the top of {@link FileService}.
     */
    GS("gs");

    private String name;

    private FileSystem(String fsn) {
      this.name = fsn;
    }

    /**
     * Returns the name of the file system.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the {@code FileSystem} with the given name.
     *
     * @throws IllegalArgumentException if the given name is not the name of any
     *         of the file systems.
     */
    public static FileSystem fromName(String name) {
      for (FileSystem fs : FileSystem.values()) {
        if (fs.getName().equals(name)) {
          return fs;
        }
      }
      throw new IllegalArgumentException(name + " is not the name of a file system.");
    }
  }

  private String namePart;
  private String fullPath;
  private FileSystem fileSystem;

  private BlobKey cachedBlobKey;

  /**
   * Constructs an {@code AppEngineFile} from the given data
   *
   * @param fileSystem a {@code non-null FileSystem}.
   * @param namePart a {@code non-null} name part. Warning: Do not use the full
   *        path here.
   */
  public AppEngineFile(FileSystem fileSystem, String namePart) {
    this("/" + fileSystem.getName() + "/" + checkNamePart(namePart), fileSystem, namePart);
  }

  private static String checkNamePart(String namePart) {
    Preconditions.checkNotNull(namePart, "namePart");
    namePart = namePart.trim();
    Preconditions.checkArgument(!namePart.isEmpty(), "namePart is empty");
    return namePart;
  }

  /**
   * Constructs an {@code AppEngineFile} from the given data
   *
   * @param fullPath a {@code non-null} full path. Warning: Do not use a name
   *        part here.
   */
  public AppEngineFile(String fullPath) {
    this(fullPath, null, null);
  }

  /**
   * Constructs an {@code AppEngineFile} from the given data.
   *
   * @param fullPath the {@code non-null} full path.
   * @param fileSystem if this is {@code null} it will be parsed from {@code
   *        fullPath} and an {@code IllegalArgumentException} will be thrown if
   *        the parsing fails. If this is not {@code null} it is the caller's
   *        responsibility to ensure that it matches {@code fullPath}, no
   *        checking will be done.
   * @param namePart The same comment from {@code fileSystem} applies to this
   *        parameter.
   */
  private AppEngineFile(String fullPath, FileSystem fileSystem, String namePart) {
    Preconditions.checkNotNull(fullPath, "fullPath");
    fullPath = fullPath.trim();
    Preconditions.checkArgument(!fullPath.isEmpty(), "fullPath is empty");
    this.namePart = namePart;
    this.fullPath = fullPath;
    this.fileSystem = fileSystem;
    if (null == fileSystem || null == namePart) {
      parseFullPath();
    }
  }

  /**
   * Throws {@code IllegalArgumentException} if {@code fullPath} cannot be
   * parsed into a file system and a name part.
   */
  private void parseFullPath() {
    Matcher m = fullPathPattern.matcher(fullPath);
    if (!m.matches()) {
      throw new IllegalArgumentException(fullPath + " is not a valid path");
    }
    String fileSystemString = m.group(1);
    fileSystem = FileSystem.fromName(fileSystemString);
    namePart = m.group(2);
  }

  /**
   * Returns the name part of the file.
   */
  public String getNamePart() {
    return namePart;
  }

  /**
   * Returns the full path of the file.
   */
  public String getFullPath() {
    return fullPath;
  }

  /**
   * Returns the file system of the file.
   */
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public String toString() {
    return fullPath;
  }

  BlobKey getCachedBlobKey(){
    return cachedBlobKey;
  }

  void setCachedBlobKey(BlobKey key){
    this.cachedBlobKey = key;
  }

  /**
   * Returns a boolean indicating whether or not this instance can be used for
   * writing.
   */
  public boolean isWritable() {
    if (fileSystem == FileSystem.GS) {
      return namePart.startsWith(FileServiceImpl.GS_CREATION_HANDLE_PREFIX);
    }
    return true;
  }

  /**
   * Returns a boolean indicating whether or not this instance can be used for
   * reading.
   */
  public boolean isReadable() {
    if (fileSystem == FileSystem.GS) {
      return !namePart.startsWith(FileServiceImpl.GS_CREATION_HANDLE_PREFIX);
    }
    return true;
  }
}
