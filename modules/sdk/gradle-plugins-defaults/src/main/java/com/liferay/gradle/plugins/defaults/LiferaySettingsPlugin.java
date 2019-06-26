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

import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.defaults.tasks.BaseIDEProfileTask;
import com.liferay.gradle.util.Validator;

import bsh.commands.dir;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

/**
 * @author Andrea Di Giorgi
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class LiferaySettingsPlugin implements Plugin<Settings> {

	public static final String PROJECT_PATH_PREFIX_PROPERTY_NAME =
		"project.path.prefix";

	@Override
	public void apply(Settings settings) {
		Gradle gradle = settings.getGradle();
		
		String projectPathsProperty = System.getProperty("liferay.project.paths");
		if (projectPathsProperty != null) {
			String[] projectPaths = projectPathsProperty.split(",");
			
			gradle.projectsEvaluated((a) -> {
				
				Collection<String> projectNamesToKeep = new HashSet<>();
				
				for (String projectPath : projectPaths) {
					
					File projectFile = new File(projectPath);
					
					if (projectFile.exists()) {
						//String sourceProjectGradlePath = settings.findProject(settings.getStartParameter().getProjectDir()).getPath();
						String sourceProjectGradlePath = settings.findProject(projectFile).getPath();							
						
						Collection<Project> projects = gradle.getRootProject().getAllprojects();
						
						Project sourceProject = projects.stream()
								.filter(p -> p.getPath().equals(sourceProjectGradlePath))
								.findAny()
								.orElse(null);
						
						projects = BaseIDEProfileTask.getDependencyProjects(sourceProject);
						
						Collection<Project> projectsWithParents = getProjectsWithParents(projects);
						
						projectNamesToKeep.addAll(projectsWithParents.stream().map(Project::getName).collect(Collectors.toSet()));
						
					}
					retainProjects(gradle.getRootProject().getChildProjects(), projectNamesToKeep);
				}

			});
			
		}
		
		File rootDir = settings.getRootDir();

		Path rootDirPath = rootDir.toPath();

		String projectPathPrefix = GradleUtil.getProperty(
			settings, PROJECT_PATH_PREFIX_PROPERTY_NAME, "");

		if (Validator.isNotNull(projectPathPrefix)) {
			if (projectPathPrefix.charAt(0) != ':') {
				projectPathPrefix = ":" + projectPathPrefix;
			}

			if (projectPathPrefix.charAt(projectPathPrefix.length() - 1) ==
					':') {

				projectPathPrefix = projectPathPrefix.substring(
					0, projectPathPrefix.length() - 1);
			}
		}

		try {
			Path projectPathRootDirPath = rootDirPath;

			if (isPortalRootDirPath(rootDirPath)) {
				projectPathRootDirPath = rootDirPath.resolve("modules");
			}

			_includeProjects(
				settings, projectPathRootDirPath, projectPathPrefix);

		}
		catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
	private Collection<Project> getProjectsWithParents(Collection<Project> projects) {
		Collection<Project> projectsWithParents = new HashSet<>(projects);
		for (Project project : projects) {
			projectsWithParents.add(project);
			while ((project = project.getParent()) != null) {
				if (!projectsWithParents.contains(project)) {
					projectsWithParents.add(project);
				}
			}
		}
		return projectsWithParents;
		
	}
	
	private void retainProjects(Map<String, Project> childProjects, Collection<String> projectNamesToKeep) {
		Collection<String> projectsToRemove = new HashSet<>();
		for (Entry<String, Project> childProjectEntry : childProjects.entrySet()) {
			Project childProject = childProjectEntry.getValue();
			
			Map<String, Project> subChildProjects = childProject.getChildProjects();
			
			if (subChildProjects != null && !subChildProjects.isEmpty()) {
				retainProjects(subChildProjects, projectNamesToKeep);
			}
			
			if (!projectNamesToKeep.contains(childProjectEntry.getKey())) {
				projectsToRemove.add(childProjectEntry.getKey());
			}
		}
		for (String projectNameToRemove : projectsToRemove) {
			childProjects.remove(projectNameToRemove);
		}
	}

	protected void includeProject(
		Settings settings, Path projectDirPath, Path projectPathRootDirPath,
		String projectPathPrefix) {

		String projectGradlePath = convertPathToGradlePath(projectDirPath, projectPathRootDirPath, projectPathPrefix);
		
		addGradlePathToModel(projectDirPath, projectGradlePath, settings);
	}
	
	protected void addGradlePathToModel(Path projectDirPath, String projectPath, Settings settings) {
		settings.include(new String[] {projectPath});

		ProjectDescriptor projectDescriptor = settings.findProject(projectPath);

		projectDescriptor.setProjectDir(projectDirPath.toFile());
	}
	
	protected Path convertGradlePathToPath(Path projectPathRootDirPath, String gradlePath, String projectPathPrefix) {
		if (Validator.isNotNull(projectPathPrefix)) {
			gradlePath = gradlePath.replace(projectPathPrefix + ":", "");
			
		}
		
		gradlePath = gradlePath.replace(':', File.separatorChar);
		
		if (gradlePath.charAt(0) == File.separatorChar) {
			gradlePath = gradlePath.substring(1);
		}
		
		return projectPathRootDirPath.resolve(gradlePath);
	}
	
	protected String convertPathToGradlePath(Path projectDirPath, Path projectPathRootDirPath,
			String projectPathPrefix) {
		Path relativePath = projectPathRootDirPath.relativize(projectDirPath);

		String projectPath = relativePath.toString();

		projectPath =
			projectPathPrefix + ":" +
				projectPath.replace(File.separatorChar, ':');
		
		return projectPath;
	}

	private Set<Path> _getDirPaths(String key, Path rootDirPath) {
		String dirNamesString = System.getProperty(key);

		if (Validator.isNull(dirNamesString)) {
			return Collections.emptySet();
		}

		Set<Path> dirPaths = new HashSet<>();

		for (String dirName : dirNamesString.split(",")) {
			dirPaths.add(rootDirPath.resolve(dirName));
		}

		return dirPaths;
	}

	private <T extends Enum<T>> Set<T> _getFlags(
		String prefix, Class<T> clazz) {

		Set<T> flags = EnumSet.allOf(clazz);

		Iterator<T> iterator = flags.iterator();

		while (iterator.hasNext()) {
			T flag = iterator.next();

			String flagName = flag.toString();

			flagName = flagName.replace('_', '.');
			flagName = flagName.toLowerCase();

			if (!Boolean.getBoolean(prefix + flagName)) {
				iterator.remove();
			}
		}

		return flags;
	}

	private ProjectDirType _getProjectDirType(Path dirPath) {
		if (Files.exists(dirPath.resolve("build.xml"))) {
			return ProjectDirType.ANT_PLUGIN;
		}

		if (Files.exists(dirPath.resolve("bnd.bnd"))) {
			return ProjectDirType.MODULE;
		}

		Path applicationPropertiesPath = dirPath.resolve(
			"src/main/resources/application.properties");

		if (Files.exists(applicationPropertiesPath)) {
			return ProjectDirType.SPRING_BOOT;
		}

		if (Files.exists(dirPath.resolve("gulpfile.js"))) {
			return ProjectDirType.THEME;
		}

		return ProjectDirType.UNKNOWN;
	}

	private void _includeProjects(
			final Settings settings, final Path projectPathRootDirPath,
			final String projectPathPrefix)
		throws IOException {

		final Set<String> buildProfileFileNames =
			GradlePluginsDefaultsUtil.getBuildProfileFileNames(
				System.getProperty("build.profile"),
				GradleUtil.getProperty(
					settings, "liferay.releng.public", true));
		final Set<Path> excludedDirPaths = _getDirPaths(
			"build.exclude.dirs", projectPathRootDirPath);
		final Set<Path> includedDirPaths = _getDirPaths(
			"build.include.dirs", projectPathRootDirPath);
		final Set<ProjectDirType> excludedProjectDirTypes = _getFlags(
			"build.exclude.", ProjectDirType.class);

			Files.walkFileTree(
			projectPathRootDirPath,
			new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(
					Path dirPath, BasicFileAttributes basicFileAttributes) {

					if (dirPath.equals(projectPathRootDirPath)) {
						return FileVisitResult.CONTINUE;
					}

					if (excludedDirPaths.contains(dirPath)) {
						return FileVisitResult.SKIP_SUBTREE;
					}

					String dirName = String.valueOf(dirPath.getFileName());

					if (dirName.equals("build") ||
						dirName.equals("node_modules") ||
						dirName.equals("node_modules_cache")) {

						return FileVisitResult.SKIP_SUBTREE;
					}

					ProjectDirType projectDirType = _getProjectDirType(dirPath);

					if (projectDirType == ProjectDirType.UNKNOWN) {
						return FileVisitResult.CONTINUE;
					}

					if (excludedProjectDirTypes.contains(projectDirType)) {
						return FileVisitResult.SKIP_SUBTREE;
					}

					if (!includedDirPaths.isEmpty() &&
						!_startsWith(dirPath, includedDirPaths)) {

						return FileVisitResult.SKIP_SUBTREE;
					}

					if (buildProfileFileNames != null) {
						boolean found = false;

						for (String fileName : buildProfileFileNames) {
							if (Files.exists(dirPath.resolve(fileName))) {
								found = true;

								break;
							}
						}

						if (!found) {
							return FileVisitResult.SKIP_SUBTREE;
						}
					}
					
					includeProject(
						settings, dirPath, projectPathRootDirPath,
						projectPathPrefix);

					return FileVisitResult.SKIP_SUBTREE;
				}

			});
	}
	
	protected void addProjectToModel(Path dirPath, Runnable runnable) {
		runnable.run();
	}

	protected boolean isPortalRootDirPath(Path dirPath) {
		if (!Files.exists(dirPath.resolve("modules"))) {
			return false;
		}

		if (!Files.exists(dirPath.resolve("portal-impl"))) {
			return false;
		}

		return true;
	}

	private boolean _startsWith(Path path, Iterable<Path> parentPaths) {
		for (Path parentPath : parentPaths) {
			if (path.startsWith(parentPath)) {
				return true;
			}
		}

		return false;
	}

	private static enum ProjectDirType {

		ANT_PLUGIN, MODULE, SPRING_BOOT, THEME, UNKNOWN

	}

}