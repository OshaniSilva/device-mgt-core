/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.apimgt.webapp.publisher;

import io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServices;
import io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServicesImpl;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherRESTAPIServices;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.BadRequestException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantSearchResult;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final String WS_UNLIMITED_TIER = "AsyncUnlimited";
    private static final String API_PUBLISH_ENVIRONMENT = "Default";
    private static final String CREATED_STATUS = "CREATED";
    private static final String PUBLISH_ACTION = "Publish";
    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());
        RealmService realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);
        try {
            boolean tenantFound = false;
            boolean tenantsLoaded = false;
            TenantSearchResult tenantSearchResult = null;
            for (String tenantDomain : tenants) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                if (!tenantsLoaded) {
                    tenantSearchResult = realmService.getTenantManager()
                            .listTenants(Integer.MAX_VALUE, 0, "asc", "UM_ID", null);
                    tenantsLoaded = true;
                }
                if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    tenantFound = true;
                    realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
                            .getRealmConfiguration().getAdminUserName();
                } else {
                    List<Tenant> allTenants = tenantSearchResult.getTenantList();
                    for (Tenant tenant : allTenants) {
                        if (tenant.getDomain().equals(tenantDomain)) {
                            tenantFound = true;
                            tenant.getAdminName();
                            break;
                        } else {
                            tenantFound = false;
                        }
                    }
                }

                if (tenantFound) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(apiConfig.getOwner());
                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                    try {
                        apiConfig.setOwner(APIUtil.getTenantAdminUserName(tenantDomain));
                        apiConfig.setTenantDomain(tenantDomain);
                        APIProvider apiProvider = API_MANAGER_FACTORY.getAPIProvider(apiConfig.getOwner());
                        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(apiConfig.getOwner()),
                                apiConfig.getName(), apiConfig.getVersion());

                        if (!apiProvider.isAPIAvailable(apiIdentifier)) {

                            // add new scopes as shared scopes
                            Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
                            for (ApiScope apiScope : apiConfig.getScopes()) {
                                if (!allSharedScopeKeys.contains(apiScope.getKey())) {
                                    Scope scope = new Scope();
                                    scope.setName(apiScope.getName());
                                    scope.setDescription(apiScope.getDescription());
                                    scope.setKey(apiScope.getKey());
                                    scope.setRoles(apiScope.getRoles());
                                    apiProvider.addSharedScope(scope, tenantDomain);
                                }
                            }
                            API api = getAPI(apiConfig, true);
                            api.setId(apiIdentifier);
                            API createdAPI = apiProvider.addAPI(api);
                            if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                apiProvider.saveAsyncApiDefinition(api, apiConfig.getAsyncApiDefinition());
                            }
                            if (CREATED_STATUS.equals(createdAPI.getStatus())) {
                                // if endpoint type "dynamic" and then add in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);
                                    apiProvider.addApiSpecificMediationPolicy(createdAPI.getUuid(), mediation,
                                            tenantDomain);
                                }
                                apiProvider.changeLifeCycleStatus(tenantDomain, createdAPI.getUuid(), PUBLISH_ACTION, null);
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(createdAPI.getUuid());
                                apiRevision.setDescription("Initial Revision");
                                String apiRevisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setDeployment(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);
                                apiProvider.deployAPIRevision(createdAPI.getUuid(), apiRevisionId, apiRevisionDeploymentList);
                            }
                        } else {
                            if (WebappPublisherConfig.getInstance().isEnabledUpdateApi()) {

                                // With 4.x to 5.x upgrade
                                // - there cannot be same local scope assigned in 2 different APIs
                                // - local scopes will be deprecated in the future, so need to move all scopes as shared scopes

                                // if an api scope is not available as shared scope, but already assigned as local scope -> that means, the scopes available for this API has not moved as shared scopes
                                // in order to do that :
                                // 1. update the same API removing scopes from URI templates
                                // 2. add scopes as shared scopes
                                // 3. update the API again adding scopes for the URI Templates

                                // if an api scope is not available as shared scope, and not assigned as local scope -> that means, there are new scopes
                                // 1. add new scopes as shared scopes
                                // 2. update the API adding scopes for the URI Templates

                                Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
                                Set<ApiScope> scopesToMoveAsSharedScopes = new HashSet<>();
                                for (ApiScope apiScope : apiConfig.getScopes()) {
                                    // if the scope is not available as shared scope and it is assigned to an API as a local scope
                                    // need remove the local scope and add as a shared scope
                                    if (!allSharedScopeKeys.contains(apiScope.getKey())) {
                                        if (apiProvider.isScopeKeyAssignedLocally(apiIdentifier, apiScope.getKey(), tenantId)) {
                                            // collect scope to move as shared scopes
                                            scopesToMoveAsSharedScopes.add(apiScope);
                                        } else {
                                            // if new scope add as shared scope
                                            Scope scope = new Scope();
                                            scope.setName(apiScope.getName());
                                            scope.setDescription(apiScope.getDescription());
                                            scope.setKey(apiScope.getKey());
                                            scope.setRoles(apiScope.getRoles());
                                            apiProvider.addSharedScope(scope, tenantDomain);
                                        }
                                    }
                                }

                                // Get existing API
                                API existingAPI = apiProvider.getAPI(apiIdentifier);

                                if (scopesToMoveAsSharedScopes.size() > 0) {
                                    // update API to remove local scopes
                                    API api = getAPI(apiConfig, false);
                                    api.setStatus(existingAPI.getStatus());
                                    apiProvider.updateAPI(api);

                                    for (ApiScope apiScope : scopesToMoveAsSharedScopes) {
                                        Scope scope = new Scope();
                                        scope.setName(apiScope.getName());
                                        scope.setDescription(apiScope.getDescription());
                                        scope.setKey(apiScope.getKey());
                                        scope.setRoles(apiScope.getRoles());
                                        apiProvider.addSharedScope(scope, tenantDomain);
                                    }
                                }

                                existingAPI = apiProvider.getAPI(apiIdentifier);
                                API api = getAPI(apiConfig, true);
                                api.setStatus(existingAPI.getStatus());
                                apiProvider.updateAPI(api);

                                if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                    apiProvider.saveAsyncApiDefinition(api, apiConfig.getAsyncApiDefinition());
                                }

                                // if endpoint type "dynamic" and then add /update in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);

                                    List<Mediation> mediationList = apiProvider
                                            .getAllApiSpecificMediationPolicies(apiIdentifier);
                                    boolean isMediationPolicyFound = false;
                                    for (Mediation m : mediationList) {
                                        if (apiConfig.getInSequenceName().equals(m.getName())) {
                                            m.setConfig(apiConfig.getInSequenceConfig());
                                            apiProvider
                                                    .updateApiSpecificMediationPolicyContent(existingAPI.getUuid(), m,
                                                            tenantDomain);
                                            isMediationPolicyFound = true;
                                            break;
                                        }
                                    }
                                    if (!isMediationPolicyFound) {
                                        apiProvider.addApiSpecificMediationPolicy(existingAPI.getUuid(), mediation,
                                                tenantDomain);
                                    }
                                }

                                // Assumption: Assume the latest revision is the published one
                                String latestRevisionUUID = apiProvider.getLatestRevisionUUID(existingAPI.getUuid());
                                List<APIRevisionDeployment> latestRevisionDeploymentList =
                                        apiProvider.getAPIRevisionDeploymentList(latestRevisionUUID);

                                List<APIRevision> apiRevisionList = apiProvider.getAPIRevisions(existingAPI.getUuid());
                                if (apiRevisionList.size() >= 5) {
                                    String earliestRevisionUUID = apiProvider.getEarliestRevisionUUID(existingAPI.getUuid());
                                    List<APIRevisionDeployment> earliestRevisionDeploymentList =
                                            apiProvider.getAPIRevisionDeploymentList(earliestRevisionUUID);
                                    apiProvider.undeployAPIRevisionDeployment(existingAPI.getUuid(), earliestRevisionUUID, earliestRevisionDeploymentList);
                                    apiProvider.deleteAPIRevision(existingAPI.getUuid(), earliestRevisionUUID, tenantDomain);
                                }

                                // create new revision
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(existingAPI.getUuid());
                                apiRevision.setDescription("Updated Revision");
                                String apiRevisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);

                                apiProvider.deployAPIRevision(existingAPI.getUuid(), apiRevisionId, latestRevisionDeploymentList);

                                if (CREATED_STATUS.equals(existingAPI.getStatus())) {
                                    apiProvider.changeLifeCycleStatus(tenantDomain, existingAPI.getUuid(), PUBLISH_ACTION, null);
                                }
                            }
                        }
                        if (apiConfig.getApiDocumentationSourceFile() != null) {
                            API api = getAPI(apiConfig, true);

                            String fileName =
                                    CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                            File.separator + "resources" + File.separator + "api-docs" + File.separator +
                                            apiConfig.getApiDocumentationSourceFile();

                            BufferedReader br = new BufferedReader(new FileReader(fileName));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = null;
                            String ls = System.lineSeparator();
                            while ((line = br.readLine()) != null) {
                                stringBuilder.append(line);
                                stringBuilder.append(ls);
                            }
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                            br.close();
                            String docContent = stringBuilder.toString();

                            Documentation apiDocumentation = new Documentation(DocumentationType.HOWTO, apiConfig.getApiDocumentationName());
                            apiDocumentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
                            apiDocumentation.setSourceType(Documentation.DocumentSourceType.MARKDOWN);
                            apiDocumentation.setCreatedDate(new Date());
                            apiDocumentation.setLastUpdated(new Date());
                            apiDocumentation.setSummary(apiConfig.getApiDocumentationSummary());
                            apiDocumentation.setOtherTypeName(null);

                            try {
                                //Including below code lines inside the try block because  'getDocumentation' method returns an APIManagementException exception when it doesn't have any existing doc
                                Documentation existingDoc = apiProvider.getDocumentation(api.getId(), DocumentationType.HOWTO, apiConfig.getApiDocumentationName());
                                apiProvider.removeDocumentation(api.getId(), existingDoc.getId(), null);
                            } catch (APIManagementException e) {
                                log.info("There is no any existing api documentation.");
                            }
                            apiProvider.addDocumentation(api.getId(), apiDocumentation);
                            apiProvider.addDocumentationContent(api, apiConfig.getApiDocumentationName(), docContent);
                        }
                    } catch (FaultGatewaysException | APIManagementException | IOException e) {
                        String msg = "Error occurred while publishing api";
                        log.error(msg, e);
                        throw new APIManagerPublisherException(e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving admin user from tenant user realm";
            log.error(msg, e);
            throw new APIManagerPublisherException(e);
        }
    }

    @Override
    public void updateScopeRoleMapping()
            throws APIManagerPublisherException {
        // todo: This logic has written assuming all the scopes are now work as shared scopes
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());

        APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
        APIApplicationKey apiApplicationKey;
        AccessTokenInfo accessTokenInfo;
        try {
            apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials();
            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (BadRequestException e) {
            String errorMsg = "Error while generating application";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        } catch (APIApplicationServicesException e) {
            throw new RuntimeException(e);
        }

        try {
            for (String tenantDomain : tenants) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServices();
//                APIProvider apiProvider = API_MANAGER_FACTORY.getAPIProvider(MultitenantUtils.getTenantAwareUsername(
//                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
//                                .getAdminUserName()));

                try {
                    String fileName =
                            CarbonUtils.getCarbonConfigDirPath() + File.separator + "etc"
                                    + File.separator + tenantDomain + ".csv";
                    if (Files.exists(Paths.get(fileName))) {
                        BufferedReader br = new BufferedReader(new FileReader(fileName));
                        int lineNumber = 0;
                        Map<Integer, String> roles = new HashMap<>();
                        String line = "";
                        String splitBy = ",";
                        while ((line = br.readLine()) != null)   //returns a Boolean value
                        {
                            lineNumber++;
                            String[] scopeMapping = line.split(splitBy);    // use comma as separator
                            if (lineNumber == 1) { // skip titles
                                for (int i = 0; i < scopeMapping.length; i++) {
                                    if (i > 3) {
                                        roles.put(i, scopeMapping[i]); // add roles to the map
                                    }
                                }
                                continue;
                            }

                            Scope scope = new Scope();
                            scope.setName(
                                    scopeMapping[0] != null ? StringUtils.trim(scopeMapping[0]) : StringUtils.EMPTY);
                            scope.setDescription(
                                    scopeMapping[1] != null ? StringUtils.trim(scopeMapping[1]) : StringUtils.EMPTY);
                            scope.setKey(
                                    scopeMapping[2] != null ? StringUtils.trim(scopeMapping[2]) : StringUtils.EMPTY);
                            //                        scope.setPermissions(
                            //                                scopeMapping[3] != null ? StringUtils.trim(scopeMapping[3]) : StringUtils.EMPTY);

                            String roleString = "";
                            for (int i = 4; i < scopeMapping.length; i++) {
                                if (scopeMapping[i] != null && StringUtils.trim(scopeMapping[i]).equals("Yes")) {
                                    roleString = roleString + "," + roles.get(i);
                                }
                            }
                            if (roleString.length() > 1) {
                                roleString = roleString.substring(1); // remove first , (comma)
                            }
                            scope.setRoles(roleString);

//                            if (apiProvider.isSharedScopeNameExists(scope.getKey(), tenantDomain)) {
//                                apiProvider.updateSharedScope(scope, tenantDomain);
                            if (publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo, scope.getKey())) {
                                publisherRESTAPIServices.updateSharedScope(apiApplicationKey, accessTokenInfo, scope);
                            } else {
                                // todo: come to this level means, that scope is removed from API, but haven't removed from the scope-role-permission-mappings list
                                if (log.isDebugEnabled()) {
                                    log.debug(scope.getKey() + " not available as shared scope");
                                }
                            }
                        }
                    }
                } catch (IOException | DirectoryIteratorException ex) {
                    log.error("failed to read scopes from file.", ex);
                } catch (APIApplicationServicesException | BadRequestException e) {
                    String errorMsg = "Error while generating an OAuth token";
                    log.error(errorMsg, e);
                    throw new APIManagerPublisherException(e);
                }

            }
        }
