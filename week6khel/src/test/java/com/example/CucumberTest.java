package com.example;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber Test Runner for Khel App - FIXED VERSION
 *
 * This class serves as the entry point for running Cucumber tests.
 * It configures the test suite to run all feature files in the features directory
 * and uses the step definitions in the steps package.
 *
 * @author Khel App Development Team
 * @version 2.0 - Fixed Configuration
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html"
)
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "steps"
)
@ConfigurationParameter(
        key = FILTER_TAGS_PROPERTY_NAME,
        value = "not @skip"
)
@ConfigurationParameter(
        key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
        value = "true"
)
@ConfigurationParameter(
        key = SNIPPET_TYPE_PROPERTY_NAME,
        value = "camelcase"
)
public class CucumberTest {
    // This class remains empty, it's used to configure the Cucumber test suite
}