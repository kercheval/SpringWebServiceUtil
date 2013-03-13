package org.kercheval.statistics;

/**
 * This class represents the information published via JMX for
 * {@link Timer} objects.
 * <p>
 * Each {@link Timer} created in the system is registered as a
 * JMX mbean and accessible via jconsole or any other JMX client in
 * VM that created the {@link Timer}.
 *
 * @author John Kercheval
 */
public interface TimerMBean
{
    /**
     * Get the name of this timer.  The name is specified in the
     * {@link Timer#getTimer(String)} static factory method.
     *
     * @return the name of this counter
     */
    public String getName();

    /**
     * Get the total number of events timed for this timer.  The count for a timer begins at zero
     * and is incremented on every reference via the {@link Timer.TimerState#stop}
     * method. Active timer usage is not included in this value return.
     *
     * @return the value of the counter
     */
    public long getTotalCalls();

    /**
     * Get the total time (in milliseconds) of all users of this timer.
     * Active timer usage is not included in this value return.
     *
     * @return the total time consumed by all users of this timer
     */
    public long getTotalTime();

    /**
     * Get the average time for all uses of this timer.  This is the
     * standard average of total time divided by total calls.  This
     * method will return 0 if no calls to this timer have yet been
     * made.  Active timer usage is not included in this value return.
     *
     * @return the average time for all uses of this timer
     */
    public double getAverageTime();
}
