package PSM;
import java.io.BufferedReader;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import vasco.*;
import vasco.callgraph.CallGraphTransformer;
import com.google.common.collect.*;
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import wpds.impl.Weight;
import wpds.impl.Weight.NoWeight;
import soot.jimple.infoflow.*;
import soot.jimple.infoflow.InfoflowConfiguration.AliasingAlgorithm;
import soot.jimple.infoflow.InfoflowConfiguration.ImplicitFlowMode;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.methodSummary.data.provider.LazySummaryProvider;
import soot.jimple.infoflow.methodSummary.taintWrappers.SummaryTaintWrapper;
import soot.jimple.infoflow.sourcesSinks.definitions.ISourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.MethodSourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.manager.ISourceSinkManager;
import soot.jimple.infoflow.sourcesSinks.manager.SinkInfo;
import soot.jimple.infoflow.sourcesSinks.manager.SourceInfo;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.internal.JIfStmt;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
//import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.infoflow.problems.InfoflowProblem;
//import soot.jimple.infoflow.android.*;
//import soot.jimple.infoflow.android.entryPointCreators.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import PSM.Features.AbstractSootFeature;
import PSM.Features.*;
import PSM.CallGraphTemplate;
import PSM.CallGraphTemplate.LocalGraph;
import PSM.CallGraphTemplate.Vertex;
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
    public static String sourcePath = System.getProperty("user.dir")+"/source.txt";
    public static String sinkPath = System.getProperty("user.dir")+"/sink.txt";
    public static String bomPath = System.getProperty("user.dir")+"/basic/BOM.txt";
    public static String bimPath = System.getProperty("user.dir")+"/basic/BIM.txt";
    public static String eomPath = System.getProperty("user.dir")+"/basic/EOM.txt";
    public static String eimPath = System.getProperty("user.dir")+"/basic/EIM.txt";
    public Set<String> EOM = new HashSet<String>();
    public Set<String> EIM = new HashSet<String>();
    public Set<String> BOM = new HashSet<String>();
    public Set<String> BIM = new HashSet<String>();
    public Set<String> source = new HashSet<String>();
    public Set<String> sink = new HashSet<String>();
    // these might change
	public String apkFilePath = System.getProperty("user.dir")+"/examples/kik.apk";
	public String sourceSinkFilePath = System.getProperty("user.dir")+"/sourcesandsinks.txt";
    public Set<Method> methods = new HashSet<Method>();
    public String testCp;
    public Set<SootMethod> basicSource = new HashSet<SootMethod>();
    public Set<SootMethod> basicSink = new HashSet<SootMethod>();


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
	
	public Set<String> loadSource() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(sourcePath)); 
		Set<String> source = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			source.add(line); 
			line = bufReader.readLine(); 
			}
		bufReader.close();
		//System.out.println("Source is loaded with "+source.size()+" methods.");
		return source;
	}
	
	public Set<String> loadSink() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(sinkPath)); 
		Set<String> sink = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			sink.add(line); 
			line = bufReader.readLine(); 
			} 
		bufReader.close();
		//System.out.println("Sink is loaded with "+sink.size()+" methods.");
		return sink;
	}
	
	 /**
     * Traverse a graph in depth-first order and print the vertices.
     *
     * @param hrefGraph a graph based on URI objects
     *
     * @param start the vertex where the traversal should start
     */
	public static void traverseHrefGraph(Graph<String, DefaultEdge> hrefGraph, String start)
    {
		int count = 0;
        Iterator<String> iterator = new DepthFirstIterator<>(hrefGraph, start);
        System.out.println("Starting from source: ");
        while (iterator.hasNext()) {
        	String method = iterator.next();
            System.out.println(count+": "+method);
            count++;
        }
        System.out.println("Flows from source is finished.");
    }

    /**
     * Render a graph in DOT format.
     *
     * @param hrefGraph a graph based on URI objects
     */
    public static void renderHrefGraph(Graph<String, DefaultEdge> hrefGraph)
        throws ExportException
    {

        DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(hrefGraph, writer);
        System.out.println(writer.toString());
    }
	
	public void findFlow() throws Exception {
		source = loadSource();
		sink = loadSink();
	    String targetPath = System.getProperty("user.dir")+"/examples/fuseki-server_0.jar";
		String libPath = System.getProperty("user.dir")+"/lib/mysql-connector-java-8.0.28.jar";
		
		Options.v().set_output_dir(System.getProperty("user.dir")+"/sootOutput");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_whole_program(true);
	    Options.v().set_include_all(true);
	    PackManager.v().writeOutput();
	    
	    AbstractInfoflow infoflow = new Infoflow();
		infoflow.getConfig().setEnableLineNumbers(true);
		//infoflow.getConfig().setIncrementalResultReporting(true);
		infoflow.getConfig().setInspectSinks(true);
		infoflow.getConfig().setInspectSources(true);
		infoflow.getConfig().getAccessPathConfiguration().setAccessPathLength(10);
		infoflow.getConfig().setLogSourcesAndSinks(true);
		infoflow.getConfig().setWriteOutputFiles(true);
		infoflow.setTaintWrapper(new SummaryTaintWrapper(new LazySummaryProvider("summariesManual")));

		Collection<String> epoints = new ArrayList<String>();
		epoints.add("<org.apache.jena.fuseki.FusekiCmd: void main(java.lang.String[])>");
		
		DefaultEntryPointCreator entryPoints = new DefaultEntryPointCreator(epoints);
		
		System.out.println("Now finding flows between sources and sinks: ");

		ISourceSinkManager sourceSinkMgr = new ISourceSinkManager() {

			@Override
			public SourceInfo getSourceInfo(Stmt sCallSite, InfoflowManager manager) {
				if (sCallSite.containsInvokeExpr() && (sCallSite instanceof AssignStmt || sCallSite instanceof DefinitionStmt)) {
					for (SootMethod s : basicSource) {
						if (s.getName().equals(sCallSite.getInvokeExpr().getMethod().getName())){
					if (sCallSite instanceof AssignStmt) { AccessPath ap = manager.getAccessPathFactory().createAccessPath(((AssignStmt) sCallSite).getLeftOp(), true); return new SourceInfo(null, ap);}
					if (sCallSite instanceof DefinitionStmt) { AccessPath ap = manager.getAccessPathFactory().createAccessPath(((DefinitionStmt) sCallSite).getLeftOp(), true); return new SourceInfo(null, ap);}}
					}
				}
				return null;
			}

			@Override
			public SinkInfo getSinkInfo(Stmt sCallSite, InfoflowManager manager, AccessPath ap) {
				if (!sCallSite.containsInvokeExpr())
					return null;

				SootMethod target = sCallSite.getInvokeExpr().getMethod();
				SinkInfo targetInfo = new SinkInfo((ISourceSinkDefinition) new MethodSourceSinkDefinition(new SootMethodAndClass(target)));
				
				//if(target.getName().toString().toLowerCase().contains("print")) {
				for (SootMethod s : basicSink) {
				if (s.getName().equals(target.getName())
						&& !target.getReturnType().toString().toLowerCase().contains("bool")
						&& !target.getName().toLowerCase().contains("append")
						&& !target.getName().toLowerCase().contains("tostring")
						&& !target.getName().toLowerCase().contains("error")
						&& !target.getName().toLowerCase().contains("init")
						&& !target.getName().toLowerCase().contains("bug")
						&& !target.getName().toLowerCase().contains("abort")
						&& !target.getName().toLowerCase().contains("read")
						&& !target.getName().toLowerCase().contains("main")){
					return targetInfo;
				}}
				return null;
			}

			@Override
			public void initialize() {
				// TODO Auto-generated method stub
				
			}
		};

		infoflow.computeInfoflow(targetPath, libPath, entryPoints, sourceSinkMgr);
		//infoflow.computeInfoflow(targetPath, libPath, entryPoints, source, sink);
		System.out.println("*****");
		Writer flowResult = new FileWriter(System.getProperty("user.dir")+"/flows.txt");
		infoflow.getResults().printResults(flowResult);
		System.out.println("Find "+infoflow.getResults().size()+" flows with "+infoflow.getResults().numConnections()+" connections.\n");
		
		PrintWriter flowsource = new PrintWriter("flowFrom.txt", "UTF-8");
    	for (Stmt m : infoflow.getCollectedSources()) {
    		//bsWriter.println(m+" -> _SOURCE_");
    		flowsource.println(m);
    	}
    	//bsWriter.println("Finished.");
    	flowsource.close();
    	
    	PrintWriter flowsink = new PrintWriter("flowEnd.txt", "UTF-8");
    	for (Stmt m : infoflow.getCollectedSinks()) {
    		//bkWriter.println(m+" -> _SINK_");
    		flowsink.println(m);
    	}
    	//bkWriter.println("Finished.");
    	flowsink.close();
    	
    	System.out.println("Flow start and end points printed.\n");

	}
	/*
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

	}*/
	

	Set<SootMethod> depthFirstTraversal(LocalGraph graph, SootMethod root) {
	    Set<SootMethod> visited = new LinkedHashSet<SootMethod>();
	    Stack<SootMethod> stack = new Stack<SootMethod>();
	    stack.push(root);
	    while (!stack.isEmpty()) {
	    	SootMethod vertex = stack.pop();
	        if (!visited.contains(vertex)) {
	            visited.add(vertex);
	            for (Vertex v : graph.getAdjVertices(vertex)) {              
	                stack.push(v.label);
	            }
	        }
	    }
	    return visited;
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
	
	public Set<String> loadEOM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(eomPath)); 
		Set<String> EOM = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			EOM.add(line); 
			line = bufReader.readLine(); 
			} 
		bufReader.close();
		System.out.println("EOM is loaded with "+EOM.size()+" methods.");
		return EOM;
	}
	
	public Set<String> loadEIM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(eimPath)); 
		Set<String> EIM = new HashSet<String>(); 
		String line = bufReader.readLine(); 
		while (line != null) { 
			EIM.add(line); 
			line = bufReader.readLine(); 
			} 
		bufReader.close();
		System.out.println("EIM is loaded with "+EIM.size()+" methods.");
		return EIM;
	}
	
	static void setSparkPointsToAnalysis() {
		System.out.println("[spark] Starting analysis ...");
				
		HashMap opt = new HashMap();
		
		opt.put("enabled","true");
		opt.put("verbose","true");
		opt.put("ignore-types","false");          
		opt.put("force-gc","false");            
		opt.put("pre-jimplify","false");          
		opt.put("vta","false");                   
		opt.put("rta","false");                   
		opt.put("field-based","true");           
		opt.put("types-for-sites","false");        
		opt.put("merge-stringbuffer","true");   
		opt.put("string-constants","true");     
		opt.put("simulate-natives","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            
		opt.put("simplify-offline","false");    
		opt.put("simplify-sccs","false");        
		opt.put("ignore-types-for-sccs","false");
		opt.put("propagator","worklist");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid");
		opt.put("dump-html","false");           
		opt.put("dump-pag","false");             
		opt.put("dump-solution","false");        
		opt.put("topo-sort","false");           
		opt.put("dump-types","true");             
		opt.put("class-method-var","true");     
		opt.put("dump-answer","false");          
		opt.put("add-tags","false");             
		opt.put("set-mass","false"); 
		
		SparkTransformer.v().transform("",opt);
		
		System.out.println("[spark] Done!");
	}
	
	
	public void findAllEdgesInto(SootMethod m, CallGraph cg) {
		for(Iterator<soot.jimple.toolkits.callgraph.Edge> it = cg.edgesOutOf(m); it.hasNext(); ){
	            soot.jimple.toolkits.callgraph.Edge edge = it.next();
	            System.out.println(String.format("Method '%s' invokes method '%s' through stmt '%s", edge.src(), edge.tgt(), edge.srcUnit()));
	        }
	}
	
	private void loadMethodsFromTestLib(final Set<String> testClasses) throws Exception {
		
		Map<Value, SootMethod> map = new LinkedHashMap<Value, SootMethod>();
		Map<Value, SootMethod> sinkMap = new LinkedHashMap<Value, SootMethod>();
	    int methodCount = methods.size();
	    BOM = loadBOM();
	    BIM = loadBIM();
	    EOM = loadEOM();
	    EIM = loadEIM();
	    
	    new AbstractSootFeature(testCp) {


	      @Override
	      public Type appliesInternal(Method method) throws Exception {
	    	  System.out.println("Start looking for sources and sinks: ");
	    	  setSparkPointsToAnalysis();
	    	  
	    	  

	        for (String className : testClasses) {
	        	
	          SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
	          sc.setApplicationClass();

	          //int numOfEdges = 0;
	          Multimap<SootMethod, SootMethod> call = ArrayListMultimap.create();
	          //Map<SootMethod, SootMethod> call = new LinkedHashMap<SootMethod, SootMethod>();
	    	  CallGraph callGraph = Scene.v().getCallGraph();
	    	  //PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
	    	  
	    	  Set<SootMethod> startSet = new HashSet<SootMethod>();
	    	  Set<Value> valueSet = new HashSet<Value>();
	    	  //Map<Integer, Local> valueMap = new LinkedHashMap<Integer, Local>();
	    	  boolean hasSource = false;
	    	  

	          for (SootMethod m : sc.getMethods()) {
	        	 
	  	        if (m.isConcrete() && !m.getName().toLowerCase().contains("init")
	  	        		&& !m.getName().toLowerCase().contains("main")) {

	  	        	if (EOM.contains(m.toString()) || BOM.contains(m.toString())) {
	  	        		basicSource.add(m);
	  	        		hasSource = true;
	  	        	}
	  	        	
	  	        	for (Unit u : m.retrieveActiveBody().getUnits()) {
	        			  Value value = null;

	        			  SootMethod methodBOM = null;
	        			  SootMethod first = null;
	        			  if (u instanceof AssignStmt) {
	        				  if (((AssignStmt) u).containsInvokeExpr()) {
	        					  //int lineo = getLineNumber((Stmt)u);
	        					  InvokeExpr invokeSource = ((AssignStmt) u).getInvokeExpr();
	        					  //System.out.println(invokeSource.getMethod());
	        					  for (String s : BOM) {
	        				  		if ((s.equals(invokeSource.getMethod().toString()))) {
	        					  //if(invokeSource.getMethod().getName().toLowerCase().contains("next")) {
	        				  			methodBOM = invokeSource.getMethod();
	        				  			value = ((AssignStmt) u).getLeftOp();	
	        				  			//System.out.println("value: "+value+" invocation: "+invokeSource+" "+u);
	        				  			map.put(value,methodBOM);
	        				  			basicSource.add(m);
	        				  			basicSource.add(methodBOM);
	        				  			valueSet.add(value);
	        				  			hasSource=true;
	        				  			}
	        				  		}
	        				  }
	        			  }
	        			  /*
	        			  if (u instanceof ReturnStmt) {
	        				  ReturnStmt stmt = (ReturnStmt) u;
	        				  if (map.containsKey(stmt.getOp())) {
	        					  Value v = stmt.getOp();
	        					  basicSource.add(map.get(v)); //basicSource.add(m);
	        					  hasSource = true;
	        					  }
	        				  }*/
	        			  }
	  	        	}
	  	        }
	          //System.out.println("Finished scanning for class: "+sc);
	          
	          if(hasSource == true) {
	        	  System.out.println("Now we build call graphs for class: "+sc);
	        	  LocalGraph localGraph = new LocalGraph();
	        	  Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
	        	  
	        	  for (SootMethod sm : sc.getMethods()) {
	        		  if(!sm.getName().contains("init")) {
	        			  if(basicSource.contains(sm)) System.out.println("Source here: "+sm);
	        			  Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
	        			  Iterator<soot.jimple.toolkits.callgraph.Edge> edgesIn = callGraph.edgesInto(sm);
	        			  while (edgesOut.hasNext()) {
	        				  soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next(); 
	        				  //System.out.println(edgesOut);
	        				  if(!edgeOut.src().getName().contains("init") && !edgeOut.tgt().getName().contains("init")) {
	        					  //System.out.println("Out from: " +sm+" edge: "+edgeOut);
	        					  //call.put(edgeOut.tgt(), edgeOut.src());
	        					  //localGraph.addVertex(edgeOut.src());
	        					  //localGraph.addVertex(edgeOut.tgt());
	        					  //localGraph.addEdge(edgeOut.tgt(), edgeOut.src());
	        					  g.addVertex(edgeOut.src().getName());
	        					  g.addVertex(edgeOut.tgt().getName());
	        					  g.addEdge(edgeOut.tgt().getName(), edgeOut.src().getName());
	        					  if((edgeOut.src().getDeclaringClass() != edgeOut.tgt().getDeclaringClass()) && !edgeOut.tgt().getDeclaringClass().toString().contains("java")) {
	        						  System.out.println("Global flow here: "+edgeOut.src()+" calls: "+edgeOut.tgt()+" via: "+edgeOut);
	        					  }
	        					  //System.out.println(edge.src()+" calls: "+edge.tgt()+" via: "+edge);
	        					  }
	        				  }
	        			  while (edgesIn.hasNext()) {
	        				  soot.jimple.toolkits.callgraph.Edge edgeIn = edgesIn.next(); 
	        				  if(!edgeIn.src().getName().contains("init") && !edgeIn.tgt().getName().contains("init")) {
	        				  //System.out.println("In to: " +sm+" edge: "+edgeIn);
	        				  //call.put(edgeIn.tgt(), edgeIn.src());
	        					  //localGraph.addVertex(edgeIn.src());
	        					  //localGraph.addVertex(edgeIn.tgt());
	        					  //localGraph.addEdge(edgeIn.tgt(), edgeIn.src());
	        					  g.addVertex(edgeIn.src().getName());
	        					  g.addVertex(edgeIn.tgt().getName());
	        					  g.addEdge(edgeIn.tgt().getName(), edgeIn.src().getName());
	        				  }
	        			  }
	        		  }
	        	  }
	        	  if(!g.edgeSet().isEmpty()) {
	        	  for (SootMethod source : basicSource) {
	        		  Boolean start = g.vertexSet().contains(source.getName());
	        		  if(start) {
	        			  System.out.println("Start traversal for source: "+source+"...");
	        			  traverseHrefGraph(g, source.getName());
	        			  }
	        		  }
	        	  //System.out.println("Callgraph for class: "+sc);
	        	  //renderHrefGraph(g);
	        	  }
	        	  } 
	          hasSource = false;
	          }
	        
	        /*
	        for (String className : testClasses) {
	        	if(className.contains("ABServer")) {
	        		SootClass mainClass = Scene.v().forceResolve(className, SootClass.BODIES);
	        		mainClass.setApplicationClass();
	        		
	        		CallGraph callGraph = Scene.v().getCallGraph();
	        		
	        		System.out.println("Now we build call graphs for class: "+mainClass);
		        	  
		        	  for (SootMethod sm : mainClass.getMethods()) {
		        		  //if(sm.isConcrete()) {
		        		  System.out.println(sm+"\n");
		        			  Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
		        			  Iterator<soot.jimple.toolkits.callgraph.Edge> edgesIn = callGraph.edgesInto(sm);
		        			  while (edgesOut.hasNext()) {
		        				  soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next(); 
		        				  //if(!edge.srcStmt().equals(null) && edge.srcStmt().containsInvokeExpr()) {
		        					  System.out.println(edgeOut);
		        					  //System.out.println(edge.src()+" calls: "+edge.tgt()+" via: "+edge);
		        					  //}
		        				  }
		        			  while (edgesIn.hasNext()) {
		        				  soot.jimple.toolkits.callgraph.Edge edgeIn = edgesIn.next(); 
		        				  System.out.println(edgeIn);
		        			  }
		        		  //}
		        	  }
	        	}
	        }*/

	        for (String className : testClasses) {
		          SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
		          Map<Stmt, SootMethod> graph = new LinkedHashMap<Stmt, SootMethod>();
		          sc.setApplicationClass();

	          for (SootMethod sm : sc.getMethods()) {    
	        	  
	        	  if (sm.isConcrete() && BIM.contains(sm.toString())) {
	        		  //System.out.println(sm);
	        		  basicSink.add(sm);
	        	  }
	        	  
	        	  if (sm.isConcrete() && EIM.contains(sm.toString())) {
	        		  //System.out.println(sm);
	        		  basicSink.add(sm);
	        	  }
	        	  
	        	  if (sm.isConcrete()) {
	        		  
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  for (String s : BIM) {
	        				  if (s.contains(invokeExpr.getMethod().toString()) 
	        						  && !(invokeExpr.getMethod().getName().contains("init"))
	        						  && !(invokeExpr.getMethod().getName().contains("error"))
	        						  && !(invokeExpr.getMethod().getName().contains("bug"))
	        						  && !(invokeExpr.getMethod().getName().contains("abort"))) {
	        					  //System.out.println(sm);
	        					  //sinkMap.put(invokeExpr.getArg(0), invokeExpr.getMethod());
	        					  //basicSink.add(invokeExpr.getMethod());
	        					  //System.out.println("Class: "+sc+" Method: "+sm);
	        					  //System.out.println(sm);
	        					  //System.out.println(invokeExpr.getMethod());
	        					  basicSink.add(sm);
	        					  //basicSink.add(invokeExpr.getMethod());)
	        				  }}
	        			  }
	        		  }
	        	  }
	          }
	          /*
	          for (SootMethod sm : sc.getMethods()) {    
	        	  
	        	  if (sm.isConcrete() && (sm.toString().toLowerCase().contains("url")
						  || sm.getDeclaringClass().toString().toLowerCase().contains("url")
						  || sm.toString().toLowerCase().contains("http")
						  //|| sm.getDeclaringClass().toString().toLowerCase().contains("http")
						  //|| sm.toString().toLowerCase().contains("database")
						  || sm.getDeclaringClass().toString().toLowerCase().contains("http"))
						  && sm.toString().toLowerCase().contains("init")
						  && sm.toString().toLowerCase().contains("exception")) {
	        		  System.out.println(sm);
	        		  basicSink.add(sm);
	        	  }
	        	  
	        	  if (sm.isConcrete()) {
	        		  
	        		  for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        			  if (((Stmt) u).containsInvokeExpr()) {
	        				  InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
	        				  
	        				  if ((invokeExpr.getMethod().toString().toLowerCase().contains("http")
	        						  || invokeExpr.getMethod().getDeclaringClass().toString().toLowerCase().contains("http")
	        						  //|| invokeExpr.getMethod().toString().toLowerCase().contains("query")
	        						  //|| invokeExpr.getMethod().getDeclaringClass().toString().toLowerCase().contains("database")
	        						  || invokeExpr.getMethod().toString().toLowerCase().contains("url")
	        						  || invokeExpr.getMethod().getDeclaringClass().toString().toLowerCase().contains("url"))
	        						  && !invokeExpr.getMethod().toString().toLowerCase().contains("init")
	        						  && !invokeExpr.getMethod().toString().toLowerCase().contains("exception")) {
	        				  //if (BIM.contains(invokeExpr.getMethod().getName().toString())) {
	        					  //sinkMap.put(invokeExpr.getArg(0), invokeExpr.getMethod());
	        					  basicSink.add(invokeExpr.getMethod());
	        					  //System.out.println("Class: "+sc+" Method: "+sm);
	        					  System.out.println(sm);
	        					  System.out.println(invokeExpr.getMethod());
	        					  basicSink.add(sm);
	        					  //basicSink.add(invokeExpr.getMethod());)
	        				  }
	        			  }
	        		  }
	        	  }
	          }*/
	          
	          
	
	          /*
	          for (SootMethod sm : sc.getMethods()) {        	  
	        	  if (sm.isConcrete()) {
	        		  for (Unit u1 : sm.retrieveActiveBody().getUnits()) {
	        			  if (u1 instanceof AssignStmt) {
	        				  if (sinkMap.containsKey(((AssignStmt) u1).getLeftOp())) {
	        					  //System.out.println(((AssignStmt) u1).getLeftOp()+" "+sinkMap.get(((AssignStmt) u1).getLeftOp())+" "+u1);
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
	          }*/


	          
	          for (SootMethod sm : sc.getMethods()) {
	        	  try {
	        		  Map<Value, SootMethod> paramVals = new LinkedHashMap<Value, SootMethod>();
	        		  //Set<Value> paramVals = new HashSet<Value>();
	        		  if (!sm.getName().contains("main") && sm.isConcrete()) {
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
/*
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
	        	          }*/
	        	      }
	        	          
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
/*
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
	        	          }*/
	        	      }
	        	      
	        	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
/*
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
	        	          }*/
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
	    		//bsWriter.println(m+" -> _SOURCE_");
	    		bsWriter.println(m);
	    	}
	    	//bsWriter.println("Finished.");
	    	bsWriter.close();
	    	
	    	PrintWriter bkWriter = new PrintWriter("sink.txt", "UTF-8");
	    	for (SootMethod m : basicSink) {
	    		//bkWriter.println(m+" -> _SINK_");
	    		bkWriter.println(m);
	    	}
	    	//bkWriter.println("Finished.");
	    	bkWriter.close();
	    	/*
	    	PrintWriter baWriter = new PrintWriter("sinkall.txt", "UTF-8");
	    	for (SootMethod m : basicSinkAll) {
	    		//bkWriter.println(m+" -> _SINK_");
	    		baWriter.println(m);
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
	    
	    System.out.println("Loaded " + (methods.size() - methodCount)  + " methods from JAR files. \n");
	    System.out.println("Found " + basicSource.size() + " Source Methods.");
	    System.out.println("Found " + basicSink.size() + " Sink Methods.");
	    System.out.println("Sources and Sinks collected. \n");
	    

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
