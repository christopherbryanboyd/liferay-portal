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

package com.liferay.gradle.plugins.workspace.configurators;

import com.liferay.gradle.plugins.LiferayBasePlugin;
import com.liferay.gradle.plugins.LiferayFrontendPlugin;
import com.liferay.gradle.plugins.LiferayOSGiPlugin;
import com.liferay.gradle.plugins.extensions.LiferayExtension;
import com.liferay.gradle.plugins.jasper.jspc.JspCPlugin;
import com.liferay.gradle.plugins.poshi.runner.PoshiRunnerPlugin;
import com.liferay.gradle.plugins.service.builder.ServiceBuilderPlugin;
import com.liferay.gradle.plugins.workspace.WorkspaceExtension;
import com.liferay.gradle.plugins.workspace.WorkspacePlugin;
import com.liferay.gradle.plugins.workspace.internal.util.FileUtil;
import com.liferay.gradle.plugins.workspace.internal.util.GradleUtil;

import groovy.json.JsonSlurper;
import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySourceSpec;
import org.gradle.api.file.CopySpec;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginConvention;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;

/**
 * @author Andrea Di Giorgi
 * @author David Truong
 */
public class ModulesProjectConfigurator extends BaseProjectConfigurator {

	public ModulesProjectConfigurator(Settings settings) {
		super(settings);

		_defaultRepositoryEnabled = GradleUtil.getProperty(
			settings,
			WorkspacePlugin.PROPERTY_PREFIX + NAME +
				".default.repository.enabled",
			_DEFAULT_REPOSITORY_ENABLED);
		_jspPrecompileEnabled = GradleUtil.getProperty(
			settings,
			WorkspacePlugin.PROPERTY_PREFIX + NAME + ".jsp.precompile.enabled",
			_DEFAULT_JSP_PRECOMPILE_ENABLED);
	}
	private void _configureRootTaskDistBundle(final Task assembleTask) {
		Project project = assembleTask.getProject();

		Copy copy = (Copy)GradleUtil.getTask(
			project.getRootProject(),
			RootProjectConfigurator.DIST_BUNDLE_TASK_NAME);

		copy.dependsOn(assembleTask);

		copy.into("osgi/modules", _copyJarClosure(project, assembleTask));
	}

	private File _getJarFile(Project project) {
		return project.file(
			"dist/" + GradleUtil.getArchivesBaseName(project) + "-" +
				project.getVersion() + ".jar");
	}
	
	@SuppressWarnings({"rawtypes", "serial", "unused"})
	private Closure _copyJarClosure(Project project, final Task assembleTask) {
		return new Closure<Void>(project) {

			public void doCall(CopySpec copySpec) {
				Project project = assembleTask.getProject();

				File jarFile = _getJarFile(project);

				ConfigurableFileCollection configurableFileCollection =
					project.files(jarFile);

				configurableFileCollection.builtBy(assembleTask);

				copySpec.from(jarFile);
			}

		};
	}
	@Override
	public void apply(Project project) {
		final WorkspaceExtension workspaceExtension = GradleUtil.getExtension(
			(ExtensionAware)project.getGradle(), WorkspaceExtension.class);

			
		_applyPlugins(project);

		if (isDefaultRepositoryEnabled()) {
			GradleUtil.addDefaultRepositories(project);
		}

		_configureLiferay(project, workspaceExtension);

		//Jar jar = (Jar)GradleUtil.getTask(project, JavaPlugin.JAR_TASK_NAME);

		//_configureRootTaskDistBundle(jar, compileJSPTask);

		Task assembleTask = GradleUtil.getTask(
			project, "build");
		
		_configureRootTaskDistBundle(assembleTask);
		addTaskDockerDeploy(
				project, _copyJarClosure(project, assembleTask),
				workspaceExtension);
		
		if (project.getTasks().findByName(JspCPlugin.COMPILE_JSP_TASK_NAME) != null) {
			project.getTasks().getByName(JspCPlugin.COMPILE_JSP_TASK_NAME, new Action<Task>() {
	
				@Override
				public void execute(Task compileJSPTask) {
					_configureTaskCompileJSP(
						(JavaCompile)compileJSPTask, workspaceExtension);
				}
	
			});
			
		}
		//addTaskDockerDeploy(project, jar, workspaceExtension);
	}

	@Override
	public String getName() {
		return NAME;
	}

	public boolean isDefaultRepositoryEnabled() {
		return _defaultRepositoryEnabled;
	}

	public boolean isJspPrecompileEnabled() {
		return _jspPrecompileEnabled;
	}

	public void setDefaultRepositoryEnabled(boolean defaultRepositoryEnabled) {
		_defaultRepositoryEnabled = defaultRepositoryEnabled;
	}

