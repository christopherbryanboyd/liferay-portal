package com.liferay.project.templates;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class GradleRunnerTestRule implements TestRule{

	public GradleRunner gradleRunner;

	private static void _stopGradleDaemonProcess() throws IOException {
		File projectDir = new File(".");

		String executable = _getGradleExecutable(projectDir);

			ProcessBuilder processBuilder = new ProcessBuilder();

			if ((projectDir != null) && projectDir.exists()) {

				processBuilder.directory(projectDir);
			}
			processBuilder.command(executable + " --stop");
			processBuilder.start();
		}

	public static File findParentFile(File dir, String[] fileNames, boolean checkParents) {
		if (dir == null) {
			return null;
		}
		else if (Objects.equals(".", dir.toString()) || !dir.isAbsolute()) {
			try {
				dir = dir.getCanonicalFile();
			}
			catch (Exception e) {
				dir = dir.getAbsoluteFile();
			}
		}

		for (String fileName : fileNames) {
			File file = new File(dir, fileName);

			if (file.exists()) {
				return dir;
			}
		}

		if (checkParents) {
			return findParentFile(dir.getParentFile(), fileNames, checkParents);
		}

		return null;
	}

	public static File getGradleWrapper(File dir) {
		File gradleRoot = findParentFile(dir, new String[] {_GRADLEW_UNIX_FILE_NAME, _GRADLEW_WINDOWS_FILE_NAME}, true);

		if (gradleRoot != null) {
			if (isWindows()) {
				return new File(gradleRoot, _GRADLEW_WINDOWS_FILE_NAME);
			}

			return new File(gradleRoot, _GRADLEW_UNIX_FILE_NAME);
		}

		return null;
	}

	@Override
	public Statement apply(Statement statement, Description description) {

		return new Statement() {
			 @Override
	            public void evaluate() throws Throwable {
				 	_stopGradleDaemonProcess();
				 try {
					 statement.evaluate();

				 } finally {
					 _stopGradleDaemonProcess();
				}
			 }
		};
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");

		osName = osName.toLowerCase();

		return osName.contains("windows");
	}

	private static String _getGradleExecutable(File dir) throws NoSuchElementException {
		File gradlew = getGradleWrapper(dir);
		String executable = "gradle";

		if ((gradlew == null) || !gradlew.exists()) {
			throw new NoSuchElementException("Gradle wrapper not found and Gradle is not installed");
		}

		if (gradlew != null) {
			try {
				if (!gradlew.canExecute()) {
					gradlew.setExecutable(true);
				}

				executable = gradlew.getCanonicalPath();
			}
			catch (Throwable th) {
			}
		}

		return executable;
	}

	private static final String _GRADLEW_UNIX_FILE_NAME = "gradlew";

	private static final String _GRADLEW_WINDOWS_FILE_NAME = "gradlew.bat";

}