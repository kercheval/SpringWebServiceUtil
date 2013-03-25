package org.kercheval.controllers;

import java.util.Collection;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.kercheval.jmx.JMXRegistered;
import org.kercheval.statistics.Counter;
import org.kercheval.statistics.Timer;

public class StatisticsControllerTest
{
    @Test
    public void testCounters()
    {
        final StatisticsController controller = new StatisticsController();
        Collection<Counter> counters = controller.getCounterStatistics();
        final int initialSize = counters.size();
        final JMXRegistered newCounter = Counter.getCounter("StatisticsControllerTest.testCounters");

        counters = controller.getCounterStatistics();
        Assert.assertEquals("Invalid counter length", initialSize+1, counters.size());
        Assert.assertTrue("Counter not found in collection", counters.contains(newCounter));
    }

    @Test
    public void testTimers()
    {
        final StatisticsController controller = new StatisticsController();
        Collection<Timer> timers = controller.getTimerStatistics();
        final int initialSize = timers.size();
        final Timer newTimer = Timer.getTimer("StatisticsControllerTest.testTimers");

        timers = controller.getTimerStatistics();
        Assert.assertEquals("Invalid counter length", initialSize+1, timers.size());
        Assert.assertTrue("Counter not found in collection", timers.contains(newTimer));
    }

    @Test
    public void testThreadDump()
    {
        final StatisticsController controller = new StatisticsController();
        final String threadDump = controller.getThreadDump();
        Assert.assertTrue("Can not find test method name in thread dump", threadDump.contains("testThreadDump"));
    }

    @Test
    public void testGetProperties()
    {
        final StatisticsController controller = new StatisticsController();
        Assert.assertSame(System.getProperties(), controller.getSystemProperties());
    }

    @Test
    public void testHostProperties()
    {
        final StatisticsController controller = new StatisticsController();
        final Properties hostProps = controller.getHostInfo();
        Assert.assertEquals("Invalid property count", 6, hostProps.size());
        Assert.assertTrue("Unable to find host name", hostProps.containsKey("host.name"));
        Assert.assertTrue("Unable to find host time", hostProps.containsKey("host.time"));
    }

    @Test
    public void testMemoryProperties()
    {
        final StatisticsController controller = new StatisticsController();
        final Properties memProps = controller.getMemoryUsage();
        Assert.assertEquals("Invalid property count", 3, memProps.size());
        Assert.assertTrue("Unable to find mem used", memProps.containsKey("vm.memory.used"));
        Assert.assertTrue("Unable to find mem free", memProps.containsKey("vm.memory.free"));
    }
}
