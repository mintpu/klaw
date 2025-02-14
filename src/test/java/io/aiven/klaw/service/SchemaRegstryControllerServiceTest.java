package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.SchemaRequestModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SchemaRegstryControllerServiceTest {

  @Mock private UserDetails userDetails;

  @Mock private HandleDbRequests handleDbRequests;

  @Mock private MailUtils mailService;

  @Mock private ManageDatabase manageDatabase;

  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  @Mock CommonUtilsService commonUtilsService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  private SchemaRegstryControllerService schemaRegstryControllerService;

  private Env env;

  @BeforeEach
  public void setUp() throws Exception {
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");

    schemaRegstryControllerService =
        new SchemaRegstryControllerService(clusterApiService, mailService);
    ReflectionTestUtils.setField(schemaRegstryControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(schemaRegstryControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        schemaRegstryControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        schemaRegstryControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(1)
  public void getSchemaRequests() {
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getAllSchemaRequests(anyBoolean(), anyString(), anyInt()))
        .thenReturn(getSchemasReqs());
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(Arrays.asList(""));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.selectAllUsersInfoForTeam(anyInt(), anyInt()))
        .thenReturn(Arrays.asList(userInfo));
    when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("teamname");

    List<SchemaRequestModel> listReqs =
        schemaRegstryControllerService.getSchemaRequests("1", "", "all");
    assertThat(listReqs).hasSize(2);
  }

  @Test
  @Order(2)
  public void deleteSchemaRequestsSuccess() {
    int schemaReqId = 1001;

    stubUserInfo();
    when(handleDbRequests.deleteSchemaRequest(anyInt(), anyInt())).thenReturn("success");
    String result = schemaRegstryControllerService.deleteSchemaRequests("" + schemaReqId);
    assertThat(result).isEqualTo("success");
  }

  @Test
  @Order(3)
  public void deleteSchemaRequestsFailure() {
    int schemaReqId = 1001;

    stubUserInfo();
    when(handleDbRequests.deleteSchemaRequest(anyInt(), anyInt()))
        .thenThrow(new RuntimeException("Error"));
    String result = schemaRegstryControllerService.deleteSchemaRequests("" + schemaReqId);
    assertThat(result).contains("failure");
  }

  @Test
  @Order(4)
  public void execSchemaRequestsSuccess() throws KlawException {
    int schemaReqId = 1001;

    ResponseEntity<String> response =
        new ResponseEntity<>("Schema registered id\": 215", HttpStatus.OK);
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setUsername("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(response);
    when(handleDbRequests.updateSchemaRequest(any(), anyString())).thenReturn("success");
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    String result = schemaRegstryControllerService.execSchemaRequests("" + schemaReqId);
    assertThat(result).contains("success");
  }

  @Test
  @Order(5)
  public void execSchemaRequestsFailure1() throws KlawException {
    int schemaReqId = 1001;

    ResponseEntity<String> response = new ResponseEntity<>("Schema not registered", HttpStatus.OK);
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setUsername("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(response);
    when(handleDbRequests.updateSchemaRequest(any(), anyString())).thenReturn("success");
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    String result = schemaRegstryControllerService.execSchemaRequests("" + schemaReqId);
    assertThat(result).contains("Failure");
  }

  @Test
  @Order(6)
  public void execSchemaRequestsFailure2() throws KlawException {
    int schemaReqId = 1001;

    ResponseEntity<String> response =
        new ResponseEntity<>("Schema registered id\": 215", HttpStatus.OK);
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setUsername("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(response);
    when(handleDbRequests.updateSchemaRequest(any(), anyString()))
        .thenThrow(new RuntimeException("Error"));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    String result = schemaRegstryControllerService.execSchemaRequests("" + schemaReqId);
    assertThat(result).contains("Error");
  }

  @Test
  @Order(7)
  public void uploadSchemaSuccess() {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    schemaRequest.setSchemafull("{}");
    schemaRequest.setUsername("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");
    Topic topic = new Topic();
    topic.setEnvironment("1");
    topic.setTeamId(101);

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.requestForSchema(any())).thenReturn("success");
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(topic));

    String result = schemaRegstryControllerService.uploadSchema(schemaRequest);
    assertThat(result).isEqualTo("success");
  }

  @Test
  @Order(8)
  public void uploadSchemaFailure() {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    schemaRequest.setSchemafull("{}");
    schemaRequest.setUsername("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");
    Topic topic = new Topic();
    topic.setEnvironment("1");
    topic.setTeamId(101);

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.requestForSchema(any())).thenThrow(new RuntimeException("Error"));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(topic));

    String result = schemaRegstryControllerService.uploadSchema(schemaRequest);
    assertThat(result).contains("failure");
  }

  private List<SchemaRequest> getSchemasReqs() {
    List<SchemaRequest> schList = new ArrayList<>();
    SchemaRequest schReq = new SchemaRequest();
    schReq.setSchemafull("<Schema>");
    schReq.setEnvironment("1");
    schReq.setTopicstatus("created");
    schReq.setTeamId(101);
    schReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    schList.add(schReq);

    schReq = new SchemaRequest();
    schReq.setSchemafull("<Schema1>");
    schReq.setEnvironment("1");
    schReq.setTopicstatus("created");
    schReq.setTeamId(102);
    schReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    schList.add(schReq);

    return schList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
