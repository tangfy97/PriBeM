package PSM;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import wpds.impl.Weight;
import wpds.impl.Weight.NoWeight;
import soot.jimple.infoflow.*;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.sourcesSinks.definitions.ISourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.MethodSourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.manager.ISourceSinkManager;
import soot.jimple.infoflow.sourcesSinks.manager.SinkInfo;
import soot.jimple.infoflow.sourcesSinks.manager.SourceInfo;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
//import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.infoflow.problems.InfoflowProblem;
import soot.jimple.infoflow.android.*;
import soot.jimple.infoflow.android.entryPointCreators.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import com.google.common.collect.Table;

import PSM.Features.AbstractSootFeature;
import PSM.Features.*;
import PSM.Info.Method;
import polyglot.ast.Assert;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.results.BackwardBoomerangResults;
import boomerang.results.ForwardBoomerangResults;
import boomerang.scene.AllocVal;
import boomerang.scene.AnalysisScope;
import boomerang.scene.ControlFlowGraph.Edge;
import boomerang.scene.SootDataFlowScope;
import boomerang.scene.Statement;
import boomerang.scene.Val;
import boomerang.scene.jimple.BoomerangPretransformer;
import boomerang.scene.jimple.SootCallGraph;
import javafx.util.Pair;
public class ReadClasses {

    private static final String String = null;
	public static String sourceDirectory = System.getProperty("user.dir");
    public static String jarDirectory = System.getProperty("user.dir")+"/examples";
    public static String androidDirPath = System.getProperty("user.dir")+"/lib/android.jar";
    //public static String bomPath = System.getProperty("user.dir")+"/basic/acc.txt";
    public static String bomPath = System.getProperty("user.dir")+"/basic/BOM33.txt";
    public static String bimPath = System.getProperty("user.dir")+"/basic/BIM3.txt";
    public static String ocPath = System.getProperty("user.dir")+"/entrypoints.txt";
    public static String epPath = System.getProperty("user.dir")+"/onCreate.txt";
    public Collection<String> OC = new HashSet<String>();
    public Collection<String> EP = new HashSet<String>();
    public static Set<String> BOM = new HashSet<String>();
    public Set<String> BIM = new HashSet<String>();
    // these might change
	public String apkFilePath = System.getProperty("user.dir")+"/examples/kik.apk";
	public String sourceSinkFilePath = System.getProperty("user.dir")+"/sourcesandsinks.txt";
    public Set<Method> methods = new HashSet<Method>();
    public String testCp;
    public Set<SootMethod> basicSource = new HashSet<SootMethod>();
    public Set<SootMethod> basicSink = new HashSet<SootMethod>();
    public Set<SootMethod> flow2Return = new HashSet<SootMethod>();
    public Set<SootMethod> flow2Sink = new HashSet<SootMethod>();
    public Set<SootMethod> flow2FieldM = new HashSet<SootMethod>();
    public Set<SootClass> flow2FieldC = new HashSet<SootClass>();
    public Set<SootMethod> onCreate = new HashSet<SootMethod>();
    public Set<SootMethod> entrypoints = new HashSet<SootMethod>();

    public ReadClasses(String testCp) {
      this.testCp = testCp;
    }
    public Set<Method> methods() {
        return methods;
    }
    
    public void loadTestSet(final Set<String> testClasses) throws Exception {
    		System.out.println("Start reading BOM and BIM...");
    	    loadMethodsFromTestLib(testClasses);
    	    System.out.println("Methods extraction finished.");
    	  }
    /*
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
	*/

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
	
