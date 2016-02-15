package swat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class WhitelistBuildingTransformer implements ClassFileTransformer {
	
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if ("java/io/ObjectInputStream".equals(className)) {
			return applyWhitelistBuildingCode(classfileBuffer);
		}
		return null;
	}

	private byte[] applyWhitelistBuildingCode(byte[] classfileBuffer) {
		try {
			ClassPool classPool = ClassPool.getDefault();
			CtClass ctClass = classPool.get("java.io.ObjectInputStream");
			ctClass.addField( CtField.make("private static java.util.Set whitelist = new java.util.HashSet();", ctClass) );
			ctClass.addField( CtField.make("private static java.io.FileWriter exportWriter = new java.io.FileWriter(\"whitelist.swat\");", ctClass) );
			ctClass.addMethod( CtMethod.make("private static void addToWhitelist(Object what) {"
					+ "  if (what != null && what instanceof java.io.ObjectStreamClass) {"
					+ "    java.io.ObjectStreamClass candidate = (java.io.ObjectStreamClass)what;"
					+ "    if (!whitelist.contains(candidate.getName())) {"
					+ "      whitelist.add(candidate.getName());"
					+ "      String name = candidate.getName();"
					+ "      System.out.println(\"Adding to SWAT whitelist:\"+name);"
					+ "      exportWriter.write(name+\"\\n\");"
					+ "      exportWriter.flush();"
					+ "    }"
					+ "  }"
					+ "}", ctClass) );
			CtMethod ctMethod = ctClass.getDeclaredMethod("resolveClass");
			ctMethod.insertBefore("addToWhitelist($args[0]);");
			byte[] byteCode = ctClass.toBytecode();
			ctClass.detach();
			return byteCode;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
