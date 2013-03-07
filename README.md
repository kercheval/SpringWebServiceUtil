#SpringWebServiceUtil

---

This project contains common utilities that I find I use in every
service or web application I build.  This is published to help
automate the dependency updates and have a single location for this
utility code. 

The basic contents are as follows

*Timer* - A thread safe timer class that is statically stored and has a
fast debug logging stop.

*Counter* - A thread safe counter class that enables simple counts and
is maintained statically.

*MethodTrace* - A class that allows the determination of a stack trace
element or method name x levels back in the call stack (allowing
better error handling and log messaging).

*Config* - A simple property configuration helper class that has
simple type specific queries with defaults and that loads properties
from the class path

*StatisticsController* A Spring MVC controller class that will
return as JSON or string content for the following

- /statistics/counters - This entry point will return a JSON list of
the current state of all Counter items currently in the system.
- /statistics/timers - This entry point will return a JSON list of all
the current values of all Timer items in the system.
- /statistics/memory - This entry point will return a JSON list showing the
current memory status of the running VM.
- /statistics/system - This entry point will return a JSON list of
current VM system properties.
- /statistics/host - This entry point will return a JSON list of all
host related system properties.
- /statistics/threads - This entry point will execute a VM thread dump
and return that output as a string.

The statistics controller has been annotated with Swagger annotations
and can be easily incorporated into your documentation.

---

John Kercheval (kercheval@gmail.com)

##Licensing

<a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Gradle CM Plugins</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/kercheval" property="cc:attributionName" rel="cc:attributionURL">John Kercheval</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US">Creative Commons Attribution 3.0 Unported License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/kercheval/SpringWebApplication" rel="dct:source">https://github.com/kercheval/SpringWebApplication</a>.
