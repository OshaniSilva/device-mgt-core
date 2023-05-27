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

function initializeSpeed() {
    var serverUrl = "/api/device-mgt/v1.0/geo-services/alerts/Speed";
    // var serverUrl = "/portal/store/carbon.super/fs/gadget/geo-dashboard/controllers/get_alerts.jag?executionPlanType=Speed&deviceId=" + deviceId;
    invokerUtil.get(serverUrl, function (response) {
        response = JSON.parse(response);
        if (response) {
            $("#speedAlertValue").val(response.speedLimit);
        }
    });
}
initializeSpeed();
