package swat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class WhitelistBuildingTransformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if ("java/io/ObjectInputStream".equals(className)) {
			return applyWhitelistBuildingCodeOIS(loader, classfileBuffer);
		} else if ("com/thoughtworks/xstream/mapper/DefaultMapper".equals(className)) {
			return applyWhitelistBuildingCodeXStream(loader, classfileBuffer);
		}
		return null;
	}

	
	private byte[] applyWhitelistBuildingCodeOIS(ClassLoader loader, byte[] classfileBuffer) {
		try {
			ClassPool classPool = ClassPool.getDefault();
			classPool.appendClassPath(new LoaderClassPath(loader));
			CtClass ctClass = classPool.get("java.io.ObjectInputStream");

			ctClass.addField( CtField.make("private static java.util.Set whitelist = new java.util.HashSet();", ctClass) );
			ctClass.addField( CtField.make("private static java.io.FileWriter exportWriter = new java.io.FileWriter(\"whitelist-ois.swat\");", ctClass) );
			ctClass.addField( CtField.make("private static java.io.FileWriter exportWithStacktracesWriter = new java.io.FileWriter(\"whitelist-ois-with-stacktraces.swat\");", ctClass) );
			
			ctClass.addMethod( CtMethod.make("protected static synchronized void addToWhitelist(Object what) {"
					+ "  if (what != null && what instanceof java.io.ObjectStreamClass) {"
					+ "    java.io.ObjectStreamClass candidate = (java.io.ObjectStreamClass)what;"
					+ "    if (!whitelist.contains(candidate.getName())) {"
					+ "      whitelist.add(candidate.getName());"
					+ "      String name = candidate.getName();"
					+ "      System.out.println(\"Adding to SWAT OIS whitelist:\"+name);"
					+ "      exportWriter.write(name+\"\\n\");"
					+ "      exportWriter.flush();"
					+ "      exportWithStacktracesWriter.write(name+\"\\n---\\n\");"
					+ "      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();"
					+ "      for (int i=2; i<stackTrace.length; i++) {"
					+ "        exportWithStacktracesWriter.write(String.valueOf(stackTrace[i]));"
					+ "        exportWithStacktracesWriter.write(\"\\n\");"
					+ "      }"
					+ "      exportWithStacktracesWriter.write(\"\\n\\n=============================\\n\\n\");"
					+ "      exportWithStacktracesWriter.flush();"
					+ "    }"
					+ "  }"
					+ "}", ctClass) );
			
			CtMethod ctMethod = ctClass.getDeclaredMethod("resolveClass");
			ctMethod.insertBefore("addToWhitelist($args[0]);");
			
			byte[] byteCode = ctClass.toBytecode();
			ctClass.detach();
			return byteCode;
		} catch (Exception ex) {
			System.err.println("Unable to instrument OIS");
			ex.printStackTrace();
		}
		return null;
	}
	
	
	private byte[] applyWhitelistBuildingCodeXStream(ClassLoader loader, byte[] classfileBuffer) {
		try {
			ClassPool classPool = ClassPool.getDefault();
			classPool.appendClassPath(new LoaderClassPath(loader));
			CtClass ctClass = classPool.get("com.thoughtworks.xstream.mapper.DefaultMapper");
			ctClass.addField( CtField.make("private static java.util.Set whitelist = new java.util.HashSet();", ctClass) );
			ctClass.addField( CtField.make("private static java.io.FileWriter exportWriter = new java.io.FileWriter(\"whitelist-xstream.swat\");", ctClass) );
			ctClass.addField( CtField.make("private static java.io.FileWriter exportWithStacktracesWriter = new java.io.FileWriter(\"whitelist-xstream-with-stacktraces.swat\");", ctClass) );
			
			ctClass.addMethod( CtMethod.make("protected static synchronized void addToWhitelist(Object what) {"
					+ "  if (what != null && what instanceof java.lang.String) {"
					+ "    java.lang.String candidate = (java.lang.String)what;"
					+ "    if (!whitelist.contains(candidate)) {"
					+ "      whitelist.add(candidate);"
					+ "      System.out.println(\"Adding to SWAT XStream whitelist:\"+candidate);"
					+ "      exportWriter.write(candidate+\"\\n\");"
					+ "      exportWriter.flush();"
					+ "      exportWithStacktracesWriter.write(candidate+\"\\n---\\n\");"
					+ "      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();"
					+ "      for (int i=2; i<stackTrace.length; i++) {"
					+ "        exportWithStacktracesWriter.write(String.valueOf(stackTrace[i]));"
					+ "        exportWithStacktracesWriter.write(\"\\n\");"
					+ "      }"
					+ "      exportWithStacktracesWriter.write(\"\\n\\n=============================\\n\\n\");"
					+ "      exportWithStacktracesWriter.flush();"
					+ "    }"
					+ "  }"
					+ "}", ctClass) );
			CtMethod ctMethod = ctClass.getDeclaredMethod("realClass");
			ctMethod.insertBefore("addToWhitelist($args[0]);");

			byte[] byteCode = ctClass.toBytecode();
			ctClass.detach();
			return byteCode;
		} catch (Exception ex) {
			System.err.println("Unable to instrument XStream");
			ex.printStackTrace();
		}
		return null;
	}
}
