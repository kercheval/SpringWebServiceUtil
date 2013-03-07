package org.kercheval.controllers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.kercheval.statistics.Counter;
import org.kercheval.statistics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.tempusfugit.concurrency.ThreadDump;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.jaxrs.JavaHelp;

@Controller
@RequestMapping("/statistics")
@Api(
    value = "/statistics",
    description = "Obtain API Timer information")
public class StatisticsController
    extends JavaHelp
{
    final Logger logger;

    public StatisticsController()
    {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Creating Controller " + this.getClass().getName());
    }

    @RequestMapping(
        value = "/counters",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "List all counters",
        notes = "Returns a list of all counters available.  These counters represent allocated entities in the API.  The returned list is unsorted.",
        responseClass = "com.asd.statistics.Counter",
        multiValueResponse = true)
    public Collection<Counter> getCounterStatistics()
    {
        return Counter.getCounters();
    }

    @RequestMapping(
        value = "/host",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "List host information for the current VM",
        notes = "Returns a list of host properties which the current VM is running on.  The returned list is unsorted.")
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

    @RequestMapping(
        value = "/memory",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "List memory usage in the running VM",
        notes = "Show the current allocated, free and maximum memory in bytes in the VM.  The allocated memory value is interesting, but the free memory may be misleading since the VM may allocate more memory up to the value specified by the -Xmx VM parameter.  The maximum memory value represents what the VM will attempt to allocate if necessary, but may be limited by OS memory constraints.")
    public Properties getMemoryUsage()
    {
        final Properties memProps = new Properties();
        memProps.put("vm.memory.used", Runtime.getRuntime().totalMemory());
        memProps.put("vm.memory.free", Runtime.getRuntime().freeMemory());
        memProps.put("vm.memory.maximum", Runtime.getRuntime().maxMemory());

        return memProps;
    }

    @RequestMapping(
        value = "/system",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "List all system properties for the current VM",
        notes = "Returns the list of system properties available for the VM running the API.  The returned list is unsorted.")
    public Properties getSystemProperties()
    {
        return System.getProperties();
    }

    @RequestMapping(
        value = "/threads",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "Obtain a thread dump of the current VM",
        notes = "Returns a full thread dump of all threads in the running VM.  This will also perform deadlock detection and show results.")
    public String getThreadDump()
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ThreadDump.dumpThreads(new PrintStream(baos));

        return baos.toString();
    }

    @RequestMapping(
        value = "/timers",
        method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
        value = "List all timers",
        notes = "Returns a list of all timers available.  These timers give information about call frequency and response rates for API service and dao calls in the system.  The returned list is unsorted.",
        responseClass = "com.asd.statistics.Timer",
        multiValueResponse = true)
    public Collection<Timer> getTimerStatistics()
    {
        return Timer.getTimers();
    }
}
