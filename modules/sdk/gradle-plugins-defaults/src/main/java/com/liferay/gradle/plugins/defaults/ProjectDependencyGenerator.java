package com.liferay.gradle.plugins.defaults;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;

import com.liferay.gradle.plugins.defaults.internal.LiferaySourceProject;

public class ProjectDependencyGenerator {
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	public static Collection<String> getProjectDependencies(Settings settings, String[] projectPaths, Collection<String> projectsChecked, boolean includeChildren) {
		ProjectDependencyGenerator generator = new ProjectDependencyGenerator();
		
		Collection<String> projectDependencies = generator._getProjectDependencies(settings, projectPaths, projectsChecked, includeChildren);
		
		generator.executor.shutdown();
		
		return projectDependencies;
	}
	private Collection<String> _getProjectDependencies(Settings settings, String[] projectPaths, Collection<String> projectsChecked, boolean includeChildren) {
		Collection<String> paths = new HashSet<>();
		if (projectPaths.length > 0) {
			Collection<ProjectDescriptor> pds = new HashSet<>();
			for (String projectPath : projectPaths) {
				
				ProjectDescriptor pd = settings.findProject(projectPath);
				
				if (pd != null) {
					pds.add(pd);
				}
			}
			try {
				for (String projectPath : projectPaths) {
					System.out.println("Project path found: " + projectPath);
					projectsChecked.add(projectPath);
				}
				CompletableFuture<?> cf1 = CompletableFuture.supplyAsync(() -> 
				{
					return getProjectDependenciesIndividual(settings, projectPaths, includeChildren);
				}, 
				executor).thenAcceptAsync(p -> 
				{
					synchronized(paths) {
						paths.addAll(p);
					}
				}, executor);
				
				Collection<String> additionalPaths = new HashSet<>();
				Collection<ProjectDescriptor> parentProjects = new HashSet<>();
				for (ProjectDescriptor pd : pds) {
					parentProjects.addAll(LiferaySourceProject.getProjectDescriptorParents(pd));
				}
				Collection<String> validPaths = new HashSet<>();
				
				cf1.get();
				for (String path : paths) {
					if (!projectsChecked.contains(path)) {
						ProjectDescriptor subPd = settings.findProject(path);
						if (!parentProjects.contains(subPd)) {
							Collection<ProjectDescriptor> subParentProjects = LiferaySourceProject.getProjectDescriptorParents(subPd);
							if (!subParentProjects.contains(subPd)) {
								validPaths.add(path);
							}
						}
					}
				}
				
				if (!validPaths.isEmpty()) {
					Collection<String> subProjectDependencies = _getProjectDependencies(settings, validPaths.toArray(new String[0]), projectsChecked, false);
					additionalPaths.addAll(subProjectDependencies);
					paths.addAll(additionalPaths);
				}
	
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return paths;
	}

	private Collection<String> getProjectDependenciesIndividual(Settings settings, String[] projectPaths, boolean includeChildren) {
		
		Collection<String> paths = new HashSet<>();
		try {
			StringBuilder sb = new StringBuilder();
			for (int x = 0; x < projectPaths.length; x++) {
				String s = projectPaths[x];
				sb.append(s);
				if (x != (projectPaths.length - 1)) {
					sb.append(',');
				}
			}
			//
			Collection<String> args = new ArrayList<>(Arrays.asList(new File(settings.getRootDir().getParentFile(),"gradlew").getAbsolutePath(), "-Dorg.gradle.parallel=true", "-Dorg.gradle.configureondemand=true", "-q"));
			for (String projectPath : projectPaths) {
				args.add(projectPath + ":outputRequiredProjectsTask");
			}
			args.add("-Dliferay.project.path.filter=" + sb.toString());
			if (includeChildren) {
				args.add("-Dliferay.build.include.children");
			}

			//args.add("-Dorg.gradle.debug=true");
			Process p = new ProcessBuilder(args.toArray(new String[0])
					)
					.directory(settings.getRootDir())
					.start();
			p.waitFor();
			String stderr= IOUtils.toString(p.getErrorStream());
			String stdout= IOUtils.toString(p.getInputStream());
			if (!stderr.trim().isEmpty()) {
				throw new GradleException(stderr);
			}
			try (Scanner s = new Scanner(stdout)) {
				while (s.hasNextLine()) {
					paths.add(s.nextLine());
				}
			}
		}
		catch (Throwable th) {
			throw new RuntimeException(th);
		}
		return paths;
	}
	

}
