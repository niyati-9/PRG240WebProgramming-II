package steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Spring Configuration for Cucumber Tests
 *
 * This class provides the Spring context configuration for Cucumber tests.
 * It ensures that Cucumber step definitions can work with Spring beans if needed.
 *
 * @author Khel App Development Team
 * @version 1.0
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = {CucumberSpringConfiguration.class})
public class CucumberSpringConfiguration {

    // This class can be empty or contain Spring bean configurations
    // It serves as the glue between Cucumber and Spring

    /**
     * Constructor
     */
    public CucumberSpringConfiguration() {
        System.out.println("🌱 Cucumber Spring Configuration initialized");
    }
}