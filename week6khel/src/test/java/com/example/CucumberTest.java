package com.example;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Cucumber Test Runner for Khel App
 *
 * This class serves as the entry point for running Cucumber tests.
 * It configures the test suite to run all feature files in the features directory
 * and uses the step definitions in the steps package.
 *
 * @author Khel App Development Team
 * @version 1.0
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "steps")
public class CucumberTest {
    // This class remains empty, it's just used to configure the Cucumber test suite
}