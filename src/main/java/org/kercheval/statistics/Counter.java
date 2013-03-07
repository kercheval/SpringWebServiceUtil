package org.kercheval.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Counter
{
    private static final Map<String, Counter> COUNTER_MAP = new HashMap<String, Counter>();

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

    public long getCount()
    {
        return count.get();
    }

    public String getName()
    {
        return name;
    }

    public void increment(final long delta)
    {
        count.getAndAdd(delta);
    }
}
