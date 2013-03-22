package org.kercheval.statistics;

import org.junit.Assert;
import org.junit.Test;

import scala.actors.threadpool.Arrays;

public class CounterTest
{
    private static final String MY_TEST_COUNTER = "My Test Counter";
    private static final String MY_PARENT_TEST_COUNTER = "My Parent Test Counter";
    private static final String MY_SECOND_PARENT_TEST_COUNTER = "My Second Parent Test Counter";
    private static final String MY_PARENTED_TEST_COUNTER = "My Parented Test Counter";

    @Test
    public void testCounter()
    {
        final int numCounters = Counter.getCounters().size();
        final Counter counter = Counter.getCounter(MY_TEST_COUNTER);
        final Counter counterParent = Counter.getCounter(MY_PARENT_TEST_COUNTER);
        final Counter counterSecondParent = Counter.getCounter(MY_SECOND_PARENT_TEST_COUNTER);
        final Counter counterAgain = Counter.getCounter(MY_TEST_COUNTER, counterParent);
        final Counter counterParented = Counter.getCounter(MY_PARENTED_TEST_COUNTER, counterParent, counterSecondParent);
        final Counter counterParentedSame = Counter.getCounter(MY_PARENTED_TEST_COUNTER, counterParent, counterSecondParent);
        final Counter counterParentedAgain = Counter.getCounter(MY_PARENTED_TEST_COUNTER, (Counter[]) null);
        final Counter counterParentedAgainAndAgain = Counter.getCounter(MY_PARENTED_TEST_COUNTER, counterAgain);

        Assert.assertSame(counter, counterAgain);
        Assert.assertSame(counterParented, counterParentedSame);
        Assert.assertSame(counterParented, counterParentedAgain);
        Assert.assertSame(counterParented, counterParentedAgainAndAgain);
        Assert.assertEquals(counter.getCount(), counterAgain.getCount());

        final Counter[] parentArray = { counterParent, counterSecondParent };
        Assert.assertTrue(Arrays.equals(counterParented.getParents(), parentArray));
        Assert.assertEquals(0, counter.getCount());
        Assert.assertEquals(MY_TEST_COUNTER, counter.getName());

        counter.increment(1);

        Assert.assertEquals(1, counter.getCount());

        counter.increment(101);

        Assert.assertEquals(102, counter.getCount());

        Assert.assertEquals(numCounters + 4, Counter.getCounters().size());

        counterParented.increment(3);
        Assert.assertEquals(counterParented.getCount(), counterParent.getCount());
        counterParent.increment(3);
        Assert.assertEquals(counterParented.getCount() + 3, counterParent.getCount());
    }
}
