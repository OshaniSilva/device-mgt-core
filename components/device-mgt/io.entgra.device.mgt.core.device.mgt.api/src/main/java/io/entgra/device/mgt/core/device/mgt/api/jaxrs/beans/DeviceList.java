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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.entgra.device.mgt.core.device.mgt.common.BasePaginatedResult;
import io.swagger.annotations.ApiModelProperty;
import io.entgra.device.mgt.core.device.mgt.common.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends BasePaginatedResult {

    private List<Device> devices = new ArrayList<>();

    @ApiModelProperty(name = "totalCost", value = "Total cost of all devices per tenant", required = false)
    private double totalCost;

    @ApiModelProperty(name = "message", value = "Send information text to the billing UI", required = false)
    private String message;

    @ApiModelProperty(name = "deviceCount", value = "Total count of all devices per tenant", required = false)
    private double deviceCount;

    @ApiModelProperty(name = "billPeriod", value = "Billed period", required = false)
    private String billPeriod;

    @ApiModelProperty(value = "List of devices returned")
    @JsonProperty("devices")
    public List<Device> getList() {
        return devices;
    }

    public void setList(List<Device> devices) {
        this.devices = devices;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public double getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(double deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("  devices: [").append(devices).append("\n");
        sb.append("]}\n");
        return sb.toString();
    }

}

