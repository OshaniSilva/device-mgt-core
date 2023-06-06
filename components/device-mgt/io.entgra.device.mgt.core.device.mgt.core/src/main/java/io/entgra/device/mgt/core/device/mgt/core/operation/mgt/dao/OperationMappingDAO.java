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
package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMapping;

import java.util.List;

public interface OperationMappingDAO {

    void addOperationMapping(Operation operation, Integer deviceId, boolean isScheduled, Device device, Integer tenantId) throws OperationManagementDAOException;

    void addOperationMapping(Operation operation, List<Device> devices, boolean isScheduled, Integer tenantId)
            throws OperationManagementDAOException;

    void removeOperationMapping(int operationId, Integer deviceId) throws OperationManagementDAOException;

    void updateOperationMapping(int operationId, Integer deviceId, Operation.PushNotificationStatus pushNotificationStatus) throws
            OperationManagementDAOException;
    void updateOperationMapping(List<OperationMapping> operationMappingList) throws
            OperationManagementDAOException;


}
