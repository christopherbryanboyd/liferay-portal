package com.liferay.project.templates;

import java.io.File;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class GradleRunnerTestRule implements TestRule{

	public GradleRunner gradleRunner;
	public File projectDir = new File(".");

	@Override
	public Statement apply(Statement statement, Description description) {

		return new Statement() {
			 @Override
	            public void evaluate() throws Throwable {
				 	gradleRunner = GradleRunner.create();
				 	gradleRunner.withProjectDir(projectDir);
				 	gradleRunner.withArguments("--stop").build();
				 try {
					 statement.evaluate();

				 } finally {
					 gradleRunner.withArguments("--stop").build();
				}
			 }
		};
	}

}