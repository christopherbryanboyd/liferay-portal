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

package com.liferay.gradle.plugins.defaults.tasks;

import com.google.api.client.repackaged.com.google.common.base.Objects;

import com.liferay.gradle.plugins.defaults.internal.util.BuildProfileUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CleanBuildProfileTask extends DefaultTask {

	public static final String CLEAN_BUILD_PROFILE_TASK_NAME =
		"cleanBuildProfile";

	@TaskAction
	public void cleanBuildProfile() throws Exception {
		Project currentProject = getProject();

		Project sourceProject = BuildProfileUtil.getSourceProject(
			currentProject);

		String profileName = System.getProperty(
			SetBuildProfileTask.BUILD_PROFILE_NAME_PROPERTY_NAME);

		if ((profileName == null) || profileName.isEmpty()) {
			profileName = sourceProject.getPath();

			System.setProperty(
				SetBuildProfileTask.BUILD_PROFILE_NAME_PROPERTY_NAME,
				profileName);
		}
		else if (!Objects.equal(sourceProject.getPath(), profileName) &&
				 (profileName.contains(",") || profileName.contains(":"))) {

			StringBuilder sb = new StringBuilder();

			sb.append(SetBuildProfileTask.BUILD_PROFILE_NAME_PROPERTY_NAME);
			sb.append("'" + profileName + "' is invalid. ");
			sb.append(System.lineSeparator());
			sb.append("Profile name cannot contain ',' or ':'.");
			sb.append(System.lineSeparator());

			throw new IllegalArgumentException(sb.toString());
		}

		Logger logger = getLogger();

		logger.lifecycle(
			"Cleaning " + SetBuildProfileTask.BUILD_PROFILE_NAME_PROPERTY_NAME +
				": {}",
			profileName);

		Collection<Project> projectDependencies =
			BuildProfileUtil.getProjectDependencies(currentProject);

		for (Project projectDependency : projectDependencies) {
			File lfrBuildFile = new File(
				projectDependency.getProjectDir(),
				GradlePluginsDefaultsUtil.BUILD_PROFILE_FILE_NAME);

			try {
				_processBuildFile(profileName, logger, lfrBuildFile);
			}
			catch (IOException ioe) {
				StringBuilder sb = new StringBuilder();

				sb.append("Error cleaning ");
				sb.append(SetBuildProfileTask.BUILD_PROFILE_NAME_PROPERTY_NAME);
				sb.append(" specified as ");
				sb.append(profileName);
				sb.append(" in ");
				sb.append(lfrBuildFile.getAbsolutePath());

				String message = sb.toString();

				throw new GradleException(message, ioe);
			}
		}
	}

	private void _processBuildFile(
			String profileName, Logger logger, File lfrBuildFile)
		throws FileNotFoundException, IOException {

		ArrayList<String> list = new ArrayList<>();

		if (lfrBuildFile.exists()) {
			try (Scanner s = new Scanner(lfrBuildFile)) {
				while (s.hasNext()) {
					String foundProfileName = s.next();

					if (!foundProfileName.isEmpty() &&
						!list.contains(foundProfileName)) {

						list.add(foundProfileName);
					}
				}
			}
		}

		if (list.contains(profileName)) {
			logger.lifecycle(
				"Removing " + profileName + " from {}", lfrBuildFile);
			list.remove(profileName);
			lfrBuildFile.delete();

			if (!list.isEmpty()) {
				try (FileWriter fw = new FileWriter(lfrBuildFile)) {
					for (String foundProfileName : list) {
						fw.write(foundProfileName + System.lineSeparator());
					}
				}
			}
		}
		else if (list.isEmpty()) {
			logger.lifecycle(
				"Deleting empty marker file {}",
				lfrBuildFile.getAbsolutePath());

			lfrBuildFile.delete();
		}
	}

}