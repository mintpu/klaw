package io.aiven.klaw.model;

public enum KafkaClustersType {
  KAFKA("kafka"),
  SCHEMA_REGISTRY("schemaregistry"),
  KAFKA_CONNECT("kafkaconnect");

  public final String value;

  KafkaClustersType(String value) {
    this.value = value;
  }
}
