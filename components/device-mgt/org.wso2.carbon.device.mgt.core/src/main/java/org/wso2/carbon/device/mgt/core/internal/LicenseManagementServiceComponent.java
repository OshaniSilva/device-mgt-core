/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.core.LicenseManager;
import org.wso2.carbon.device.mgt.core.LicenseManagerImpl;
import org.wso2.carbon.device.mgt.core.service.LicenseManagementService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.license.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class LicenseManagementServiceComponent {

    private static Log log = LogFactory.getLog(LicenseManagementServiceComponent.class);

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Initializing license management core bundle");
        }
        LicenseManager licenseManager = new LicenseManagerImpl();
        LicenseManagementDataHolder.getInstance().setLicenseManager(licenseManager);

        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service LicenseManagementService");
        }
        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.registerService(LicenseManagementService.class.getName(), new LicenseManagementService(), null);
        if (log.isDebugEnabled()) {
            log.debug("License management core bundle has been successfully initialized");
        }
    }

    /**
     * Sets Realm Service.
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        LicenseManagementDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        LicenseManagementDataHolder.getInstance().setRealmService(null);
    }
}
