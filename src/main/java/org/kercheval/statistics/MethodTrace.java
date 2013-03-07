package org.kercheval.statistics;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodTrace
{
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
