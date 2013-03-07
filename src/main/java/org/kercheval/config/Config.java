package org.kercheval.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Config
{
    public static final String DEFAULT_FILENAME = "config.properties";

    private final Logger logger;
    private Properties properties = new Properties();

    public Config()
    {
        this(DEFAULT_FILENAME);
    }

    public Config(final String configFileName)
    {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Loading configuration file " + configFileName);

        try
        {
            properties = getPropertiesFromClasspath(configFileName);
        }
        catch (final IOException e)
        {
            logger.error("Unable to load properties from configuration file " + configFileName
                + ": " + e.getMessage());
        }
    }

    public boolean getBoolean(final String name, final boolean defaultValue)
    {
        boolean rVal;

        final String property = (String) properties.get(name);

        if (null == property)
        {
            rVal = defaultValue;
        }
        else
        {
            rVal = Boolean.valueOf(property);
        }
        return rVal;
    }

    public Properties getProperties()
    {
        return properties;
    }

    @SuppressWarnings("resource")
    private Properties getPropertiesFromClasspath(final String propFileName)
        throws IOException
    {
        final Properties props = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(propFileName);

            if (inputStream == null)
            {
                throw new FileNotFoundException("property file '" + propFileName
                    + "' not found in the classpath");
            }

            props.load(inputStream);
        }
        finally
        {
            if (null != inputStream)
            {
                inputStream.close();
            }
        }
        logPropertyLoad(props);

        return props;
    }

    public String getString(final String name, final String defaultValue)
    {
        String rVal = defaultValue;
        final String property = (String) properties.get(name);

        if (null == property)
        {
            rVal = defaultValue;
        }
        else
        {
            rVal = property;
        }
        return rVal;
    }

    private void logPropertyLoad(final Properties props)
    {
        logger.debug("   Loaded properties numbering " + props.size());
        for (final Entry<Object, Object> entry : props.entrySet())
        {
            logger.debug("     key: " + entry.getKey() + " value: " + entry.getValue());
        }
    }

    public void setProperties(final Properties properties)
    {
        this.properties = properties;
    }
}
