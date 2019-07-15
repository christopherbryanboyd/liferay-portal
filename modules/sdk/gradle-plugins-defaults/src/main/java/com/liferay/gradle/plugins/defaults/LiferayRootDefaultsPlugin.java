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

import com.liferay.gradle.plugins.SourceFormatterDefaultsPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.FileUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradlePluginsDefaultsUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.source.formatter.SourceFormatterPlugin;

import java.io.File;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Andrea Di Giorgi
 */
public class LiferayRootDefaultsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		if (FileUtil.exists(project, "app.bnd")) {
			LiferaySettingsPlugin.applyPluginSafely(project, LiferayAppDefaultsPlugin.class);
		}

		if (LiferaySettingsPlugin.applyPluginSafely(project, SourceFormatterPlugin.class)) {
			LiferaySettingsPlugin.applyPluginSafely(project, SourceFormatterDefaultsPlugin.INSTANCE);
	
			File portalRootDir = GradleUtil.getRootDir(
				project.getRootProject(), "portal-impl");
	
			GradlePluginsDefaultsUtil.configureRepositories(project, portalRootDir);
	
			for (Project subproject : project.getSubprojects()) {
				Map<String, Project> childProjects = subproject.getChildProjects();
	
				if (childProjects.isEmpty()) {
					LiferaySettingsPlugin.applyPluginSafely(subproject, LiferayDefaultsPlugin.class);
				}
			}
		}
	}

}