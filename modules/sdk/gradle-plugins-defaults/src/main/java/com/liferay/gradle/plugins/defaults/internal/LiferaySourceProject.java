package com.liferay.gradle.plugins.defaults.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

import com.liferay.gradle.plugins.defaults.LiferayDependencyGenerator;
import com.liferay.gradle.plugins.defaults.LiferaySettingsPlugin;
import com.liferay.gradle.plugins.internal.LangBuilderDefaultsPlugin;
import com.liferay.gradle.util.Validator;

public class LiferaySourceProject implements Plugin<Project> {

	public static Collection<Project> getDependencyProjects(Settings settings, Project project, Collection<Project> alreadyChecked) {
		Set<Project> projects = new LinkedHashSet<>();
		if (!alreadyChecked.contains(project)) {

		projects.add(project);

		Set<ConfigurationContainer> configurationContainers = new HashSet<>();
		
			
		ConfigurationContainer configurationContainer =
			project.getConfigurations();

		configurationContainers.add(configurationContainer);
		
		
		Collection<Configuration> configuration = configurationContainers.stream(
				).flatMap(
						Set::stream
						).collect(Collectors.toSet());

		for (Configuration c : configuration) {
			_collectDependencyProjects(c, settings, projects, alreadyChecked);
		}
		for (Project p : projects) {
			if (!alreadyChecked.contains(p)) {
				
				Map<String, Project> childProjects = p.getChildProjects();
				
				
				for (Project childProject : childProjects.values()) {
				Set<ConfigurationContainer> configurationContainers2 = new HashSet<>();
				
				ConfigurationContainer configurationContainer2 =
						childProject.getConfigurations();

					configurationContainers2.add(configurationContainer2);
					
					
					Collection<Configuration> subConfiguration = configurationContainers2.stream(
							).flatMap(
									Set::stream
									).collect(Collectors.toSet());

					for (Configuration c : subConfiguration) {
						_collectDependencyProjects(c, settings, projects, alreadyChecked);
					}
				}
			}
		}
		}
		return projects;
	}

	public static Collection<Project> projectsToAccumulate(Settings settings, Project project) {
		
		Collection<Project> projects = new HashSet<>();
				
		Map<String, Project> childProjects = project.getChildProjects();
		
		for (Project childProject : childProjects.values()) {
			if (!projects.contains(childProject)) {
				projects.addAll(projectsToAccumulate(settings, childProject));
				projects.add(childProject);
				
				//projectsToAccumulate(settings, childProject, projects, configurationContainers, alreadyChecked);
			
				//configurationContainers.add(childProject.getConfigurations());
			}
		}
		return projects;
	}

	protected static final String SOURCE_PROJECT_PATH_KEY =
		"source.project.path";

	public static void _collectDependencyProjects(Configuration configuration, Settings settings,
		Collection<Project> dependencyProjects, Collection<Project> alreadyChecked) {

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
				project -> getDependencyProjects(settings, project, alreadyChecked)
			).flatMap(
				Collection::stream
			).forEach(
				dependencyProjects::add
			);
			
			

