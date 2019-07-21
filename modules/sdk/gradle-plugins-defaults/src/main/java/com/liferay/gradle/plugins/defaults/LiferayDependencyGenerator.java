
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

import com.liferay.gradle.plugins.defaults.internal.LiferaySourceProject;
import com.liferay.gradle.plugins.defaults.internal.WhipDefaultsPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.defaults.tasks.OutputRequiredProjectsTask;
import com.liferay.gradle.plugins.internal.EclipseDefaultsPlugin;
import com.liferay.gradle.util.Validator;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.util.ConfigureUtil;

/**
 * @author Andrea Di Giorgi
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class LiferayDependencyGenerator extends LiferaySettingsPlugin implements Action<Project> {
	private Collection<String> projectsToKeep = new HashSet<>();

	public static final Plugin<Settings> INSTANCE = new LiferayDependencyGenerator();

	public static Settings SETTINGS = null;
	public static final String PROJECT_PATH_PREFIX_PROPERTY_NAME =
		"project.path.prefix";
	
	@Override
	public void apply(final Settings settings) {
		
		SETTINGS = settings;
		String projectPathsProperty = LiferaySourceProject.getCalculateLiferayProjectPathsProperty();
		if (projectPathsProperty == null || "false".equals(projectPathsProperty.toLowerCase())) {
			super.apply(settings);
			
			return;
		}			
		else {
			
			settings.getStartParameter().setConfigureOnDemand(false);
			settings.getStartParameter().setParallelProjectExecutionEnabled(true);
			calculateProjectPathsSpecified(settings, LiferaySourceProject.getCalculateLiferayProjectPathsProperty());
			
			//System.clearProperty("calculate.liferay.project.paths");
			settings.getGradle().beforeProject(new Action<Project>() {

				@Override
				public void execute(Project project) {
					// TODO Auto-generated method stub
					OutputRequiredProjectsTask task = GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
					task.setEnabled(false);
				}
			});
			settings.getGradle().projectsEvaluated(new Action<Gradle>() {

				@Override
				public void execute(Gradle gradle) {
					// TODO Auto-generated method stub
			
					Collection<String> projects = LiferaySourceProject._getSourceProjectDescriptors(settings, LiferaySourceProject.getLiferayProjectPathsProperty());
				
					for (String str : projects) {
						System.out.println(str);
					}
				}
			});
			settings.getStartParameter().setRefreshDependencies(true);
			settings.getStartParameter().setSearchUpwards(true);
			settings.getStartParameter().setConfigureOnDemand(true);
			settings.getStartParameter().setRecompileScripts(true);
			settings.getStartParameter().setRerunTasks(true);
			settings.getStartParameter().setParallelProjectExecutionEnabled(false);
			
			
			Gradle gradle = settings.getGradle();
	
			File rootDir = settings.getRootDir();
	
			Path rootDirPath = rootDir.toPath();
	
			String projectPathPrefix = GradleUtil.getProperty(
				settings, PROJECT_PATH_PREFIX_PROPERTY_NAME, "");
	
			try {
				Path projectPathRootDirPath = rootDirPath;
	
				if (isPortalRootDirPath(rootDirPath)) {
					projectPathRootDirPath = rootDirPath.resolve("modules");
				}
	
				
				gradle.beforeProject(this);
				_includeProjects(
					settings, projectPathRootDirPath, projectPathPrefix);
				
			}
			catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}
	

	protected void addGradlePathToModel(
		Path projectDirPath, String projectPath, Settings settings) {

		settings.include(new String[] {projectPath});

		ProjectDescriptor projectDescriptor = settings.findProject(projectPath);

		projectDescriptor.setProjectDir(projectDirPath.toFile());
	}

	protected void addProjectToModel(Path dirPath, Runnable runnable) {
		runnable.run();
	}

	protected Path convertGradlePathToPath(
		Path projectPathRootDirPath, String gradlePath,
		String projectPathPrefix) {

		if (Validator.isNotNull(projectPathPrefix)) {
			gradlePath = gradlePath.replace(projectPathPrefix + ":", "");
		}

		gradlePath = gradlePath.replace(':', File.separatorChar);

		if (gradlePath.charAt(0) == File.separatorChar) {
			gradlePath = gradlePath.substring(1);
		}

		return projectPathRootDirPath.resolve(gradlePath);
	}

	public static String convertPathToGradlePath(
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

		addGradlePathToModel(projectDirPath, projectGradlePath, settings);
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
				LiferayDependencyGenerator::_getDependencyProjects
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

					if (Thread.currentThread().isInterrupted()) {
						throw new RuntimeException("Thread was interrupted.");
					}
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
		Collection<String> projectPathsToKeep) {

		Collection<String> projectsToRemove = new HashSet<>();

		for (Map.Entry<String, Project> childProjectEntry :
				childProjects.entrySet()) {

			Project childProject = childProjectEntry.getValue();

			Map<String, Project> subChildProjects =
				childProject.getChildProjects();

			if ((subChildProjects != null) && !subChildProjects.isEmpty()) {
				_retainProjects(subChildProjects, projectPathsToKeep);
			}

			if (!projectPathsToKeep.contains(childProjectEntry.getKey())) {
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

	@Override
	public void execute(Project project) {
		/*for (ProjectDescriptor pd : _getSourceProjectDescriptors(SETTINGS)) {
			if (pd.getPath().equals(project.getPath())) {
				// Noop task
				GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
				//printNamesToKeep(SETTINGS, project);
			}
		}*/
	}

}