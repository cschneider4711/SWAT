package swat;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class SwatAgent {

	// invoked when agent is started via -javaagent switch before application
	public static void premain(String args, Instrumentation instrumentation) throws IOException {
		System.out.println("Registering SWAT agent through premain");
		initialize(args, instrumentation);
	}
	
	// invoked when agent is attached via Sun tools API while application is running
	public static void agentmain(String args, Instrumentation instrumentation) throws IOException {
		System.out.println("Registering SWAT agent through agentmain");
		initialize(args, instrumentation);
	}

	private static void initialize(String args, Instrumentation instrumentation) {
		ClassFileTransformer classFileTransformer = new WhitelistBuildingTransformer();
		instrumentation.addTransformer(classFileTransformer);
	}
	
	public static void main(String[] args) {
		System.out.println("Usage: add as agent to the JVM start via -javaagent:/pointing/to/this/agent-file.jar");
	}
	
}
