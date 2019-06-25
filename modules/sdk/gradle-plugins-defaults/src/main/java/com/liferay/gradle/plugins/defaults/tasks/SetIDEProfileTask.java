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
public class SetIDEProfileTask extends BaseIDEProfileTask {

	public static final String SET_IDE_PROFILE_TASK_NAME = "setIdeProfile";

	@TaskAction
	public void setIDEProfile() throws Exception {
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

		logger.lifecycle("Setting build profile name: {}", profileName);

		Collection<Project> projectDependencies = getDependencyProjects(
			project);

		for (Project projectDependency : projectDependencies) {
			File buildProfilesFile = new File(
				projectDependency.getProjectDir(), IDE_PROFILES_FILENAME);

			try {
				_processIDEProfilesFile(profileName, buildProfilesFile);

				logger.lifecycle(
					"Added profile {} to {}.", profileName, buildProfilesFile);
			}
			catch (IOException ioe) {
				throw new GradleException(
					"Unable to create build profiles marker:" +
						buildProfilesFile.getAbsolutePath(),
					ioe);
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Build profile " + profileName + " created successfully.");
		sb.append(System.lineSeparator());
		sb.append("Please the following JVM arguments when importing ");
		sb.append("into your IDE gradle import wizard:");
		sb.append(System.lineSeparator());
		sb.append("-D");
		sb.append(LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME);
		sb.append("=" + profileName);

		if (profileName.contains(":")) {
			String[] path = profileName.split(":");

			String simpleProfileName = path[path.length - 1];

			sb.append(System.lineSeparator());
			sb.append("The value can also be specified as: ");
			sb.append(System.lineSeparator());
			sb.append("-D");
			sb.append(LiferaySettingsIDEPlugin.IDE_PROFILE_PROPERTY_NAME);
			sb.append("=" + simpleProfileName);
		}

		sb.append(System.lineSeparator());

		sb.append(
			"The value may be comma separated to specify multiple profiles.");

		logger.lifecycle(sb.toString());
	}

	private void _processIDEProfilesFile(
			String profileName, File buildProfilesFile)
		throws FileNotFoundException, IOException {

		ArrayList<String> profileNames = new ArrayList<>();

		boolean missingProfile = false;

		if (buildProfilesFile.exists()) {
			try (Scanner scanner = new Scanner(buildProfilesFile)) {
				while (scanner.hasNext()) {
					String line = scanner.next();

					if (!line.isEmpty()) {
						if (!profileNames.contains(line)) {
							profileNames.add(line);
						}
					}
				}
			}

			missingProfile = !profileNames.contains(profileName);

			if (missingProfile) {
				buildProfilesFile.delete();
			}
		}
		else {
			missingProfile = true;
		}

		if (missingProfile) {
			profileNames.add(profileName);
		}

		if (missingProfile) {
			boolean buildFileExists = buildProfilesFile.exists();

			try (FileWriter fileWriter = new FileWriter(
					buildProfilesFile, buildFileExists)) {

				if (buildFileExists) {
					fileWriter.write(System.lineSeparator());
				}

				for (String foundProfileName : profileNames) {
					fileWriter.write(foundProfileName + System.lineSeparator());
				}
			}
		}
	}

}