//        catch (UserStoreException e) {
//            String msg = "Error occurred while reading tenant admin username";
//            log.error(msg, e);
//            throw new APIManagerPublisherException(e);
//        }
//        catch (APIManagementException e) {
//            String msg = "Error occurred while loading api provider";
//            log.error(msg, e);
//            throw new APIManagerPublisherException(e);
//        }
        finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private API getAPI(APIConfig config, boolean includeScopes) {

        APIIdentifier apiIdentifier = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());
        API api = new API(apiIdentifier);
        api.setDescription("");
        String context = config.getContext();
        context = context.startsWith("/") ? context : ("/" + context);
        api.setContext(context + "/" + config.getVersion());
        api.setStatus(CREATED_STATUS);
        api.setWsdlUrl(null);
        api.setResponseCache("Disabled");
        api.setContextTemplate(context + "/{version}");
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
            api.setAsyncApiDefinition(config.getAsyncApiDefinition());
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            try {
                api.setUriTemplates(asyncApiParser.getURITemplates(config.getAsyncApiDefinition(), true));
            } catch (APIManagementException e) {

            }
            api.setWsUriMapping(asyncApiParser.buildWSUriMapping(config.getAsyncApiDefinition()));
        } else {
            api.setSwaggerDefinition(APIPublisherUtil.getSwaggerDefinition(config));

            Set<URITemplate> uriTemplates = new HashSet<>();
            Iterator<ApiUriTemplate> iterator;
            for (iterator = config.getUriTemplates().iterator(); iterator.hasNext(); ) {
                ApiUriTemplate apiUriTemplate = iterator.next();
                URITemplate uriTemplate = new URITemplate();
                uriTemplate.setAuthType(apiUriTemplate.getAuthType());
                uriTemplate.setHTTPVerb(apiUriTemplate.getHttpVerb());
                uriTemplate.setResourceURI(apiUriTemplate.getResourceURI());
                uriTemplate.setUriTemplate(apiUriTemplate.getUriTemplate());
                if (includeScopes) {
                    Scope scope = new Scope();
                    if (apiUriTemplate.getScope() != null) {
                        scope.setName(apiUriTemplate.getScope().getName());
                        scope.setDescription(apiUriTemplate.getScope().getDescription());
                        scope.setKey(apiUriTemplate.getScope().getKey());
                        scope.setRoles(apiUriTemplate.getScope().getRoles());
                        uriTemplate.setScopes(scope);
                    }

                }
                uriTemplates.add(uriTemplate);
            }
            api.setUriTemplates(uriTemplates);
        }

        api.setApiOwner(config.getOwner());


        api.setDefaultVersion(config.isDefault());

        Set<String> tags = new HashSet<>();
        tags.addAll(Arrays.asList(config.getTags()));
        api.setTags(tags);

        Set<Tier> availableTiers = new HashSet<>();
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
            availableTiers.add(new Tier(WS_UNLIMITED_TIER));
        } else {
            availableTiers.add(new Tier(UNLIMITED_TIER));
        }
        api.setAvailableTiers(availableTiers);

        Set<String> environments = new HashSet<>();
        environments.add(API_PUBLISH_ENVIRONMENT);
        api.setEnvironments(environments);

        if (config.isSharedWithAllTenants()) {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        } else {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT);
            api.setVisibility(APIConstants.API_PRIVATE_VISIBILITY);
        }
        String endpointConfig = "{ \"endpoint_type\": \"http\", \"sandbox_endpoints\": { \"url\": \" " +
                config.getEndpoint() + "\" }, \"production_endpoints\": { \"url\": \" " + config.getEndpoint() + "\" } }";
        api.setTransports(config.getTransports());
        api.setType("HTTP");

        // if dynamic endpoint
        if (config.getEndpointType() != null && "dynamic".equals(config.getEndpointType())) {
            endpointConfig = "{ \"endpoint_type\":\"default\", \"sandbox_endpoints\":{ \"url\":\"default\" }, \"production_endpoints\":{ \"url\":\"default\" } }";
            api.setInSequence(config.getInSequenceName());
        }

        // if ws endpoint
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
            endpointConfig = "{ \"endpoint_type\": \"ws\", \"sandbox_endpoints\": { \"url\": \" " +
                    config.getEndpoint() + "\" }, \"production_endpoints\": { \"url\": \" " + config.getEndpoint()
                    + "\" } }";
            api.setTransports("wss,ws");
            api.setType("WS");
        }
        api.setEndpointConfig(endpointConfig);
        List<String> accessControlAllowOrigins = new ArrayList<>();
        accessControlAllowOrigins.add("*");

        List<String> accessControlAllowHeaders = new ArrayList<>();
        accessControlAllowHeaders.add("authorization");
        accessControlAllowHeaders.add("Access-Control-Allow-Origin");
        accessControlAllowHeaders.add("Content-Type");
        accessControlAllowHeaders.add("SOAPAction");
        accessControlAllowHeaders.add("apikey");
        accessControlAllowHeaders.add("Internal-Key");
        List<String> accessControlAllowMethods = new ArrayList<>();
        accessControlAllowMethods.add("GET");
        accessControlAllowMethods.add("PUT");
        accessControlAllowMethods.add("DELETE");
        accessControlAllowMethods.add("POST");
        accessControlAllowMethods.add("PATCH");
        accessControlAllowMethods.add("OPTIONS");
        CORSConfiguration corsConfiguration = new CORSConfiguration(false, accessControlAllowOrigins, false,
                accessControlAllowHeaders, accessControlAllowMethods);
        api.setCorsConfiguration(corsConfiguration);

        api.setAuthorizationHeader("Authorization");
        List<String> keyManagers = new ArrayList<>();
        keyManagers.add("all");
        api.setKeyManagers(keyManagers);
        api.setEnableStore(true);
        api.setEnableSchemaValidation(false);
        api.setMonetizationEnabled(false);
        return api;
    }
}
