package org.kercheval.main;

import org.kercheval.statistics.Counter;
import org.kercheval.statistics.Timer;
import org.kercheval.statistics.Timer.TimerState;

public class Main
{
    //
    // Simple main to start a VM and create a few timers and counters.
    // This is a testing main.
    //
    public static void main(final String[] args) throws InterruptedException
    {
        final Timer timerParent = Timer.getTimer("Parent");
        final Timer timerNormal = Timer.getTimer("org.kercheval", "Timer.detail", "Timer", timerParent);

        final Counter counterParent = Counter.getCounter("Parent");
        final Counter counterNormal = Counter.getCounter("org.kercheval", "Counter.detail", "Counter", counterParent);

        while (true) {
            final TimerState timerState = timerNormal.start();
            try {
                counterNormal.increment(1);
                Thread.sleep(100 + counterNormal.getCount());
            }
            finally
            {
                timerState.stop();
                counterParent.increment(1);
            }
        }
    }

}
