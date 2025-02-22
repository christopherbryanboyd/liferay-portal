/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.sharepoint.soap.repository.connector;

/**
 * @author Iván Zaera
 */
public class SharepointConnectionFactory {

	public static SharepointConnection getInstance(
		SharepointConnection.ServerVersion serverVersion, String serverProtocol,
		String serverAddress, int serverPort, String sitePath,
		String libraryName, String libraryPath, String username,
		String password) {

		return new SharepointConnectionImpl(
			serverVersion, serverProtocol, serverAddress, serverPort, sitePath,
			libraryName, libraryPath, username, password);
	}

}