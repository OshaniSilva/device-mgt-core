/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OldPasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ProfileFeature;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.Scope;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.policy.mgt.common.PolicyPayloadValidator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class RequestValidationUtil {

    private static final Log log = LogFactory.getLog(RequestValidationUtil.class);

    /**
     * Checks if multiple criteria are specified in a conditional request.
     *
     * @param type      Device type upon which the selection is done
     * @param user      Device user upon whom the selection is done
     * @param roleName  Role name upon which the selection is done
     * @param ownership Ownership type upon which the selection is done
     * @param status    Enrollment status upon which the selection is done
     */
    public static void validateSelectionCriteria(final String type, final String user, final String roleName,
                                                 final String ownership, final String status) {
        List<String> inputs = new ArrayList<String>() {{
            add(type);
            add(user);
            add(roleName);
            add(ownership);
            add(status);
        }};

//        boolean hasOneSelection = false;
//        for (String i : inputs) {
//            if (i == null) {
//                continue;
//            }
//            hasOneSelection = !hasOneSelection;
//            if (!hasOneSelection) {
//                break;
//            }
//        }
        int count = 0;
        for (String i : inputs) {
            if (i == null) {
                continue;
            }
            count++;
            if (count > 1) {
                break;
            }
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one selection criteria defined through query parameters").build());
        }

    }


    public static void validateDeviceIdentifier(String type, String id) {
        boolean isErroneous = false;
        ErrorResponse.ErrorResponseBuilder error = new ErrorResponse.ErrorResponseBuilder();
        if (id == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device identifier cannot be null");
        }
        if (type == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device type cannot be null");
        }
        if (isErroneous) {
            throw new InputValidationException(error.setCode(400l).setMessage("Invalid device identifier").build());

        }
    }

    public static void validateStatus(List<String> statusList) {
        for (String status : statusList) {
            switch (status) {
                case "ACTIVE":
                case "INACTIVE":
                case "UNCLAIMED":
                case "UNREACHABLE":
                case "SUSPENDED":
                case "DISENROLLMENT_REQUESTED":
                case "REMOVED":
                case "BLOCKED":
                case "CREATED":
                    break;
                default:
                    String msg = "Invalid enrollment status type: " + status + ". \nValid status types are " +
                                 "ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE | SUSPENDED | " +
                                 "DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                                                               .setCode(HttpStatus.SC_BAD_REQUEST)
                                                               .setMessage(msg).build());
            }
        }
    }

    public static void validateOwnershipType(String ownership) {
        if (ownership == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Ownership type cannot be null").build());
        }
        switch (ownership) {
            case "BYOD":
            case "COPE":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                                "Invalid ownership type received. " +
                                        "Valid ownership types are BYOD | COPE").build());
        }
    }

    public static void validateNotificationStatus(String status) {
        if (status == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Notification status type cannot be null").build());
        }
        switch (status) {
            case "NEW":
            case "CHECKED":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification status type " +
                                "received. Valid status types are NEW | CHECKED").build());
        }
    }

    public static void validateNotificationId(int id) {
        if (id <= 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification id. " +
                            "Only positive integers are accepted as valid notification Ids").build());
        }
    }

    public static void validateNotification(Notification notification) {
        if (notification == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Notification content " +
                            "cannot be null").build());
        }
    }

    public static void validateTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Timestamp value " +
                            "cannot be null or empty").build());
        }
        try {
            Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Invalid timestamp value").build());
        }
    }

    public static void validateActivityId(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                    .setMessage("Activity Id cannot be null or empty. It should be in the form of " +
                            "'[ACTIVITY][_][any-positive-integer]' instead").build());
        }
        String[] splits = activityId.split("_");
        if (splits.length > 1 && splits[0] != null && !splits[0].isEmpty() && "ACTIVITY".equals(splits[0])) {
            try {
                Long.parseLong(splits[1]);
            } catch (NumberFormatException e) {
                throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                        .setMessage(
                                "Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'")
                        .build());
            }
        } else {
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                    .setMessage("Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'")
                    .build());
        }
    }

    public static void validateApplicationInstallationContext(ApplicationWrapper installationCtx) {
        int count = 0;

        if (installationCtx.getDeviceIdentifiers() != null && installationCtx.getDeviceIdentifiers().size() > 0) {
            count++;
        }
        if (installationCtx.getUserNameList() != null && installationCtx.getUserNameList().size() > 0) {
            count++;
        }
        if (installationCtx.getRoleNameList() != null && installationCtx.getRoleNameList().size() > 0) {
            count++;
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one application installation criteria defined").build());
        }
    }

    public static void validateApplicationUninstallationContext(ApplicationWrapper installationCtx) {
        int count = 0;

        if (installationCtx.getDeviceIdentifiers() != null && installationCtx.getDeviceIdentifiers().size() > 0) {
            count++;
        }
        if (installationCtx.getUserNameList() != null && installationCtx.getUserNameList().size() > 0) {
            count++;
        }
        if (installationCtx.getRoleNameList() != null && installationCtx.getRoleNameList().size() > 0) {
            count++;
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one application un-installation criteria defined").build());
        }
    }

    public static void validateUpdateConfiguration(PlatformConfiguration config) {
        if (config == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Configurations are not defined.")
                            .build());
        } else if (config.getConfiguration() == null || config.getConfiguration().size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Does not contain any " +
                            "configuration entries.").build());
        }
    }

    public static void validateDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        if (deviceIdentifiers == null || deviceIdentifiers.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Device identifier list is " +
                            "empty.").build());
        }
    }

    public static List<org.wso2.carbon.policy.mgt.common.ProfileFeature> validatePolicyDetails(
            PolicyWrapper policyWrapper) {
        if (policyWrapper == null) {
            String msg = "Found an empty policy";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
        return validateProfileFeatures(policyWrapper.getProfile().getProfileFeaturesList());
    }

    public static List<org.wso2.carbon.policy.mgt.common.ProfileFeature> validateProfileFeatures
            (List<ProfileFeature> profileFeatures) {

        if (profileFeatures.isEmpty()) {
            String msg = "Found Empty Policy Feature list to validate.";
            log.error(msg);
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                    .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        } else {
            List<org.wso2.carbon.policy.mgt.common.ProfileFeature> features = new ArrayList<>();
            String deviceType = null;
            for (ProfileFeature profileFeature : profileFeatures) {
                if (StringUtils.isBlank(profileFeature.getDeviceTypeId())) {
                    String msg = "Found an invalid policy feature with empty device type data.";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
                }
                if (deviceType != null && !deviceType.equals(profileFeature.getDeviceTypeId())) {
                    String msg = "Found two different device types in profile feature list.";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
                }
                deviceType = profileFeature.getDeviceTypeId();
                org.wso2.carbon.policy.mgt.common.ProfileFeature feature = new org.wso2.carbon.policy.mgt.common.ProfileFeature();
                feature.setContent(profileFeature.getContent());
                feature.setDeviceType(profileFeature.getDeviceTypeId());
                feature.setFeatureCode(profileFeature.getFeatureCode());
                feature.setPayLoad(profileFeature.getPayLoad());
                features.add(feature);
            }

            try {
                DeviceType deviceTypeObj = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(deviceType);
                if (deviceTypeObj == null) {
                    String msg = "Found an unsupported device type to validate profile feature.";
                    log.error(msg);
                    throw new InputValidationException(
                            new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                                    .build());
                }

                Class<?> clz;
                switch (deviceTypeObj.getName()) {
                case Constants.ANDROID:
                    clz = Class.forName(Constants.ANDROID_POLICY_VALIDATOR);
                    PolicyPayloadValidator enrollmentNotifier = (PolicyPayloadValidator) clz.getDeclaredConstructor()
                            .newInstance();
                    return enrollmentNotifier.validate(features);
                case Constants.IOS:
                    //todo
                case Constants.WINDOWS:
                    //todo
                default:
                    log.error("No policy validator found for device type  " + deviceType);
                    break;
                }
            } catch (DeviceManagementException e) {
                String msg = "Error occurred when validating whether device type is valid one or not " + deviceType;
                log.error(msg, e);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                                .setMessage(msg).build());
            } catch (InstantiationException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when creating an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (IllegalAccessException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when accessing an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (ClassNotFoundException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when loading an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error occurred while constructing validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (InvocationTargetException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error occurred while instantiating validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            }
            return features;
        }
    }


    public static void validatePolicyIds(List<Integer> policyIds) {
        if (policyIds == null || policyIds.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Policy Id list is empty.").build
                            ());
        }
    }

    public static void validateRoleName(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Role name isn't valid.").build
                            ());
        }
    }

    public static void validateUsers(List<String> users) {
        if (users == null || users.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("User list isn't valid.").build
                            ());
        }
    }

    public static void validateCredentials(OldPasswordResetWrapper credentials) {
        if (credentials == null || credentials.getNewPassword() == null || credentials.getOldPassword() == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Old or New password " +
                            "fields cannot be empty").build());
        }
    }

    public static void validateRoleDetails(RoleInfo roleInfo) {
        if (roleInfo == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request body is "
                            + "empty").build());
        } else if (roleInfo.getRoleName() == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request body is "
                            + "incorrect").build());
        }
    }

    public static void validateScopes(List<Scope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Scope details of the request body" +
                            " is incorrect or empty").build());
        }
    }

    public static void validatePaginationParameters(int offset, int limit) {
        if (offset < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter offset is s " +
                            "negative value.").build());
        }
        if (limit < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit is a " +
                            "negative value.").build());
        }
        if (limit > 100) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit should" +
                            " be less than or equal to 100.").build());
        }

    }

    public static void validateOwnerParameter(String owner) {
        if (owner == null || owner.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter owner should" +
                            " be non empty.").build());
        }
    }

    public static boolean isNonFilterRequest(String username, String firstName, String lastName, String emailAddress) {
        return StringUtils.isEmpty(username) && StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)
                && StringUtils.isEmpty(emailAddress);
    }

}
