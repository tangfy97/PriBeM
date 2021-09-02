package PSM;
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
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
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
	          //if (sc == null) continue;
	          //if (!testClasses.contains(sc.getName())) continue;

	            for (SootMethod sm : sc.getMethods()) {
	              for (Unit u : sm.retrieveActiveBody().getUnits()) {
	                  if (!(u instanceof Stmt))
	                    continue;
	                  Stmt stmt = (Stmt) u;
	                  
	                  //System.out.println(stmt);
	                  
	                  if (!stmt.containsInvokeExpr())
	                    continue;

	                  InvokeExpr inv = stmt.getInvokeExpr();
	                  
	                  
	                  //System.out.println(inv.getMethod().getName());
	                  
	                  if ((!inv.getMethod().getName().contains("init"))) {
	                  //if ((inv.getMethod().getDeclaringClass().getName().contains("java.io.")) && (!(inv.getArgCount() == 0))) {
	                	//System.out.println("method: " + inv.getMethod().getName() + inv.getArgs());
	                	  SootMethod gm = inv.getMethod();
	                	  Set<Value> paramVals = new HashSet<Value>();
	                	  //System.out.println(gm.retrieveActiveBody());
	                	  for (Unit u1 : gm.retrieveActiveBody().getUnits()) {
	                	        // Check for invocations
	                	        if (((Stmt) u1).containsInvokeExpr()) {
	                	          InvokeExpr invokeExpr = ((Stmt) u1).getInvokeExpr();
	                	          //System.out.println(invokeExpr);
	                	          Value leftOp = null;
	                	          if (u1 instanceof AssignStmt) leftOp = ((AssignStmt) u1).getLeftOp();
	                	          if (leftOp != null) paramVals.add(leftOp);
	                	          // TODO: Add arguments as well? Not sure.
	                	          if (invokeExpr.getMethod().getName().toLowerCase().contains("read")){
	                	          if(true) {
	                	            paramVals.addAll(invokeExpr.getArgs());
	                	            //System.out.println("arg: "+paramVals);
	                	          }
	                	          }
	                	        }

	                	        if (u1 instanceof AssignStmt) {
	                	          Value leftOp = ((AssignStmt) u1).getLeftOp();
	                	          //System.out.println("left: "+leftOp);
	                	          Value rightOp = ((AssignStmt) u1).getRightOp();
	                	          //System.out.println("right: "+rightOp);
	                	          if (paramVals.contains(leftOp)) paramVals.remove(leftOp);
	                	          if (paramVals.contains(rightOp)) {
	                	            paramVals.add(leftOp);
	                	            System.out.println("assign: "+paramVals);
	                	          }
	                	        }

	                	        // Check for invocations
	                	        if (u1 instanceof ReturnStmt) {
	                	          ReturnStmt rstmt = (ReturnStmt) u1;
	                	          //System.out.println("returnstmt: "+rstmt);
	                	          //System.out.println("returnop: "+rstmt.getOp()+" val: "+paramVals);
	                	          if (paramVals.contains(rstmt.getOp())) {
	                	        	  System.out.println("returnstmt: "+rstmt);
		                	          System.out.println("returnop: "+rstmt.getOp()+" val: "+paramVals);
	                	        	  System.out.println("YES! Method name: "+gm);
	                	          }
	                	        }
	                	      }
	                  }
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
