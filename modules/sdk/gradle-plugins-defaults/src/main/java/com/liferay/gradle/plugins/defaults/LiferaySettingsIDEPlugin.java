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

package com.liferay.gradle.plugins.defaults;

import com.liferay.gradle.plugins.defaults.internal.IDEProfilesPlugin;
import com.liferay.gradle.plugins.defaults.tasks.BaseIDEProfileTask;
import com.liferay.gradle.plugins.defaults.tasks.CleanIDEProfileTask;
import com.liferay.gradle.plugins.defaults.tasks.SetIDEProfileTask;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

/**
 * @author Gregory Amerson
 */
public class LiferaySettingsIDEPlugin extends LiferaySettingsPlugin {

	public static final String IDE_PROFILE_PROPERTY_NAME = "ide.profile";

	@Override
	@SuppressWarnings("serial")
	public void apply(Settings settings) {
		String ideProfile = System.getProperty(IDE_PROFILE_PROPERTY_NAME, "");

		if (!ideProfile.isEmpty()) {
			_ideProfiles = ideProfile.split(",");
		}

		Gradle gradle = settings.getGradle();

		StartParameter startParameter = gradle.getStartParameter();

		List<String> taskNames = startParameter.getTaskNames();

		if (taskNames.contains(
				CleanIDEProfileTask.CLEAN_IDE_PROFILE_TASK_NAME) ||
			taskNames.contains(SetIDEProfileTask.SET_IDE_PROFILE_TASK_NAME)) {

			_profileTaskRequested = true;

			if (ideProfile.isEmpty()) {
				ideProfile = _getDefaultProfileName(
					gradle.getRootProject(), startParameter.getCurrentDir());

				System.setProperty(IDE_PROFILE_PROPERTY_NAME, ideProfile);
			}
		}

		super.apply(settings);

		if (_profileTaskRequested) {
			gradle.beforeProject(
				new Closure<Void>(settings) {

					@SuppressWarnings("unused")
					public void doCall(Project project) {
						IDEProfilesPlugin.INSTANCE.apply(project);
					}

				});
		}
	}

	@Override
	protected void includeProject(
		Settings settings, Path projectDirPath, Path projectPathRootDirPath,
		String projectPathPrefix) {

		if (_profileTaskRequested || (_ideProfiles.length == 0)) {
			super.includeProject(
				settings, projectDirPath, projectPathRootDirPath,
				projectPathPrefix);

			return;
		}

		Path ideProfilesPath = projectDirPath.resolve(
			BaseIDEProfileTask.IDE_PROFILES_FILENAME);

		if (_checkIDEProfiles(_ideProfiles, ideProfilesPath)) {
			super.includeProject(
				settings, projectDirPath, projectPathRootDirPath,
				projectPathPrefix);
		}
	}

	private boolean _checkIDEProfiles(
		final String[] profileNames, Path profilePath) {

		if (!Files.exists(profilePath)) {
			return false;
		}

		boolean found = false;

		try (Scanner scanner = new Scanner(profilePath)) {
			while (scanner.hasNext()) {
				String line = scanner.next();

				for (String profileName : profileNames) {
					if (profileName.contains(":") &&
						line.endsWith(profileName)) {

						found = true;
					}
					else if (line.equals(profileName)) {
						found = true;
					}
					else {
						boolean lineContainsColon = line.contains(":");

						if (lineContainsColon) {
							String[] lineSplit = line.split(":");
							found =
								lineSplit[lineSplit.length - 1].equals(
									profileName) ||
								(lineSplit[lineSplit.length - 1] + ":").equals(
									":" + profileName);
						}
					}

					if (found) {
						break;
					}
				}
			}
		}
		catch (IOException ioe) {
		}

		return found;
	}

	private String _getDefaultProfileName(
		Project rootProject, File currentDir) {

		return rootProject.getAllprojects(
		).stream(
		).filter(
			p -> Objects.equals(p.getProjectDir(), currentDir)
		).findFirst(
		).map(
			Project::getPath
		).get();
	}

	private String[] _ideProfiles = new String[0];
	private boolean _profileTaskRequested;

}