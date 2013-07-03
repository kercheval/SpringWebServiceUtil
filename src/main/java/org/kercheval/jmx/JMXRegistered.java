package org.kercheval.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * This helper base class ensures that any object created which
 * inherits from this class will be registered as a JMX mBean in
 * the platform mBean server.
 *
 * All basic naming and categorization information can be specified
 * in the constructor based on the needs of the deriving object.  This
 * base class has most effective use in long running objects or
 * beans.
 *
 * JMX registration is done at construction.  JMX deregistration is
 * supported.
 *
 * @author John Kercheval
 */
public class JMXRegistered
{
    private static final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private final Logger log;
    private final String domain;
    private final String type;
    private final String name;
    private final String objectNameString;

    /**
     * This constructor will register this object with the JMX platform server.
     * All string parameters passed into this constructor will be sanitized
     * to prevent any whitespace within the strings.  All whitespace within the
     * strings will be converted to a period (.).
     *
     * @param log the logger to use for this class
     * @param domain the domain for the JMX mBean object
     * @param type the type of object for this mBean
     * @param name the name of this JMB mBean object
     */
    public JMXRegistered(final Logger log, final String domain, final String type, final String name)
    {
        this.log = log;
        this.domain = sanitize(domain);
        this.type = sanitize(type);
        this.name = sanitize(name);
        objectNameString = new StringBuilder(this.domain)
            .append(":type=").append(this.type)
            .append(",name=").append(this.name).toString();

        try
        {
            final ObjectName objectName = ObjectName.getInstance(objectNameString);
            if (!mBeanServer.isRegistered(objectName))
            {
                log.debug("Registering bean named " + objectName.toString());
                mBeanServer.registerMBean(this, objectName);
            }
            else
            {
                log.warn("Attempt made to register an already existing mBean for '" + objectName.toString() + "'");
            }
        }
        catch (final Exception e)
        {
            // Ignore errors except to log the problem
            log.warn("Error creating mBean for '" + objectNameString + "': " + e);
        }
    }

    /**
     * Unregister this object from the mBean platform server.
     */
    public void unRegister()
    {
        try
        {
            final ObjectName objectName = ObjectName.getInstance(objectNameString);
            if (mBeanServer.isRegistered(objectName))
            {
                log.debug("Unregistering bean named " + objectName.toString());
                mBeanServer.unregisterMBean(objectName);
            }
        }
        catch (final Exception e)
        {
            // Ignore errors except to log the problem
            log.warn("Error unregistering mBean for '" + objectNameString + "': " + e);
        }
    }

    /**
     * Determine if the current object is registered with the current mBean
     * platform server.
     *
     * @return true if the current object is registered with the mBean server
     */
    public boolean isRegistered()
    {
        boolean rVal = false;
        try
        {
            final ObjectName objectName = ObjectName.getInstance(objectNameString);
            rVal = mBeanServer.isRegistered(objectName);
        }
        catch (final Exception e)
        {
            // Ignore errors except to log the problem
            log.warn("Error testing registration for mBean for '" + objectNameString + "': " + e);
        }
        return rVal;
    }

    /**
     * Retrieve the domain of the current object.  This domain contains
     * no whitespace and is used in the creation of the JMX mBean
     * object (ie 'org.kercheval').
     *
     * @return the domain name used for this JMX mBean
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * Retrieve the type of the current object.  This type contains
     * no whitespace and is used in the creation of the JMX mBean
     * object.
     *
     * @return the type used for this JMX mBean
     */
    public String getType()
    {
        return type;
    }

    /**
     * Retrieve the name of the current object.  This name contains
     * no whitespace and is used in the creation of the JMX mBean
     * object.
     *
     * @return the name used for this JMX mBean
     */
    public String getName()
    {
        return name;
    }

    private String sanitize(final String candidate)
    {
        final StringBuilder rVal = new StringBuilder();
        if (!StringUtils.isEmpty(candidate)) {
            int count = 0;
            for (final String segment: StringUtils.split(candidate))
            {
                if (count > 0) {
                    rVal.append(".");
                }
                rVal.append(segment);
                count++;
            }
        }
        return rVal.toString();
    }
}
