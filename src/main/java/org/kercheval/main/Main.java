package org.kercheval.main;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.pojo.JSONDoc;
import org.jsondoc.core.util.JSONDocUtils;
import org.kercheval.statistics.Counter;
import org.kercheval.statistics.Timer;
import org.kercheval.statistics.Timer.TimerState;
import org.reflections.Reflections;

public class Main
{
    //
    // Simple main to start a VM and create a few timers and counters.
    // This is a testing main.
    //
    public static void main(final String[] args) throws InterruptedException, JsonGenerationException, JsonMappingException, IOException
    {
        final Timer timerParent = Timer.getTimer("Parent");
        final Timer timerNormal = Timer.getTimer("org.kercheval", "Timer.detail", "Timer", timerParent);

        final Counter counterParent = Counter.getCounter("Parent");
        final Counter counterNormal = Counter.getCounter("org.kercheval", "Counter.detail", "Counter", counterParent);

        int i = 1;
        while (i <= 10) {
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
            System.out.print(i + " ");
            i++;
        }
        System.out.println();

        final Reflections reflections = new Reflections("org.kercheval", Main.class.getClassLoader());
        final JSONDoc apiDoc = new JSONDoc("0.42", "http://thanksforallthefish");
        apiDoc.setApis(JSONDocUtils.getApiDocs(reflections.getTypesAnnotatedWith(Api.class)));
        apiDoc.setObjects(JSONDocUtils.getApiObjectDocs(reflections.getTypesAnnotatedWith(ApiObject.class)));

        final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        final String json = ow.writeValueAsString(apiDoc);

        System.out.print(json);
    }

}
