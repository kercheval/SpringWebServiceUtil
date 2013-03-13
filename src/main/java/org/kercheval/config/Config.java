package org.kercheval.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class is used as a very simple property loader to load
 * configuration files from the classpath.
 * <p>
 * The <code>Config</code> class is intended to be used as a
 * very straight-forward configuration class that can be simply
 * used in mocking test cases as well as in production.
 * <p>
 * Properties can be loaded explicitly (such as when doing JUNIT
 * testing) via the <code>setProperties</code> method.
 * <p>
 * For more extensive needs (JNDI/JDBC loads or other less frequent
 * use cases), use something like the apache commons configuration
 * library (http://commons.apache.org/proper/commons-configuration/)
 *
 * @author John Kercheval
 */
@Component
public class Config
{
    /**
     * The filename used for configuration property load if
     * the default constructor is used.  The default filename
     * is 'config.properties'.
     */
    public static final String DEFAULT_FILENAME = "config.properties";

    private final Logger logger;
    private Properties properties = new Properties();

    /**
     * This default constructor loads properties from a file in
     * the classpath using the default filename 'config.properties'.
     *
     * @see #Config(String)
     */
    public Config()
    {
        this(DEFAULT_FILENAME);
    }

    /**
     * This constructor will load properties from the classpath
     * using the filename specified by the parameter
     * <code>configFileName</code>.
     * <p>
     * The file load will be logged at info level.  Values loaded
     * from the properties file will be logged at debug level.
     * Failure to load the file will be logged at error level.
     * <p>
     * The file format used should be the same as specified for
     * <code>java.util.Properties.load()</code>.
     *
     * @param configFileName    the name of the file to load as a
     *                          properties file.
     */
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

    /**
     * Get the string value of the property specified by the parameter 'name'.
     *
     * @param name          the name of the property to return
     * @param defaultValue  the value to return of the named property is
     *                      not present in the configuration
     * @return              the string value of the named property
     */
    public String getString(final String name, final String defaultValue)
    {
        String rVal = defaultValue;

        final String property = (String) properties.get(name);
        if (null != property)
        {
            rVal = property;
        }

        return rVal;
    }

    /**
     * Get the named property as a boolean value.  Any value other than
     * 'true' will be returned as false.
     *
     * @param name              The name of the property to return
     * @param defaultValue      The value to return of the property is not present
     * @return                  The boolean value of the property named
     */
    public boolean getBoolean(final String name, final boolean defaultValue)
    {
        boolean rVal = defaultValue;

        final String property = (String) properties.get(name);
        if (null != property)
        {
            rVal = Boolean.valueOf(property);
        }
        return rVal;
    }

    /**
     * Get the named property as an Integer value.  If the property does not
     * exist or is unable to be converted to an Integer value, then the value
     * defined in the parameter <code>defaultValue</code> will be returned.
     * <p>
     * Failure to convert a value found in the property file to an Integer will
     * result in a log entry at the error level.
     *
     * @param name              The name of the property to return
     * @param defaultValue      The value to return of the property is not present or invalid
     * @return                  The Integer value of the property named
     */
    public Integer getInteger(final String name, final Integer defaultValue)
    {
        Integer rVal = defaultValue;

        final String property = (String) properties.get(name);
        if (null != property)
        {
            try
            {
                rVal = Integer.valueOf(property);
            }
            catch (final NumberFormatException e)
            {
                logger.error("Unable to convert property " + name
                    + " to Integer, using default value.  Exception message was: " + e.getMessage());
            }
        }
        return rVal;
    }

    /**
     * Get the properties for this object.  This will return the actual
     * backing Properties object for this configuration.  Any modification
     * of the return value from this method will be reflected in other
     * methods until or unless the properties are reset.
     *
     * @return the backing <code>java.util.Properties</code> for this
     *          configuration object.
     * @see Config#setProperties(Properties)
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Set the backing store properties for this configuration object.  The
     * prior property files will be discarded.  This method will use the passed
     * properties directly so any modification of the passed
     * <code>java.util.Properties</code> object will be reflected in the other
     * query methods of this object.
     *
     * @param properties the new set of properties to use for this configuration
     */
    public void setProperties(final Properties properties)
    {
        this.properties = properties;
        logPropertyLoad(properties);
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

    private void logPropertyLoad(final Properties props)
    {
        logger.debug("   Loaded properties numbering " + props.size());
        for (final Entry<Object, Object> entry : props.entrySet())
        {
            logger.debug("     key: " + entry.getKey() + " value: " + entry.getValue());
        }
    }
}
