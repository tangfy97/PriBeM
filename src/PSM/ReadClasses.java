package PSM;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.*;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import soot.jimple.infoflow.*;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.sourcesSinks.definitions.ISourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.MethodSourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.manager.ISourceSinkManager;
import soot.jimple.infoflow.sourcesSinks.manager.SinkInfo;
import soot.jimple.infoflow.sourcesSinks.manager.SourceInfo;
import soot.jimple.infoflow.problems.InfoflowProblem;
import soot.jimple.infoflow.android.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import PSM.Features.AbstractSootFeature;
import PSM.Features.*;
import PSM.Info.Method;
import polyglot.ast.Assert;

public class ReadClasses {

    public static String sourceDirectory = System.getProperty("user.dir");
    public static String jarDirectory = System.getProperty("user.dir")+"/examples";
    public static String androidDirPath = System.getProperty("user.dir")+"/lib/android.jar";
    // these might change
	public String apkFilePath = System.getProperty("user.dir")+"/examples/nc.apk";
	public String sourceSinkFilePath = System.getProperty("user.dir")+"/SourcesAndSinks.txt";
    public Set<Method> methods = new HashSet<Method>();
    public String testCp;
    public Set<SootMethod> basicSource = new HashSet<SootMethod>();
    public Set<SootMethod> basicSink = new HashSet<SootMethod>();
    public Set<SootMethod> flow2Return = new HashSet<SootMethod>();
    public Set<SootMethod> flow2Sink = new HashSet<SootMethod>();
    public Set<SootMethod> flow2FieldM = new HashSet<SootMethod>();
    public Set<SootClass> flow2FieldC = new HashSet<SootClass>();

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
					className = className.substring(0, Math.max(className.indexOf("$") - 1, className.length()));
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
				if (listOfFiles[i].getName().endsWith(".jar") || listOfFiles[i].getName().endsWith(".apk"))
					classes.addAll(getAllClassesFromJar(listOfFiles[i].getAbsolutePath()));
			}
		}
		return classes;
	}
	
	public void findFlow() {
		String targetPath = System.getProperty("user.dir")+"/examples/nc-dex2jar.jar";
		String libPath = System.getProperty("user.dir")+"/examples/nc-dex2jar.jar";

		IInfoflow infoflow = new Infoflow();
		Collection<String> epoints = new ArrayList<String>();
		epoints.add("<com.fasterxml.jackson.core.io.IOContext: char[] allocNameCopyBuffer(int)>");
		//epoints.add("<manipulation.basic: void <init>()>");
		//epoints.add("<calculation.add: void <init>()>");
		
		
		/*
		Collection<String> source = new ArrayList<String>();
		Collection<String> sink = new ArrayList<String>();
		String entryPoints="<toy.test: void main(java.lang.String[])>";
		source.add("<toy.test: int scan()>");
		sink.add("<calculation.add: int db(int)>");*/
		
		DefaultEntryPointCreator entryPoints = new DefaultEntryPointCreator(epoints);

		ISourceSinkManager sourceSinkMgr = new ISourceSinkManager() {

			@Override
			public SourceInfo getSourceInfo(Stmt sCallSite, InfoflowManager manager) {
				if (sCallSite.containsInvokeExpr()
						&& sCallSite instanceof DefinitionStmt
						&& sCallSite.getInvokeExpr().getMethod().getName().toLowerCase().contains("buf")) {
					AccessPath ap = manager.getAccessPathFactory().createAccessPath(
							((DefinitionStmt) sCallSite).getLeftOp(), true);
					return new SourceInfo(null, ap);
				}
				return null;
			}

			@Override
			public SinkInfo getSinkInfo(Stmt sCallSite, InfoflowManager manager, AccessPath ap) {
				if (!sCallSite.containsInvokeExpr())
					return null;

				SootMethod target = sCallSite.getInvokeExpr().getMethod();
				SinkInfo targetInfo = new SinkInfo((ISourceSinkDefinition) new MethodSourceSinkDefinition(new SootMethodAndClass(target)));
/*
				if ((target.getName().toLowerCase().contains("pt"))){ //|| target.getSignature().equals(sinkAP2)|| target.getSignature().equals(sink)) && sCallSite.getInvokeExpr().getArgCount() > 0) {
					if (ap == null)
						return targetInfo;
					else if (ap.getPlainValue() == sCallSite.getInvokeExpr().getArg(0))
						if (ap.isLocal() || ap.getTaintSubFields())
							return targetInfo;
				}*/
				return targetInfo;
			}

			@Override
			public void initialize() {
				// TODO Auto-generated method stub
				
			}
		};

		infoflow.computeInfoflow(targetPath, libPath, entryPoints, sourceSinkMgr);
		//infoflow.computeInfoflow(targetPath, libPath, entryPoints, source, sink);
		System.out.println(infoflow.getResults());
		//infoflow.getCollectedSinks();
		//infoflow.getCollectedSources();

	}
	
	public void fd() throws Exception, XmlPullParserException {
		InfoflowAndroidConfiguration conf = new InfoflowAndroidConfiguration();
		conf.getAnalysisFileConfig().setAndroidPlatformDir(androidDirPath);
		conf.getAnalysisFileConfig().setTargetAPKFile(apkFilePath);
		conf.getAnalysisFileConfig().setSourceSinkFile(sourceSinkFilePath);
		// apk中的dex文件有对方法数量的限制导致实际app中往往是多dex，不作设置将仅分析classes.dex
		conf.setMergeDexFiles(true);
		// 设置AccessPath长度限制，默认为5，设置负数表示不作限制，AccessPath会在后文解释
		conf.getAccessPathConfiguration().setAccessPathLength(-1);
		// 设置Abstraction的path长度限制，设置负数表示不作限制，Abstraction会在后文解释
		conf.getSolverConfiguration().setMaxAbstractionPathLength(-1);
		SetupApplication setup = new SetupApplication(conf);
		// 设置Callback的声明文件（不显式地设置好像FlowDroid会找不到）
		setup.setCallbackFile("res/AndroidCallbacks.txt");
		setup.runInfoflow();
	}
	

	

	
	
	private void loadMethodsFromTestLib(final Set<String> testClasses) {
	    int methodCount = methods.size();

	    new AbstractSootFeature(testCp) {

	      @Override
	      public Type appliesInternal(Method method) {
	        for (String className : testClasses) {
	          SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
	          for (SootMethod sm : sc.getMethods()) {
	        	  //if (!sm.getName().contains("main") && sm.isConcrete()) {
	        	  if (sm.isConcrete()) {
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  if ((invokeExpr.getMethod().getDeclaringClass().getName().contains("java.io") ||
	        						  invokeExpr.getMethod().getName().toLowerCase().contains("scann"))
	        						  && (invokeExpr.getMethod().getDeclaringClass().getName().toLowerCase().contains("input") 
	        						  || invokeExpr.getMethod().getDeclaringClass().getName().toLowerCase().contains("read")
	        						  || invokeExpr.getMethod().getName().toLowerCase().contains("get")
	        						  || invokeExpr.getMethod().getDeclaringClass().getName().toLowerCase().contains("file"))
	        						  && !invokeExpr.getMethod().getName().contains("close")
	        						  && !invokeExpr.getMethod().getName().isEmpty() 
	        						  && !invokeExpr.getMethod().getName().contains("init") 
	        						  && !invokeExpr.getMethod().getName().contains("print")) {
	            	        	  basicSource.add(invokeExpr.getMethod());
	            	          }
	        			  }
	        		  }
	        	  }
	          }
	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (((sm.getDeclaringClass().getName().toLowerCase().contains("java.io") && 
	        			  (sm.getName().toLowerCase().contains("input") || sm.getName().toLowerCase().contains("read")
	        					  ||sm.getName().toLowerCase().contains("get") || sm.getName().toLowerCase().contains("file"))
	        			  ) || sm.getName().toLowerCase().contains("scann")) && !sm.getName().toLowerCase().contains("print") && !sm.getName().isEmpty() && !sm.getName().contains("init") && !sm.getName().contains("close")
	        			  )
	        		  basicSource.add(sm);
	          }
	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  if (((invokeExpr.getMethod().getDeclaringClass().getName().contains("java.io") 
	        						  && (invokeExpr.getMethod().getName().toLowerCase().contains("output") 
	        						  || invokeExpr.getMethod().getName().toLowerCase().contains("write"))
	        						  || invokeExpr.getMethod().getName().toLowerCase().contains("print")
	        						  || invokeExpr.getMethod().getName().toLowerCase().contains("llll"))
	        						  || invokeExpr.getMethod().getDeclaringClass().getName().contains("url"))
	        						  && invokeExpr.getMethod().getParameterCount() > 0
	        						  && !invokeExpr.getMethod().getName().toLowerCase().contains("logo")
	        						  //&& !invokeExpr.getMethod().getReturnType().toString().toLowerCase().contains("void")
	        						  && !invokeExpr.getMethod().getReturnType().toString().toLowerCase().contains("bool")) {
	        					  basicSink.add(invokeExpr.getMethod());
	            	          }
	        			  }
	        		  }
	        	  }
	          }
	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (((sm.getDeclaringClass().getName().toLowerCase().contains("java.io") && 
	        			  (sm.getName().toLowerCase().contains("output") || sm.getName().toLowerCase().contains("write"))
	        			  || sm.getName().toLowerCase().contains("print")
	        			  || sm.getName().toLowerCase().contains("llll")) 
	        			  ||(sm.getDeclaringClass().getName().toLowerCase().contains("url"))) 
	        			  && sm.getParameterCount() > 0
	        			  //&& sm.getReturnType().toString().toLowerCase().contains("void")
	        			  && !sm.getName().toLowerCase().contains("logo")
	        			  && !sm.getReturnType().toString().toLowerCase().contains("bool"))
	        		  basicSink.add(sm);
	          }
	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  try {
	        		  Set<Value> paramVals = new HashSet<Value>();
	        		  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {

	        	          if (((Stmt) u).containsInvokeExpr()) {
	        	            InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        	            //Value leftOp = null;
	        	            //if (u instanceof AssignStmt) leftOp = ((AssignStmt) u).getLeftOp();
	        	            //if (leftOp != null) paramVals.add(leftOp);
	        	            if (basicSource.contains(invokeExpr.getMethod())) {
	        	              paramVals.addAll(invokeExpr.getArgs());
	        	            }
	        	          }
	        	          
	        	          

	        	          if (u instanceof AssignStmt) {
	        	        	  
	        	            Value leftOp = ((AssignStmt) u).getLeftOp();
	        	            Value rightOp = ((AssignStmt) u).getRightOp();
	        	            
	        	            if (!((AssignStmt) u).containsInvokeExpr()) {
	        	            	if (paramVals.contains(leftOp)) paramVals.remove(leftOp);
	        	            	if (paramVals.contains(rightOp)) {
	        	            		paramVals.add(leftOp);
	        	            		if ((((AssignStmt) u).containsFieldRef())) {
	        	            			//System.out.println(" Flow to a field: "+((AssignStmt) u).getFieldRef());
	        	            			flow2FieldM.add(sm);
	        	            			flow2FieldC.add(sc);
	        	            		}
	        	            	}
	        	            }
	        	          }

	        	          if (u instanceof ReturnStmt) {
	        	            ReturnStmt stmt = (ReturnStmt) u;
	        	            //if (paramVals.contains(stmt.getOp())) {
	        	            if (paramVals.contains(stmt.getOp()) 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("bool") 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("int") 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("void")) {
	        	            	//System.out.println(stmt.getOp().getType());
	        	            	flow2Return.add(sm);
	        	            }
	        	          }
	        	          
	        	          if (((Stmt) u).containsInvokeExpr()) {
	        	        	  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        	        	  if (basicSink.contains(invokeExpr.getMethod())) {
	        	        	  //if (invokeExpr.getMethod().getName().toLowerCase().contains("print")) {
	        	        		  for (Value arg : invokeExpr.getArgs())
	        	        			  if (paramVals.contains(arg)) flow2Sink.add(sm);
	        	        	  }
	        	          }
	        	          
	        	      }}
	        	  } catch (Exception ex) {
	        		  System.err.println("Something went wrong:");
	        		  ex.printStackTrace();
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
	    
	    //List<String> distinctBasicSource = basicSource.stream().distinct().collect(Collectors.toList());
	    //System.out.println(distinctBasicSource);
	    
	    try {    	
	    	PrintWriter bsWriter = new PrintWriter("BOM.txt", "UTF-8");
	    	for (SootMethod m : basicSource) {
	    		bsWriter.println(m+" -> _BOM_");
	    	}
	    	//bsWriter.println("Finished.");
	    	bsWriter.close();
	    	
	    	PrintWriter bkWriter = new PrintWriter("BIM.txt", "UTF-8");
	    	for (SootMethod m : basicSink) {
	    		bkWriter.println(m+" -> _SINK_");
	    	}
	    	//bkWriter.println("Finished.");
	    	bkWriter.close();
	    	
	    	PrintWriter frWriter = new PrintWriter("F2R.txt", "UTF-8");
	    	for (SootMethod m : flow2Return) {
	    		frWriter.println(m+" -> _SOURCE_");
	    	}
	    	//frWriter.println("Finished.");
	    	frWriter.close();
	    	
	    	PrintWriter fsWriter = new PrintWriter("F2S.txt", "UTF-8");
	    	for (SootMethod m : flow2Sink) {
	    		fsWriter.println(m+" -> _DEFECT_");
	    	}
	    	//fsWriter.println("Finished.");
	    	fsWriter.close();
	    	
	    	PrintWriter ffWriter = new PrintWriter("F2FC.txt", "UTF-8");
	    	for (SootClass c : flow2FieldC) {
	    		ffWriter.println(c+" -> _CLASS_");
	    	}
	    	//ffWriter.println("Finished.");
	    	ffWriter.close();
	    	
	    	PrintWriter ffmWriter = new PrintWriter("F2FM.txt", "UTF-8");
	    	for (SootMethod m : flow2FieldM) {
	    		ffmWriter.println(m+" -> _SOURCE_");
	    	}
	    	//ffmWriter.println("Finished.");
	    	ffmWriter.close();
	    	
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	    
	    System.out.println("Loaded " + (methods.size() - methodCount)  + " methods from JAR files.");
	    System.out.println("Found " + basicSource.size() + " Basic Source Methods.");
	    System.out.println("Found " + basicSink.size() + " Basic Sink Methods.");
	    System.out.println("Found " + flow2Return.size() + " Methods flow to return.");
	    System.out.println("Found " + flow2Sink.size() + " Methods flow to a sink.");
	    System.out.println("Found " + flow2FieldC.size() + " Methods in this Class flow to a field in class.");
	    System.out.println("Found " + flow2FieldM.size() + " Methods flow to a field in class.");
	    
	    
	    
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
