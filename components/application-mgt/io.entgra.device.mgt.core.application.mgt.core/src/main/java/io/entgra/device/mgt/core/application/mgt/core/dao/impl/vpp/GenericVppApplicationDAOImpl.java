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

package io.entgra.device.mgt.core.application.mgt.core.dao.impl.vpp;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssetDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.dao.VppApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.device.mgt.core.application.mgt.core.util.DAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.List;

public class GenericVppApplicationDAOImpl  extends AbstractDAOImpl implements VppApplicationDAO {
    private static final Log log = LogFactory.getLog(GenericVppApplicationDAOImpl.class);

    @Override
    public int addVppUser(VppUserDTO userDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int vppUserId = -1;
        String sql = "INSERT INTO "
                + "AP_VPP_USER("
                + "CLIENT_USER_ID, "
                + "DM_USERNAME, "
                + "TENANT_ID, "
                + "EMAIL, "
                + "INVITE_CODE, "
                + "STATUS,"
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "MANAGED_ID,"
                + "TEMP_PASSWORD) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                long currentTime = System.currentTimeMillis();
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, tenantId);
                stmt.setString(4, userDTO.getEmail());
                stmt.setString(5, userDTO.getInviteCode());
                stmt.setString(6, userDTO.getStatus());
                stmt.setLong(7, currentTime);
                stmt.setLong(8, currentTime);
                stmt.setString(9, userDTO.getManagedId());
                stmt.setString(10, userDTO.getTmpPassword());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        vppUserId = rs.getInt(1);
                    }
                }
                return vppUserId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add  the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppUserDTO updateVppUser(VppUserDTO userDTO, int tenantId)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE "
                + "AP_VPP_USER "
                + "SET "
                + "CLIENT_USER_ID = ?,"
                + "DM_USERNAME = ?, "
                + "TENANT_ID = ?, "
                + "EMAIL = ?, "
                + "INVITE_CODE = ?, "
                + "STATUS = ?, "
                + "LAST_UPDATED_TIME = ?, "
                + "MANAGED_ID = ?, "
                + "TEMP_PASSWORD = ? "
                + "WHERE ID = ?";
        try {
            Connection conn = this.getDBConnection();
            long updatedTime = System.currentTimeMillis();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, tenantId);
                stmt.setString(4, userDTO.getEmail());
                stmt.setString(5, userDTO.getInviteCode());
                stmt.setString(6, userDTO.getStatus());
                stmt.setLong(7, updatedTime);
                stmt.setString(8, userDTO.getManagedId());
                stmt.setString(9, userDTO.getTmpPassword());
                stmt.setInt(10, userDTO.getId());
                stmt.executeUpdate();
                if (stmt.executeUpdate() == 1) {
                    return userDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when updating the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to updating the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppUserDTO getUserByDMUsername(String emmUsername, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "ID, "
                + "CLIENT_USER_ID, "
                + "TENANT_ID, "
                + "EMAIL, "
                + "INVITE_CODE, "
                + "STATUS, "
                + "CREATED_TIME, "
                + "LAST_UPDATED_TIME, "
                + "MANAGED_ID, "
                + "TEMP_PASSWORD "
                + "FROM AP_VPP_USER "
                + "WHERE DM_USERNAME = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, emmUsername);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadVppUser(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when retrieving vpp user by EMM Username.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve vpp user by EMM Username.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one user for: " + emmUsername;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssetDTO getAssetByAppId(int appId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "ID, "
                + "APP_ID, "
                + "TENANT_ID, "
                + "CREATED_TIME, "
                + "LAST_UPDATED_TIME, "
                + "ADAM_ID, "
                + "ASSIGNED_COUNT, "
                + "DEVICE_ASSIGNABLE, "
                + "PRICING_PARAMS, "
                + "PRODUCT_TYPE, "
                + "RETIRED_COUNT, "
                + "REVOCABLE "
//                + "SUPPORTED_PLATFORMS "
                + "FROM AP_ASSETS "
                + "WHERE APP_ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadAsset(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when retrieving asset data of app id "+ appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve asset by app id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }  catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one app for app id: " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int addAsset(VppAssetDTO vppAssetDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int assetId = -1;
        String sql = "INSERT INTO "
                + "AP_ASSETS("
                + "APP_ID, "
                + "TENANT_ID, "
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "ADAM_ID,"
                + "ASSIGNED_COUNT,"
                + "DEVICE_ASSIGNABLE,"
                + "PRICING_PARAMS,"
                + "PRODUCT_TYPE,"
                + "RETIRED_COUNT,"
                + "REVOCABLE) "
//                + "SUPPORTED_PLATFORMS) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                long currentTime = System.currentTimeMillis();
                stmt.setInt(1, vppAssetDTO.getAppId());
                stmt.setInt(2, tenantId);
                stmt.setLong(3, currentTime);
                stmt.setLong(4, currentTime);
                stmt.setString(5, vppAssetDTO.getAdamId());
                stmt.setString(6, vppAssetDTO.getAssignedCount());
                stmt.setString(7, vppAssetDTO.getDeviceAssignable());
                stmt.setString(8, vppAssetDTO.getPricingParam());
                stmt.setString(9, vppAssetDTO.getProductType());
                stmt.setString(10, vppAssetDTO.getRetiredCount());
                stmt.setString(11, vppAssetDTO.getRevocable());
//                List<String> platformList =  vppAssetDTO.getSupportedPlatforms();
//                String platformString = String.join(",", platformList);
//                stmt.setString(12, platformString);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        assetId = rs.getInt(1);
                    }
                }
                return assetId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssetDTO updateAsset(VppAssetDTO vppAssetDTO, int tenantId)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE "
                + "AP_ASSETS "
                + "SET "
                + "APP_ID = ?,"
                + "LAST_UPDATED_TIME = ?, "
                + "ADAM_ID = ?, "
                + "ASSIGNED_COUNT = ?, "
                + "DEVICE_ASSIGNABLE = ?, "
                + "PRICING_PARAMS = ?, "
                + "PRODUCT_TYPE = ?, "
                + "RETIRED_COUNT = ?, "
                + "REVOCABLE = ? "
//                + "SUPPORTED_PLATFORMS = ? "
                + "WHERE ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            long updatedTime = System.currentTimeMillis();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vppAssetDTO.getAppId());
                stmt.setLong(2, updatedTime);
                stmt.setString(3, vppAssetDTO.getAdamId());
                stmt.setString(4, vppAssetDTO.getAssignedCount());
                stmt.setString(5, vppAssetDTO.getDeviceAssignable());
                stmt.setString(6, vppAssetDTO.getPricingParam());
                stmt.setString(7, vppAssetDTO.getProductType());
                stmt.setString(8, vppAssetDTO.getRetiredCount());
                stmt.setString(9, vppAssetDTO.getRevocable());
//                List<String> platformList =  vppAssetDTO.getSupportedPlatforms();
//                String platformString = String.join(",", platformList);
//                stmt.setString(10, platformString);
                stmt.setInt(10, vppAssetDTO.getId());
                stmt.setLong(11, tenantId);
                stmt.executeUpdate();
                if (stmt.executeUpdate() == 1) {
                    return vppAssetDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when updating the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to updating the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
