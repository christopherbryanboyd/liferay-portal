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

package com.liferay.gradle.plugins.defaults.internal;

import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.defaults.tasks.CleanIDEProfileTask;
import com.liferay.gradle.plugins.defaults.tasks.SetIDEProfileTask;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

/**
 * @author Gregory Amerson
 */
public class IDEProfilesPlugin implements Plugin<Project> {

	public static final Plugin<Project> INSTANCE = new IDEProfilesPlugin();

	@Override
	public void apply(Project project) {
		GradleUtil.addTask(
			project, CleanIDEProfileTask.CLEAN_IDE_PROFILE_TASK_NAME,
			CleanIDEProfileTask.class);

		GradleUtil.addTask(
			project, SetIDEProfileTask.SET_IDE_PROFILE_TASK_NAME,
			SetIDEProfileTask.class);

		project.afterEvaluate(
			new Action<Project>() {

				@Override
				public void execute(Project project) {
					_configureBuildProfileTasks(project);
				}

			});
	}

	private IDEProfilesPlugin() {
	}

	private void _configureBuildProfileTasks(Project project) {
		try {
			Task setBuildProfileTask = GradleUtil.getTask(
				project, SetIDEProfileTask.SET_IDE_PROFILE_TASK_NAME);

			setBuildProfileTask.dependsOn(
				GradleUtil.getTask(
					project, CleanIDEProfileTask.CLEAN_IDE_PROFILE_TASK_NAME));
		}
		catch (UnknownTaskException ute) {
		}
	}

}