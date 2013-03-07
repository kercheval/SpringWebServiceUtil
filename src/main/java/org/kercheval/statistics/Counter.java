package org.kercheval.statistics;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counter
    implements CounterMBean
{
    private static final Map<String, Counter> COUNTER_MAP = new HashMap<String, Counter>();
    private static final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(Counter.class);

    public static Collection<Counter> getCounters()
    {
        synchronized (COUNTER_MAP)
        {
            return Collections.unmodifiableCollection(COUNTER_MAP.values());
        }
    }

    public static Counter getCounter(final String name)
    {
        synchronized (COUNTER_MAP)
        {
            Counter newCounter = COUNTER_MAP.get(name);
            if (null == newCounter)
            {
                newCounter = new Counter(name);
                COUNTER_MAP.put(name, newCounter);
                try
                {
                    mBeanServer.registerMBean(newCounter, new ObjectName("org.kercheval:type=Counter,name=" + name));
                }
                catch (final Exception e)
                {
                    // Ignore errors except to log the problem
                    log.debug("Error creating mBean for Counter '" + newCounter.getName() +
                        "': " + e.getMessage());
                }
            }
            return newCounter;
        }
    }

    private final AtomicLong count = new AtomicLong(0);
    private final String name;

    private Counter(final String name)
    {
        this.name = name;
    }

    @Override
    public long getCount()
    {
        return count.get();
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void increment(final long delta)
    {
        count.getAndAdd(delta);
    }
}
