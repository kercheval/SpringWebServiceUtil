package org.kercheval.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kercheval.jmx.JMXRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.actors.threadpool.Arrays;

/**
 * Implements a thread safe, atomic timer that is registered as a JMX bean.
 * <p>
 * <code>Timer</code> objects contain aggregate statistics about the number
 * of events that have been timed, the total time for all events and the
 * average elapsed time for all events.
 * <p>
 * An event is timed by calling the factory method {@link Timer#start()} and
 * obtaining a {@link TimerState} object.  Once the event being timed has
 * been completed a call to {@link TimerState#stop} will update the originating
 * <code>Timer</code> statistics atomically.  Many events may be being timed
 * with the same <code>Timer</code> at the same time.
 * <p>
 * A timer may have one or more parent timers to allow aggregation statistics
 * to be maintained.  Normally a parented timer will be initiated using
 * the factory method {@link Timer#getTimer(String, String, String, Timer...)} and then obtained
 * again for usage via {@link Timer#getTimer(String)}.  Warnings will be
 * logged if inconsistent factory method usage is made for a specific timer and
 * different parent parameters.
 * <p>
 * When a new timer is created, it is registered as an mbean in the current
 * JMX server instance and can be read via any JMX client.
 *
 * @author John Kercheval
 * @see TimerState
 */
