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

import scala.actors.threadpool.Arrays;

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
 * A counter may have one or more parent counters to allow aggregation statistics
 * to be maintained.  Normally a parented counter will be initiated using
 * the factory method {@link Counter#getCounter(String, Counter...)} and then obtained
 * again for usage via {@link Counter#getCounter(String)}.  Warnings will be
 * logged if inconsistent factory method usage is made for a specific counter and
 * different parent parameters.
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
     * Get a counter with the name and parents specified.  The counter returned
     * is a thread safe and atomic counter.  Multiple threads may be
     * updating or reading a particular counter at the same time.
     * <p>
     * When updating a counter with a parent, the parent counter will also be
     * updated.  This allows for simple aggregate counts.  A counters parents
     * may never change, so if a getCounter call with a different parent set
     * is used, a warning log message will be written.  A call to
     * {@link Counter#getCounter(String)} will not write a warning message.
     * The first counter to be created will always be returned, so if you are
     * using parented counters, take care in construction order.
     * <p>
     * This factory method will create a new counter if one with
     * the specified name has not already been created.  If a new counter
     * is created, that counter is registered as a JMX mbean and can be
     * accessed via jconsole or other JMX client.
     *
     * @param name the name of the counter to get
     * @param parents the parent counters to update when this counter is updated
     * @return the counter with the name specified
     */
    public static Counter getCounter(final String name, final Counter... parents)
    {
        synchronized (COUNTER_MAP)
        {
            Counter newCounter = COUNTER_MAP.get(name);
            if (null == newCounter)
            {
                newCounter = new Counter(name, parents);
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
            else
            {
                //
                // Verify the parents passed in have not changed and log that fact if
                // they have.  We explicitly do not fail in this case
                //
                if ((parents != null) &&
                                !Arrays.equals(parents, newCounter.parents)) {
                    log.warn("Counter factory method called with different parents than initial construction for Counter '" + newCounter.getName() + "'");
                }
            }
            return newCounter;
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
        return getCounter(name, (Counter[]) null);
    }

    private final AtomicLong count = new AtomicLong(0);
    private final String name;
    private final Counter[] parents;

    private Counter(final String name, final Counter... parents)
    {
        this.name = name;
        this.parents = parents;
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
     * Return the parent counters for this counter.  The parent counters
     * are determined by the first call to a factory method for a counter
     * of a specific name.  The parents of a counter may not be changed
     * so care should be taken to ensure the parent list is appropriate
     * before the first call to obtain a counter is made using
     * {@link Counter#getCounter}.
     *
     * @return an array of parent counters or null if no parents exist
     */
    public Counter[] getParents()
    {
        return parents;
    }

    /**
     * Increment the current counter.  The current value in this will be
     * incremented by the number passed in the delta parameter.  Typical
     * counter behavior will use one for the delta value, but other values
     * are accepted.
     * <p>
     * The parent counters (if any) will be updated by the delta value after
     * the counter has been updated.  During update, the parent counters may
     * be inconsistent with the child counter as updates are not transactional.
     * <p>
     * Counter updates are thread safe and atomic for the counter.
     *
     * @param delta the value to increment the current counter by
     */
    public void increment(final long delta)
    {
        count.getAndAdd(delta);
        if (null != parents)
        {
            for (final Counter parent: parents)
            {
                parent.increment(delta);
            }
        }
    }
}