	public void findFlow() throws Exception {
		BOM = loadBOM();
	    BIM = loadBIM();
		String targetPath = System.getProperty("user.dir")+"/examples/nc-dex2jar.jar";
		String libPath = System.getProperty("user.dir")+"/lib/android.jar";

		IInfoflow infoflow = new Infoflow();
		Collection<String> epoints = EP;
		//Collection<String> epoints = new ArrayList<String>();
		//epoints.add("<com.fasterxml.jackson.core.io.IOContext: char[] allocNameCopyBuffer(int)>");
		
		//take a look with all source that has passed into thoughtcrime but not coming from thoughtcrime
		//then take a look with these sources, mark them as sinks, how many of them actually have values that come from BOM
		
		DefaultEntryPointCreator entryPoints = new DefaultEntryPointCreator(epoints);

		ISourceSinkManager sourceSinkMgr = new ISourceSinkManager() {

			@Override
			public SourceInfo getSourceInfo(Stmt sCallSite, InfoflowManager manager) {
				if (sCallSite.containsInvokeExpr()
						//&& sCallSite instanceof DefinitionStmt){
						//&& sCallSite.getInvokeExpr().getMethod().getDeclaringClass().getName().toLowerCase().contains("edittext")){
						//&& sCallSite instanceof DefinitionStmt && !sCallSite.getInvokeExpr().getMethod().getDeclaringClass().getName().toLowerCase().contains("securesms")) {
						&& sCallSite instanceof DefinitionStmt && sCallSite.getInvokeExpr().getMethod().getDeclaringClass().getName().toLowerCase().contains("edittext")) {
						//&& sCallSite instanceof DefinitionStmt && sCallSite.getInvokeExpr().getMethod().getName().toLowerCase().contains("get")) {
					AccessPath ap = manager.getAccessPathFactory().createAccessPath(
							((DefinitionStmt) sCallSite).getLeftOp(), true);
					return new SourceInfo(null, ap);
				}
				return null;
			}
			//&& BOM.contains(sCallSite.getInvokeExpr().getMethod().getName())){

			@Override
			public SinkInfo getSinkInfo(Stmt sCallSite, InfoflowManager manager, AccessPath ap) {
				if (!sCallSite.containsInvokeExpr())
					return null;

				SootMethod target = sCallSite.getInvokeExpr().getMethod();
				SinkInfo targetInfo = new SinkInfo((ISourceSinkDefinition) new MethodSourceSinkDefinition(new SootMethodAndClass(target)));

				if ((target.getDeclaringClass().getName().toLowerCase().contains("thoughtcrime"))){ //|| target.getSignature().equals(sinkAP2)|| target.getSignature().equals(sink)) && sCallSite.getInvokeExpr().getArgCount() > 0) {
					if (ap == null)
						return targetInfo;
					else if (ap.getPlainValue() == sCallSite.getInvokeExpr().getArg(0))
						if (ap.isLocal() || ap.getTaintSubFields())
							return targetInfo;
					return targetInfo;
				}
				return targetInfo;
			}

			@Override
			public void initialize() {
				// TODO Auto-generated method stub
				
			}
		};

		infoflow.computeInfoflow(targetPath, libPath, entryPoints, sourceSinkMgr);
		//infoflow.computeInfoflow(targetPath, libPath, entryPoints, source, sink);
		infoflow.getResults();
		//infoflow.getCollectedSinks();
		//infoflow.getCollectedSources();

	}
	
