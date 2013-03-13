package org.kercheval.config;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest
{
    private static String TEST_PROPERTY = "TestProperty";

    public static Config getEmptyConfig()
    {
        final Config config = new Config();
        final Properties props = new Properties();
        config.setProperties(props);
        return config;
    }

    @Test
    public void testConfig()
    {
        final Config config = new Config("test_config.properties");
        Assert.assertTrue(config.getProperties().size() > 0);

        final Properties props = new Properties();
        config.setProperties(props);

        boolean value = config.getBoolean(TEST_PROPERTY, false);
        Assert.assertFalse(value);
        value = config.getBoolean(TEST_PROPERTY, true);
        Assert.assertTrue(value);

        String strValue = config.getString(TEST_PROPERTY, "green");
        Assert.assertEquals("green", strValue);

        props.put(TEST_PROPERTY, "true");
        value = config.getBoolean(TEST_PROPERTY, false);
        Assert.assertTrue(value);
        strValue = config.getString(TEST_PROPERTY, "green");
        Assert.assertEquals("true", strValue);

        props.clear();
        Integer intValue = config.getInteger(TEST_PROPERTY, 34);
        Assert.assertEquals(Integer.valueOf(34), intValue);

        props.put(TEST_PROPERTY, "23");
        intValue = config.getInteger(TEST_PROPERTY, 34);
        Assert.assertEquals(Integer.valueOf(23), intValue);

        props.put(TEST_PROPERTY, "23and43");
        intValue = config.getInteger(TEST_PROPERTY, 34);
        Assert.assertEquals(Integer.valueOf(34), intValue);
    }

    @Test
    public void testInvalidFile()
    {
        final Config config = new Config("Filedoesnotexist.properties");
        Assert.assertEquals(0, config.getProperties().size());
    }
}
