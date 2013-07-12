package org.kercheval.controllers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.kercheval.statistics.Counter;
import org.kercheval.statistics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.tempusfugit.concurrency.ThreadDump;

/**
 * This class supports the creation of a statistic interface for web
 * services.
 * <p>
 * This controller has been annotated using Spring MVC annotations and
 * can be used without modification in your service as long as the
 * /statistics entry point is not already in use.
 * <p>
 * Several entry points are defined to acquire information via HTTP.
 * <ul>
 * <li>/statistics/counters - This entry point will return a JSON
 * list of the current state of all {@link Counter} items currently
 * in the system.</li>
 * <li>/statistics/timers - This entry point will return a JSON list
 * of all the current values of all {@link Timer} items in the system.</li>
 * <li>/statistics/memory - This entry point will return a JSON list
 * showing the current memory status of the running VM.</li>
 * <li>/statistics/system - This entry point will return a JSON list
 * of current VM system properties.</li>
 * <li>/statistics/host - This entry point will return a JSON list of
 * all host related system properties.</li>
 * <li>/statistics/threads - This entry point will execute a VM thread
 * dump and return that output as a string.</li>
 * </ul>
 * <p>
 * The statistics controller has been annotated with Swagger annotations and
 * can be easily incorporated into your documentation
 * <p>
 * To enable this controller, you must be certain that annotation
 * based scanning is enabled in spring and the following context in
 * in your spring configuration file
 * <p>
 * <code>&lt;context:component-scan base-package="org.kercheval" /&gt;</code>
 * <p>
 * or you explicitly add this class to your MVC configuration.
 * <p>
 * This class has been annotated with Swagger annotations to support
 * swagger documentation.
 *
 * @author John Kercheval
 */
@Controller
@RequestMapping("/statistics")
@Api(
    name = "Statistics",
    description = "Obtain API Timer information")
public class StatisticsController
{
    final Logger logger;

    /**
     * Generate the logger for the controller and log the creation of the
     * controller at the debug level.
     */
    public StatisticsController()
    {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Creating Controller " + this.getClass().getName());
    }

    /**
     * Get an unmodifiable collection of all the {@link Counter}
     * objects defined in the system.  This method when used as a
     * web service entry point will return JSON data.
     *
     * @return the collection of {@link Counter} objects in the system
     */
    @RequestMapping(
        value = "/counters",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/counters",
        verb=ApiVerb.GET,
        description = "Returns a list of all counters available.  These counters represent allocated entities in the API.  The returned list is unsorted.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public Collection<Counter> getCounterStatistics()
    {
        return Counter.getCounters();
    }

    /**
     * Get host specific runtime and system properties of the current VM.
     * This method when used as a
     * web service entry point will return JSON data.
     *
     * @return a set of Properties which represent the current host of the VM
     */
    @RequestMapping(
        value = "/host",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/host",
        verb=ApiVerb.GET,
        description = "Returns a list of host properties which the current VM is running on.  The returned list is unsorted.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public Properties getHostInfo()
    {
        final Properties hostProps = new Properties();
        InetAddress addr;

        try
        {
            addr = InetAddress.getLocalHost();

            hostProps.put("host.name", addr.getHostName());
            hostProps.put("host.address", addr.getHostAddress());
        }
        catch (final UnknownHostException e)
        {
            logger.error("Unable to obtain host address: " + e.getMessage());
        }

        hostProps.put("host.time", new Date().toString());
        hostProps.put("user.name", System.getProperty("user.name"));
        hostProps.put("os.name", System.getProperty("os.name"));
        hostProps.put("os.version", System.getProperty("os.version"));

        return hostProps;
    }

    /**
     * Get the memory specific runtime properties of the current VM.
     * This method when used as a
     * web service entry point will return JSON data.
     *
     * @return a set of Properties which represent the current system memory state of the VM
     */
    @RequestMapping(
        value = "/memory",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/memory",
        verb=ApiVerb.GET,
        description = "Show the current allocated, free and maximum memory in bytes in the VM.  The allocated memory value is interesting, but the free memory may be misleading since the VM may allocate more memory up to the value specified by the -Xmx VM parameter.  The maximum memory value represents what the VM will attempt to allocate if necessary, but may be limited by OS memory constraints.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public Properties getMemoryUsage()
    {
        final Properties memProps = new Properties();
        memProps.put("vm.memory.used", Runtime.getRuntime().totalMemory());
        memProps.put("vm.memory.free", Runtime.getRuntime().freeMemory());
        memProps.put("vm.memory.maximum", Runtime.getRuntime().maxMemory());

        return memProps;
    }

    /**
     * Get all system properties of the current VM.  This method when used as a
     * web service entry point will return JSON data.
     *
     * @return a set of Properties which represent the current system properties of the VM
     */
    @RequestMapping(
        value = "/system",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/system",
        verb=ApiVerb.GET,
        description = "Returns the list of system properties available for the VM running the API.  The returned list is unsorted.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public Properties getSystemProperties()
    {
        return System.getProperties();
    }

    /**
     * Get a thread dump of the entire VM running this service.  This
     * is done using the Tempus Fugit concurrent code library
     * (http://tempusfugitlibrary.org).  This library will detect
     * active deadlocks as well as the jstack similar thread dump.
     *
     * @return a string representation of the current thread state of the VM
     */
    @RequestMapping(
        value = "/threads",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/system",
        verb=ApiVerb.GET,
        description = "Returns a full thread dump of all threads in the running VM.  This will also perform deadlock detection and show results.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public String getThreadDump()
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ThreadDump.dumpThreads(new PrintStream(baos));

        return baos.toString();
    }

    /**
     * Get an unmodifiable collection of all the {@link Timer}
     * objects defined in the system.  This method when used as a
     * web service entry point will return JSON data.
     *
     * @return the collection of {@link Timer} objects in the system
     */
    @RequestMapping(
        value = "/timers",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiMethod(
        path="/statistics/system",
        verb=ApiVerb.GET,
        description = "Returns a list of all timers available.  These timers give information about call frequency and response rates for API service and dao calls in the system.  The returned list is unsorted.",
        produces={MediaType.APPLICATION_JSON_VALUE},
        consumes={MediaType.APPLICATION_JSON_VALUE}
    )
    public Collection<Timer> getTimerStatistics()
    {
        return Timer.getTimers();
    }
}
