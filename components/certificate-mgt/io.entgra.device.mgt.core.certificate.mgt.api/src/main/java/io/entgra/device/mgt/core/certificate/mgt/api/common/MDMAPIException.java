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

package io.entgra.device.mgt.core.certificate.mgt.api.common;

/**
 * Custom exception class for handling CDM API related exceptions.
 */
public class MDMAPIException extends Exception {

	private static final long serialVersionUID = 7950151650447893900L;
	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public MDMAPIException(String msg, Exception e) {
		super(msg, e);
		setErrorMessage(msg);
	}

	public MDMAPIException(String msg, Throwable cause) {
		super(msg, cause);
		setErrorMessage(msg);
	}

	public MDMAPIException(String msg) {
		super(msg);
		setErrorMessage(msg);
	}

	public MDMAPIException() {
		super();
	}

	public MDMAPIException(Throwable cause) {
		super(cause);
	}
}
