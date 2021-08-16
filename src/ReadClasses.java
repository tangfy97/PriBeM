import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.*;

import soot.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadClasses {
	
	public static String javapath = System.getProperty("java.class.path");
	public static String jredir = System.getProperty("java.home")+"/lib/rt.jar";
	public static String path = javapath+File.pathSeparator+jredir;
    public static String sourceDirectory = System.getProperty("user.dir");
    public static String jarDirectory = System.getProperty("user.dir")+"/examples";
    public static String clsName = "UsageExample";
    public Set<Method> methods = new HashSet<Method>();
    public String testCp;

    public ReadClasses(String testCp) {
      this.testCp = testCp;
    }
    public Set<Method> methods() {
        return methods;
    }
    
    public void loadTestSet(final Set<String> testClasses) {
    	    loadMethodsFromTestLib(testClasses);
    	    //Util.createSubclassAnnotations(methods, testCp);
    	    //methods = Util.sanityCheck(methods, trainingSet);
    	    //Util.printStatistics("Test set complete", methods);
    	  }
	

	public static Set<String> getAllClassesFromJar(String jarFile) throws IOException {
		Set<String> classes = new HashSet<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
				String className = entry.getName().replace('/', '.');
				className = className.substring(0, className.length() - ".class".length());
				if (className.contains("$"))
					className = className.substring(0, className.indexOf("$") - 1);
				classes.add(className);
			}
		}
		zip.close();
		return classes;
	}
	
	public static Set<String> getAllClassesFromDirectory(String dir) throws IOException {
		Set<String> classes = new HashSet<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getName().endsWith(".jar"))
					classes.addAll(getAllClassesFromJar(listOfFiles[i].getAbsolutePath()));
			}
		}
		return classes;
	}
	
	private void loadMethodsFromTestLib(final Set<String> testClasses) {
	    int methodCount = methods.size();

	    new AbstractSootFeature(testCp) {

	      @Override
	      public Type appliesInternal(Method method) {
	        for (String className : testClasses) {
	          SootClass sc = Scene.v().forceResolve(className, SootClass.HIERARCHY);
	          if (sc == null) continue;
	          if (!testClasses.contains(sc.getName())) continue;
	          if (!sc.isInterface() && !sc.isPrivate())
	            for (SootMethod sm : sc.getMethods()) {
	            if (sm.isConcrete()) {
	              // This is done by hand here because of the cases where the
	              // character ' is in the signature. This is not supported by the
	              // current Soot.
	              // TODO: Get Soot to support the character '
	              String sig = sm.getSignature();
	              sig = sig.substring(sig.indexOf(": ") + 2, sig.length());
	              String returnType = sig.substring(0, sig.indexOf(" "));
	              String methodName =
	                  sig.substring(sig.indexOf(" ") + 1, sig.indexOf("("));
	              List<String> parameters = new ArrayList<String>();
	              for (String parameter : sig
	                  .substring(sig.indexOf("(") + 1, sig.indexOf(")"))
	                  .split(",")) {
	                if (!parameter.trim().isEmpty())
	                  parameters.add(parameter.trim());
	              }

	              Method newMethod =
	                  new Method(methodName, parameters, returnType, className);
	              System.out.println(newMethod.getSignature());
	              methods.add(newMethod);
	            }
	          }
	        }
	        return Type.NOT_SUPPORTED;
	      }

	    }.applies(new Method("a", "void", "x.y"));
	    System.out.println("Loaded " + (methods.size() - methodCount)  + " methods from JAR files.");
	  } 
	
	
	public static String buildCP(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getName().endsWith(".jar") || listOfFiles[i].getName().endsWith(".apk")) {
					if (sb.length() > 0) {
						sb.append(System.getProperty("path.separator"));
					}
					sb.append(listOfFiles[i].getAbsolutePath().toString());
				}
			}
		}
		return sb.toString();
	}
	
}
