/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManagementProviderServiceImpl implements GroupManagementProviderService {

    private static Log log = LogFactory.getLog(GroupManagementProviderServiceImpl.class);

    private GroupDAO groupDAO;

    /**
     * Set groupDAO from GroupManagementDAOFactory when class instantiate.
     */
    public GroupManagementProviderServiceImpl() {
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createGroup(DeviceGroup deviceGroup, String defaultRole, String[] defaultPermissions)
            throws GroupManagementException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        DeviceGroupBuilder groupBroker = new DeviceGroupBuilder(deviceGroup);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int groupId = -1;
        try {
            GroupManagementDAOFactory.beginTransaction();
            boolean nameIsExists = this.groupDAO.isGroupExist(deviceGroup.getName(), tenantId);
            if (!nameIsExists) {
                groupId = this.groupDAO.addGroup(groupBroker, tenantId);
                GroupManagementDAOFactory.commitTransaction();
                if (groupId < 0) {
                    return -1;
                }
                groupBroker.setId(groupId);
            } else {
                return -2;
            }
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while adding deviceGroup " +
                    "'" + deviceGroup.getName() + "' to database.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        addGroupSharingRole(groupBroker.getOwner(), groupId, defaultRole, defaultPermissions);
        if (log.isDebugEnabled()) {
            log.debug("DeviceGroup added: " + groupBroker.getName());
        }
        return groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(DeviceGroup deviceGroup) throws GroupManagementException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        try {
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.updateGroup(deviceGroup, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while modifying deviceGroup " +
                    "'" + deviceGroup.getName() + "'.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteGroup(int groupId) throws GroupManagementException {
        String roleName;
        DeviceGroup deviceGroup = getGroup(groupId);
        if (deviceGroup == null) {
            return false;
        }
        List<String> groupRoles = getRoles(groupId);
        for (String role : groupRoles) {
            if (role != null) {
                roleName = role.replace("Internal/group-" + groupId + "-", "");
                removeGroupSharingRole(groupId, roleName);
            }
        }
        try {
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.deleteGroup(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup " + deviceGroup.getName() + " removed.");
            }
            return true;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while removing group " +
                                               "'" + groupId + "' data.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(int groupId) throws GroupManagementException {
        DeviceGroupBuilder groupBroker;
        try {
            GroupManagementDAOFactory.openConnection();
            groupBroker = this.groupDAO.getGroup(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupId, e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        if (groupBroker != null) {
            groupBroker.setUsers(this.getUsers(groupId));
            groupBroker.setRoles(this.getRoles(groupId));
            return groupBroker.getGroup();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> findInGroups(String groupName, String owner) throws GroupManagementException {
        List<DeviceGroupBuilder> deviceGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.findInGroups(groupName, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while finding group " + groupName, e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        List<DeviceGroup> groupsWithData = new ArrayList<>();
        for (DeviceGroupBuilder groupBroker : deviceGroups) {
            groupBroker.setUsers(this.getUsers(groupBroker.getId()));
            groupBroker.setRoles(this.getRoles(groupBroker.getId()));
            groupsWithData.add(groupBroker.getGroup());
        }
        return groupsWithData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            Map<Integer, DeviceGroup> groups = new HashMap<>();
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-")) {
                    DeviceGroup deviceGroup = extractNewGroupFromRole(groups, role);
                    if (deviceGroup != null) {
                        groups.put(deviceGroup.getId(), deviceGroup);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount(String username) throws GroupManagementException {
        return this.getGroups(username).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shareGroup(String username, int groupId, String sharingRole)
            throws GroupManagementException {
        return modifyGroupShare(username, groupId, sharingRole, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unshareGroup(String username, int groupId, String sharingRole)
            throws GroupManagementException {
        return modifyGroupShare(username, groupId, sharingRole, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addGroupSharingRole(String username, int groupId, String roleName,
                                       String[] permissions)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        String[] userNames = new String[1];
        try {
            DeviceGroup deviceGroup = getGroup(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupId + "-" + roleName;
            userNames[0] = username;
            Permission[] carbonPermissions = new Permission[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                carbonPermissions[i] = new Permission(permissions[i], CarbonConstants.UI_PERMISSION_ACTION);
            }
            userStoreManager.addRole(role, userNames, carbonPermissions);
            return true;
        } catch (UserStoreException e) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeGroupSharingRole(int groupId, String roleName)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        try {
            DeviceGroup deviceGroup = getGroup(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupId + "-" + roleName;
            userStoreManager.deleteRole(role);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles;
        List<String> groupRoles;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles = userStoreManager.getRoleNames();
            groupRoles = new ArrayList<>();
            for (String r : roles) {
                if (r != null && r.contains("Internal/group-" + groupId + "-")) {
                    groupRoles.add(r.replace("Internal/group-" + groupId + "-", ""));
                }
            }
            return groupRoles;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(String username, int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        List<String> groupRoleList = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-" + groupId)) {
                    String roleName = role.replace("Internal/group-" + groupId + "-", "");
                    groupRoleList.add(roleName);
                }
            }
            return groupRoleList;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupUser> getUsers(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        Map<String, GroupUser> groupUserHashMap = new HashMap<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getRoles(groupId);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/group-" + groupId + "-" + role);
                for (String user : users) {
                    GroupUser groupUser;
                    if (groupUserHashMap.containsKey(user)) {
                        groupUser = groupUserHashMap.get(user);
                        groupUser.getGroupRoles().add(role);
                    } else {
                        groupUser = new GroupUser();
                        groupUser.setUsername(user);
                        groupUser.setGroupRoles(new ArrayList<String>());
                        groupUser.getGroupRoles().add(role);
                        groupUserHashMap.put(user, groupUser);
                    }
                }
            }
            return new ArrayList<>(groupUserHashMap.values());
        } catch (UserStoreException e) {
            String errorMsg = "User store error in fetching user list for group id:" + groupId;
            log.error(errorMsg, e);
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Device> getDevices(int groupId) throws GroupManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return this.groupDAO.getDevices(groupId, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while getting devices in group.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Device> getDevices(int groupId, PaginationRequest request)
            throws GroupManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return this.groupDAO.getDevices(groupId, request, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while getting devices in group.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeviceCount(int groupId) throws GroupManagementException {
        try {
            int count;
            GroupManagementDAOFactory.beginTransaction();
            count = groupDAO.getDeviceCount(groupId,
                                            CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
            return count;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while retrieving device count of group " +
                                               "'" + groupId + "'.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addDevice(DeviceIdentifier deviceIdentifier, int groupId)
            throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
            deviceGroup = this.getGroup(groupId);
            if (device == null || deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            this.groupDAO.addDevice(groupId, device.getId(), tenantId);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while retrieving device.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while adding device to group '" + groupId + "'.", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeDevice(DeviceIdentifier deviceIdentifier, int groupId)
            throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
            deviceGroup = this.getGroup(groupId);
            if (device == null || deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            this.groupDAO.removeDevice(groupId, device.getId(), tenantId);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while retrieving device.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while adding device to group '" + groupId + "'.", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPermissions(String username, int groupId) throws GroupManagementException {
        UserRealm userRealm;
        List<String> roles = getRoles(username, groupId);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> lstPermissions = new ArrayList<>();
            String[] resourceIds = userRealm.getAuthorizationManager().getAllowedUIResourcesForUser(username, "/");
            if (resourceIds != null) {
                for (String resourceId : resourceIds) {
                    for (String roleName : roles) {
                        if (userRealm.getAuthorizationManager().
                                isRoleAuthorized("Internal/group-" + groupId + "-" + roleName, resourceId,
                                                 CarbonConstants.UI_PERMISSION_ACTION)) {
                            lstPermissions.add(resourceId);
                        }
                    }
                }
            }
            String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
            return UserCoreUtil.optimizePermissions(permissions);
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username, String permission)
            throws GroupManagementException {
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(username);
            for (String role : roles) {
                if (role != null && role.contains("Internal/group-") && userRealm.getAuthorizationManager()
                        .isRoleAuthorized(role, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    DeviceGroup deviceGroup = extractNewGroupFromRole(groups, role);
                    if (deviceGroup != null) {
                        groups.put(deviceGroup.getId(), deviceGroup);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String username, int groupId, String permission)
            throws GroupManagementException {
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> roles = this.getRoles(username, groupId);
            for (String role : roles) {
                if (userRealm.getAuthorizationManager()
                        .isRoleAuthorized("Internal/group-" + groupId + "-" + role, permission,
                                          CarbonConstants.UI_PERMISSION_ACTION)) {
                    return true;
                }
            }
            return false;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    private boolean modifyGroupShare(String username, int groupId, String sharingRole,
                                     boolean isAddNew)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles = new String[1];
        try {
            DeviceGroup deviceGroup = getGroup(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
            roles[0] = "Internal/group-" + groupId + "-" + sharingRole;
            if (isAddNew) {
                userStoreManager.updateRoleListOfUser(username, null, roles);
            } else {
                userStoreManager.updateRoleListOfUser(username, roles, null);
            }
            return true;
        } catch (UserStoreException e) {
            throw new GroupManagementException("User store error in adding user " + username + " to group name:" +
                                               groupId, e);
        }
    }

    private DeviceGroup extractNewGroupFromRole(Map<Integer, DeviceGroup> groups, String role)
            throws GroupManagementException {
        try {
            int groupId = Integer.parseInt(role.split("-")[1]);
            if (!groups.containsKey(groupId)) {
                return getGroup(groupId);
            }
        } catch (NumberFormatException e) {
            log.error("Unable to extract groupId from role " + role, e);
        }
        return null;
    }

}
