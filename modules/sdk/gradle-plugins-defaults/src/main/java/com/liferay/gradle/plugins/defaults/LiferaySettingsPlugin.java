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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.util.Validator;

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

		File rootDir = settings.getRootDir();

		Path rootDirPath = rootDir.toPath();

		String projectPathPrefix = _getProjectPathPrefix(settings);

		try {
			Path projectPathRootDirPath = rootDirPath;

			if (isPortalRootDirPath(rootDirPath)) {
				projectPathRootDirPath = rootDirPath.resolve("modules");
			}
			gradle.projectsEvaluated(new Action<Gradle>() {

				
				@Override
				public void execute(Gradle gradle) {
					String projectPathsProperty = System.getProperty(
							"liferay.project.paths");
					
					if (projectPathsProperty != null && !"false".equals(projectPathsProperty.toLowerCase())) {
						_afterEvaluate( settings);
					}

				}
				
			});

			_includeProjects(
					settings, projectPathRootDirPath, projectPathPrefix);
		}
		catch (Throwable ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	private String _getProjectPathPrefix(Settings settings) {
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
		return projectPathPrefix;
	}

	protected void addGradlePathToModel(
			Path projectDirPath, String projectPath, Settings settings) {
				settings.include(new String[] {projectPath});
				ProjectDescriptor projectDescriptor = settings.findProject(projectPath);
				projectDescriptor.setProjectDir(projectDirPath.toFile());
	}

	protected String convertGradlePathToPathString(
			Path projectPathRootDirPath, String gradlePath,
			String projectPathPrefix) {

		if (Validator.isNotNull(projectPathPrefix)) {
			gradlePath = gradlePath.replace(projectPathPrefix + ":", "");
		}

		gradlePath = gradlePath.replace(':', File.separatorChar);

		if (gradlePath.charAt(0) == File.separatorChar) {
			gradlePath = gradlePath.substring(1);
		}

		String returnValue = projectPathRootDirPath.resolve(gradlePath).toAbsolutePath().normalize().toString();
		
		return _fixUpString(returnValue, projectPathRootDirPath.toFile());
	}

	protected String convertPathToGradlePath(
			Path projectDirPath, Path projectPathRootDirPath,
			String projectPathPrefix) {

		Path relativePath = projectPathRootDirPath.relativize(projectDirPath);

		String projectPath = relativePath.toString();

		projectPath =
				projectPathPrefix + ":" +
						projectPath.replace(File.separatorChar, ':');

		return projectPath;
	}

	protected void includeProject(
			Settings settings, Path projectDirPath, Path projectPathRootDirPath,
			String projectPathPrefix) {

		String projectGradlePath = convertPathToGradlePath(
				projectDirPath, projectPathRootDirPath, projectPathPrefix);

		synchronized(settings) {
			addGradlePathToModel(projectDirPath, projectGradlePath, settings);
		}
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

	private static void _collectDependencyProjects(
			Collection<Project> dependencyProjects, Configuration configuration) {

		DependencySet dependencySet = configuration.getDependencies();

		DomainObjectSet<ProjectDependency> projectDependencies =
				dependencySet.withType(ProjectDependency.class);

		if (!projectDependencies.isEmpty()) {
			ResolvedConfiguration resolvedConfiguration =
					configuration.getResolvedConfiguration();

			resolvedConfiguration.getFirstLevelModuleDependencies();

			projectDependencies.stream(
					).map(
							ProjectDependency::getDependencyProject
							).map(
									LiferaySettingsPlugin::_getDependencyProjects
									).flatMap(
											Collection::stream
											).forEach(
													dependencyProjects::add
													);
		}
	}

	private static Collection<Project> _getDependencyProjects(Project project) {
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
								c -> _collectDependencyProjects(projects, c)
								);

		return projects;
	}

	private void _afterEvaluate(Settings settings) {
		Gradle gradle = settings.getGradle();
		
		Collection<String> projectNamesToKeep = new HashSet<>();

		Collection<ProjectDescriptor> getSourceProjects =
				_getSourceProjectDescriptors(settings);

		Collection<Project> projects = gradle.getRootProject().getAllprojects();

		Iterator<ProjectDescriptor> it = getSourceProjects.iterator();

		while (it.hasNext()) {
			File projectDir = it.next(
					).getProjectDir();

			if (projectDir.exists()) {
				_populateProjectNamesToKeep(settings, projectNamesToKeep, projects, projectDir);
			}
		}

		_retainProjects(
				gradle.getRootProject(
						).getChildProjects(),
				projectNamesToKeep);
	}

	private Collection<Project> _populateProjectNamesToKeep(Settings settings, Collection<String> projectNamesToKeep,
			Collection<Project> projects, File projectDir) {
		String sourceProjectGradlePath = settings.findProject(
				projectDir
				).getPath();

		Project sourceProject = getProjectByGradlePath(projects, sourceProjectGradlePath);

		if (sourceProject != null) {

			projects = _getDependencyProjects(sourceProject);

			Collection<Project> projectsWithParents =
					_getProjectsWithParents(projects);

			projectNamesToKeep.addAll(
					projectsWithParents.stream(
							).map(
									Project::getName
									).collect(
											Collectors.toSet()
											));
		}
		return projects;
	}

	private Project getProjectByGradlePath(Collection<Project> projects, String sourceProjectGradlePath) {
		Project sourceProject = projects.stream(
				).filter(
						p -> p.getPath(
								).equals(
										sourceProjectGradlePath
										)
						).findAny(
								).orElse(
										null
										);
		return sourceProject;
	}

	private Collection<Project> _findProjectsEndingWith(
			Settings settings, String gradlePath) {

		Collection<Project> foundProjects = new ArrayList<>();

		for (Project project :
			settings.getGradle().getRootProject().getAllprojects()) {

			if (project.getPath().endsWith(gradlePath)) {
				foundProjects.add(project);
			}
		}

		return foundProjects;
	}

	private Project _findProjectWithName(Settings settings, String pathString) {
		Project foundProject = null;

		for (Project project :
			settings.getGradle().getRootProject().getAllprojects()) {

			if (project.getName().equals(pathString)) {
				foundProject = project;

				break;
			}
		}

		return foundProject;
	}

	private String _fixUpString(
			String possibleProjectDirString,
			File pathToCheckForDuplicateFolderNames) {

		possibleProjectDirString = possibleProjectDirString.replace(
				"\\", File.separator);
		possibleProjectDirString = possibleProjectDirString.replace(
				"/", File.separator);
		
		if (possibleProjectDirString.startsWith(File.separator) && !new File(possibleProjectDirString).exists()) {
			possibleProjectDirString = possibleProjectDirString.substring(1);
		}
		if (possibleProjectDirString.endsWith(File.separator)) {

			possibleProjectDirString = possibleProjectDirString.substring(
					0, possibleProjectDirString.length() - 1);
		}

		Collection<String> stringsToFix = new ArrayList<>();

		for (File possibleDuplicateFile :
			pathToCheckForDuplicateFolderNames.listFiles()) {

			if (possibleDuplicateFile.isDirectory()) {
				stringsToFix.add(possibleDuplicateFile.getName());
			}
		}

		for (String stringToFix : stringsToFix) {
			possibleProjectDirString = possibleProjectDirString.replace(
					stringToFix + File.separator + stringToFix, stringToFix);
		}

		possibleProjectDirString = possibleProjectDirString.replace(
				"modules" + File.separator + "modules", "modules");

		possibleProjectDirString = possibleProjectDirString.replace(
				"liferay-portal" + File.separator + "liferay-portal",
				"liferay-portal");

		possibleProjectDirString = possibleProjectDirString.replace(
				"liferay-portal" + File.separator + "modules" + File.separator + "liferay-portal" + File.separator + "modules",
				"liferay-portal" + File.separator + "modules");

		return possibleProjectDirString;
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

	private Collection<Project> _getProjectsWithParents(
			Collection<Project> projects) {

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

	private Collection<ProjectDescriptor> _getSourceProjectDescriptors(
			Settings settings) {

		Collection<ProjectDescriptor> sourceProjects = new ArrayList<>();

		try {
			String projectPathsProperty = System.getProperty(
					"liferay.project.paths");

			if (projectPathsProperty != null) {
				if (!projectPathsProperty.isEmpty()) {
					if (!projectPathsProperty.toLowerCase().equals("false")) {

						projectPathsProperty = projectPathsProperty.replace("\"", "");
						_collectProjectsByPath(settings, sourceProjects, projectPathsProperty);
					}
					else {

						// The user specified the value as false.
						// Do nothing (maybe issue a warning?)

					}
				}
				else {

					// The property is specified but empty, so just resolve against the start parameter dir
					// (Whatever directory was selected in Eclipse)

					ProjectDescriptor pd = settings.findProject(
							settings.getStartParameter(
									).getCurrentDir());

					if (pd != null) {
						sourceProjects.add(
								settings.findProject(pd.getProjectDir()));
					}
					else {
						throw new GradleException(
								"Path not found in gradle model");
					}
				}
			}
		}
		catch (Throwable th) {
			throw new RuntimeException(th);
		}

		return sourceProjects;
	}

	private void _collectProjectsByPath(Settings settings, Collection<ProjectDescriptor> sourceProjects,
			String projectPathsProperty) {
		// Change the path separator to a comma (instead of semicolon),
		// just in case someone is doing the correct Windows system behavior.
		// (We can't do the unix variant (colon) because we support gradle paths

		projectPathsProperty = projectPathsProperty.replace(
				';', ',');
		String[] projectPathsProperties =
				projectPathsProperty.split(",");

		for (String pathString : projectPathsProperties) {
			try {
				pathString = _fixUpString(pathString, settings.getRootDir());
				boolean containsSlash = pathString.indexOf(File.separator) > -1;

				boolean containsColon = pathString.contains(
						":");

				if (containsColon) {
					if (containsSlash) {
						throw new IllegalArgumentException("Mixing slashes and Colons is not supported");
					}
					else {
						int projectSize = sourceProjects.size();
						
						_collectProjectsWithColon(settings, sourceProjects, pathString);
						
						if (sourceProjects.size() == projectSize) {
							// We didn't get any new projects.
							// Try to convert the gradle path to a file path and give it our best shot.
							pathString = convertGradlePathToPathString(
									settings.getRootDir().toPath(),
									pathString,
									"");
							
							_collectProjectsWithSlash(settings, sourceProjects, pathString);
						}
					}

				}
				else if (containsSlash) {

					_collectProjectsWithSlash(settings, sourceProjects, pathString);
				}
				else {

					// Try to resolve the project by name against the gradle model

					Project foundProject = _findProjectWithName(
							settings, pathString);

					if (foundProject == null) {
						throw new GradleException(
								"Path not found in gradle model");
					}
					else {

						sourceProjects.add(
								settings.findProject(
										foundProject.getProjectDir()));
					}
				}
			}
			catch (Throwable th) {

				// TODO: Potentially issue a warning.
				// Also probably just skip this project and move on.

			}
		}
	}
	private void _collectProjectsWithSlash(Settings settings, Collection<ProjectDescriptor> sourceProjects,
			String pathString) {

		// If the path contains a slash, try to find it in the gradle
		// model. If it can't be found in the gradle model as a project,
		// resolve the path itself against the root project path and
		// ensure it exists. If it exists, search the gradle model.
		// Otherwise, throw an Exception.

		File projectDir = new File(pathString);

		if (!projectDir.exists()) {

			// Try to resolve against root directory.

			String possibleProjectDirString = new File(
					settings.getRootDir(), pathString).getAbsolutePath();

			possibleProjectDirString = _fixUpString(possibleProjectDirString, settings.getRootDir());

			File possibleProjectDir = new File(possibleProjectDirString);

			// If the user specified both the eclipse directory and the property, try to relativize against
			// the property first.
			if (!possibleProjectDir.exists()) {
				
				possibleProjectDirString = new File(pathString).toURI().relativize(new File(settings.getStartParameter().getCurrentDir().getAbsolutePath().toString()).toURI()).getPath();
				
				if (!possibleProjectDir.exists() || !pathString.endsWith(File.separator + possibleProjectDir.getName())) {
					Path pathPath = Paths.get(pathString);
					
					Path possibleProjectDirPath = Paths.get(possibleProjectDirString);
					
					for (Path path : pathPath) {
						if (!possibleProjectDirPath.endsWith(path.getFileName())) {
							possibleProjectDirPath = possibleProjectDirPath.resolve(path);
						}
					}
					possibleProjectDirString = possibleProjectDirPath.toString();
				}
				possibleProjectDir = new File(possibleProjectDirString);
				
			}
			
			if (!possibleProjectDir.exists()) {

				// Try to resolve against specified eclipse directory.

				possibleProjectDirString = new File(
						settings.getStartParameter(
								).getCurrentDir(),
						pathString).getAbsolutePath();

				possibleProjectDir = new File(possibleProjectDirString);
			}

			if (possibleProjectDir.exists()) {
				if (!possibleProjectDir.
						isDirectory()) {

					throw new GradleException(
							"Path must be a directory");
				}

				ProjectDescriptor
				projectDescriptor =
				settings.findProject(
						possibleProjectDir);

				if (projectDescriptor != null) {
					sourceProjects.add(
							projectDescriptor);
				}
				else {
					throw new GradleException(
							"Path not found in gradle model");
				}
			}
			else {

				try {
					Path pathPath = Paths.get(pathString);


					Collection<Path> gradleProjectPaths = new ArrayList<>();
					Collection<Path> foundProjectPaths = new ArrayList<>();

					// Try to find the project by end path.
					if (!pathPath.isAbsolute()) {
						if (pathPath.getNameCount() > 1) {
							gradleProjectPaths = settings.getGradle()
									.getRootProject()
									.getAllprojects()
									.stream()
									.map(Project::getProjectDir)
									.map(File::toPath)
									.collect(Collectors.toSet());

							foundProjectPaths = gradleProjectPaths.stream()
									.filter(p -> p.endsWith(pathPath))
									.collect(Collectors.toSet());
						}

						// Try to find the project by name.
						if (foundProjectPaths.isEmpty()) {
							foundProjectPaths = gradleProjectPaths.stream()
									.filter(p -> p.getFileName().equals(pathPath.getFileName()))
									.collect(Collectors.toSet());
						}
						for (Path path : foundProjectPaths) {

							ProjectDescriptor
							projectDescriptor =
							settings.findProject(
									path.toFile());

							if (projectDescriptor != null) {
								sourceProjects.add(projectDescriptor);
							}
							else {
								throw new GradleException(
										"Path not found in gradle model");
							}
						}

					}
				}
				catch (Throwable e) {
					throw new GradleException(
							"Issue with gradle path");
				}

			}
		}
		else if (!projectDir.isDirectory()) {
			throw new GradleException(
					"Path must be a directory");
		}
		else {

			// The path exists and is a directory, now find the Gradle project.

			ProjectDescriptor project =
					settings.findProject(projectDir);

			if (project != null) {
				sourceProjects.add(project);
			}
			else {
				throw new GradleException(
						"Path not found in gradle model");
			}
		}
	}

	private void _collectProjectsWithColon(Settings settings, Collection<ProjectDescriptor> sourceProjects,
			String pathString) {
		String prefixProperty = System.getProperty(
				PROJECT_PATH_PREFIX_PROPERTY_NAME, "");

		if (pathString.endsWith(":") &&
				!pathString.startsWith(":")) {

			// Fix the gradle path for the user

			pathString =
					":" + pathString.replace(":", "");
		}

		String fullPathString = pathString.replace(
				prefixProperty, "");

		int colonCount;

		if (!prefixProperty.isEmpty()) {
			fullPathString =
					prefixProperty + ":" + pathString;
			fullPathString = fullPathString.replace(
					"::", ":");
			fullPathString = fullPathString.replace(
					"::", ":");
			colonCount =
					fullPathString.length() -
					fullPathString.replace(
							":", ""
							).length();
		}
		else {
			colonCount =
					pathString.length() -
					pathString.replace(
							":", ""
							).length();
		}

		if (colonCount == 1) {

			// Probably the shorthand version, try to resolve
			// against the full path. Otherwise, issue a warning and
			// try to resolve against the project name without a colon.
			// Throw an Exception if this fails.

			ProjectDescriptor projectDescriptor =
					settings.findProject(
							fullPathString);

			if (projectDescriptor == null) {
				if (!Objects.equals(
						pathString,
						fullPathString)) {

					// Try without the prefix property.

					projectDescriptor =
							settings.findProject(
									pathString);
				}

				if (projectDescriptor == null) {

					// Just loop through the model and find the projects we want.

					Collection<Project>
					desiredProjects =
					_findProjectsEndingWith(
							settings,
							fullPathString);

					if (desiredProjects.isEmpty()) {
						desiredProjects =
								_findProjectsEndingWith(
										settings,
										pathString);
					}

					if (desiredProjects.isEmpty()) {
						throw new GradleException(
								"Path not found in gradle model");
					}

					for (Project desiredProject :
						desiredProjects) {

						if (desiredProject !=
								null) {

							projectDescriptor =
									settings.
									findProject(
											desiredProject.
											getProjectDir());

							if (projectDescriptor !=
									null) {

								sourceProjects.add(
										projectDescriptor);
							}

							// Should we throw an exception here in an else case?

						}
					}
				}
				else {
					sourceProjects.add(
							projectDescriptor);
				}
			}
			else {
				sourceProjects.add(
						projectDescriptor);
			}
		}
		else {

			// Multiple colons.
			// Probably the full gradle path, try to resolve it. Otherwise,
			// try to resolve against the end of the path.
			// without a colon.
			// Throw an Exception if this fails.

			ProjectDescriptor projectDescriptor =
					settings.findProject(
							fullPathString);

			if (projectDescriptor == null) {
				projectDescriptor =
						settings.findProject(
								pathString);

				if (projectDescriptor == null) {
					Collection<Project>
					desiredProjects =
					_findProjectsEndingWith(
							settings,
							fullPathString);

					if (desiredProjects.isEmpty()) {
						desiredProjects =
								_findProjectsEndingWith(
										settings,
										pathString);
					}

					if (desiredProjects.isEmpty()) {
						throw new GradleException(
								"Path not found in gradle model");
					}

					for (Project desiredProject :
						desiredProjects) {

						if (desiredProject !=
								null) {

							ProjectDescriptor
							foundProjectDescriptor =
							settings.
							findProject(
									desiredProject.
									getProjectDir());

							if (foundProjectDescriptor !=
									null) {

								sourceProjects.add(
										foundProjectDescriptor);
							}
						}
					}
				}
				else {
					sourceProjects.add(
							projectDescriptor);
				}
			}
			else {
				sourceProjects.add(
						projectDescriptor);
			}
		}
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

	private void _retainProjects(
			Map<String, Project> childProjects,
			Collection<String> projectNamesToKeep) {

		Collection<String> projectsToRemove = new HashSet<>();

		for (Map.Entry<String, Project> childProjectEntry :
			childProjects.entrySet()) {

			Project childProject = childProjectEntry.getValue();

			Map<String, Project> subChildProjects =
					childProject.getChildProjects();

			if ((subChildProjects != null) && !subChildProjects.isEmpty()) {
				_retainProjects(subChildProjects, projectNamesToKeep);
			}

			if (!projectNamesToKeep.contains(childProjectEntry.getKey())) {
				projectsToRemove.add(childProjectEntry.getKey());
			}
		}

		for (String projectNameToRemove : projectsToRemove) {
			childProjects.remove(projectNameToRemove);
		}
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