// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

import com.google.appengine.api.utils.FutureWrapper;
import com.google.apphosting.api.logservice.LogServicePb.LogOffset;
import com.google.apphosting.api.logservice.LogServicePb.LogReadRequest;
import com.google.apphosting.api.logservice.LogServicePb.LogReadResponse;
import com.google.apphosting.api.ApiProxy;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * {@code LogServiceImpl} is an implementation of {@link LogService}
 * that makes API calls to {@link ApiProxy}.
 *
 */
final class LogServiceImpl implements LogService {
  static final String PACKAGE = "logservice";
  static final String READ_RPC_NAME = "Read";

  @Override
  public LogQueryResult fetch(LogQuery query) {
    try {
      return fetchAsync(query).get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof LogServiceException) {
        throw (LogServiceException) e.getCause();
      } else {
        throw new LogServiceException(e.getMessage());
      }
    } catch (InterruptedException e) {
      throw new LogServiceException(e.getMessage());
    }
  }

  Future<LogQueryResult> fetchAsync(LogQuery query) {
    LogReadRequest request = new LogReadRequest();

    request.setAppId(ApiProxy.getCurrentEnvironment().getAppId());

    Long startTimeUs = query.getStartTimeUsec();
    if (startTimeUs != null) {
      request.setStartTime(startTimeUs);
    }

    Long endTimeUs = query.getEndTimeUsec();
    if (endTimeUs != null) {
      request.setEndTime(endTimeUs);
    }

    request.setCount(query.getBatchSize());

    if (query.getMinLogLevel() != null) {
      request.setMinimumLogLevel(query.getMinLogLevel().ordinal());
    }

    request.setIncludeIncomplete(query.getIncludeIncomplete());
    request.setIncludeAppLogs(query.getIncludeAppLogs());

    List<String> versionIds = query.getMajorVersionIds();
    String[] convertedVersionIds;
    if (versionIds.isEmpty()) {
      String currentVersionId = ApiProxy.getCurrentEnvironment()
          .getVersionId();
      String majorVersionId = currentVersionId.split("\\.")[0];
      convertedVersionIds = new String[]{ majorVersionId };
    } else {
      convertedVersionIds = versionIds.toArray(new String[] {});
    }

    for (String versionId : convertedVersionIds) {
      request.addVersionId(versionId);
    }

    String offset = query.getOffset();
    if (offset != null) {
      LogOffset logOffset = new LogOffset();
      logOffset.setRequestId(offset);
      request.setOffset(logOffset);
    }

    final LogQuery finalizedQuery = query;
    ApiProxy.ApiConfig apiConfig = new ApiProxy.ApiConfig();

    Future<byte[]> responseBytes = ApiProxy.makeAsyncCall(PACKAGE,
      READ_RPC_NAME, request.toByteArray(), apiConfig);
    return new FutureWrapper<byte[], LogQueryResult>(responseBytes) {
      @Override
      protected LogQueryResult wrap(byte[] responseBytes) {
        LogReadResponse response = new LogReadResponse();
        response.mergeFrom(responseBytes);
        return new LogQueryResult(response, finalizedQuery);
      }

      @Override
      protected Throwable convertException(Throwable cause) {
        if (cause instanceof ApiProxy.ApplicationException) {
          ApiProxy.ApplicationException e = (ApiProxy.ApplicationException) cause;
          return new LogServiceException(e.getMessage());
        }
        return cause;
      }
    };
  }
}
