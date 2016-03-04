# SWAT (Serial Whitelist Application Trainer)

This is a simple JVM instrumentation agent, which helps you to build a whitelist
 for classic Java deserialization occurrences as well as XStream based ones. Once (on a test server) the instrumentation runs, it creates and logs files (under the folder where the JVM process was started from) with the observed deserialization classes. This can be used to build a reasonable whitelist by observing the deserializations on a test server.

Usage: add as agent to the JVM start via -javaagent:/pointing/to/this/swat-agent-file.jar
Resulting whitelist will get logged to stdout and also into file whitelist.swat

