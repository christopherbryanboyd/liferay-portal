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
import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.defaults.tasks.OutputRequiredProjectsTask;
import com.liferay.gradle.util.Validator;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

/**
 * @author Andrea Di Giorgi
 */
public class LiferaySettingsPlugin implements Plugin<Settings> {

	private static Map<String, String> projectsMap = new HashMap<>();
	public static final String PROJECT_PATH_PREFIX_PROPERTY_NAME =
		"project.path.prefix";

	@Override
	public void apply(Settings settings) {
		File rootDir = settings.getRootDir();

		Path rootDirPath = rootDir.toPath();

		String projectPathPrefix = getProjectPathPrefix(settings);

		try {
			Path projectPathRootDirPath = rootDirPath;

			if (_isPortalRootDirPath(rootDirPath)) {
				projectPathRootDirPath = rootDirPath.resolve("modules");
			}
			
			if (System.getProperty("eclipse.commands") != null &&
				(
					(LiferaySourceProject
						.getLiferayProjectPathsProperty() != null &&
						(LiferaySourceProject
							.getLiferayProjectPathsProperty().isEmpty() ||
						LiferaySourceProject
							.getLiferayProjectPathsProperty().toLowerCase().equals("true")))
					||
					LiferaySourceProject
					.getLiferayProjectPathsProperty() == null)
				) {

				if (settings.getStartParameter().getProjectDir().toPath().startsWith(rootDirPath) && 
					!Files.isSameFile(settings.getStartParameter().getProjectDir().toPath(), rootDirPath)) {
					
					if (Files.isDirectory(settings.getStartParameter().getProjectDir().toPath()) &&
							Files.exists(settings.getStartParameter().getProjectDir().toPath().resolve("build.gradle"))) {
						
						System.setProperty(
								"liferay.project.paths", LiferayDependencyGenerator.convertPathToGradlePath(settings.getStartParameter().getProjectDir().toPath(), rootDirPath, ""));
					}
					
				}
				
			}

			if (LiferaySourceProject.getCalculateLiferayProjectPathsProperty() != null) {
			
                settings.getStartParameter().setConfigureOnDemand(true);
                settings.getStartParameter().setParallelProjectExecutionEnabled(true);
				calculateProjectPathsSpecified(settings, LiferaySourceProject.getCalculateLiferayProjectPathsProperty());
				
                System.clearProperty("calculate.liferay.project.paths");
			}
			
			Collection<String> includedGradlePaths = new HashSet<>();
			if (LiferaySourceProject.getLiferayProjectPathsProperty() != null) {
				
				if (!projectsMap.containsKey(LiferaySourceProject.getLiferayProjectPathsProperty())) {
					includedGradlePaths.addAll(getIncludedGradlePaths(settings));
				
				settings.getStartParameter().setParallelProjectExecutionEnabled(true);
                settings.getStartParameter().setConfigureOnDemand(true);
                
                StringBuilder sb = new StringBuilder();
                Iterator<String> it = includedGradlePaths.iterator();
                while (it.hasNext()) {
                	sb.append(it.next());
                	if (it.hasNext()) {
                		sb.append(",");
                	}
                }
                projectsMap.put(LiferaySourceProject.getLiferayProjectPathsProperty(), sb.toString());
				settings.getGradle().projectsEvaluated(
					new Action<Gradle>() {
						
							Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, LiferaySourceProject.getLiferayProjectPathFilterProperty());

							
		                	@Override
		                	public void execute(Gradle gradle) {
		                		settings.getStartParameter().setConfigureOnDemand(true);
		                		System.clearProperty("liferay.project.paths");
				                settings.getGradle().getTaskGraph().beforeTask(new Action<Task>() {
				                	
				                	@Override
				                	public void execute(Task task) {
				                		if (task.getName().equals("outputRequiredProjectsTask")) {
				                			
				                			for (String projectString : additionalProjectPaths) {
				                				if (includedGradlePaths.contains(projectString)) {
				                					Project project = settings.getGradle().getRootProject().findProject(projectString);
				                					
				                					if (project != null) {
				                						try {
				                							GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
				                						}
				                						catch (Exception e) {
				                							// Ignore
				                						}
				                					}
				                				}
				                			}
				                			
				                		}
				                	}
				                });
		                	}
						});
				/*settings.getGradle().buildFinished(new Action<BuildResult>() {
					
					@Override
					public void execute(BuildResult result) {
						System.clearProperty("liferay.project.paths");
					}
				});*/
				}
				else {
					String[] paths = projectsMap.get(LiferaySourceProject.getLiferayProjectPathsProperty()).split(",");
					includedGradlePaths.addAll(Arrays.asList(paths));
				}
			}

			_includeProjects(
				settings, projectPathRootDirPath, projectPathPrefix, includedGradlePaths);
		}
		catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
		catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}
	private Collection<String> includedProjects = null;

	private Collection<String> getIncludedGradlePaths(Settings settings) {
		if (includedProjects == null) {
			includedProjects = new HashSet<>();
			try {
			Collection<String> args = new ArrayList<>(
					Arrays.asList(
						"sh", "gradlew", 
						"-Dcalculate.liferay.project.paths=" + LiferaySourceProject.getLiferayProjectPathsProperty(), 
						"-Dorg.gradle.configureondemand=true", 
						"-Dorg.gradle.parallel=true", 
						"-p", "modules",
						":outputRequiredProjectsTask"));
			
				if (System.getProperty("liferay.gradle.debug") != null) {
					args.add("-Dorg.gradle.debug=true");
				}
				
					Process p = new ProcessBuilder(args.toArray(new String[0]))
							.directory(settings.getRootDir().getParentFile())
							.start();
					p.waitFor();
					String stderr= IOUtils.toString(p.getErrorStream());
					String stdout = IOUtils.toString(p.getInputStream());
					System.out.println("From gradlew -Dcalculate.liferay.project.paths=" + LiferaySourceProject.getLiferayProjectPathsProperty());
					System.out.println(stdout);
					try (Scanner s = new Scanner(stdout)) {
						while (s.hasNextLine()) {
							String nextLine = s.nextLine();
							if (nextLine.startsWith(":")) {
								includedProjects.add(nextLine);
							}
						}
					}
					if (!stderr.trim().isEmpty()) {
						throw new GradleException(stderr);
					}
			}
			catch (Throwable th) {
				throw new RuntimeException(th);
			}

		}
		return includedProjects;
	}

	private String getProjectPathPrefix(Settings settings) {
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

	private void calculateProjectPathsSpecified(Settings settings, String sourceProperty) {
		settings.getGradle().beforeProject(new Action<Project>() {

			@Override
			public void execute(Project project) {
				// TODO Auto-generated method stub

				System.out.println("Adding task outputRequiredProjectsTask to project " + project.getPath());
				GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
			}
		});
		settings.getGradle().projectsLoaded(new Action<Gradle>() {
		
				@Override
				public void execute(Gradle arg0) {
					Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, sourceProperty);
					Collection<String> projectsAndDependencies = new HashSet<>();
					Collection<String> projectsChecked = new HashSet<>();
					projectsAndDependencies.addAll(ProjectDependencyGenerator.getProjectDependencies(settings, additionalProjectPaths.toArray(new String[0]), projectsChecked, true));
					
					
					for (String s : projectsAndDependencies) {
						System.out.println(s);
					}
				}


				
		});
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

	protected void _includeProject(
		Settings settings, Path projectDirPath, Path projectPathRootDirPath,
		String projectPathPrefix) {

		Path relativePath = projectPathRootDirPath.relativize(projectDirPath);

		String projectPath = relativePath.toString();

		projectPath =
			projectPathPrefix + ":" +
				projectPath.replace(File.separatorChar, ':');

		settings.include(new String[] {projectPath});

		ProjectDescriptor projectDescriptor = settings.findProject(projectPath);

		projectDescriptor.setProjectDir(projectDirPath.toFile());
	}

	private void _includeProjects(
			final Settings settings, final Path projectPathRootDirPath,
			final String projectPathPrefix, Collection<String> includedGradlePaths)
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
					if (includedGradlePaths != null && !includedGradlePaths.isEmpty()) {
					try {
						for (String gradlePath : includedGradlePaths) {
							String stringPath = LiferaySourceProject.convertGradlePathToPathString(projectPathRootDirPath, gradlePath, projectPathPrefix);
	
							Path pathPath = Paths.get(stringPath);
							
							try {
							if (Files.isSameFile(pathPath, dirPath)) {
								_includeProject(
									settings, dirPath, projectPathRootDirPath,
									projectPathPrefix);
								break;
							}
							}
							catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						}
					}
					catch (Throwable th) {
						_includeProject(
								settings, dirPath, projectPathRootDirPath,
								projectPathPrefix);
					}
					}
					else {

						_includeProject(
								settings, dirPath, projectPathRootDirPath,
								projectPathPrefix);
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

			});
	}

	private boolean _isPortalRootDirPath(Path dirPath) {
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
