package org.kercheval.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXRegisteredTest
{
    private static final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(JMXRegisteredTest.class);

    public interface TestBeanMBean
    {
        int getFoo();
    }

    public class TestBean
        extends JMXRegistered
        implements TestBeanMBean
    {
        private final int foo = 2;
        public TestBean(final Logger log, final String domain, final String type, final String name)
        {
            super(log, domain, type, name);
        }

        @Override
        public int getFoo()
        {
            return foo;
        }
    }

    @Test
    public void testBeanRegistration()
        throws MalformedObjectNameException, NullPointerException
    {
        final String name = "My Name ";
        final String type = " My Type";
        final String domain = "org.test ";
        final ObjectName objectName = ObjectName.getInstance("org.test:type=My.Type,name=My.Name");

        TestBean testBean = new TestBean(log, domain, type, name);
        Assert.assertTrue(mBeanServer.isRegistered(objectName));
        Assert.assertTrue(testBean.isRegistered());
        Assert.assertEquals("Invalid domain", "org.test", testBean.getDomain());
        Assert.assertEquals("Invalid type", "My.Type", testBean.getType());
        Assert.assertEquals("Invalid name", "My.Name", testBean.getName());

        testBean = new TestBean(log, domain, type, name);
        Assert.assertTrue(mBeanServer.isRegistered(objectName));
        Assert.assertTrue(testBean.isRegistered());

        testBean.unRegister();
        Assert.assertFalse(mBeanServer.isRegistered(objectName));
        Assert.assertFalse(testBean.isRegistered());
        testBean.unRegister();
        Assert.assertFalse(mBeanServer.isRegistered(objectName));
        Assert.assertFalse(testBean.isRegistered());

        testBean = new TestBean(log, domain, type, name);
        Assert.assertTrue(mBeanServer.isRegistered(objectName));
        Assert.assertTrue(testBean.isRegistered());
        testBean.unRegister();
        Assert.assertFalse(mBeanServer.isRegistered(objectName));
        Assert.assertFalse(testBean.isRegistered());

        testBean = new TestBean(log, "type:=fooname=", null, null);
        Assert.assertFalse(mBeanServer.isRegistered(objectName));
        Assert.assertFalse(testBean.isRegistered());
        testBean.unRegister();
    }
}
