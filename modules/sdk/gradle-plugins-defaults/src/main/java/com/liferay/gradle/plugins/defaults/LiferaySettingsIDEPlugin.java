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

import com.liferay.gradle.plugins.defaults.internal.IDEProfilesPlugin;
import com.liferay.gradle.plugins.defaults.tasks.BaseIDEProfileTask;
import com.liferay.gradle.plugins.defaults.tasks.CleanIDEProfileTask;
import com.liferay.gradle.plugins.defaults.tasks.SetIDEProfileTask;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.initialization.ProjectDirectoryProjectSpec;

/**
 * @author Gregory Amerson
 */
public class LiferaySettingsIDEPlugin extends LiferaySettingsPlugin {

	public static final String IDE_PROFILE_PROPERTY_NAME = "ide.profile";
}