	public void setJspPrecompileEnabled(boolean jspPrecompileEnabled) {
		_jspPrecompileEnabled = jspPrecompileEnabled;
	}

	@Override
	protected Iterable<File> doGetProjectDirs(File rootDir) throws Exception {
		final Set<File> projectDirs = new HashSet<>();

		Files.walkFileTree(
			rootDir.toPath(),
			new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(
						Path dirPath, BasicFileAttributes basicFileAttributes)
					throws IOException {

					if (Files.exists(dirPath.resolve("bnd.bnd"))) {
						projectDirs.add(dirPath.toFile());

						return FileVisitResult.SKIP_SUBTREE;
					}
					Path dirNamePath = dirPath.getFileName();

					String dirName = dirNamePath.toString();

					if (dirName.equals("build") || dirName.equals("dist") ||
						dirName.equals("node_modules") ||
						dirName.equals("node_modules_cache")) {

						return FileVisitResult.SKIP_SUBTREE;
					}

					if (Files.exists(dirPath.resolve("package.json"))) {
						Map<String, Object> packageJsonMap = _getPackageJsonMap(
							dirPath.toFile());

						Map<String, Object> scripts =
							(Map<String, Object>)packageJsonMap.get("scripts");

						if ((scripts != null) &&
							(scripts.get("build") != null)) {

							projectDirs.add(dirPath.toFile());
						}
						return FileVisitResult.SKIP_SUBTREE;

					}

					return FileVisitResult.CONTINUE;
				}

			});

		return projectDirs;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> _getPackageJsonMap(File dir) {
		File file = new File(dir, "package.json");

		if (!file.exists()) {
			return Collections.emptyMap();
		}

		JsonSlurper jsonSlurper = new JsonSlurper();

		return (Map<String, Object>)jsonSlurper.parse(file);
	}
	protected static final String NAME = "modules";

	private void _applyPlugins(Project project) {

		if (Files.exists(project.getProjectDir().toPath().resolve("package.json"))) {
			GradleUtil.applyPlugin(project, LiferayFrontendPlugin.class);
		}
		else {
			GradleUtil.applyPlugin(project, LiferayOSGiPlugin.class);
			
			if (FileUtil.exists(project, "service.xml")) {
				GradleUtil.applyPlugin(project, ServiceBuilderPlugin.class);
			}
			
		}
	}

	private void _configureLiferay(
		Project project, WorkspaceExtension workspaceExtension) {

		LiferayExtension liferayExtension = GradleUtil.getExtension(
			project, LiferayExtension.class);

		liferayExtension.setAppServerParentDir(workspaceExtension.getHomeDir());
	}

	private void _configureRootTaskDistBundle(
		final Jar jar, final JavaCompile compileJSPTask) {

		final Project project = jar.getProject();

		Copy copy = (Copy)GradleUtil.getTask(
			project.getRootProject(),
			RootProjectConfigurator.DIST_BUNDLE_TASK_NAME);

		copy.into(
			"osgi/modules",
			new Closure<Void>(project) {

				@SuppressWarnings("unused")
				public void doCall(CopySourceSpec copySourceSpec) {
					copySourceSpec.from(jar);
				}

			});

		if (isJspPrecompileEnabled()) {
			copy.into(
				new Closure<String>(project) {

					@SuppressWarnings("unused")
					public String doCall() {
						return _getCompileJSPDestinationDirName(project);
					}

				},
				new Closure<Void>(project) {

					@SuppressWarnings("unused")
					public void doCall(CopySourceSpec copySourceSpec) {
						copySourceSpec.from(compileJSPTask);
					}

				});
		}
	}

	private void _configureTaskCompileJSP(
		JavaCompile compileJSPTask, WorkspaceExtension workspaceExtension) {

		if (!isJspPrecompileEnabled()) {
			return;
		}

		File dir = new File(
			workspaceExtension.getHomeDir(),
			_getCompileJSPDestinationDirName(compileJSPTask.getProject()));

		compileJSPTask.setDestinationDir(dir);
	}

	private String _getCompileJSPDestinationDirName(Project project) {
		BasePluginConvention basePluginConvention = GradleUtil.getConvention(
			project, BasePluginConvention.class);

		return "work/" + basePluginConvention.getArchivesBaseName() + "-" +
			project.getVersion();
	}

	private static final boolean _DEFAULT_JSP_PRECOMPILE_ENABLED = false;

	private static final boolean _DEFAULT_REPOSITORY_ENABLED = true;

	private boolean _defaultRepositoryEnabled;
	private boolean _jspPrecompileEnabled;

}