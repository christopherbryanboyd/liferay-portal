package com.liferay.gradle.plugins.defaults.internal.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class ProjectInclusionTest {

	@BeforeClass
	public static void setup() throws Exception {
		dependencyMap.put(":apps:portal-search:portal-search-api", 18);
		dependencyMap.put(":apps:portal-search", 106);
		dependencyMap.put(":apps:site", 125);
		dependencyMap.put(":apps:reading-time", 15);
		dependencyMap.put(":apps:portal-security-audit", 34);
		dependencyMap.put(":apps:portal-security-audit:portal-security-audit-api", 17);
		dependencyMap.put(":sdk:gradle-plugins-defaults", 2);
	}
	@Test
	public void testDependencyCounts() throws Exception {
		

		for (Entry<String, Integer> entry : dependencyMap.entrySet()) {
			Assert.assertTrue(Objects.equals(getDependencyCount(entry.getKey()), entry.getValue()));
			
		}
	}
	
	private static int getDependencyCount(String gradlePath) throws Exception{
		Path path = Paths.get(".").toAbsolutePath();
		
		Path rootPath = Paths.get(File.separator).resolve(path.subpath(0, path.getNameCount() - 3));
	
		Collection<String> args = new ArrayList<>(
				Arrays.asList(
				"sh", "gradlew",
				"-p", "modules" + gradlePath.replace(':', File.separatorChar),
				"-Dorg.gradle.parallel=true", 
				"-Dorg.gradle.configureondemand=true"));
		args.add(":projects");
		args.add("-Dliferay.project.paths=" + gradlePath);

		Process p = new ProcessBuilder(args.toArray(new String[0])
				)
				.directory(rootPath.getParent().toFile())
				.start();
		p.waitFor();
		String stderr= IOUtils.toString(p.getErrorStream());
		String stdout= IOUtils.toString(p.getInputStream());
		System.out.println(stdout);
		if (!stderr.trim().isEmpty()) {
			throw new GradleException(stderr);
		}
		return org.apache.commons.lang.StringUtils.countMatches(stdout, "Project ':");
	}
	
	private static Map<String, Integer> dependencyMap = new HashMap<>();
}
