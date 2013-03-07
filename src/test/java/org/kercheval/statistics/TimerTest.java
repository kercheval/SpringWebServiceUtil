package org.kercheval.statistics;

import java.util.Collection;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mockit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.code.tempusfugit.concurrency.ThreadUtils;
import com.google.code.tempusfugit.temporal.Duration;

public class TimerTest
{
    private static final String TEST_TIMER_1 = "Test Timer 1";

    private boolean debugLog = true;
    private int debugLogCalled = 0;

    @After
    public void tearDown()
        throws Exception
    {
        Mockit.tearDownMocks();
    }

    @Test
    public void testDebugLogging()
    {
        final Logger testLogger = new MockUp<Logger>()
        {
            @SuppressWarnings({
                "synthetic-access", "unused"
            })
            @Mock
            public void debug(final String msg)
            {
                debugLogCalled++;
            }

            @SuppressWarnings("synthetic-access")
            @Mock
            public boolean isDebugEnabled()
            {
                return debugLog;
            }
        }.getMockInstance();
        final Timer timer = Timer.getTimer("Debug Log Test Timer");
        debugLog = false;
        debugLogCalled = 0;
        Timer.TimerState timerState = timer.start();
        timerState.stopAndDebugLog(testLogger, "Test");
        Assert.assertEquals(0, debugLogCalled);
        debugLog = true;
        timerState = timer.start();
        timerState.stopAndDebugLog(testLogger, "Test");
        Assert.assertEquals(1, debugLogCalled);
    }

    @Test
    public void testGetTimer()
    {
        final Timer firstTimer = Timer.getTimer("foobar");
        final Timer secondTimer = Timer.getTimer("bazquux");
        final Timer thirdTimer = Timer.getTimer("foobar");

        Assert.assertNotSame(firstTimer, secondTimer);
        Assert.assertSame(firstTimer, thirdTimer);
    }

    @Test
    public void testStartStop()
    {
        final int initialTimerCount = Timer.getTimers().size();
        final Timer timer = Timer.getTimer(TEST_TIMER_1);
        Assert.assertEquals(TEST_TIMER_1, timer.getName());

        Timer.TimerState timerState = timer.start();
        ThreadUtils.sleep(Duration.millis(50));
        timerState.stop();
        Assert.assertEquals(1, timer.getTotalCalls());
        Assert.assertTrue(timer.getTotalTime() >= 50);
        Assert.assertEquals(timer.getTotalTime(), timer.getAverageTime(), 0);

        timerState = timer.start();
        Assert.assertEquals(1, timer.getTotalCalls());
        Assert.assertTrue(timer.getTotalTime() >= 50);

        final double lastAverage = timer.getAverageTime();
        Assert.assertEquals(timer.getTotalTime(), lastAverage, 0);
        timerState.stop();

        Assert.assertEquals(2, timer.getTotalCalls());
        Assert.assertTrue(timer.getTotalTime() >= 50);
        Assert.assertTrue(timer.getAverageTime() < lastAverage);

        final Timer secondTimer = Timer.getTimer("Test Timer 2");

        timerState = secondTimer.start();
        timerState.stop();

        Assert.assertEquals(2, timer.getTotalCalls());
        Assert.assertEquals(1, secondTimer.getTotalCalls());
        Assert.assertNotEquals(timer.getTotalTime(), secondTimer.getTotalTime());

        final Collection<Timer> timers = Timer.getTimers();
        Assert.assertEquals(initialTimerCount + 2, timers.size());
    }

    @Test
    public void testTimerState()
    {
        final Timer timer = Timer.getTimer("Timer State Test");
        final Timer.TimerState timerState = timer.start();
        timerState.stop();
        try
        {
            timerState.stop();
            Assert.fail("Exception expected");
        }
        catch (final IllegalStateException e)
        {
            // Expected
        }
    }
}
