package org.kercheval.statistics;

import org.junit.Assert;
import org.junit.Test;

public class CounterTest
{
    private static final String MY_TEST_COUNTER = "My Test Counter";

    @Test
    public void testCounter()
    {
        final int numCounters = Counter.getCounters().size();
        final Counter counter = Counter.getCounter(MY_TEST_COUNTER);

        Assert.assertEquals(0, counter.getCount());
        Assert.assertEquals(MY_TEST_COUNTER, counter.getName());

        counter.increment(1);

        Assert.assertEquals(1, counter.getCount());

        counter.increment(101);

        Assert.assertEquals(102, counter.getCount());

        Assert.assertEquals(numCounters + 1, Counter.getCounters().size());
    }
}
