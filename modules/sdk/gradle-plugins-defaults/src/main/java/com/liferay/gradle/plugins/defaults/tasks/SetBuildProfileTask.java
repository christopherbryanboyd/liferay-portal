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

import com.liferay.gradle.plugins.defaults.LiferaySettingsPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.BuildProfileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
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
public class SetBuildProfileTask extends DefaultTask {

	public static final String BUILD_PROFILE_NAME_PROPERTY_NAME =
		"build.profile.name";

	public static final Path CURRENT_WORKING_PATH = Paths.get(
		System.getProperty("user.dir"));

	public static final String LFRBUILD_PROFILES_FILENAME =
		".lfrbuild-profiles";

	public static final String SET_BUILD_PROFILE_TASK_NAME = "setBuildProfile";

	@TaskAction
	public void setBuildProfile() throws Exception {
		Project currentProject = getProject();

		Project sourceProject = BuildProfileUtil.getSourceProject(
			currentProject);

		String profileName = System.getProperty(
			BUILD_PROFILE_NAME_PROPERTY_NAME);

		if ((profileName == null) || profileName.isEmpty()) {
			profileName = sourceProject.getPath();

			System.setProperty(BUILD_PROFILE_NAME_PROPERTY_NAME, profileName);
		}
		else if (!Objects.equals(sourceProject.getPath(), profileName) &&
				 (profileName.contains(",") || profileName.contains(":"))) {

			StringBuilder sb = new StringBuilder();

			sb.append(BUILD_PROFILE_NAME_PROPERTY_NAME);
			sb.append("'" + profileName + "' is invalid. ");
			sb.append(System.lineSeparator());
			sb.append("Profile name cannot contain ',' or ':'.");
			sb.append(System.lineSeparator());

			throw new IllegalArgumentException(sb.toString());
		}

		Logger logger = getLogger();

		logger.lifecycle("Setting build profile name: {}", profileName);

		Collection<Project> projectDependencies =
			BuildProfileUtil.getProjectDependencies(currentProject);

		for (Project projectDependency : projectDependencies) {
			File lfrBuildFile = new File(
				projectDependency.getProjectDir(), LFRBUILD_PROFILES_FILENAME);

			try {
				_processBuildFile(profileName, logger, lfrBuildFile);
			}
			catch (IOException ioe) {
				throw new GradleException(
					"Unable to create build profile custom marker:" +
						lfrBuildFile.getAbsolutePath(),
					ioe);
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Build profile " + profileName + " created successfully.");
		sb.append(System.lineSeparator());
		sb.append("To import or use this profile, ");
		sb.append("please use the following argument: ");
		sb.append(System.lineSeparator());
		sb.append("-D");
		sb.append(LiferaySettingsPlugin.BUILD_PROFILE_PROPERTY_NAME);
		sb.append("=" + profileName);

		if (profileName.contains(":")) {
			String[] path = profileName.split(":");

			String simpleProfileName = path[path.length - 1];

			sb.append("The value can also be specified as: ");
			sb.append(System.lineSeparator());
			sb.append("-D");
			sb.append(LiferaySettingsPlugin.BUILD_PROFILE_PROPERTY_NAME);
			sb.append("=" + simpleProfileName);
		}

		sb.append(System.lineSeparator());

		sb.append(
			"The value may be comma separated to specify multiple profiles.");

		String message = sb.toString();

		logger.lifecycle(message);
	}

	private static void _processBuildFile(
			String profileName, Logger logger, File lfrBuildFile)
		throws FileNotFoundException, IOException {

		ArrayList<String> list = new ArrayList<>();

		boolean missingProfile = false;

		if (lfrBuildFile.exists()) {
			logger.lifecycle("Reading {}", lfrBuildFile);

			try (Scanner s = new Scanner(lfrBuildFile)) {
				while (s.hasNext()) {
					String foundProfileName = s.next();

					if (!foundProfileName.isEmpty()) {
						if (!list.contains(foundProfileName)) {
							list.add(foundProfileName);
						}
					}
				}
			}

			missingProfile = !list.contains(profileName);

			if (missingProfile) {
				lfrBuildFile.delete();
			}
		}
		else {
			missingProfile = true;
		}

		if (missingProfile) {
			list.add(profileName);
		}

		if (missingProfile) {
			boolean buildFileExists = lfrBuildFile.exists();

			try (FileWriter fw = new FileWriter(
					lfrBuildFile, buildFileExists)) {

				if (buildFileExists) {
					fw.write(System.lineSeparator());
				}

				for (String foundProfileName : list) {
					fw.write(foundProfileName + System.lineSeparator());
				}
			}
		}
	}

}