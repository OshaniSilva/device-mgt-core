/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template;

import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.InitialOperationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeDefinitionProvider;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This inherits the capabiliy that is provided through the file based device type manager service.
 * This will create and instance of device management service through a json payload.
 */
public class HTTPDeviceTypeManagerService extends DeviceTypeManagerService implements DeviceTypeDefinitionProvider {

    private DeviceTypeMetaDefinition deviceTypeMetaDefinition;
    private static final String DEFAULT_PULL_NOTIFICATION_CLASS_NAME = "io.entgra.device.mgt.core.device.mgt.extensions.pull.notification.PullNotificationSubscriberImpl";

    public HTTPDeviceTypeManagerService(String deviceTypeName, DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        super(getDeviceTypeConfigIdentifier(deviceTypeName), getDeviceTypeConfiguration(
                deviceTypeName, deviceTypeMetaDefinition));
        this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
    }

    private static DeviceTypeConfiguration getDeviceTypeConfiguration(String deviceTypeName, DeviceTypeMetaDefinition
            deviceTypeMetaDefinition) {
        DeviceTypeConfiguration deviceTypeConfiguration = new DeviceTypeConfiguration();

        if (deviceTypeMetaDefinition != null) {

            if (deviceTypeMetaDefinition.getProperties() != null &&
                    deviceTypeMetaDefinition.getProperties().size() > 0) {
                DeviceDetails deviceDetails = new DeviceDetails();
                Properties properties = new Properties();
                properties.addProperties(deviceTypeMetaDefinition.getProperties());
                deviceDetails.setProperties(properties);
                deviceTypeConfiguration.setDeviceDetails(deviceDetails);
            }
            if (deviceTypeMetaDefinition.getFeatures() != null && deviceTypeMetaDefinition.getFeatures().size() > 0) {
                Features features = new Features();
                List<io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.Feature> featureList
                        = new ArrayList<>();
                for (Feature feature : deviceTypeMetaDefinition.getFeatures()) {
                    io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.Feature configFeature =
                            new io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.Feature();
                    if (feature.getCode() != null && feature.getName() != null) {
                        configFeature.setCode(feature.getCode());
                        configFeature.setDescription(feature.getDescription());
                        if (feature.getTooltip() != null) {
                            configFeature.setTooltip(feature.getTooltip());
                        }
                        configFeature.setName(feature.getName());
                        if (feature.getMetadataEntries() != null && feature.getMetadataEntries().size() > 0) {
                            List<String> metaValues = new ArrayList<>();
                            for (Feature.MetadataEntry metadataEntry : feature.getMetadataEntries()) {
                                metaValues.add(metadataEntry.getValue().toString());
                            }
                            configFeature.setMetaData(metaValues);
                        }
                        if (feature.getConfirmationTexts() != null) {
                            List<String> confirmationTextValues = new ArrayList<>();
                            Feature.ConfirmationTexts confirmationText = feature.getConfirmationTexts();
                            confirmationTextValues.add(confirmationText.getDeleteConfirmModalTitle());
                            confirmationTextValues.add(confirmationText.getDeleteConfirmModalText());
                            confirmationTextValues.add(confirmationText.getDeleteConfirmationTextDescribe());
                            confirmationTextValues.add(confirmationText.getDeleteConfirmationText());
                            confirmationTextValues.add(confirmationText.getCancelText());
                            confirmationTextValues.add(confirmationText.getConfirmText());
                            confirmationTextValues.add(confirmationText.getInputLabel());
                            confirmationTextValues.add(confirmationText.getInputRequireMessage());
                            configFeature.setConfirmationTexts(confirmationTextValues);
                        }
                        if (feature.getDangerZoneTooltipTexts() != null) {
                            List<String> dangerZoneTextValues = new ArrayList<>();
                            Feature.DangerZoneTooltipTexts dangerZoneText = feature.getDangerZoneTooltipTexts();
                            dangerZoneTextValues.add(dangerZoneText.getToolTipTitle());
                            dangerZoneTextValues.add(dangerZoneText.getToolTipPopConfirmText());
                            dangerZoneTextValues.add(dangerZoneText.getConfirmText());
                            dangerZoneTextValues.add(dangerZoneText.getCancelText());
                            dangerZoneTextValues.add(dangerZoneText.getToolTipAvailable());
                            configFeature.setDangerZoneTooltipTexts(dangerZoneTextValues);
                        }
                        featureList.add(configFeature);
                    }
                }
                features.addFeatures(featureList);
                deviceTypeConfiguration.setFeatures(features);
            }

            deviceTypeConfiguration.setName(deviceTypeName);
            //TODO: Add it to the license management service.
//            if (deviceTypeMetaDefinition.getLicense() != null) {
//                License license = new License();
//                license.setLanguage(deviceTypeMetaDefinition.getLicense().getLanguage());
//                license.setText(deviceTypeMetaDefinition.getLicense().getText());
//                license.setVersion(deviceTypeMetaDefinition.getLicense().getVersion());
//                deviceTypeConfiguration.setLicense(license);
//            }
            PolicyMonitoring policyMonitoring = new PolicyMonitoring();
            policyMonitoring.setEnabled(deviceTypeMetaDefinition.isPolicyMonitoringEnabled());
            deviceTypeConfiguration.setPolicyMonitoring(policyMonitoring);

            ProvisioningConfig provisioningConfig = new ProvisioningConfig();
            provisioningConfig.setSharedWithAllTenants(deviceTypeMetaDefinition.isSharedWithAllTenants());
            deviceTypeConfiguration.setProvisioningConfig(provisioningConfig);

            PushNotificationConfig pushNotificationConfig = deviceTypeMetaDefinition.getPushNotificationConfig();
            if (pushNotificationConfig != null) {
                PushNotificationProvider pushNotificationProvider = new PushNotificationProvider();
                pushNotificationProvider.setType(pushNotificationConfig.getType());
                //default schedule value will be true.
                pushNotificationProvider.setScheduled(true);
                if (pushNotificationConfig.getProperties() != null &&
                        pushNotificationConfig.getProperties().size() > 0) {
                    ConfigProperties configProperties = new ConfigProperties();
                    List<Property> properties = new ArrayList<>();
                    for (Map.Entry<String, String> entry : pushNotificationConfig.getProperties().entrySet()) {
                        Property property = new Property();
                        property.setName(entry.getKey());
                        property.setValue(entry.getValue());
                        properties.add(property);
                    }
                    configProperties.addProperties(properties);
                    pushNotificationProvider.setConfigProperties(configProperties);
                }
                pushNotificationProvider.setFileBasedProperties(true);
                deviceTypeConfiguration.setPushNotificationProvider(pushNotificationProvider);
            }

//            This is commented until the task registration handling issue is solved
//            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceTypeMetaDefinition.getTaskConfig();
//            if (operationMonitoringTaskConfig != null) {
//                TaskConfiguration taskConfiguration = new TaskConfiguration();
//                taskConfiguration.setEnabled(operationMonitoringTaskConfig.isEnabled());
//                taskConfiguration.setFrequency(operationMonitoringTaskConfig.getFrequency());
//                if (operationMonitoringTaskConfig.getMonitoringOperation() != null) {
//                    List<TaskConfiguration.Operation> operations = new ArrayList<>();
//                    for (MonitoringOperation monitoringOperation : operationMonitoringTaskConfig
//                            .getMonitoringOperation()) {
//                        TaskConfiguration.Operation operation = new TaskConfiguration.Operation();
//                        operation.setOperationName(monitoringOperation.getTaskName());
//                        operation.setRecurrency(monitoringOperation.getRecurrentTimes());
//                        operations.add(operation);
//                    }
//                    taskConfiguration.setOperations(operations);
//                }
//                deviceTypeConfiguration.setTaskConfiguration(taskConfiguration);
//            }

            if (deviceTypeMetaDefinition.getInitialOperationConfig() != null) {
                InitialOperationConfig initialOperationConfig = deviceTypeMetaDefinition.getInitialOperationConfig();
                deviceTypeConfiguration.setOperations(initialOperationConfig.getOperations());
            }
        }
        PullNotificationSubscriberConfig pullNotificationSubscriber = new PullNotificationSubscriberConfig();
        pullNotificationSubscriber.setClassName(DEFAULT_PULL_NOTIFICATION_CLASS_NAME);
        deviceTypeConfiguration.setPullNotificationSubscriberConfig(pullNotificationSubscriber);
        return deviceTypeConfiguration;
    }

    private static DeviceTypeConfigIdentifier getDeviceTypeConfigIdentifier(String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return new DeviceTypeConfigIdentifier(deviceType, tenantDomain);
    }

    @Override
    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        return deviceTypeMetaDefinition;
    }
}
