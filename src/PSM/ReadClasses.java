package PSM;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.*;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.util.Chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PSM.Features.AbstractSootFeature;
import PSM.Features.*;
import PSM.Info.Method;

public class ReadClasses {

    public static String sourceDirectory = System.getProperty("user.dir");
    public static String jarDirectory = System.getProperty("user.dir")+"/examples";
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
    	    System.out.println("Methods extraction finished.");
    	  }
    public void featureChecker () {
    	IFeature classNameContainsUser = new MethodClassContainsNameFeature("java.io");
	    IFeature methodreturnsconstant = new MethodReturnsConstantFeature("cp");
	    IFeature methodNameContainsName = new MethodNameContainsFeature("print");
	    IFeature methodHasInvocation = new MethodInvocationFeature("cp","x");
	    IFeature paramFlowsToReturn = new ParameterFlowsToReturn("cp");
	    IFeature methodInvocatesMethod = new MethodCallsAnotherMethod("cp","**");
	    System.out.println("***** Checking features *****");
	    for (Method s : methods) {
	    	if (paramFlowsToReturn.check(s))
	    		System.out.println("<" + s.getSignature() + " has a parameter that flows to return >.");
	    	if (classNameContainsUser.check(s))
	    		System.out.println("<" + s.getSignature() + " is part of class that contains the name '' >.");
	    		//System.out.println("***********");
	    	if (methodNameContainsName.check(s))
	    		System.out.println("<" + s.getSignature() + " contains the name '' >.");
	    		//System.out.println("***********");
		    if (methodreturnsconstant.check(s))
		    	System.out.println("<" + s.getSignature() + " returns a constant >.");
		    	if (methodHasInvocation.check(s))
		    		System.out.println("< An invocation to " + s.getSignature() + " is made >.");
		    		//System.out.println("***********");
		    System.out.println("***********");
		}
	    System.out.println("***** Job finished *****");
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
	          SootClass sc = Scene.v().forceResolve(className, SootClass.SIGNATURES);

	            for (SootMethod sm : sc.getMethods()) {
	            	if (!sm.getName().contains("main"))
	            	try {
	            	      Set<Value> paramVals = new HashSet<Value>();

	            	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
	            	        if (((Stmt) u).containsInvokeExpr()) {
	            	          InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	            	          Value leftOp = null;
	            	          
	            	          //String[] matches = new String[] {"print", "init"};
	            	          if ((invokeExpr.getMethod().getDeclaringClass().getName().contains("java.io.")) && (!(invokeExpr.getArgCount() == 0) && 
	            	        		  !invokeExpr.getMethod().getName().contains("init")&& !invokeExpr.getMethod().getName().contains("print"))) {
	            	        	  //System.out.println(invokeExpr.getMethod().getName());
	            	          }
	            	          
	            	          
	            	          //if (u instanceof AssignStmt) leftOp = ((AssignStmt) u).getLeftOp();
	            	          //if (leftOp != null) paramVals.add(leftOp);
	            	          
	            	          if (invokeExpr.getMethod().getName().toLowerCase().contains("name")) {
	            	        	  //System.out.println(invokeExpr.getMethod());
	            	        	  if (u instanceof AssignStmt) leftOp = ((AssignStmt) u).getLeftOp();
		            	          if (leftOp != null) paramVals.add(leftOp);
	            	        	  paramVals.addAll(invokeExpr.getArgs());
	            	        	  for (Unit u1 : invokeExpr.getMethod().retrieveActiveBody().getUnits()) {
	            	        		  if (u1 instanceof IdentityStmt) {
	            	        	          IdentityStmt id = (IdentityStmt) u1;
	            	        	          if (id.getRightOp() instanceof ParameterRef) paramVals.add(id.getLeftOp());
	            	        	          }
	            	        	  }
	            	          }
	            	          if (invokeExpr.getMethod().getName().toLowerCase().contains("print")) {
	            	        	  //System.out.println(invokeExpr.getMethod().getName());
	            	        	  for (Value arg : invokeExpr.getArgs())
	            	        		  if (paramVals.contains(arg)) System.out.println("YES! Method name: "+sm.getName()+" to sink print");
	            	        	  }
	            	        }
	            	        

	            	        // Check for invocations
	            	        if (u instanceof ReturnStmt) {
	            	          ReturnStmt stmt = (ReturnStmt) u;
	            	          if (paramVals.contains(stmt.getOp())) {
                	        	  System.out.println("YES! Method name: "+sm.getName()+" to return");
                	          }
	            	        }
	            	      }
	            	      throw new RuntimeException(
	            	          "No return statement in method " + method.getSignature());
	            	    } catch (Exception ex) {
	            	      // System.err.println("Something went wrong:");
	            	      // ex.printStackTrace();
	            	    }
	                  
	                
	              
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
	              //System.out.println(newMethod.getSignature());
	              methods.add(newMethod);
	            //}
	          }
	        }
	        return Type.NOT_SUPPORTED;
	      }

		@Override
		public boolean check(Method method) {
			// TODO Auto-generated method stub
			return false;
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
