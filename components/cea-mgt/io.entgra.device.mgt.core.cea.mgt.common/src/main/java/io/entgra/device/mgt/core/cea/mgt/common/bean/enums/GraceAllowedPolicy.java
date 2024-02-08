/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean.enums;

public enum GraceAllowedPolicy {
    NEW_AND_EXISTING("NEW_AND_EXISTING"),
    EXISTING_ONLY("EXISTING_ONLY"),
    NEW_ONLY("NEW_ONLY"),
    NOT_ALLOWED("NOT_ALLOWED");

    private final String name;

    GraceAllowedPolicy(String name) {
        this.name = name;
    }

    public boolean equalsName(String thatName) {
        return name.equals(thatName);
    }

    public String toString() {
        return name;
    }
}