	public void fd() throws Exception, XmlPullParserException {
		Options.v().set_allow_phantom_refs(true);
	    Options.v().set_prepend_classpath(true);
	    Options.v().set_whole_program(true);
	    Options.v().set_include_all(true);
	    Options.v().set_soot_classpath(testCp);
	    Options.v().setPhaseOption("cg.spark", "on");
	    Options.v().set_output_format(Options.output_format_none);
	    Options.v().set_no_bodies_for_excluded(true);
	    
	    Options.v().setPhaseOption("jb", "use-original-names:true");

	    Options.v().set_prepend_classpath(true);
	    //Scene.v().addBasicClass("android.app.IntentService",0);
		
		
		
		
		InfoflowAndroidConfiguration conf = new InfoflowAndroidConfiguration();
		conf.getAnalysisFileConfig().setAndroidPlatformDir(androidDirPath);
		conf.getAnalysisFileConfig().setTargetAPKFile(apkFilePath);
		conf.getAnalysisFileConfig().setSourceSinkFile(sourceSinkFilePath);
		InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.SPARK;
		
		conf.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
		conf.setCallgraphAlgorithm(cgAlgorithm);    // SPARK
		conf.setIgnoreFlowsInSystemPackages(false); // Without this line the onCreate method doesn't have any entries
		
		
		conf.setMergeDexFiles(true);
		conf.getAccessPathConfiguration().setAccessPathLength(-1);
		conf.getSolverConfiguration().setMaxAbstractionPathLength(-1);
		SetupApplication setup = new SetupApplication(conf);
		setup.setCallbackFile("res/AndroidCallbacks.txt");
		
		setup.runInfoflow();
		//setup.constructCallgraph();
		//setup.printEntrypoints();
		/*
		for (SootMethod x : setup.getDummyMainMethod().getDeclaringClass().getMethods())
            entrypoints.add(x);
		
		PrintWriter ocWriter = new PrintWriter("entrypoints.txt", "UTF-8");
    	for (SootMethod m : entrypoints) {
    		//bsWriter.println(m+" -> _SOURCE_");
    		ocWriter.println(m);
    	}
    	//bsWriter.println("Finished.");
    	ocWriter.close();*/
		
		/*
		CallGraph callGraph = Scene.v().getCallGraph();
		  Chain<SootClass> classes = Scene.v().getClasses();
		  for (SootClass cls : classes)
		  {
		    if (!cls.getName().startsWith("com.android.insecurebank"))
		      continue;
		    System.out.println(String.format("Class %s:", cls.getName()));
		    for (SootMethod sootMethod : cls.getMethods())
		    {
		      System.out.println(String.format("\tMethod %s, Phantom: %b", sootMethod.getName(), sootMethod.isPhantom()));
		      for (Edge e : iteratorToIterable(callGraph.edgesInto(sootMethod)))
		      {
		        System.out.println(String.format("\t\tCall graph call from: %s:%s", e.src().getDeclaringClass().getName(), e.src().getName()));
		      }
		      for (Edge e : iteratorToIterable(callGraph.edgesOutOf(sootMethod)))
		      {
		        System.out.println(String.format("\t\tCall graph call to: %s:%s", e.tgt().getDeclaringClass().getName(), e.tgt().getName()));
		      }
		    }
		  }*/
	}
	