			/*Set<Project> projects = new HashSet<Project>();
			for (Project dependencyProject : dependencyProjects) {
				projectsToAccumulate(settings, dependencyProject, projects, configurationContainers, alreadyChecked);
				
			}*/
			

		}
	}
	
	public static final LiferaySourceProject INSTANCE =
		new LiferaySourceProject();
	
	public static Collection<String> projectPaths = new ArrayList<String>();
	
	@Override
	public void apply(Project arg0) {

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
	
	public static String convertGradlePathToPathString(
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
	
	public static Collection<Project> _getProjectsWithParents(
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
	public static Collection<ProjectDescriptor> getProjectDescriptorParents(
			ProjectDescriptor pd) {

		Collection<ProjectDescriptor> projectsWithParents = new HashSet<>();

		ProjectDescriptor pdParent = pd.getParent();
		projectsWithParents.add(pdParent);
		while ((pdParent = pdParent.getParent()) != null) {
			if (!projectsWithParents.contains(pdParent)) {
				projectsWithParents.add(pdParent);
			}
		}
		return projectsWithParents;
	}
	/*
	public static Collection<Project> _getDependencyProjects(Project project) {
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
									LiferaySourceProject::_getDependencyProjects
									).flatMap(
											Collection::stream
											).forEach(
													dependencyProjects::add
													);
		}
	}*/
	
	
	
	public static Collection<String> _collectProjectsByPath(Settings settings,
			String projectPathsProperty) {
		// Change the path separator to a comma (instead of semicolon),
		// just in case someone is doing the correct Windows system behavior.
		// (We can't do the unix variant (colon) because we support gradle paths
		
		Collection<String> projects = new HashSet<>();

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
						
						//_collectProjectsWithColon(settings, sourceProjects, pathString);
						
						//if (sourceProjects.size() == projectSize) {
						// We didn't get any new projects.
						// Try to convert the gradle path to a file path and give it our best shot.
						pathString = convertGradlePathToPathString(
								settings.getRootDir().toPath(),
								pathString,
								"");
						
						projects.addAll(_collectProjectsWithSlash(settings, pathString));
					}
					

				}
				else if (containsSlash) {

					projects.addAll(_collectProjectsWithSlash(settings, pathString));
				}
				else {

					// Try to resolve the project by name against the gradle model

					ProjectDescriptor foundProject = _findProjectWithName(
							settings, pathString);

					if (foundProject == null) {
						throw new GradleException(
								"Path not found in gradle model");
					}
					else {

						projects.add(
								settings.findProject(
										foundProject.getProjectDir()).getPath());
					}
				}
			}
			catch (Throwable th) {

				// TODO: Potentially issue a warning.
				// Also probably just skip this project and move on.

			}
		}
		return projects;
	}

	public static String _fixUpString(
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

	public static Collection<String> _collectProjectsWithSlash(Settings settings,
			String pathString) {

		Collection<String> dirs = new HashSet<>();
		// If the path contains a slash, try to find it in the gradle
		// model. If it can't be found in the gradle model as a project,
		// resolve the path itself against the root project path and
		// ensure it exists. If it exists, search the gradle model.
		// Otherwise, throw an Exception.

		File projectDir = new File(pathString).getAbsoluteFile();

		if (!projectDir.exists()) {

			// Try to resolve against root directory.

			String possibleProjectDirString = new File(
					settings.getRootDir(), pathString).getAbsolutePath();

			possibleProjectDirString = _fixUpString(possibleProjectDirString, settings.getRootDir());

			File possibleProjectDir = new File(possibleProjectDirString).getAbsoluteFile();

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
				possibleProjectDir = new File(possibleProjectDirString).getAbsoluteFile();
				
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

				
				ProjectDescriptor projectDescriptor = getProjectDescriptorFromPath(settings, possibleProjectDir);

				if (projectDescriptor != null) {
					
					dirs.add(projectDescriptor.getPath());
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
							gradleProjectPaths = getAllSettingsProjects(settings)
									.stream()
									.map(ProjectDescriptor::getProjectDir)
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
								dirs.add(projectDescriptor.getPath());
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
			
			ProjectDescriptor project = getProjectDescriptorFromPath(settings, projectDir);

			if (project != null) {
				dirs.add(project.getPath());
			}
			else {
				throw new GradleException(
						"Path not found in gradle model");
			}
		}
		return dirs;
	}

	public static ProjectDescriptor getProjectDescriptorFromPath(Settings settings, File projectDir) {
		//ProjectDescriptor project = settings.findProject(projectDir);
		ProjectDescriptor project = null;
		
		Collection<ProjectDescriptor> settingsDescriptors = getAllSettingsProjects(settings);
		
		for (ProjectDescriptor pd : settingsDescriptors) {
			try {
				if (Files.isSameFile(pd.getProjectDir().toPath(), projectDir.toPath())) {
					project = pd;
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (project == null) {
			String gradlePath = LiferayDependencyGenerator.convertPathToGradlePath(projectDir.toPath(), settings.getRootDir().toPath(), "");
			project = settings.findProject(gradlePath);
		}
		//if (project == null) {
			//String gradlePath = LiferaySettingsPlugin.convertPathToGradlePath(settings.getRootDir().toPath(), projectDir.toPath(), "");
			/*String gradlePath = settings.getGradle()
					.getRootProject()
					.getAllprojects()
					.stream()
					.filter(x -> {
						try {
							return Files.isSameFile(x.getProjectDir().toPath(), projectDir.toPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					})
					.map(p -> p.getPath())
					.findAny()
					.orElse(null);*/
			
			//project = settings.findProject(gradlePath);
		//}
		return project;
	}
	
	public static Collection<ProjectDescriptor> getAllSettingsProjects(Settings settings) {
		Collection<ProjectDescriptor> settingsProjects = new HashSet<>();
		
		settingsProjects.add(settings.getRootProject());
		
		return _getAllSettingsSubProjects(
		settings.getRootProject().getChildren());
		
		
	}

	private static Collection<ProjectDescriptor> _getAllSettingsSubProjects(Collection<ProjectDescriptor> settingsProjects) {
		Collection<ProjectDescriptor> settingsProjectsNew = new HashSet<>(settingsProjects);
		
		for (ProjectDescriptor pd : settingsProjects) {
			
			settingsProjectsNew.addAll(_getAllSettingsSubProjects(pd.getChildren()));
		}
		
		return settingsProjectsNew;
		
		
	}
	public static ProjectDescriptor _findProjectWithName(Settings settings, String pathString) {

		ProjectDescriptor pd = null;

		Collection<ProjectDescriptor> settingsProjects = getAllSettingsProjects(settings);
		
		pd = settingsProjects
			.stream()
			.filter(x -> x.getName().equals(pathString))
			.findAny()
			.orElse(null);


		return pd;
	}
	public static Collection<String> _getSourceProjectDescriptorFiltered(
			Settings settings) {

		Collection<String> sourceProjects = new ArrayList<>();

		try {
			String projectPathsProperty = getLiferayProjectPathFilterProperty();

			if (projectPathsProperty != null) {
				if (!projectPathsProperty.isEmpty()) {
					if (!projectPathsProperty.toLowerCase().equals("false")) {

						projectPathsProperty = projectPathsProperty.replace("\"", "");
						sourceProjects = _collectProjectsByPath(settings, projectPathsProperty);
					}
					else {

						// The user specified the value as false.
						// Do nothing (maybe issue a warning?)

					}
				}
				else {
					// The property is specified but empty, so just resolve against the start parameter dir
					// (Whatever directory was selected in Eclipse)

					ProjectDescriptor pd = getProjectDescriptorFromPath(settings,
							settings.getStartParameter().getCurrentDir());

					if (pd != null) {
						sourceProjects.add(
								pd.getPath());
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
	public static Collection<String> _getSourceProjectDescriptors(
			Settings settings, String projectPathsProperty) {

		Collection<String> sourceProjects = new ArrayList<>();

		try {

			if (projectPathsProperty != null) {
				if (!projectPathsProperty.isEmpty()) {
					if (!projectPathsProperty.toLowerCase().equals("false")) {

						projectPathsProperty = projectPathsProperty.replace("\"", "");
						sourceProjects = _collectProjectsByPath(settings, projectPathsProperty);
					}
					else {

						// The user specified the value as false.
						// Do nothing (maybe issue a warning?)

					}
				}
				else {
					// The property is specified but empty, so just resolve against the start parameter dir
					// (Whatever directory was selected in Eclipse)

					ProjectDescriptor pd = getProjectDescriptorFromPath(settings,
							settings.getStartParameter().getCurrentDir());

					if (pd != null) {
						sourceProjects.add(
								pd.getPath());
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

	public static String getLiferayProjectPathFilterProperty() {
		String projectPathsProperty = System.getProperty(
				"liferay.project.path.filter");
		return projectPathsProperty;
	}
	public static String getCalculateLiferayProjectPathsProperty() {
		String projectPathsProperty = System.getProperty(
				"calculate.liferay.project.paths");
		return projectPathsProperty;
	}
	public static String getLiferayProjectPathsProperty() {
		String projectPathsProperty = System.getProperty(
				"liferay.project.paths");
		return projectPathsProperty;
	}
	public static String getLiferayProjectPathsCalculatedProperty() {
		String projectPathsProperty = System.getProperty(
				"liferay.project.paths.calculated");
		return projectPathsProperty;
	}
}
