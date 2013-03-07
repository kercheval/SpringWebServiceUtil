package org.kercheval.statistics;

public interface TimerMBean
{
    public String getName();
    public long getTotalCalls();
    public long getTotalTime();
    public double getAverageTime();
}
