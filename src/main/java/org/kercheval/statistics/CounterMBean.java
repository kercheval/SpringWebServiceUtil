package org.kercheval.statistics;

/**
 * This class represents the information published via JMX for
 * {@link Counter} objects.
 * <p>
 * Each {@link Counter} created in the system is registered as a
 * JMX mbean and accessible via jconsole or any other JMX client in
 * VM that created the {@link Counter}.
 *
 * @author John Kercheval
 */
public interface CounterMBean
{
    /**
     * Get the count from this counter.  The count for a counter begins at zero
     * and is incremented via the {@link Counter#increment(long)} method.
     *
     * @return the value of the counter
     */
    public long getCount();

    /**
     * Get the name of this counter.  The name is specified in the
     * {@link Counter#getCounter(String)} static factory method.
     *
     * @return the name of this counter
     */
    public String getName();
}