	public Set<String> loadBOM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(bomPath)); 
		Set<String> BOM = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			BOM.add(line); 
			line = bufReader.readLine(); 
			}
		bufReader.close();
		System.out.println("BOM is loaded with "+BOM.size()+" methods.");
		return BOM;
	}
	
	public Set<String> loadBIM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(bimPath)); 
		Set<String> BIM = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			BIM.add(line); 
			line = bufReader.readLine(); 
			} 
		bufReader.close();
		System.out.println("BIM is loaded with "+BIM.size()+" methods.");
		return BIM;
	}
	

	private static void setup() throws Exception {
		  Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
		  PackManager.v().getPack("wjtp").add(transform);
	  }

		private static void analyze() {
		PackManager.v().getPack("cg").apply();
		BoomerangPretransformer.v().apply();
		PackManager.v().getPack("wjtp").apply();
		}

	  private static Transformer createAnalysisTransformer() {
	    return new SceneTransformer() {
	      protected void internalTransform(
	          String phaseName, @SuppressWarnings("rawtypes") Map options) {
	        SootCallGraph sootCallGraph = new SootCallGraph();
	        AnalysisScope scope =
	            new AnalysisScope(sootCallGraph) {
	              @Override
	              protected Collection<? extends Query> generate(Edge cfgEdge) {
	                Statement statement = cfgEdge.getStart();
	                //if (statement.getMethod().getName().contains("read")
	                if (BOM.contains(statement.getMethod().toString())
	                //if (statement.getMethod().getDeclaringClass().toString().contains("java.io")
	                	//&&!statement.getMethod().getName().contains("init")
	                    //&& statement.getMethod().isConstructor()
	                    && statement.isAssign()) {
	                  //if (statement.getRightOp().isIntConstant()) {
	                	System.out.println("****method****: "+statement.getMethod());
	                	//basicSource.add(statement.getMethod().getName());
	                    return Collections.singleton(
	                        new ForwardQuery(
	                            cfgEdge,
	                            new AllocVal(
	                                statement.getLeftOp(), statement, statement.getRightOp())));
	                  //}
	                }
	                return Collections.emptySet();
	              }
	            };

	        Collection<Query> seeds = scope.computeSeeds();
	        for (Query query : seeds) {
	          // 1. Create a Boomerang solver.
	          Boomerang solver =
	              new Boomerang(
	                  sootCallGraph, SootDataFlowScope.make(Scene.v()), new DefaultBoomerangOptions());
	          System.out.println("Solving query: " + query);
	          // 2. Submit a query to the solver.
	          ForwardBoomerangResults<NoWeight> forwardBoomerangResults =
	              solver.solve((ForwardQuery) query);

	          // 3. Process forward results
	          Table<Edge, Val, NoWeight> results = forwardBoomerangResults.asStatementValWeightTable();
	          for (Edge s : results.rowKeySet()) {
	        	  //System.out.println(s);
	            // 4. Filter results based on your use statement, in our case the call of
	            //if (s.getTarget().toString().contains("print")) {
	        	  if(true) {
	              // 5. Check that a propagated value is used at the particular statement.
	              for (Val reachingVal : results.row(s).keySet()) {
	                if (s.getTarget().uses(reachingVal)) {
	                  //System.out.println(query + " reaches " + s);
	                }
	              }
	            }
	          }
	        }
	      }
	    };
	  }
	
	
	private void loadMethodsFromTestLib(final Set<String> testClasses) throws Exception {
		Set<Value> valueSet = new HashSet<Value>();
		Map<Value, SootMethod> map = new LinkedHashMap<Value, SootMethod>();
		Map<Value, SootMethod> sinkMap = new LinkedHashMap<Value, SootMethod>();
	    int methodCount = methods.size();
	    BOM = loadBOM();
	    BIM = loadBIM();
	    //OC = loadOC();
	    new AbstractSootFeature(testCp) {

	      @Override
	      public Type appliesInternal(Method method) throws Exception {
	    	  System.out.println("Local flow analysis: ");
	    	  setup();
	        for (String className : testClasses) {
	          SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
	          sc.setApplicationClass();
	          
	          //setup();
	          //analyze();
	          
	          for (SootMethod m : sc.getMethods()) {
	        	  //if (m.getName().contains("Main")) System.out.println(sc);
	  	        if (m.isConcrete()) {
	  	        	
	  	        	for (Unit u : m.retrieveActiveBody().getUnits()) {
	        			  Value value = null;
	        			  SootMethod methodBOM = null;
	        			  if (u instanceof AssignStmt) {
	        				  if (((AssignStmt) u).containsInvokeExpr()) {
	        					  InvokeExpr invokeSource = ((AssignStmt) u).getInvokeExpr();
	        				  		if ((BOM.contains(invokeSource.getMethod().toString()))) {
	        					  //if(invokeSource.getMethod().getDeclaringClass().getName().contains("java.io")) {
	        				  			methodBOM = invokeSource.getMethod();
	        				  			basicSource.add(methodBOM);
	        				  			value = ((AssignStmt) u).getLeftOp();	
	        				  			map.put(value,methodBOM);
	        				  			//System.out.println("value "+value);
	        				  			//System.out.println("source: "+u);
	        				  			//valueSet.add(value);
	        				  		}
	        				  }
	        			  }
	        		  }
	  	        	for (Unit u0 : m.retrieveActiveBody().getUnits()) {
	  	        		
	  	        		
	  	        		  if (((Stmt) u0).containsInvokeExpr()) {
	  	        			  InvokeExpr ie = ((Stmt) u0).getInvokeExpr();
	  	        			  for (int i = 0; i < ie.getArgCount(); i++) {
	  	        				  //the function cannot be valueOf and the type must change
	  	        				  //if (valueSet.contains(ie.getArg(i)) && !ie.getMethod().getName().toString().toLowerCase().contains("valueof")) {
	  	        				  if (map.containsKey(ie.getArg(i)) && !ie.getMethod().getName().toString().toLowerCase().contains("valueof") && !ie.getMethod().getName().toString().toLowerCase().contains("init")) {
	  	        					  //System.out.println("Value: "+ie.getArg(i)+" from BOM gets processed here: "+ie+" from: ");
	  	        					  //System.out.println(map.get(ie.getArg(i)));
	  	        					if(!(ie.getMethod().getReturnType() == ie.getArg(i).getType()) 
	  	        							&& !ie.getMethod().getReturnType().toString().contains("void") 
	  	        							&& (ie.getArg(i).getType().toString().toLowerCase().contains("string")
	  	        									|| ie.getArg(i).getType().toString().toLowerCase().contains("list")
	  	        									|| ie.getArg(i).getType().toString().toLowerCase().contains("array")
	  	        									|| ie.getArg(i).getType().toString().toLowerCase().contains("buffer")
	  	        									|| ie.getArg(i).getType().toString().toLowerCase().contains("byte"))
	  	        							&& !ie.getArg(i).toString().toLowerCase().contains("$")
	  	        							&& !ie.getArg(i).getType().toString().toLowerCase().contains("exception")
	  	        							&& !ie.getMethod().getReturnType().toString().toLowerCase().contains("exception")
	  	        							&& !ie.getArg(i).toString().toLowerCase().contains("#")) {
	  	        							//&& !ie.getMethod().getReturnType().toString().toLowerCase().contains("string")) {
	  	        					  //if(!(ie.getMethod().getReturnType() == ie.getArg(i).getType()) && !ie.getMethod().getReturnType().toString().contains("void")) {
	  	        						  System.out.println(ie.getArg(i)+" "+map.get(ie.getArg(i))+" "+ie+" "+ie.getArg(i).getType()+" "+ie.getMethod().getReturnType());
	  	        					  }
	  	        					  if(!(ie.getMethod().getReturnType() == ie.getArg(i).getType()) && ie.getMethod().getReturnType().toString().contains("void")) {
	  	        						  //System.out.println("Value: "+ie.getArg(i)+" gets processed and no return.");
	  	        					  }
	  	        				  }
	  	        			  }
	  	        		  }

	  	        		  
	  	        		  if (u0 instanceof AssignStmt) {
	  	        			  Value leftVal = ((AssignStmt) u0).getLeftOp();
	  	        			  Value rightVal = ((AssignStmt) u0).getRightOp();
	  	        			  //if (map.containsKey(leftVal)) map.remove(leftVal);
	  	        			  if (map.containsKey(rightVal)) {
	  	        				  map.put(leftVal, m);
	  	        				  if ((((AssignStmt) u0).containsFieldRef())) {
	  	        					  //System.out.println("Value: "+rightVal+ " from BOM: "+map.get(rightVal)+"flows to a field: "+((AssignStmt) u0).getFieldRef());
	  	        				  }
	  	        			  }
	  	        		  }
	  	        	  }
	  	        	
	  	        	//analyze();
	  	        }
	  	      }
	          /*
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (!sm.getName().contains("main") && sm.isConcrete()) {
		        	  for (Unit u0 : sm.retrieveActiveBody().getUnits()) {
		        		  if (((Stmt) u0).containsInvokeExpr()) {
		        			  InvokeExpr ie = ((Stmt) u0).getInvokeExpr();
		        			  for (int i = 0; i < ie.getArgCount(); i++) {
		        				  if (valueSet.contains(ie.getArg(i))) System.out.println("Value: "+ie.getArg(i)+" from BOM gets processed here: "+ie);
		        			  }
		        		  }
		        		  
		        		  if (u0 instanceof AssignStmt) {
		        			  Value leftVal = ((AssignStmt) u0).getLeftOp();
		        			  Value rightVal = ((AssignStmt) u0).getRightOp();
		        			  if (valueSet.contains(leftVal)) System.out.println("Value: "+leftVal+" from BOM gets re-assigned here: "+u0);
		        		  }
		        	  }
	        	  }
	          }*/
	          
	          
	          /*
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  if (invokeExpr.getMethod().getDeclaringClass().getName().toString().toLowerCase().contains("Firebase")) {
	        				  //if ((BOM.contains(invokeExpr.getMethod().toString()))) {
	        					  basicSource.add(invokeExpr.getMethod());
	        					  //basicSource.add(sm);
	        				  }
	        			  }
	        		  }
	        	  }
	          }*/
	        	 
	        					  /*
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
	            	        	  //basicSource.add(invokeExpr.getMethod());}
	        					  basicSource.add(sm);}*/

	          
	        	  /*
	        	  if (((sm.getDeclaringClass().getName().toLowerCase().contains("java.io") && 
	        			  (sm.getName().toLowerCase().contains("input") || sm.getName().toLowerCase().contains("read")
	        					  ||sm.getName().toLowerCase().contains("get") || sm.getName().toLowerCase().contains("file"))
	        			  ) || sm.getName().toLowerCase().contains("scann")) && !sm.getName().toLowerCase().contains("print") && !sm.getName().isEmpty() && !sm.getName().contains("init") && !sm.getName().contains("close")
	        			  )
	        		  basicSource.add(sm);*/
	          
	          
	          for (SootMethod sm : sc.getMethods()) {        	  
	        	  if (sm.isConcrete()) {
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  
	        				  if (invokeExpr.getMethod().toString().toLowerCase().contains("sql")
	        						  && !invokeExpr.getMethod().toString().contains("init")
	        						  || invokeExpr.getMethod().getDeclaringClass().toString().toLowerCase().contains("sql")
	        						  || invokeExpr.getMethod().toString().toLowerCase().contains("query")
	        						  //|| invokeExpr.getMethod().toString().toLowerCase().contains("db")
	        						  || invokeExpr.getMethod().toString().toLowerCase().contains("database")
	        						  //|| invokeExpr.getMethod().toString().toLowerCase().contains("storage")
	        						  //|| invokeExpr.getMethod().toString().toLowerCase().contains("store")
	        						  || invokeExpr.getMethod().getDeclaringClass().toString().toLowerCase().contains("query")) {
	        				  //if (BIM.contains(invokeExpr.getMethod().getName().toString())) {
	        					  sinkMap.put(invokeExpr.getArg(0), invokeExpr.getMethod());
	        					  basicSink.add(invokeExpr.getMethod());
	        					  if (u.hasTag("LineNumberTag")) {
	        						  LineNumberTag tag = (LineNumberTag)u.getTag(("LineNumberTag"));
	        						  //System.out.println("Line: "+tag.getLineNumber());
	        					  }
	        					  //System.out.println("Class: "+sc+" Method: "+sm);
	        					  basicSink.add(sm);
	        				  }
	        			  }
	        		  }
	        	  }
	          }
	
	          
	          for (SootMethod sm : sc.getMethods()) {        	  
	        	  if (sm.isConcrete()) {
	        		  for (Unit u1 : sm.retrieveActiveBody().getUnits()) {
	        			  if (u1 instanceof AssignStmt) {
	        				  if (sinkMap.containsKey(((AssignStmt) u1).getLeftOp())) {
	        					  //System.out.println("Value from sink: "+sinkMap.get(((AssignStmt) u1).getLeftOp())+" was assigned here: "+u1);
	        					  //if (((AssignStmt) u1).getLeftOp()).getType() =! )
	        					  //System.out.println("Value: "+ie.getArg(i)+ " from BOM: "+map.get(ie.getArg(i))+" type got changed here from "+ie.getArg(i).getType()+" to "+ie.getMethod().getReturnType());
	        				  }
	        				  
	        			  }
	        			  if (((Stmt) u1).containsInvokeExpr()) {
	        				  InvokeExpr call = ((Stmt) u1).getInvokeExpr();
	  	        			  for (int i = 0; i < call.getArgCount(); i++) {
	  	        				  if (sinkMap.containsKey(call.getArg(i)) && !call.getMethod().getName().toString().toLowerCase().contains("valueof")) {
	  	        					  //System.out.println("Value: "+call.getArg(i)+" from sink: "+sinkMap.get(call.getArg(i))+ " got processed here: "+call);
	  	        				  }
	  	        			  }
	        			  }
	        		  }
	        	  }
	          }

	          /*
	          for (SootMethod sm : sc.getMethods()) {
	        	  if (BIM.contains(sm.toString())) basicSink.add(sm);
	          }*/
	        	  /*
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
	          for (SootMethod sm : sc.getMethods()) {        	  
	        	  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  if (invokeExpr.getMethod().getName().contains("crypt") || invokeExpr.getMethod().getDeclaringClass().getName().contains("crypt")) {
	        					  //System.out.println("Encryption here: "+invokeExpr);
	        				  }
	        			  }
	        		  }
	        	  }
	          }*/
	          
	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  try {
	        		  Map<Value, SootMethod> paramVals = new LinkedHashMap<Value, SootMethod>();
	        		  //Set<Value> paramVals = new HashSet<Value>();
	        		  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {

	        	          if (((Stmt) u).containsInvokeExpr()) {
	        	            InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        	            Value leftOp = null;
	        	            //if (u instanceof AssignStmt) leftOp = ((AssignStmt) u).getLeftOp();
	        	            //if (leftOp != null) paramVals.add(leftOp);
	        	            //if (basicSource.contains(invokeExpr.getMethod())) {
	        	            if (basicSource.contains(sm) || basicSource.contains(invokeExpr.getMethod())) {
	        	            	for (int i = 0; i < invokeExpr.getArgCount(); i++) {
	        	            		paramVals.put(invokeExpr.getArg(i),invokeExpr.getMethod());
	        	            	}
	        	            }
	        	          }
	        	      }
	        	          
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {

	        	          if (u instanceof AssignStmt) {
	        	        	  
	        	            Value leftOp = ((AssignStmt) u).getLeftOp();
	        	            Value rightOp = ((AssignStmt) u).getRightOp();
	        	            
	        	            //if (!((AssignStmt) u).containsInvokeExpr()) {
	        	            	if (paramVals.containsKey(leftOp)) paramVals.remove(leftOp);
	        	            	if (paramVals.containsKey(rightOp)) {
	        	            		paramVals.put(leftOp, sm);
	        	            		if ((((AssignStmt) u).containsFieldRef())) {
	        	            			//System.out.println("Value: "+rightOp+ " from BOM: "+paramVals.get(rightOp)+"flows to a field: "+((AssignStmt) u).getFieldRef());
	        	            			//flow2FieldM.add(sm);
	        	            			//flow2FieldC.add(sc);
	        	            		}
	        	            	}
	        	          }
	        	      }
	        	      
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {

	        	          if (u instanceof ReturnStmt) {
	        	            ReturnStmt stmt = (ReturnStmt) u;
	        	            //if (paramVals.contains(stmt.getOp())) {
	        	            if (paramVals.containsKey(stmt.getOp()) 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("bool") 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("int") 
	        	            		&& !stmt.getOp().getType().toString().toLowerCase().contains("void")
	        	            		) {
	        	            	//System.out.println("Value: "+stmt.getOp()+ " from BOM: "+paramVals.get(stmt.getOp())+"flows to a field: "+stmt.getOp().getType());
	        	            	flow2Return.add(sm);
	        	            }
	        	          }
	        	      }
	        	      
	        	      
	        	      /*
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        	          
	        	          if (((Stmt) u).containsInvokeExpr()) {
	        	        	  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        	        	  //if (basicSink.contains(invokeExpr.getMethod())) {
	        	        	  if (basicSink.contains(sm) || basicSink.contains(invokeExpr.getMethod())) {
	        	        	  //if (invokeExpr.getMethod().getName().toLowerCase().contains("print")) {
	        	        		  for (Value arg : invokeExpr.getArgs())
	        	        			  if (paramVals.contains(arg)) flow2Sink.add(sm);
	        	        	  }
	        	          }
	        	      }*/
	        	          
	        	      
	        		  }
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
	          //analyze();
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
	    	
	    	PrintWriter bsWriter = new PrintWriter("source.txt", "UTF-8");
	    	for (SootMethod m : basicSource) {
	    		bsWriter.println(m+" -> _SOURCE_");
	    		//bsWriter.println(m);
	    	}
	    	//bsWriter.println("Finished.");
	    	bsWriter.close();
	    	
	    	PrintWriter bkWriter = new PrintWriter("sink.txt", "UTF-8");
	    	for (SootMethod m : basicSink) {
	    		bkWriter.println(m+" -> _SINK_");
	    		//bkWriter.println(m);
	    	}
	    	//bkWriter.println("Finished.");
	    	bkWriter.close();
	    	/*
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
	    	
	    	PrintWriter fdWriter = new PrintWriter("SourcesAndSinks.txt", "UTF-8");
	    	for (SootMethod m1 : flow2FieldM) {
	    		fdWriter.println(m1+" -> _SOURCE_");
	    	}
	    	
	    	for (SootMethod m2 : flow2Return) {
	    		fdWriter.println(m2+" -> _SOURCE_");
	    	}
	    	
	    	for (SootMethod m3 : basicSink) {
	    		fdWriter.println(m3+" -> _SINK_");
	    	}
	    	
	    	for (SootMethod m4 : basicSource) {
	    		fdWriter.println(m4+" -> _SOURCE_");
	    	}
	    	fdWriter.close();
	    	*/
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
