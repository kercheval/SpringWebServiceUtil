package org.kercheval.statistics;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static utility class to enable simple and efficient access to the call stack.
 * <p>
 * This class allows the caller to determine the method name or stack trace
 * element(which includes a fully qualified name and source
 * line when available) of the current stack n calls back.  This is
 * particularly useful for logging.
 *
 * @author John Kercheval
 */
public class MethodTrace
{
    private MethodTrace() {
        // static methods only
    }

    //
    // save it static to have it available on every call
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTrace.class);
    private static Method method;

    static
    {
        try
        {
            method = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
            method.setAccessible(true);
        }
        catch (final Exception e)
        {
            LOGGER.error("Unable to initialize MethodTrace", e);
        }
    }

    /**
     * Get the method name of the calling method from the stack at a depth specified
     * by the <code>depth</code> parameter.  The depth element is counted from the
     * immediate caller of this method.
     * <p>
     * For example: if the caller of this method was called 'foo()', then calling
     * <code>getMethodName(0)</code> would return the string value 'foo'.
     *
     * @param depth the number of calls back to obtain the method name for
     * @return the name of the method <code>depth</code> elements back in the stack
     */
    public static String getMethodName(final int depth)
    {
        String rVal = null;
        try
        {
            final StackTraceElement element = (StackTraceElement) method.invoke(new Throwable(),
                depth + 1);
            rVal = element.getMethodName();
        }
        catch (final Exception e)
        {
            LOGGER.error("Unable to obtain method name", e);
        }

        return rVal;
    }

    /**
     * Get the stack name of the calling method from the stack at a depth specified
     * by the <code>depth</code> parameter.  The depth element is counted from the
     * immediate caller of this method.
     * <p>
     * For example: if the caller of this method was called 'foo()' and was from a
     * package of bar.quux and the line of call was 25 in the file baz.java, then calling
     * <code>getStackName(0)</code> would return the string value
     * 'bar.quux.foo(baz.java:25)'.
     *
     * @param depth the number of calls back to obtain the method name for
     * @return the name of the method <code>depth</code> elements back in the stack
     */
    public static String getStackName(final int depth)
    {
        String rVal = null;
        try
        {
            final StackTraceElement element = (StackTraceElement) method.invoke(new Throwable(),
                depth + 1);
            rVal = element.toString();
        }
        catch (final Exception e)
        {
            LOGGER.error("Unable to obtain stack name", e);
        }

        return rVal;
    }
}
