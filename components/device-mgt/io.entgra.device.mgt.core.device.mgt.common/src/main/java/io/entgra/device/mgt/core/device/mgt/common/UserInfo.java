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

@ApiModel(value = "UserInfo", description = "User details and the roles of the user.")
public class UserInfo extends BasicUserInfo {

    @ApiModelProperty(name = "password", value = "Base64 encoded password.", required = true )
    private String password;

    @ApiModelProperty(name = "roles", value = "List of roles.", required = true )
    private String[] roles;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getRoles() {
        String[] copiedRoles = roles;
        if (roles != null){
            copiedRoles = roles.clone();
        }
        return copiedRoles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

}
