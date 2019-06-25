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

import com.liferay.gradle.plugins.defaults.LiferaySettingsIDEPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class CleanIDEProfileTask extends BaseIDEProfileTask {

	public static final String CLEAN_IDE_PROFILE_TASK_NAME = "cleanIdeProfile";

	@TaskAction
	public void cleanIDEProfile() throws Exception {
		Project project = getProject();

		String profileName = System.getProperty(
			LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME, "");

		if (profileName.isEmpty()) {
			throw new GradleException(
				LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME +
					" system property must be set.");
		}
		else if (profileName.contains(",") || profileName.contains(":")) {
			StringBuilder sb = new StringBuilder();

			sb.append(LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME);
			sb.append("'" + profileName + "' is invalid. ");
			sb.append(System.lineSeparator());
			sb.append("Profile name cannot contain ',' or ':'.");
			sb.append(System.lineSeparator());

			throw new IllegalArgumentException(sb.toString());
		}

		Logger logger = getLogger();

		logger.lifecycle(
			"Cleaning " + LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME +
				": {}",
			profileName);

		Collection<Project> dependencyProjects = getDependencyProjects(project);

		for (Project dependencyProject : dependencyProjects) {
			File buildProfilesFile = new File(
				dependencyProject.getProjectDir(), IDE_PROFILES_FILENAME);

			try {
				_processIDEProfilesFile(profileName, logger, buildProfilesFile);
			}
			catch (IOException ioe) {
				StringBuilder sb = new StringBuilder();

				sb.append("Error cleaning ");
				sb.append(LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME);
				sb.append(" specified as ");
				sb.append(profileName);
				sb.append(" in ");
				sb.append(buildProfilesFile.getAbsolutePath());

				throw new GradleException(sb.toString(), ioe);
			}
		}
	}

	private void _processIDEProfilesFile(
			String profileName, Logger logger, File buildProfilesFile)
		throws FileNotFoundException, IOException {

		ArrayList<String> foundProfileNames = new ArrayList<>();

		if (buildProfilesFile.exists()) {
			try (Scanner scanner = new Scanner(buildProfilesFile)) {
				while (scanner.hasNext()) {
					String line = scanner.next();

					if (!line.isEmpty() && !foundProfileNames.contains(line)) {
						foundProfileNames.add(line);
					}
				}
			}
		}

		if (foundProfileNames.contains(profileName)) {
			logger.lifecycle(
				"Removing " + profileName + " from {}", buildProfilesFile);

			foundProfileNames.remove(profileName);

			buildProfilesFile.delete();

			if (!foundProfileNames.isEmpty()) {
				try (FileWriter fileWriter = new FileWriter(
						buildProfilesFile)) {

					for (String foundProfileName : foundProfileNames) {
						fileWriter.write(
							foundProfileName + System.lineSeparator());
					}
				}
			}
		}
		else if (foundProfileNames.isEmpty()) {
			logger.lifecycle(
				"Deleting empty build profiles file {}",
				buildProfilesFile.getAbsolutePath());

			buildProfilesFile.delete();
		}
	}

}