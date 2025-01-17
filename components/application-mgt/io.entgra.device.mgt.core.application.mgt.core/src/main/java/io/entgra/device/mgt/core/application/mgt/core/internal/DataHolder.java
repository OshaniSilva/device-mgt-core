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
package io.entgra.device.mgt.core.application.mgt.core.internal;

import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.device.mgt.core.application.mgt.common.services.AppmDataHandler;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.common.services.SPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.ReviewManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SubscriptionManager;
import io.entgra.device.mgt.core.application.mgt.common.services.VPPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * DataHolder is responsible for holding the references to OSGI Services.
 */
public class DataHolder {

    private DeviceManagementProviderService deviceManagementService;

    private RealmService realmService;

    private SPApplicationManager SPApplicationManager;

    private VPPApplicationManager vppApplicationManager;

    private ApplicationManager applicationManager;

    private ReviewManager reviewManager;

    private SubscriptionManager subscriptionManager;

    private ApplicationStorageManager applicationStorageManager;

    private LifecycleStateManager lifecycleStateManager;

    private AppmDataHandler configManager;

    private TaskService taskService;
    private FileTransferService fileTransferService;

    private static final DataHolder applicationMgtDataHolder = new DataHolder();

    private DataHolder() {

    }

    public static DataHolder getInstance() {
        return applicationMgtDataHolder;
    }

    public DeviceManagementProviderService getDeviceManagementService() {
        return deviceManagementService;
    }

    public void setDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public ReviewManager getReviewManager() {
        return reviewManager;
    }

    public void setReviewManager(ReviewManager reviewManager) {
        this.reviewManager = reviewManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public void setApplicationStorageManager(ApplicationStorageManager applicationStorageManager) {
        this.applicationStorageManager = applicationStorageManager;
    }

    public ApplicationStorageManager getApplicationStorageManager() {
        return applicationStorageManager;
    }

    public LifecycleStateManager getLifecycleStateManager() {
        return lifecycleStateManager;
    }

    public void setLifecycleStateManger(LifecycleStateManager lifecycleStateManager) {
        this.lifecycleStateManager = lifecycleStateManager;
    }

    public AppmDataHandler getConfigManager() {
        return configManager;
    }

    public void setConfigManager(AppmDataHandler configManager) {
        this.configManager = configManager;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public SPApplicationManager getISApplicationManager() {
        return SPApplicationManager;
    }

    public void setISApplicationManager(SPApplicationManager SPApplicationManager) {
        this.SPApplicationManager = SPApplicationManager;
    }

    public VPPApplicationManager getVppApplicationManager() {
        return vppApplicationManager;
    }

    public void setVppApplicationManager(VPPApplicationManager vppApplicationManager) {
        this.vppApplicationManager = vppApplicationManager;
    }

    public FileTransferService getFileTransferService() {
        return fileTransferService;
    }

    public void setFileTransferService(FileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
    }
}
