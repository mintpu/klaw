package io.aiven.klaw.helpers.db.rdbms;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.repository.*;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DeleteDataJdbc {

  @Autowired(required = false)
  TopicRequestsRepo topicRequestsRepo;

  @Autowired(required = false)
  private TopicRepo topicRepo;

  @Autowired(required = false)
  KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Autowired(required = false)
  SchemaRequestRepo schemaRequestRepo;

  @Autowired(required = false)
  MessageSchemaRepo messageSchemaRepo;

  @Autowired(required = false)
  EnvRepo envRepo;

  @Autowired(required = false)
  KwClusterRepo kwClusterRepo;

  @Autowired(required = false)
  TeamRepo teamRepo;

  @Autowired(required = false)
  AclRequestsRepo aclRequestsRepo;

  @Autowired(required = false)
  AclRepo aclRepo;

  @Autowired(required = false)
  UserInfoRepo userInfoRepo;

  @Autowired(required = false)
  private RegisterInfoRepo registerInfoRepo;

  @Autowired(required = false)
  private KwRolesPermsRepo kwRolesPermsRepo;

  @Autowired(required = false)
  private KwPropertiesRepo kwPropertiesRepo;

  @Autowired(required = false)
  private TenantRepo tenantRepo;

  public DeleteDataJdbc() {}

  public DeleteDataJdbc(
      TopicRequestsRepo topicRequestsRepo,
      SchemaRequestRepo schemaRequestRepo,
      EnvRepo envRepo,
      TeamRepo teamRepo,
      AclRequestsRepo aclRequestsRepo,
      AclRepo aclRepo,
      UserInfoRepo userInfoRepo) {
    this.topicRequestsRepo = topicRequestsRepo;
    this.schemaRequestRepo = schemaRequestRepo;
    this.envRepo = envRepo;
    this.teamRepo = teamRepo;
    this.aclRepo = aclRepo;
    this.aclRequestsRepo = aclRequestsRepo;
    this.userInfoRepo = userInfoRepo;
  }

  public String deleteConnectorRequest(int connectorId, int tenantId) {
    log.debug("deleteConnectorRequest {} {}", connectorId, tenantId);
    KafkaConnectorRequestID kafkaConnectorRequestID = new KafkaConnectorRequestID();
    kafkaConnectorRequestID.setConnectorId(connectorId);
    kafkaConnectorRequestID.setTenantId(tenantId);

    Optional<KafkaConnectorRequest> topicReq =
        kafkaConnectorRequestsRepo.findById(kafkaConnectorRequestID);
    if (topicReq.isPresent()) {
      topicReq.get().setConnectorStatus("deleted");
      kafkaConnectorRequestsRepo.save(topicReq.get());
    }
    return "success";
  }

  public String deleteTopicRequest(int topicId, int tenantId) {
    log.debug("deleteTopicRequest {}", topicId);

    TopicRequestID topicRequestID = new TopicRequestID();
    topicRequestID.setTenantId(tenantId);
    topicRequestID.setTopicid(topicId);

    Optional<TopicRequest> topicReq = topicRequestsRepo.findById(topicRequestID);
    if (topicReq.isPresent()) {
      topicReq.get().setTopicstatus("deleted");
      topicRequestsRepo.save(topicReq.get());
    }
    return "success";
  }

  public String deleteTopic(int topicId, int tenantId) {
    log.debug("deleteTopic {}", topicId);
    TopicID topicID = new TopicID();
    topicID.setTenantId(tenantId);
    topicID.setTopicid(topicId);

    Optional<Topic> topicReq = topicRepo.findById(topicID);
    topicReq.ifPresent(topic -> topicRepo.delete(topic));
    return "success";
  }

  public String deleteConnector(int topicId, int tenantId) {
    log.debug("deleteTopic {}", topicId);
    KwKafkaConnectorID kwKafkaConnectorID = new KwKafkaConnectorID();
    kwKafkaConnectorID.setConnectorId(topicId);
    kwKafkaConnectorID.setTenantId(tenantId);

    Optional<KwKafkaConnector> topicReq = kafkaConnectorRepo.findById(kwKafkaConnectorID);
    topicReq.ifPresent(topic -> kafkaConnectorRepo.delete(topic));
    return "success";
  }

  public String deleteSchemaRequest(int avroSchemaId, int tenantId) {
    log.debug("deleteSchemaRequest {}", avroSchemaId);
    SchemaRequestID schemaRequestID = new SchemaRequestID(avroSchemaId, tenantId);
    Optional<SchemaRequest> schemaReq = schemaRequestRepo.findById(schemaRequestID);
    if (schemaReq.isPresent()) {
      schemaReq.get().setTopicstatus("deleted");
      schemaRequestRepo.save(schemaReq.get());
    }
    return "success";
  }

  public String deleteAclRequest(int aclId, int tenantId) {
    log.debug("deleteAclRequest {}", aclId);
    AclRequestID aclRequestID = new AclRequestID();
    aclRequestID.setReq_no(aclId);
    aclRequestID.setTenantId(tenantId);
    Optional<AclRequests> optAclRequests = aclRequestsRepo.findById(aclRequestID);
    if (optAclRequests.isPresent()) {
      optAclRequests.get().setAclstatus("deleted");
      aclRequestsRepo.save(optAclRequests.get());
    }

    return "success";
  }

  public String deleteEnvironment(String envId, int tenantId) {
    log.debug("deleteEnvironment {}", envId);
    EnvID envID = new EnvID();
    envID.setId(envId);
    envID.setTenantId(tenantId);
    Optional<Env> env = envRepo.findById(envID);

    if (env.isPresent()) {
      //            env.get().setEnvExists("false");
      envRepo.delete(env.get());
      return "success";
    } else return "failure";
  }

  public String deleteCluster(int clusterId, int tenantId) {
    log.debug("deleteCluster {}", clusterId);
    KwClusters clusters = new KwClusters();
    clusters.setClusterId(clusterId);
    clusters.setTenantId(tenantId);

    List<Env> allAssociatedEnvs = envRepo.findAllByClusterIdAndTenantId(clusterId, tenantId);
    KwClusterID kwClusterID = new KwClusterID();
    kwClusterID.setClusterId(clusterId);
    kwClusterID.setTenantId(tenantId);

    if (kwClusterRepo.findById(kwClusterID).isPresent()) {
      kwClusterRepo.delete(clusters);
      envRepo.deleteAll(allAssociatedEnvs);
      return "success";
    } else return "failure";
  }

  public String deleteUserRequest(String userId) {
    log.debug("deleteUserRequest {}", userId);
    UserInfo user = new UserInfo();
    user.setUsername(userId);
    userInfoRepo.delete(user);
    return "success";
  }

  public String deleteTeamRequest(Integer teamId, int tenantId) {
    log.debug("deleteTeamRequest {}", teamId);
    Team team = new Team();
    team.setTeamId(teamId);
    team.setTenantId(tenantId);

    teamRepo.delete(team);
    return "success";
  }

  // the actual req of Acl stored in otherParams of AclReq
  public String deletePrevAclRecs(AclRequests aclsToBeDeleted) {

    log.debug("deletePrevAclRecs {}", aclsToBeDeleted.getOtherParams());
    AclID aclID = new AclID();
    aclID.setReq_no(Integer.parseInt(aclsToBeDeleted.getOtherParams()));
    aclID.setTenantId(aclsToBeDeleted.getTenantId());

    Optional<Acl> acl = aclRepo.findById(aclID);
    acl.ifPresent(value -> aclRepo.delete(value));

    return "success";
  }

  public String deleteAclSubscriptionRequest(int req_no, int tenantId) {
    log.debug("deleteAclSubscriptionRequest {}", req_no);
    AclID aclID = new AclID();
    aclID.setReq_no(req_no);
    aclID.setTenantId(tenantId);

    Optional<Acl> aclRec = aclRepo.findById(aclID);
    aclRec.ifPresent(acl -> aclRepo.delete(acl));

    return "success";
  }

  public void deleteTopics(Topic topic) {
    log.debug("deleteTopics {}", topic.getTopicname());
    List<Topic> topics =
        topicRepo.findAllByTopicnameAndEnvironmentAndTenantId(
            topic.getTopicname(), topic.getEnvironment(), topic.getTenantId());
    topicRepo.deleteAll(topics);
  }

  public void deleteConnectors(KwKafkaConnector connector) {
    log.debug("deleteConnectors {}", connector.getConnectorName());
    List<KwKafkaConnector> topics =
        kafkaConnectorRepo.findAllByConnectorNameAndEnvironmentAndTenantId(
            connector.getConnectorName(), connector.getEnvironment(), connector.getTenantId());
    kafkaConnectorRepo.deleteAll(topics);
  }

  public String deleteRole(String roleId, int tenantId) {
    List<KwRolesPermissions> rolePerms =
        kwRolesPermsRepo.findAllByRoleIdAndTenantId(roleId, tenantId);
    kwRolesPermsRepo.deleteAll(rolePerms);

    return "success";
  }

  public String deleteAllUsers(int tenantId) {
    userInfoRepo.deleteAll(userInfoRepo.findAllByTenantId(tenantId));
    registerInfoRepo.deleteAll(registerInfoRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteAllTeams(int tenantId) {
    teamRepo.deleteAll(teamRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteAllEnvs(int tenantId) {
    envRepo.deleteAll(envRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteAllClusters(int tenantId) {
    kwClusterRepo.deleteAll(kwClusterRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteAllRolesPerms(int tenantId) {
    kwRolesPermsRepo.deleteAll(kwRolesPermsRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteAllKwProps(int tenantId) {
    kwPropertiesRepo.deleteAll(kwPropertiesRepo.findAllByTenantId(tenantId));
    return "success";
  }

  public String deleteTenant(int tenantId) {
    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantId(tenantId);
    tenantRepo.delete(kwTenants);
    return "success";
  }

  public String deleteTxnData(int tenantId) {
    topicRequestsRepo.deleteAll(topicRequestsRepo.findAllByTenantId(tenantId));
    topicRepo.deleteAll(topicRepo.findAllByTenantId(tenantId));

    aclRequestsRepo.deleteAll(aclRequestsRepo.findAllByTenantId(tenantId));
    aclRepo.deleteAll(aclRepo.findAllByTenantId(tenantId));

    schemaRequestRepo.deleteAll(schemaRequestRepo.findAllByTenantId(tenantId));
    messageSchemaRepo.deleteAll(messageSchemaRepo.findAllByTenantId(tenantId));

    kafkaConnectorRepo.deleteAll(kafkaConnectorRepo.findAllByTenantId(tenantId));
    kafkaConnectorRequestsRepo.deleteAll(kafkaConnectorRequestsRepo.findAllByTenantId(tenantId));

    return "success";
  }
}
