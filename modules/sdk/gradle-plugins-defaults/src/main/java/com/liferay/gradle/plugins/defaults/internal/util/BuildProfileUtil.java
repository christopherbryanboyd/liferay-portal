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

package com.liferay.gradle.plugins.defaults.internal.util;

import com.liferay.gradle.plugins.defaults.tasks.SetBuildProfileTask;

import java.io.File;

import java.nio.file.Path;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class BuildProfileUtil {

	public static final String SOURCE_PROJECT_PATH_KEY = "source.project.path";

	public static Collection<Project> getProjectDependencies(Project project) {
		Set<Project> projects = new LinkedHashSet<>();

		projects.add(project);

		Set<ConfigurationContainer> configurationContainers = new HashSet<>();

		Map<String, Project> childProjects = project.getChildProjects();

		for (Project childProject : childProjects.values()) {
			projects.add(childProject);

			configurationContainers.add(childProject.getConfigurations());
		}

		ConfigurationContainer configurationContainer =
			project.getConfigurations();

		configurationContainers.add(configurationContainer);

		configurationContainers.stream(
		).flatMap(
			Set::stream
		).forEach(
			c -> _collectProjectDependencies(projects, c)
		);

		return projects;
	}

	public static Project getSourceProject(Project currentProject) {
		String sourceProjectName = System.getProperty(SOURCE_PROJECT_PATH_KEY);

		Project foundProject = null;

		if (sourceProjectName == null) {
			Project rootProject = currentProject.getRootProject();

			Collection<Project> allProjects = rootProject.getAllprojects();

			for (Project subproject : allProjects) {
				File subprojectFile = subproject.getProjectDir();

				Path subprojectPath = subprojectFile.toPath();

				if (subprojectPath.equals(
						SetBuildProfileTask.CURRENT_WORKING_PATH)) {

					foundProject = subproject;

					break;
				}
			}

			if (foundProject != null) {
				System.setProperty(
					SOURCE_PROJECT_PATH_KEY, foundProject.getPath());
			}
		}
		else {
			foundProject = currentProject.findProject(sourceProjectName);
		}

		return foundProject;
	}

	private static void _collectProjectDependencies(
		Collection<Project> projects, Configuration c) {

		DependencySet dependencySet = c.getDependencies();

		DomainObjectSet<ProjectDependency> projectDependencies =
			dependencySet.withType(ProjectDependency.class);

		if (!projectDependencies.isEmpty()) {
			ResolvedConfiguration rc = c.getResolvedConfiguration();

			rc.getFirstLevelModuleDependencies();

			projectDependencies.stream(
			).map(
				ProjectDependency::getDependencyProject
			).map(
				BuildProfileUtil::getProjectDependencies
			).flatMap(
				Collection::stream
			).forEach(
				projects::add
			);
		}
	}

}