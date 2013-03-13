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

/**
 * Implements a thread safe, atomic counter that is registered as a JMX bean.
 * <p>
 * This class implements a very simple counter interface with a single method
 * {@link Counter#increment(long)} that specifies the delta to add to the
 * current counter.
 * <p>
 * The counter is initialized to the value zero and created (if it does not
 * already exist) by the factory method {@link Counter#getCounter(String)}.
 * If a counter with a particular name already has been created, that counter
 * will be returned.
 * <p>
 * When a new counter is created, it is registered as an mbean in the current
 * JMX server instance and can be read via any JMX client.
 *
 * @author John Kercheval
 */
public class Counter
    implements CounterMBean
{
    private static final Map<String, Counter> COUNTER_MAP = new HashMap<String, Counter>();
    private static final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(Counter.class);

    /**
     * Get all counters created in the system in a collection that is
     * not modifiable.  This collection will be be updated if new counters
     * are created in the system and a new call to <code>getCounters</code>
     * will be necessary.
     * <p>
     * Note that any counter contained in this collection can be used
     * normally as a counter exactly as if the counter was obtained by
     * the factory method {@link Counter#getCounter(String)}
     *
     * @return an unmodifiable collection of all counters in the system
     */
    public static Collection<Counter> getCounters()
    {
        synchronized (COUNTER_MAP)
        {
            return Collections.unmodifiableCollection(COUNTER_MAP.values());
        }
    }

    /**
     * Get a counter with the name specified.  The counter returned
     * is a thread safe and atomic counter.  Multiple threads may be
     * updating or reading a particular counter at the same time.
     * <p>
     * This factory method will create a new counter if one with
     * the specified name has not already been created.  If a new counter
     * is created, that counter is registered as a JMX mbean and can be
     * accessed via jconsole or other JMX client.
     *
     * @param name the name of the counter to get
     * @return the counter with the name specified
     */
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

    /**
     * Increment the current counter.  The current value in this will be
     * incremented by the number passed in the delta parameter.  Typical
     * counter behavior will use one for the delta value, but other values
     * are accepted.
     * <p>
     * Counter updates are thread safe and atomic for the counter.
     *
     * @param delta the value to increment the current counter by
     */
    public void increment(final long delta)
    {
        count.getAndAdd(delta);
    }
}