public final class Timer
    extends JMXRegistered
    implements TimerMBean
{
    /**
     * Allows the timing of an event from a {@link Timer}
     * <p>
     * A <code>TimerState</code> is obtained by a call to {@link Timer#start()}.
     * A <code>TimerState</code> object contain the state necessary to update a
     * {@link Timer} object and all of its parents when the method
     * {@link TimerState#stop} is called.
     * <p>
     * <code>TimerState</code> objects can be queried for start time, parent
     * timer and (after the event has completed) the elapsed time for the
     * particular event.
     *
     * @author John Kercheval
     */
    public class TimerState
    {
        private final long start;
        private final Timer timer;

        private boolean active = true;
        private long elapsedTime = 0;

        /**
         * Contructor to create a TimerState.  This constructor should only
         * be called from {@link Timer#start()}.
         *
         * @param timer the parent timer creating this object
         */
        protected TimerState(final Timer timer)
        {
            this.timer = timer;
            start = System.currentTimeMillis();
        }

        /**
         * Get the elapsed time of the current event in milliseconds.  This
         * number is only available after the event has completed.  A call
         * to this method (before a call to {@link TimerState#stop}, will
         * result in an <code>IllegalStateException</code>.
         *
         * @return the elapsed time in milliseconds
         * @throws IllegalStateException returned if the timer event has not completed
         *                  (as signaled by a call to {@link TimerState#stop})
         */
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

        /**
         * Get the time of creation of this specific <code>TimerState</code>
         * object.  The time returned is in milliseconds since epoch consistent
         * with the return value of <code>System.currentTimeMillis()</code>.
         *
         * @return the start time in milliseconds since epoch
         */
        public long getStartTime()
        {
            return start;
        }

        /**
         * Get the {@link Timer} which created this <code>TimerState</code>
         *
         * @return the parent timer which created this <code>TimerState</code>
         */
        public Timer getTimer()
        {
            return timer;
        }

        /**
         * Stop the current event timer and atomically update the parent timer
         * statistics.  Once this call is made, the method {@link TimerState#getElapsedTime()}
         * may be called successfully to determine the total time of the event.
         * <p>
         * The parent timers (if any) will be updated after the timer associated with
         * this state has been updated.  During update, the parent timers may
         * be inconsistent with the child timer as updates are not transactional.
         * <p>
         * This method may be called only once and will throw an <code>IllegalStateException</code>
         * if more than one call is made to stop for any specific <code>TimerState</code> object.
         *
         * @throws IllegalStateException thrown if <code>TimerState.stop</code> is called more than once.
         */
        @SuppressWarnings("synthetic-access")
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

        /**
         * Stop the current timer event and log a debug message.  This method
         * immediately calls {@link TimerState#stop()}, builds a message from
         * the vararg message parameters and logs a debug message.
         * <p>
         * This is a very efficient logger method which uses a <code>StringBuilder</code>
         * to create the log message only if debug logging is enabled.  The vararg message
         * parameter allow efficient passing of parameters to avoid prebuilding of strings
         * for logging.
         *
         * @param logger the logger to use to log the message to after the timer is stopped
         * @param message the message to log after the timer is stopped
         * @see TimerState#stop()
         */
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
    private static final Logger log = LoggerFactory.getLogger(Timer.class);

    /**
     * Get a timer with the name and parents specified.  The timer returned
     * is a thread safe and atomic timer.  Multiple threads may be
     * updating timer statistics or using a particular timer at the
     * same time.
     * <p>
     * When updating a timer with a parent, the parent timer will also be
     * updated.  This allows for simple aggregate timers.  A timers parents
     * may never change, so if a getTimer call with a different parent set
     * is used, a warning log message will be written.  A call to
     * {@link Timer#getTimer(String)} will not write a warning message.
     * The first timer to be created will always be returned, so if you are
     * using parented timers, take care in construction order.
     * <p>
     * This factory method will create a new timer if one with
     * the specified name has not already been created.  If a new timer
     * is created, that timer is registered as a JMX mbean and can be
     * accessed via jconsole or other JMX client.
     *
     * @param domain the JMX domain to register this timer for
     * @param type the JMX type for this timer
     * @param name the name of the timer to get
     * @param parents the parent timers to update when this timer is updated
     *
     * @return the timer with the name specified
     */
    public static Timer getTimer(final String domain, final String type, final String name, final Timer... parents)
    {
        synchronized (TIMER_MAP)
        {
            Timer newTimer = TIMER_MAP.get(name);
            if (null == newTimer)
            {
                newTimer = new Timer(domain, type, name, parents);
                TIMER_MAP.put(name, newTimer);
            }
            else
            {
                //
                // Verify the parents passed in have not changed and log that fact if
                // they have.  We explicitly do not fail in this case
                //
                if ((parents != null) &&
                                !Arrays.equals(parents, newTimer.parents)) {
                    log.warn("Timer factory method called with different parents than initial construction for Timer '" + newTimer.getName() + "'");
                }
            }
            return newTimer;
        }
    }

    /**
     * Get a timer with the name specified.  The timer returned
     * is a thread safe and atomic timer.  Multiple threads may be
     * updating timer statistics or using a particular timer at the
     * same time.
     * <p>
     * This factory method will create a new timer if one with
     * the specified name has not already been created.  If a new timer
     * is created, that timer is registered as a JMX mbean and can be
     * accessed via jconsole or other JMX client.
     *
     * @param name the name of the timer to get
     * @return the timer with the name specified
     */
    public static Timer getTimer(final String name)
    {
        return getTimer("org.kercheval", "Timer", name, (Timer[]) null);
    }

    /**
     * Get all timers created in the system in a collection that is
     * not modifiable.  This collection will be be updated if new timers
     * are created in the system and a new call to <code>getTimers</code>
     * will be necessary.
     * <p>
     * Note that any timer contained in this collection can be used
     * normally as a timer exactly as if the timer was obtained by
     * the factory method {@link Timer#getTimer(String)}
     *
     * @return an unmodifiable collection of all timers in the system
     */
    public static Collection<Timer> getTimers()
    {
        synchronized (TIMER_MAP)
        {
            return Collections.unmodifiableCollection(TIMER_MAP.values());
        }
    }

    private long totalTime = 0;
    private long totalCalls = 0;

    private final Timer[] parents;

    private final Object timerLock = new Object();

    private Timer(final String domain, final String type, final String name, final Timer... parents)
    {
        super(log, domain, type, name);

        this.parents = parents;
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

    /**
     * Return the parent timers for this timer.  The parent timers
     * are determined by the first call to a factory method for a timer
     * of a specific name.  The parents of a timer may not be changed
     * so care should be taken to ensure the parent list is appropriate
     * before the first call to obtain a timer is made using
     * {@link Timer#getTimer}.
     *
     * @return an array of parent timers or null if no parents exist
     */
    public Timer[] getParents()
    {
        return parents;
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

    /**
     * Get a {@link TimerState} that allows the timing of a specific single
     * event.  The returned object contains all the state necessary to
     * update the parent timer statistics when {@link TimerState#stop} is called.
     * <p>
     * Until {@link TimerState#stop} is called, individual {@link TimerState}
     * objects have no affect on the timer statistics.
     *
     * @return a timer state variable that enables timing of a single event
     * @see TimerState#stop()
     * @see TimerState#stopAndDebugLog(Logger, String...)
     */
    public TimerState start()
    {
        return new TimerState(this);
    }

    private void stop(final TimerState state)
    {
        synchronized (timerLock)
        {
            totalTime += state.getElapsedTime();
            totalCalls++;
        }
        if (null != parents)
        {
            for (final Timer timer: parents) {
                timer.stop(state);
            }
        }
    }
}
