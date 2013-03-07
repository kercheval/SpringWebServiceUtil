package org.kercheval.statistics;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Timer
    implements TimerMBean
{
    public class TimerState
    {
        private final long start;
        private final Timer timer;

        private boolean active = true;
        private long elapsedTime = 0;

        public TimerState(final Timer timer)
        {
            this.timer = timer;
            start = System.currentTimeMillis();
        }

        public long getElapsedTime()
        {
            synchronized (timer)
            {
                if (active)
                {
                    throw new IllegalStateException("Timer state is not yet stopped");
                }
                return elapsedTime;
            }
        }

        public long getStartTime()
        {
            return start;
        }

        public Timer getTimer()
        {
            return timer;
        }

        public void stop()
        {
            synchronized (timer)
            {
                if (!active)
                {
                    throw new IllegalStateException("Timer state is already stopped");
                }
                active = false;
                elapsedTime = System.currentTimeMillis() - getStartTime();
                timer.stop(this);
            }
        }

        public void stopAndDebugLog(final Logger logger, final String... message)
        {
            synchronized (timer)
            {
                stop();
                if (logger.isDebugEnabled())
                {
                    final StringBuilder output = new StringBuilder(MethodTrace.getStackName(1));
                    output.append(" - ");
                    for (final String stringParam : message)
                    {
                        output.append(stringParam);
                    }
                    output.append(" - Executed in ");
                    output.append(getElapsedTime());
                    output.append("ms");
                    logger.debug(output.toString());
                }
            }
        }
    }

    private static final Map<String, Timer> TIMER_MAP = new HashMap<String, Timer>();
    private static final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(Timer.class);

    public static Timer getTimer(final String name)
    {
        synchronized (TIMER_MAP)
        {
            Timer newTimer = TIMER_MAP.get(name);
            if (null == newTimer)
            {
                newTimer = new Timer(name);
                TIMER_MAP.put(name, newTimer);
                try
                {
                    mBeanServer.registerMBean(newTimer, new ObjectName("org.kercheval:type=Timer,name=" + name));
                }
                catch (final Exception e)
                {
                    // Ignore errors except to log the problem
                    log.debug("Error creating mBean for Timer '" + newTimer.getName() +
                        "': " + e.getMessage());
                }
            }
            return newTimer;
        }
    }

    public static Collection<Timer> getTimers()
    {
        synchronized (TIMER_MAP)
        {
            return Collections.unmodifiableCollection(TIMER_MAP.values());
        }
    }

    private long totalTime = 0;
    private long totalCalls = 0;

    private final String name;

    private final Object timerLock = new Object();

    private Timer(final String name)
    {
        this.name = name;
    }

    @Override
    public double getAverageTime()
    {
        synchronized (timerLock)
        {
            double rVal = 0.0;
            if (getTotalCalls() > 0)
            {
                rVal = ((double) getTotalTime()) / getTotalCalls();
            }
            return rVal;
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getTotalCalls()
    {
        synchronized (timerLock)
        {
            return totalCalls;
        }
    }

    @Override
    public long getTotalTime()
    {
        synchronized (timerLock)
        {
            return totalTime;
        }
    }

    public TimerState start()
    {
        return new TimerState(this);
    }

    protected void stop(final TimerState state)
    {
        synchronized (timerLock)
        {
            totalTime += state.getElapsedTime();
            totalCalls++;
        }
    }
}
