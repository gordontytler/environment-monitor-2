package monitor.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.logging.Logger;

public class ConfigurationTest {

    static Logger logger = Logger.getLogger(ConfigurationTest.class.getName());

    @Test
    public void testSaveDefaultProperties() {
        Configuration.saveDefaultProperties();
    }

    @Test
    public void testLoadProperties() {
        Configuration.loadProperties();
    }

    @Test
    public void testGetInstance() {
        Configuration.getInstance();
    }

    @Test
    public void testGetDataDirectory() {
        Assert.assertEquals(Configuration.getInstance().getDataDirectory(), "./target/test-classes/");
    }

    @Test
    public void testUser() {
        User defaultOperator = Configuration.getInstance().getUser("some-server");
        Assert.assertNotNull(defaultOperator.getName());
        Assert.assertNotNull(defaultOperator.getPassword());
        logger.info("password is " + defaultOperator.getPassword());
    }

    @Test
    public void testGetDefaultHost() {
        String host = Configuration.getInstance().getDefaultHostForTests();
        Assert.assertNotNull(host);
    }

}
