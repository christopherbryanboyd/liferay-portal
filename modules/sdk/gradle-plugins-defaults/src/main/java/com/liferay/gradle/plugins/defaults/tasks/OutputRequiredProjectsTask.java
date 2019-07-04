package com.liferay.gradle.plugins.defaults.tasks;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskAction;

import com.liferay.gradle.plugins.defaults.LiferayDependencyGenerator;
import com.liferay.gradle.plugins.defaults.LiferaySettingsPlugin;
import com.liferay.gradle.plugins.defaults.internal.LiferaySourceProject;

public class OutputRequiredProjectsTask extends DefaultTask {

	@TaskAction
	public void OutputRequiredProjects() throws Exception {

		String projectPathsProperty = LiferaySourceProject.getLiferayProjectPathFilterProperty();

		if (projectPathsProperty != null) {
			
		
		printNamesToKeep(LiferayDependencyGenerator.SETTINGS, getProject());
		}
		// Otherwise noop.
	}
	private static void printNamesToKeep(final Settings settings, Project project) {
		Collection<String> projectsToKeep = OutputRequiredProjectsTask.getProjectPaths(project,  settings);
		for (String s : projectsToKeep) {
			System.out.println(s);
		}
	}

	public static Collection<String> getProjectPaths(Project project, Settings settings) {

		Collection<String> projectPathsToKeep = new HashSet<>();
		Collection<String> additionalProjectPaths = LiferaySourceProject._getSourceProjectDescriptors(settings, LiferaySourceProject.getLiferayProjectPathFilterProperty());
		if (additionalProjectPaths.contains(project.getPath())) {
		
			Project projectToSearch = project;
			Collection<Project> projectsWithDependencies = LiferaySourceProject.getDependencyProjects(projectToSearch);
			
			Collection<Project> projectsWithParents = LiferaySourceProject._getProjectsWithParents(projectsWithDependencies);
			
			//Collection<ProjectDescriptor> projectDescriptors = projectsWithParents.stream().map(p -> settings.findProject(p.getPath())).collect(Collectors.toSet());
			
			//Iterator<ProjectDescriptor> it = projectDescriptors.iterator();
	
			Iterator<Project> it = projectsWithParents.iterator();
			
			while (it.hasNext()) {
				/*File projectDir = it.next(
						).getProjectDir();
	
				if (projectDir.exists()) {
					_populateProjectNamesToKeep(settings, projectPathsToKeep, projectDir);
				}*/
				Project p = it.next();
				projectPathsToKeep.add(p.getPath());
				
				// Doesn't work as these projects haven't been evaluated.
				//_populateProjectNamesToKeep(settings, projectPathsToKeep, p);
			}
			
		}
		return projectPathsToKeep;
	}
	
	private static void _populateProjectNamesToKeep(Settings settings, Collection<String> projectPathsToKeep, Project project) {
		//Collection<Project> projects = settings.getGradle().getRootProject().getAllprojects();
		Project sourceProject = settings.getGradle().getRootProject().findProject(project.getPath());

		if (sourceProject != null) {

			Collection<Project> projects = LiferaySourceProject.getDependencyProjects(sourceProject);

			projectPathsToKeep.addAll(projects.stream().map(x -> x.getPath()).collect(Collectors.toSet()));
			/*Collection<Project> projectsWithParents =
					LiferaySourceProject._getProjectsWithParents(projects);

			projectPathsToKeep.addAll(
					projectsWithParents.stream(
							).map(
									Project::getPath
									).collect(
											Collectors.toSet()
											));*/
		}
	}

	private static Project getProjectByGradlePath(Collection<Project> projects, String sourceProjectGradlePath) {
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

}
