package swat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
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
			CtMethod ctMethod = ctClass.getDeclaredMethod("resolveClass");
			ctMethod.insertBefore("System.err.println(\"SWAT - Resolving class: \"+$args[0]);");
			byte[] byteCode = ctClass.toBytecode();
			ctClass.detach();
			return byteCode;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
