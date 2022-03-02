/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common.cost.mgt;

import com.google.gson.Gson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.Date;

@XmlRootElement(name = "Cost")
public class Costdata {

    private String tenantDomain;
    private Double cost;
    private long subscriptionBeginning;
    private long subscriptionEnd;

    @XmlElement(name = "tenantDomain", required = true)
    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    @XmlElement(name = "cost", required = true)
    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    @XmlElement(name = "subscriptionBeginning", required = true)
    public long getSubscriptionBeginning() {
        return subscriptionBeginning;
    }

    public void setSubscriptionBeginning(long subscriptionBeginning) {
        this.subscriptionBeginning = subscriptionBeginning;
    }

    @XmlElement(name = "subscriptionEnd", required = true)
    public long getSubscriptionEnd() {
        return subscriptionEnd;
    }

    public void setSubscriptionEnd(long subscriptionEnd) {
        this.subscriptionEnd = subscriptionEnd;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
