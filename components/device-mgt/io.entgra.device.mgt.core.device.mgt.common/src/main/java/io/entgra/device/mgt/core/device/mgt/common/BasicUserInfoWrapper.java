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

package io.entgra.device.mgt.core.device.mgt.common;

import io.entgra.device.mgt.core.device.mgt.common.BasicUserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "BasicUserInfoWrapper", description = "This contains basic details of a set of users that matches " +
        "a given criteria as a collection and a message if there's any.")
public class BasicUserInfoWrapper {

    @ApiModelProperty(
            name = "basicUserInfo",
            value = "Details of the User.",
            required = true)
    private BasicUserInfo basicUserInfo;

    @ApiModelProperty(
            name = "message",
            value = "Response message if there's any.")
    private String message;

    public BasicUserInfo getBasicUserInfo() {
        return basicUserInfo;
    }

    public void setBasicUserInfo(BasicUserInfo basicUserInfo) {
        this.basicUserInfo = basicUserInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
