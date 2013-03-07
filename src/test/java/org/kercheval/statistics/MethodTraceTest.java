package org.kercheval.statistics;

import org.junit.Assert;
import org.junit.Test;

public class MethodTraceTest
{
    @Test
    public void testMethodTrace()
    {
        Assert.assertEquals("invoke0", MethodTrace.getMethodName(1));
        Assert.assertEquals("testMethodTrace", MethodTrace.getMethodName(0));
        Assert.assertTrue(MethodTrace.getStackName(0).startsWith(
            "org.kercheval.statistics.MethodTraceTest.testMethodTrace(MethodTraceTest.java:"));
    }
}
