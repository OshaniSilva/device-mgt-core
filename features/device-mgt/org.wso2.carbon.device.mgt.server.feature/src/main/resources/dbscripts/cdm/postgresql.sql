CREATE TABLE IF NOT EXISTS  DM_DEVICE_TYPE (
  ID BIGSERIAL PRIMARY KEY,
  NAME VARCHAR(300) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_CERTIFICATE (
  ID                    BIGSERIAL NOT NULL PRIMARY KEY,
  SERIAL_NUMBER         VARCHAR(500) DEFAULT NULL,
  CERTIFICATE           BYTEA DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS  DM_DEVICE (
  ID                    BIGSERIAL NOT NULL PRIMARY KEY,
  DESCRIPTION           TEXT DEFAULT NULL,
  NAME                  VARCHAR(100) DEFAULT NULL,
  DEVICE_TYPE_ID        INTEGER DEFAULT NULL,
  DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
  TENANT_ID INTEGER DEFAULT 0,
  CONSTRAINT fk_DM_DEVICE_DM_DEVICE_TYPE2 FOREIGN KEY (DEVICE_TYPE_ID )
  REFERENCES DM_DEVICE_TYPE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_OPERATION (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  TYPE VARCHAR(50) NOT NULL,
  CREATED_TIMESTAMP TIMESTAMP NOT NULL,
  RECEIVED_TIMESTAMP TIMESTAMP NULL,
  OPERATION_CODE VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS  DM_CONFIG_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  OPERATION_CONFIG BYTEA DEFAULT NULL,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_config FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_COMMAND_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_command FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED INTEGER NOT NULL DEFAULT 0,
  OPERATION_DETAILS BYTEA DEFAULT NULL,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_policy FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_PROFILE_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED INTEGER NOT NULL DEFAULT 0,
  OPERATION_DETAILS BYTEA DEFAULT NULL,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_profile FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_ENROLMENT (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  OWNER VARCHAR(50) NOT NULL,
  OWNERSHIP VARCHAR(45) DEFAULT NULL,
  STATUS VARCHAR(50) NULL,
  DATE_OF_ENROLMENT TIMESTAMP NULL DEFAULT NULL,
  DATE_OF_LAST_UPDATE TIMESTAMP NULL DEFAULT NULL,
  TENANT_ID INTEGER NOT NULL,
  CONSTRAINT fk_dm_device_enrolment FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_ENROLMENT_OP_MAPPING (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ENROLMENT_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NOT NULL,
  STATUS VARCHAR(50) NULL,
  CONSTRAINT fk_dm_device_operation_mapping_device FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_dm_device_operation_mapping_operation FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_DEVICE_OPERATION_RESPONSE (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ENROLMENT_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NOT NULL,
  OPERATION_RESPONSE BYTEA DEFAULT NULL,
  CONSTRAINT fk_dm_device_operation_response_enrollment FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_dm_device_operation_response_operation FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- POLICY RELATED TABLES ---

CREATE  TABLE DM_PROFILE (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  PROFILE_NAME VARCHAR(45) NOT NULL ,
  TENANT_ID INTEGER NOT NULL ,
  DEVICE_TYPE_ID INTEGER NOT NULL ,
  CREATED_TIME TIMESTAMP NOT NULL ,
  UPDATED_TIME TIMESTAMP NOT NULL ,
  CONSTRAINT DM_PROFILE_DEVICE_TYPE
  FOREIGN KEY (DEVICE_TYPE_ID )
  REFERENCES DM_DEVICE_TYPE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  NAME VARCHAR(45) DEFAULT NULL ,
  DESCRIPTION VARCHAR(1000) NULL,
  TENANT_ID INTEGER NOT NULL ,
  PROFILE_ID INTEGER NOT NULL ,
  OWNERSHIP_TYPE VARCHAR(45) NULL,
  COMPLIANCE VARCHAR(100) NULL,
  PRIORITY INTEGER NOT NULL,
  ACTIVE INTEGER NOT NULL,
  UPDATED INTEGER NULL,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY
  FOREIGN KEY (PROFILE_ID )
  REFERENCES DM_PROFILE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_DEVICE_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  DEVICE BYTEA NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  CONSTRAINT FK_POLICY_DEVICE_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
  CONSTRAINT FK_DEVICE_DEVICE_POLICY
  FOREIGN KEY (DEVICE_ID )
  REFERENCES DM_DEVICE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_DEVICE_TYPE_POLICY (
  ID INTEGER NOT NULL,
  DEVICE_TYPE_ID INTEGER NOT NULL ,
  POLICY_ID INTEGER NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DEVICE_TYPE_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
  CONSTRAINT FK_DEVICE_TYPE_POLICY_DEVICE_TYPE
  FOREIGN KEY (DEVICE_TYPE_ID )
  REFERENCES DM_DEVICE_TYPE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_PROFILE_FEATURES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  PROFILE_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(30) NOT NULL,
  DEVICE_TYPE_ID INT NOT NULL,
  TENANT_ID INTEGER NOT NULL ,
  CONTENT BYTEA NULL DEFAULT NULL,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY_FEATURES
  FOREIGN KEY (PROFILE_ID)
  REFERENCES DM_PROFILE (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_ROLE_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ROLE_NAME VARCHAR(45) NOT NULL ,
  POLICY_ID INTEGER NOT NULL,
  CONSTRAINT FK_ROLE_POLICY_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_USER_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_ID INT NOT NULL ,
  USERNAME VARCHAR(45) NOT NULL,
  CONSTRAINT DM_POLICY_USER_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE  TABLE DM_DEVICE_POLICY_APPLIED (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  POLICY_CONTENT BYTEA NULL ,
  TENANT_ID INTEGER NOT NULL,
  APPLIED INTEGER[1] NULL ,
  CREATED_TIME TIMESTAMP NULL ,
  UPDATED_TIME TIMESTAMP NULL ,
  APPLIED_TIME TIMESTAMP NULL ,
  CONSTRAINT FK_DM_POLICY_DEVCIE_APPLIED
  FOREIGN KEY (DEVICE_ID )
  REFERENCES DM_DEVICE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_CRITERIA (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  TENANT_ID INT NOT NULL,
  NAME VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CRITERIA (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  CRITERIA_ID INT NOT NULL,
  POLICY_ID INT NOT NULL,
  CONSTRAINT FK_CRITERIA_POLICY_CRITERIA
  FOREIGN KEY (CRITERIA_ID)
  REFERENCES DM_CRITERIA (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
  CONSTRAINT FK_POLICY_POLICY_CRITERIA
  FOREIGN KEY (POLICY_ID)
  REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CRITERIA_PROPERTIES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_CRITERION_ID INT NOT NULL,
  PROP_KEY VARCHAR(45) NULL,
  PROP_VALUE VARCHAR(100) NULL,
  CONTENT BYTEA NULL,
  CONSTRAINT FK_POLICY_CRITERIA_PROPERTIES
  FOREIGN KEY (POLICY_CRITERION_ID)
  REFERENCES DM_POLICY_CRITERIA (ID)
  ON DELETE CASCADE
  ON UPDATE NO ACTION
);
COMMENT ON COLUMN DM_POLICY_CRITERIA_PROPERTIES.CONTENT IS 'This is used to ';

CREATE TABLE IF NOT EXISTS  DM_POLICY_COMPLIANCE_STATUS (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  STATUS INTEGER NULL,
  LAST_SUCCESS_TIME TIMESTAMP NULL,
  LAST_REQUESTED_TIME TIMESTAMP NULL,
  LAST_FAILED_TIME TIMESTAMP NULL,
  ATTEMPTS INTEGER NULL,
  CONSTRAINT FK_POLICY_COMPLIANCE_STATUS_POLICY
  FOREIGN KEY (POLICY_ID)
  REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CHANGE_MGT (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_ID INTEGER NOT NULL,
  DEVICE_TYPE_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_COMPLIANCE_FEATURES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  COMPLIANCE_STATUS_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(15) NOT NULL,
  STATUS INTEGER NULL,
  CONSTRAINT FK_COMPLIANCE_FEATURES_STATUS
  FOREIGN KEY (COMPLIANCE_STATUS_ID)
  REFERENCES DM_POLICY_COMPLIANCE_STATUS (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_ENROLMENT (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  OWNER VARCHAR(50) NOT NULL,
  OWNERSHIP VARCHAR(45) DEFAULT NULL,
  STATUS VARCHAR(50) NULL,
  DATE_OF_ENROLMENT TIMESTAMP NULL DEFAULT NULL,
  DATE_OF_LAST_UPDATE TIMESTAMP NULL DEFAULT NULL,
  TENANT_ID INT NOT NULL,
  CONSTRAINT fk_dm_device_enrolment FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_APPLICATION (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  NAME VARCHAR(150) NOT NULL,
  APP_IDENTIFIER VARCHAR(150) NOT NULL,
  PLATFORM VARCHAR(50) DEFAULT NULL,
  CATEGORY VARCHAR(50) NULL,
  VERSION VARCHAR(50) NULL,
  TYPE VARCHAR(50) NULL,
  LOCATION_URL VARCHAR(100) DEFAULT NULL,
  IMAGE_URL VARCHAR(100) DEFAULT NULL,
  APP_PROPERTIES BYTEA NULL,
  TENANT_ID INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_APPLICATION_MAPPING (
    ID BIGSERIAL NOT NULL PRIMARY KEY,
    DEVICE_ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    CONSTRAINT fk_dm_device FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_application FOREIGN KEY (APPLICATION_ID) REFERENCES
    DM_APPLICATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- POLICY RELATED TABLES  FINISHED --

-- NOTIFICATION TABLE --
CREATE TABLE IF NOT EXISTS  DM_NOTIFICATION (
  NOTIFICATION_ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  STATUS VARCHAR(10) NULL,
  DESCRIPTION VARCHAR(100) NULL,
  CONSTRAINT fk_dm_device_notification FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_dm_operation_notification FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- NOTIFICATION TABLE END --

