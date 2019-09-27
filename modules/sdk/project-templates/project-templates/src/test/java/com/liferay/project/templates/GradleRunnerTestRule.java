package com.liferay.project.templates;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class GradleRunnerTestRule implements MethodRule{

	public GradleRunner gradleRunner;

	@Override
	public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object target) {

		return new Statement() {
			 @Override
	            public void evaluate() throws Throwable {
				 	gradleRunner = GradleRunner.create();
				 try {
					 statement.evaluate();

				 } finally {
					 gradleRunner.withArguments("--stop").build();
				}
			 }
		};
	}

}
