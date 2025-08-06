package chat.octet.accordion.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Accordion tests providing common test utilities and setup.
 * 
 * @author <a href="https://github.com/eoctet">William</a>
 */
public abstract class AccordionTestBase {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @BeforeEach
    void setUp(TestInfo testInfo) {
        logger.info("Starting test: {}", testInfo.getDisplayName());
    }
    
    /**
     * Creates a test action ID with a random suffix.
     * 
     * @param prefix the prefix for the action ID
     * @return a unique action ID
     */
    protected String createTestActionId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    /**
     * Waits for a short period to allow async operations to complete.
     * 
     * @param milliseconds the time to wait in milliseconds
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        }
    }
}