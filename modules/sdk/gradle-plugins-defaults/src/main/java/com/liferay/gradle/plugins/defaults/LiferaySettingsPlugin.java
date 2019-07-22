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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

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

	private static final String _DEFAULT_PROJECT_DEPTH = "0";

	//protected static Map<String, String> projectsMap = new HashMap<>();
	public static final String PROJECT_PATH_PREFIX_PROPERTY_NAME =
		"project.path.prefix";
	
	private static boolean newRun = false;

	@Override
	public void apply(Settings settings) {
		log("com.liferay.gradle.plugins.defaults.LiferaySettingsPlugin.apply called.");
		log("liferay.project.paths is " + LiferaySourceProject.getLiferayProjectPathsProperty());
		log("calculate.liferay.project.paths is " + LiferaySourceProject.getCalculateLiferayProjectPathsProperty());

		//log("projectsMap size is " + projectsMap.size());
		
		
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


			
			Collection<String> includedGradlePaths = new HashSet<>();
			if (LiferaySourceProject.getLiferayProjectPathsProperty() != null) {
				log("LiferaySourceProject.getLiferayProjectPathsProperty [" + LiferaySourceProject.getLiferayProjectPathsProperty() + "] is not null. " + System.lineSeparator() + "Getting Gradle Paths.");
				//if (!projectsMap.containsKey("liferay.project.paths=" + LiferaySourceProject.getLiferayProjectPathsProperty())) {
					includedGradlePaths.addAll(getIncludedGradlePaths(settings));
					//System.clearProperty("calculate.liferay.project.paths");
				settings.getStartParameter().setParallelProjectExecutionEnabled(true);
                settings.getStartParameter().setConfigureOnDemand(true);

    			//settings.getStartParameter().setConfigureOnDemand(false);
                StringBuilder sb = new StringBuilder();
                Iterator<String> it = includedGradlePaths.iterator();
                while (it.hasNext()) {
                	sb.append(it.next());
                	if (it.hasNext()) {
                		sb.append(",");
                	}
                }
                
               // projectsMap.put("liferay.project.paths=" + LiferaySourceProject.getLiferayProjectPathsProperty(), sb.toString());
				settings.getGradle().projectsEvaluated(
					new Action<Gradle>() {
						
							Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, LiferaySourceProject.getLiferayProjectPathFilterProperty());

							
		                	@Override
		                	public void execute(Gradle gradle) {
		                		settings.getStartParameter().setConfigureOnDemand(true);
		                		//System.clearProperty("liferay.project.paths");
				                settings.getGradle().getTaskGraph().beforeTask(new Action<Task>() {
				                	
				                	@Override
				                	public void execute(Task task) {
				                		if (task.getName().equals("outputRequiredProjectsTask")) {
				                			
				                			for (String projectString : additionalProjectPaths) {
				                				if (includedGradlePaths.contains(projectString)) {
				                					Project project = settings.getGradle().getRootProject().findProject(projectString);
				                					
				                					if (project != null) {
				                						try {
				                							OutputRequiredProjectsTask task2 = GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
				                							task2.setEnabled(false);
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
				});
				
				}
				
				else {
					String[] paths = projectsMap.get("liferay.project.paths=" + LiferaySourceProject.getLiferayProjectPathsProperty()).split(",");
					includedGradlePaths.addAll(Arrays.asList(paths));
				}*/
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

	private static boolean isParent(String pathToCheck, Collection<String> parentOfAnyPaths) {
		for (String path : parentOfAnyPaths) {
			if (path.startsWith(pathToCheck) && path.replace(":", "").length() >  pathToCheck.replace(":", "").length()) {
				return true;
			}
		}
		return false;
	}
	
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	private Collection<String> getIncludedGradlePaths(Settings settings) {
		log("getIncludedGradlePaths called.");
		if (includedProjects == null) {
			log("getIncludedGradlePaths includedProjects is null. Creating and filling a new one.");
			includedProjects = Collections.synchronizedSet(new HashSet<>());
			try {
				String projectPaths = LiferaySourceProject._fixUpString(LiferaySourceProject.getLiferayProjectPathsProperty(), settings.getRootDir());

				log("LiferaySourceProject._fixUpString called, producing " + projectPaths);
				String[] paths = projectPaths.split(",");
				Collection<String> pathsChecked = Collections.synchronizedSet(new HashSet<>());
				
				
				try {
					log("getIncludedGradlePaths calling getDependenciesOfPath for paths " + getArgsAsString(paths));
					
					Collection<String> results = getDependenciesOfPath(settings, pathsChecked, paths);

					log("getIncludedGradlePaths returned getDependenciesOfPath for paths " + getArgsAsString(paths));
					log("getIncludedGradlePaths returned getDependenciesOfPath " + results.size() + " paths.");
					//return results;
					//}, executor).thenAcceptAsync((e) -> 
					includedProjects.addAll(results);//, executor));
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
				long includedProjectSize = includedProjects.stream().filter(x -> !isParent(x, Arrays.asList(paths))).count();
				log("initial includedProjectSize is " + includedProjectSize);
				Collection<CompletableFuture<?>> futures = new HashSet<>();
				
				while (
						Integer.parseInt(System.getProperty("liferay.gradle.search.depth", _DEFAULT_PROJECT_DEPTH)) > searchDepth.get()
						 &&
						
						includedProjectSize > pathsChecked.size() && !Thread.currentThread().isInterrupted()) {
					searchDepth.getAndIncrement();
					log("looping code: " + System.lineSeparator() + "while (includedProjectSize > pathsChecked.size() || futures.stream().filter(x -> !x.isDone()).count() > 0 && !Thread.currentThread().isInterrupted()) {");

					log("acquiring lock");
						log("lock acquired");
						Collection<String> includedProjectsToIterate = Collections.synchronizedSet(new HashSet<>(includedProjects.stream().filter(x -> !isParent(x, Arrays.asList(paths))).collect(Collectors.toSet())));
						
						if (Thread.currentThread().isInterrupted()) {
							//executor.shutdownNow();
							throw new RuntimeException("Thread is interrupted");
						}
						Collection<String> stringsToCheck = Collections.synchronizedSet(new HashSet<>());
						for (String projectString : includedProjectsToIterate) {
							if (!pathsChecked.contains(projectString) && !isParent(projectString, Arrays.asList(paths))) {
								stringsToCheck.add(projectString);
							}
						}
						if (Thread.currentThread().isInterrupted()) {
							log("Thread is interrupted. Shutting down now.");
						
							//executor.shutdownNow();
							throw new RuntimeException("Thread is interrupted");
						}
						log("Adding includedProjectsToIterate[" + includedProjectsToIterate.size() + "] to includedProjects[" + includedProjects.size() + "]");
						includedProjects.addAll(includedProjectsToIterate);
						
						try {
							
							
							log("getIncludedGradlePaths 2 calling getDependenciesOfPath for paths " + stringsToCheck.toArray(new String[0]));
							
							futures.add(CompletableFuture.supplyAsync(() ->  {
								try {
									return getDependenciesOfPath(settings, pathsChecked, stringsToCheck.toArray(new String[0]));
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return null;
							}, executor).thenAcceptAsync((e) -> includedProjects.addAll(e), executor));

							log("getIncludedGradlePaths 2 returned getDependenciesOfPath for paths " +stringsToCheck.toArray(new String[0]));
							//log("getIncludedGradlePaths 2 returned getDependenciesOfPath " + results.size() + " paths.");
							//includedProjects.addAll(results);
						} catch (Throwable e1) {
							Thread.currentThread().interrupt();
							throw new RuntimeException(e1);
						}
							
						//log("Created Future with Identity Hash Code " + System.identityHashCode(f) + " and added to futures list.");

						
						if (Thread.currentThread().isInterrupted()) {
							log("Thread is interrupted. Shutting down now 2.");
							//executor.shutdownNow();
							throw new RuntimeException("Thread is interrupted");
						}
						log("calculating includedProjectSize");
						log("futures.size is " + futures.size());
						
						futures.stream().filter(x -> !x.isDone()).forEach(x -> {
							try {
								x.get();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ExecutionException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});

						includedProjectSize = includedProjects.stream().filter(x -> !isParent(x, Arrays.asList(paths))).count();
						log("includedProjectSize is " + includedProjectSize);
						log("looped code: " + System.lineSeparator() + "while (includedProjectSize > pathsChecked.size() || futures.stream().filter(x -> !x.isDone()).count() > 0 && !Thread.currentThread().isInterrupted()) {");
					
						
						try {
							if (Integer.parseInt(System.getProperty("liferay.gradle.search.depth", _DEFAULT_PROJECT_DEPTH)) > 
							searchDepth.get()) {
								break;
							}
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
						
				}
			}
			catch (Throwable e) {
				log(e.getMessage());
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				throw new RuntimeException(e);
			}
		}
		log("getIncludedGradlePaths returning includedProjects of size " + includedProjects.size());
		return includedProjects;
	}
	private static Path logPath = Paths.get(System.getProperty("user.home"), "ecl.log");
	protected static void log(String contents) {
		/*try {
        Path filePathObj = Paths.get(logPath.toUri());
		
		if (!newRun) {
			newRun = true;
			Files.deleteIfExists(filePathObj);
			log("Deleting and recreating log.");
		}
        boolean fileExists = Files.exists(filePathObj);
        if(!fileExists) {
        	Files.createFile(filePathObj);
        }*/
        contents = "[Thread: " + Thread.currentThread().getId() + "] " + dateFormat.format(new Date()) + " :: " + contents + System.lineSeparator();
        System.out.println(contents);
        /*Files.write(filePathObj, contents.getBytes(), StandardOpenOption.APPEND);
        
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}*/
	}
	private AtomicInteger searchDepth = new AtomicInteger() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6397404120036596948L;

		{
			try {
				set(Integer.parseInt(System.getProperty("liferay.gradle.search.current.depth", "0")));
				
			}
			catch (Throwable e) {
				set(0);
			}
		}
	};
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Collection<String> getDependenciesOfPath(Settings settings, Collection<String> pathsChecked, String... paths) throws InterruptedException {

		log("getDependenciesOfPath called with paths size " + paths.length);
		log("getDependenciesOfPath pathsChecked.size() = " + pathsChecked.size());
		log("getDependenciesOfPath paths = " + getArgsAsString(paths));
		
		if (Thread.currentThread().isInterrupted()) {
			throw new RuntimeException("Thread is interrupted");
		}
		Collection<String> includedProjects = new HashSet<>();

		for (String path : paths) {
			pathsChecked.add(path);
		}
		Collection<String> pathsToCheck =new HashSet<>();
		if (LiferaySourceProject.getCalculateLiferayProjectPathsProperty() != null) {
			
			Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, LiferaySourceProject.getCalculateLiferayProjectPathsProperty());
			
			// Skip anything that are parents of LiferaySourceProject.getCalculateLiferayProjectPathsProperty
			for (String calculatePath : additionalProjectPaths) {

				if (Thread.currentThread().isInterrupted()) {
					throw new RuntimeException("Thread is interrupted");
				}
				for (String checkPath : paths) {
					if (calculatePath.startsWith(checkPath) && calculatePath.length() > checkPath.length()) {
						pathsToCheck.add(checkPath);
						
						
						Project p = settings.getGradle().getRootProject().findProject(checkPath);
						if (p != null) {
						Map<String, Project> childProjects = p.getChildProjects();
						
						for (Project childProject : childProjects.values()) {
							if (!pathsChecked.contains(childProject.getPath())) {
								pathsChecked.add(childProject.getPath());
								pathsToCheck.add(childProject.getPath());
							}
						}
						}
					}
				}
			}
		}
		if (pathsToCheck.isEmpty()) { 
			pathsToCheck = Arrays.asList(paths);
		}
		Process p = null;
		log("getDependenciesOfPath pathsToCheck[" + pathsToCheck.size() + "] = " + getArgsAsString(pathsToCheck));
		if (Thread.currentThread().isInterrupted()) {
			throw new RuntimeException("Thread is interrupted");
		}
		try {
			
			StringBuilder calculatePath = new StringBuilder();
			Iterator<String> it = pathsToCheck.iterator();
			while (it.hasNext()) {
				String path = it.next();
				calculatePath.append(path);
				if (it.hasNext()) {
					calculatePath.append(",");
				}
				
			}
			Collection<String> args = new ArrayList<>(
					Arrays.asList(
							"sh", "gradlew", 
							"-Dcalculate.liferay.project.paths=" + calculatePath.toString(), 
							"-Dorg.gradle.configureondemand=true", 
							"-Dorg.gradle.parallel=true", 
							"-Dliferay.gradle.search.current.depth=" + searchDepth.get(),
							"-Dliferay.gradle.search.depth=" + System.getProperty("liferay.gradle.current.depth", _DEFAULT_PROJECT_DEPTH),
							"-p", "modules"));
			for (String pathToCheck : pathsToCheck) {
				log("getDependenciesOfPath pathToCheck = " + pathToCheck);
				args.add(pathToCheck + ":outputRequiredProjectsTask");// + (it.hasNext() ? " " : ""));
			}
			
			
			/*Iterator<String> it = Arrays.asList(paths).iterator();
			while (it.hasNext()) {
				String str = it.next();
				args.add(str + ":outputRequiredProjectsTask");// + (it.hasNext() ? " " : ""));
			}
			*/
			if (System.getProperty("liferay.gradle.debug") != null) {
				args.add("-Dorg.gradle.debug=true");
			}
			
			p = new ProcessBuilder(args.toArray(new String[0]))
					.directory(settings.getRootDir().getParentFile())
					.start();
			log("getDependenciesOfPath about to wait for process. Args: [" + getArgsAsString(args) + "]");
			p.waitFor();				
			log("getDependenciesOfPath finishied waiting for process. Args: [" + getArgsAsString(args) + "]");
			int exitValue = p.exitValue();
			log("getDependenciesOfPath exit value of process is: " + exitValue);
			String stderr= IOUtils.toString(p.getErrorStream());
			String stdout = IOUtils.toString(p.getInputStream());
			log("getDependenciesOfPath stdout is " + stdout);
			log("getDependenciesOfPath stderr is " + stderr);
			try (Scanner s = new Scanner(stdout)) {
				while (s.hasNextLine()) {
					String nextLine = s.nextLine();
					if (nextLine.startsWith(":")) {
						includedProjects.add(nextLine);
					}
				}
			}
			if (!stderr.trim().isEmpty()) {
				//throw new GradleException(stderr);
			}
		}
		catch (InterruptedException e) {
			log("getDependenciesOfPath Exception: " + e.getClass().getName() + System.lineSeparator() + e.getMessage());
			Thread.currentThread().interrupt();
			throw e;
		}
		catch (Throwable th) {
			log("getDependenciesOfPath Exception: " + th.getClass().getName() + System.lineSeparator() + th.getMessage());
			try {
				p.destroyForcibly();
			}
			catch (Throwable pth) {
				
			}
			throw new RuntimeException(th);
		}
		
		log("getDependenciesOfPath completed paths = " + getArgsAsString(paths));
		log("getDependenciesOfPath completed and returning " + includedProjects.size());
		return includedProjects;
	}

	private String getArgsAsString(Collection<String> args) {
		return String.join(" ", args);

	}
	private String getArgsAsString(String...args) {
		return String.join(",", Arrays.asList(args));

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

	protected void calculateProjectPathsSpecified(Settings settings, String sourceProperty) {
		settings.getGradle().beforeProject(new Action<Project>() {
			
			@Override
			public void execute(Project project) {
				// TODO Auto-generated method stub

				OutputRequiredProjectsTask task = GradleUtil.addTask(project, "outputRequiredProjectsTask", OutputRequiredProjectsTask.class, true);
				task.setEnabled(true);
			}
		});
		settings.getGradle().projectsEvaluated(new Action<Gradle>() {
		
				@Override
				public void execute(Gradle arg0) {
					Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, sourceProperty);
					Collection<String> projectsAndDependencies = new HashSet<>();
					Collection<String> projectsChecked = new HashSet<>();
					//projectsAndDependencies.addAll(ProjectDependencyGenerator.getProjectDependencies(settings, additionalProjectPaths.toArray(new String[0]), projectsChecked, true));
					for (String path : additionalProjectPaths) {
						Project project = arg0.getRootProject().findProject(path);
						if (project != null) {
							projectsAndDependencies.addAll(OutputRequiredProjectsTask.getProjectPaths(project, settings));
						}
					}
					
					
					
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

		log("_includeProject called");
		log("_includeProject Path projectDirPath: " + projectDirPath);
		log("_includeProject Path projectPathRootDirPath: " + projectPathRootDirPath);

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

		log("_includeprojects called with: ");
		log("Path projectPathRootDirPath = " + projectPathRootDirPath.toString());
		log("String projectPathPrefix = " + projectPathPrefix);
		log("Collection<String> includedGradlePaths = " + getArgsAsString(includedGradlePaths));
		
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

					//log("preVisitDirectory called with: " + dirPath.toString());
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
								if (Files.isSameFile(pathPath, dirPath) || pathPath.startsWith(dirPath)) {
									log("isSameFile: " + pathPath.toString() + " - " +  dirPath.toString() + " - " + gradlePath);
									_includeProject(
										settings, dirPath, projectPathRootDirPath,
										projectPathPrefix);
									break;
								}
								else
								{
									//log("notSameFile: " + pathPath.toString() + " - " +  dirPath.toString());
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
