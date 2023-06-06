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

package io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception;

public class InvalidConfigurationStateException extends RuntimeException {

    private static final long serialVersionUID = -3151279311329070297L;

    private String errorMessage;
    private int errorCode;

    public InvalidConfigurationStateException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidConfigurationStateException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public InvalidConfigurationStateException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public InvalidConfigurationStateException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public InvalidConfigurationStateException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public InvalidConfigurationStateException() {
        super();
    }

    public InvalidConfigurationStateException(Throwable cause) {
        super(cause);
    }

}
