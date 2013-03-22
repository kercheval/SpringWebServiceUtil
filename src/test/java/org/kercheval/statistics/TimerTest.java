package org.kercheval.statistics;

import java.util.Arrays;
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
        final Timer secondTimer = Timer.getTimer("bazquux", firstTimer);
        final Timer thirdTimer = Timer.getTimer("foobar", secondTimer);
        final Timer fourthTimer = Timer.getTimer("bazquux", thirdTimer);
        final Timer fifthTimer = Timer.getTimer("bazquux", (Timer[]) null);

        Assert.assertNotSame(firstTimer, secondTimer);
        Assert.assertSame(firstTimer, thirdTimer);
        Assert.assertTrue(Arrays.equals(firstTimer.getParents(), thirdTimer.getParents()));
        Assert.assertSame(secondTimer, fourthTimer);
        Assert.assertSame(secondTimer, fifthTimer);
    }

    @Test
    public void testStartStop()
    {
        final int initialTimerCount = Timer.getTimers().size();
        final Timer timer = Timer.getTimer(TEST_TIMER_1);
        Assert.assertEquals(TEST_TIMER_1, timer.getName());
        Assert.assertEquals(Double.valueOf(0.0), Double.valueOf(timer.getAverageTime()));

        Timer.TimerState timerState = timer.start();
        Assert.assertEquals(timer, timerState.getTimer());

        try {
            timerState.getElapsedTime();
            Assert.fail("Exception expected");
        } catch(final IllegalStateException e) {
            // Ignore
        }

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

        final Timer parentTimer1 = Timer.getTimer("Test Parent Timer 1");
        final Timer parentTimer2 = Timer.getTimer("Test Parent Timer 2");
        final Timer secondTimer = Timer.getTimer("Test Timer 2", timer, parentTimer1, parentTimer2);

        timerState = secondTimer.start();
        timerState.stop();

        Assert.assertEquals(3, timer.getTotalCalls());
        Assert.assertEquals(1, secondTimer.getTotalCalls());
        Assert.assertNotEquals(timer.getTotalTime(), secondTimer.getTotalTime());
        Assert.assertEquals(secondTimer.getTotalCalls(), parentTimer1.getTotalCalls());
        Assert.assertEquals(parentTimer1.getTotalCalls(), parentTimer2.getTotalCalls());

        timerState = parentTimer1.start();
        timerState.stop();
        Assert.assertEquals(secondTimer.getTotalCalls() + 1, parentTimer1.getTotalCalls());
        Assert.assertNotEquals(parentTimer1.getTotalCalls(), parentTimer2.getTotalCalls());

        final Collection<Timer> timers = Timer.getTimers();
        Assert.assertEquals(initialTimerCount + 4, timers.size());
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
