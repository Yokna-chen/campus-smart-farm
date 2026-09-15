package com.campus.farm.integration;

public class SnapshotRetrievalException extends RuntimeException {
  public SnapshotRetrievalException(String message) { super(message); }
  public SnapshotRetrievalException(String message, Throwable cause) { super(message, cause); }
}
