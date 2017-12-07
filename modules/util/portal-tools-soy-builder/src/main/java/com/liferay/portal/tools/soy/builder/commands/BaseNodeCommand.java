/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools.soy.builder.commands;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * @author Christopher Bryan Boyd
 */
public abstract class BaseNodeCommand implements Command {

	public File getModulePath() {
		return _modulePath;
	}

	public File getNodePath() {
		return _nodePath;
	}

	public File getWorkingPath() {
		return _workingPath;
	}

	public void setModulePath(File modulePath) {
		_modulePath = modulePath;
	}

	public void setNodePath(File nodePath) {
		_nodePath = nodePath;
	}

	public void setWorkingPath(File workingPath) {
		_workingPath = workingPath;
	}

	protected static int executeCommand(String command, File workingPath)
		throws InterruptedException, IOException {

		Runtime runtime = Runtime.getRuntime();

		Process process = runtime.exec(command, null, workingPath);

		int returnCode = process.waitFor();

		return returnCode;
	}

	@Parameter(
		description = "The path to the node module to be executed.",
		names = {"--modulePath"}, required = true
	)
	private File _modulePath;

	@Parameter(
		description = "The path to the node executable.",
		names = {"--nodePath"}, required = true
	)
	private File _nodePath;

	@Parameter(
		description = "The working path (directory) of the process to be executed.",
		names = {"--workingPath"}, required = true
	)
	private File _workingPath;

}