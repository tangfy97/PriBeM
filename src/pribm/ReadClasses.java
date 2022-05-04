package pribm;

import java.io.BufferedReader;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import com.google.common.collect.*;

import pribm.CallGraphTemplate.LocalGraph;
import pribm.CallGraphTemplate.Vertex;
import pribm.Features.AbstractSootFeature;
import pribm.Info.Method;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.*;
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
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ReadClasses {

	public static String sourceDirectory = System.getProperty("user.dir");
	public static String jarDirectory = System.getProperty("user.dir") + "/data";
	public static String androidDirPath = System.getProperty("user.dir") + "/lib/android.jar";
	public static String sourcePath = System.getProperty("user.dir") + "/source.txt";
	public static String sinkPath = System.getProperty("user.dir") + "/sink.txt";
	public static String bomPath = System.getProperty("user.dir") + "/basic/BOM.txt";
	public static String bimPath = System.getProperty("user.dir") + "/basic/BIM.txt";
	public static String eomPath = System.getProperty("user.dir") + "/basic/EOM.txt";
	public static String eimPath = System.getProperty("user.dir") + "/basic/EIM.txt";
	public Set<String> EOM = new HashSet<String>();
	public Set<String> EIM = new HashSet<String>();
	public Set<String> BOM = new HashSet<String>();
	public Set<String> BIM = new HashSet<String>();
	public Set<String> source = new HashSet<String>();
	public Set<String> sink = new HashSet<String>();
	public Set<SootClass> visited = new HashSet<SootClass>();
	// these might change
	public String apkFilePath = System.getProperty("user.dir") + "/examples/kik.apk";
	public String sourceSinkFilePath = System.getProperty("user.dir") + "/sourcesandsinks.txt";
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
		// System.out.println("Source is loaded with "+source.size()+" methods.");
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
		// System.out.println("Sink is loaded with "+sink.size()+" methods.");
		return sink;
	}

	/**
	 * Traverse a graph in depth-first order and print the vertices.
	 *
	 * @param hrefGraph a graph based on URI objects
	 *
	 * @param start     the vertex where the traversal should start
	 */

	public void traverseHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph,
			SootMethod start) {
		int count = 0;
		Graph<SootMethod, DefaultEdge> cg = new DefaultDirectedGraph<>(DefaultEdge.class);
		Iterator<SootMethod> iterator = new DepthFirstIterator<>(hrefGraph, start);
		SootMethod callee = null;

		while (iterator.hasNext()) {
			SootMethod caller = iterator.next();
			if (callee != null) {
				cg.addVertex(callee);
				cg.addVertex(caller);
				cg.addEdge(callee, caller);
				System.out.println("<" + count + ": " + callee + " -> " + caller + ">");
				if (basicSource.contains(caller)) {
					System.out.println("The above invocation flows into a source.");
				} else {
					if ((caller.getDeclaringClass() != callee.getDeclaringClass()) &&
							(!caller.getDeclaringClass().toString().toLowerCase().contains("java"))) {
						System.out.println("Global flow detected: " + callee + " -> " + caller + "\n");
						System.out.println("Adding connections to callgraphs in class: " + caller.getDeclaringClass());
						if (!visited.contains(caller.getDeclaringClass())) {
							visited.add(caller.getDeclaringClass());
							traverseHrefGraphIntern(getCG(caller.getDeclaringClass()), caller);
						} else {
							System.out.println(caller.getDeclaringClass() + " has been visited already.");
						}
					}
				}
			}
			if (callee == null) {
				System.out.println("Starting from method: " + caller);
			}
			count++;
			callee = caller;
		}

		System.out.println("Flows from " + start + " is finished.");
		System.out.println("/////////////////////////////////////");
		System.out.println("\n");
		renderHrefGraph(cg);
	}

	public void traverseHrefGraphIntern(Graph<SootMethod, DefaultEdge> hrefGraph,
			SootMethod start) {
		// int count = 0;
		Graph<SootMethod, DefaultEdge> cg = new DefaultDirectedGraph<>(DefaultEdge.class);
		Iterator<SootMethod> iterator = new DepthFirstIterator<>(hrefGraph, start);
		SootMethod callee = null;

		while (iterator.hasNext()) {
			SootMethod caller = iterator.next();
			if (callee != null) {
				cg.addVertex(callee);
				cg.addVertex(caller);
				cg.addEdge(callee, caller);
				// System.out.println(count + ": " + callee + " -> " + caller);
			}
			if (callee == null) {
				System.out.println("Continue with method: " + caller);
			}
			// count++;
			callee = caller;
		}

		// System.out.println("Flows from "+start+" is finished.");
		System.out.println("\n");
		renderHrefGraph(cg);
	}

	/**
	 * Render a graph in DOT format.
	 *
	 * @param hrefGraph a graph based on URI objects
	 */
	public void renderHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph)
			throws ExportException {

		DOTExporter<SootMethod, DefaultEdge> exporter = new DOTExporter<>();
		exporter.setVertexAttributeProvider((v) -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", DefaultAttribute.createAttribute(v.toString()));
			return map;
		});
		Writer writer = new StringWriter();
		exporter.exportGraph(hrefGraph, writer);
		System.out.println(writer.toString());
	}
	/*
	 * public void findFlow() throws Exception {
	 * source = loadSource();
	 * sink = loadSink();
	 * String targetPath = System.getProperty("user.dir") +
	 * "/examples/fuseki-server_0.jar";
	 * String libPath = System.getProperty("user.dir") +
	 * "/lib/mysql-connector-java-8.0.28.jar";
	 * 
	 * Options.v().set_output_dir(System.getProperty("user.dir") + "/sootOutput");
	 * Options.v().set_output_format(Options.output_format_jimple);
	 * Options.v().set_whole_program(true);
	 * Options.v().set_include_all(true);
	 * PackManager.v().writeOutput();
	 * 
	 * AbstractInfoflow infoflow = new Infoflow();
	 * infoflow.getConfig().setEnableLineNumbers(true);
	 * // infoflow.getConfig().setIncrementalResultReporting(true);
	 * infoflow.getConfig().setInspectSinks(true);
	 * infoflow.getConfig().setInspectSources(true);
	 * infoflow.getConfig().getAccessPathConfiguration().setAccessPathLength(10);
	 * infoflow.getConfig().setLogSourcesAndSinks(true);
	 * infoflow.getConfig().setWriteOutputFiles(true);
	 * infoflow.setTaintWrapper(new SummaryTaintWrapper(new
	 * LazySummaryProvider("summariesManual")));
	 * 
	 * Collection<String> epoints = new ArrayList<String>();
	 * epoints.
	 * add("<org.apache.jena.fuseki.FusekiCmd: void main(java.lang.String[])>");
	 * 
	 * DefaultEntryPointCreator entryPoints = new DefaultEntryPointCreator(epoints);
	 * 
	 * System.out.println("Now finding flows between sources and sinks: ");
	 * 
	 * ISourceSinkManager sourceSinkMgr = new ISourceSinkManager() {
	 * 
	 * @Override
	 * public SourceInfo getSourceInfo(Stmt sCallSite, InfoflowManager manager) {
	 * if (sCallSite.containsInvokeExpr()
	 * && (sCallSite instanceof AssignStmt || sCallSite instanceof DefinitionStmt))
	 * {
	 * for (SootMethod s : basicSource) {
	 * if (s.getName().equals(sCallSite.getInvokeExpr().getMethod().getName())) {
	 * if (sCallSite instanceof AssignStmt) {
	 * AccessPath ap = manager.getAccessPathFactory()
	 * .createAccessPath(((AssignStmt) sCallSite).getLeftOp(), true);
	 * return new SourceInfo(null, ap);
	 * }
	 * if (sCallSite instanceof DefinitionStmt) {
	 * AccessPath ap = manager.getAccessPathFactory()
	 * .createAccessPath(((DefinitionStmt) sCallSite).getLeftOp(), true);
	 * return new SourceInfo(null, ap);
	 * }
	 * }
	 * }
	 * }
	 * return null;
	 * }
	 * 
	 * @Override
	 * public SinkInfo getSinkInfo(Stmt sCallSite, InfoflowManager manager,
	 * AccessPath ap) {
	 * if (!sCallSite.containsInvokeExpr())
	 * return null;
	 * 
	 * SootMethod target = sCallSite.getInvokeExpr().getMethod();
	 * SinkInfo targetInfo = new SinkInfo(
	 * (ISourceSinkDefinition) new MethodSourceSinkDefinition(new
	 * SootMethodAndClass(target)));
	 * 
	 * // if(target.getName().toString().toLowerCase().contains("print")) {
	 * for (SootMethod s : basicSink) {
	 * if (s.getName().equals(target.getName())
	 * && !target.getReturnType().toString().toLowerCase().contains("bool")
	 * && !target.getName().toLowerCase().contains("append")
	 * && !target.getName().toLowerCase().contains("tostring")
	 * && !target.getName().toLowerCase().contains("error")
	 * && !target.getName().toLowerCase().contains("init")
	 * && !target.getName().toLowerCase().contains("bug")
	 * && !target.getName().toLowerCase().contains("abort")
	 * && !target.getName().toLowerCase().contains("read")
	 * && !target.getName().toLowerCase().contains("main")) {
	 * return targetInfo;
	 * }
	 * }
	 * return null;
	 * }
	 * 
	 * @Override
	 * public void initialize() {
	 * // TODO Auto-generated method stub
	 * 
	 * }
	 * };
	 * 
	 * infoflow.computeInfoflow(targetPath, libPath, entryPoints, sourceSinkMgr);
	 * // infoflow.computeInfoflow(targetPath, libPath, entryPoints, source, sink);
	 * System.out.println("*****");
	 * Writer flowResult = new FileWriter(System.getProperty("user.dir") +
	 * "/flows.txt");
	 * infoflow.getResults().printResults(flowResult);
	 * System.out.println("Find " + infoflow.getResults().size() + " flows with "
	 * + infoflow.getResults().numConnections() + " connections.\n");
	 * 
	 * PrintWriter flowsource = new PrintWriter("flowFrom.txt", "UTF-8");
	 * for (Stmt m : infoflow.getCollectedSources()) {
	 * // bsWriter.println(m+" -> _SOURCE_");
	 * flowsource.println(m);
	 * }
	 * // bsWriter.println("Finished.");
	 * flowsource.close();
	 * 
	 * PrintWriter flowsink = new PrintWriter("flowEnd.txt", "UTF-8");
	 * for (Stmt m : infoflow.getCollectedSinks()) {
	 * // bkWriter.println(m+" -> _SINK_");
	 * flowsink.println(m);
	 * }
	 * // bkWriter.println("Finished.");
	 * flowsink.close();
	 * 
	 * System.out.println("Flow start and end points printed.\n");
	 * 
	 * }
	 */

	public Set<String> loadBOM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(bomPath));
		Set<String> BOM = new HashSet<String>();
		String line = bufReader.readLine();
		while (line != null) {
			BOM.add(line);
			line = bufReader.readLine();
		}
		bufReader.close();
		System.out.println("Basic source methods are loaded with " + BOM.size() + " methods.");
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
		System.out.println("Basic sink methods are loaded with " + BIM.size() + " methods.");
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
		System.out.println("External source methods are loaded with " + EOM.size() + " methods.");
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
		System.out.println("External sink methods are loaded with " + EIM.size() + " methods.");
		return EIM;
	}

	static void setSparkPointsToAnalysis() {
		System.out.println("[spark] Starting analysis ...");

		HashMap opt = new HashMap();

		opt.put("enabled", "true");
		opt.put("verbose", "true");
		opt.put("ignore-types", "false");
		opt.put("force-gc", "false");
		opt.put("pre-jimplify", "false");
		opt.put("vta", "false");
		opt.put("rta", "false");
		opt.put("field-based", "true");
		opt.put("types-for-sites", "false");
		opt.put("merge-stringbuffer", "true");
		opt.put("string-constants", "true");
		opt.put("simulate-natives", "true");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("simplify-offline", "false");
		opt.put("simplify-sccs", "false");
		opt.put("ignore-types-for-sccs", "false");
		opt.put("propagator", "worklist");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("dump-html", "false");
		opt.put("dump-pag", "false");
		opt.put("dump-solution", "false");
		opt.put("topo-sort", "false");
		opt.put("dump-types", "true");
		opt.put("class-method-var", "true");
		opt.put("dump-answer", "false");
		opt.put("add-tags", "false");
		opt.put("set-mass", "false");

		SparkTransformer.v().transform("", opt);

		System.out.println("[spark] Done! \n\n");
	}

	public Graph<SootMethod, DefaultEdge> getCG(SootClass sc) {
		sc.setApplicationClass();
		CallGraph callGraph = Scene.v().getCallGraph();

		System.out.println("\n");
		System.out.println("***************************");
		System.out.println("Now we build call graphs for class: " + sc);
		Graph<SootMethod, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (SootMethod sm : sc.getMethods()) {
			if (!sm.getName().contains("init")) {

				Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
				Iterator<soot.jimple.toolkits.callgraph.Edge> edgesInto = callGraph.edgesInto(sm);

				while (edgesOut.hasNext()) {
					soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next();
					// System.out.println(edgesOut);
					if (!edgeOut.src().getName().contains("init")
							&& !edgeOut.tgt().getName().contains("init")
							&& !edgeOut.src().getDeclaringClass().getName().contains("error")
							&& !edgeOut.tgt().getDeclaringClass().getName().contains("error")) {
						// we add edges and vertex
						g.addVertex(edgeOut.src());
						g.addVertex(edgeOut.tgt());
						g.addEdge(edgeOut.tgt(), edgeOut.src());
					}
				}
				while (edgesInto.hasNext()) {
					soot.jimple.toolkits.callgraph.Edge edgeIn = edgesInto.next();
					if (!edgeIn.src().getName().contains("init")
							&& !edgeIn.tgt().getName().contains("init")
							&& !edgeIn.src().getDeclaringClass().getName().contains("error")
							&& !edgeIn.tgt().getDeclaringClass().getName().contains("error")) {
						g.addVertex(edgeIn.src());
						g.addVertex(edgeIn.tgt());
						g.addEdge(edgeIn.tgt(), edgeIn.src());
					}
				}
			}
		}
		return g;

	}

	private void loadMethodsFromTestLib(final Set<String> testClasses) throws Exception {

		Map<Value, SootMethod> map = new LinkedHashMap<Value, SootMethod>();
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
					CallGraph callGraph = Scene.v().getCallGraph();
					Set<Value> valueSet = new HashSet<Value>();
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

								if (u instanceof AssignStmt) {
									if (((AssignStmt) u).containsInvokeExpr()) {
										InvokeExpr invokeSource = ((AssignStmt) u).getInvokeExpr();
										for (String s : BOM) {
											if ((s.equals(invokeSource.getMethod().toString()))) {
												 //if(invokeSource.getMethod().getName().toLowerCase().contains("next")) {
												methodBOM = invokeSource.getMethod();
												value = ((AssignStmt) u).getLeftOp();
												map.put(value, methodBOM);
												basicSource.add(m);
												basicSource.add(methodBOM);
												valueSet.add(value);
												hasSource = true;
											}
										}
									}
								}
								/*
								 * if (u instanceof ReturnStmt) {
								 * ReturnStmt stmt = (ReturnStmt) u;
								 * if (map.containsKey(stmt.getOp())) {
								 * Value v = stmt.getOp();
								 * basicSource.add(map.get(v)); //basicSource.add(m);
								 * hasSource = true;
								 * }
								 * }
								 */
							}
						}
					}

					if (hasSource == true) {
						// System.out.println("\n");
						// System.out.println("***************************");
						System.out.println("Start inspections for class: " + sc);
						Graph<SootMethod, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

						for (SootMethod sm : sc.getMethods()) {
							if (!sm.getName().contains("init")) {

								Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
								Iterator<soot.jimple.toolkits.callgraph.Edge> edgesInto = callGraph.edgesInto(sm);

								while (edgesOut.hasNext()) {
									soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next();
									// System.out.println(edgesOut);
									if (!edgeOut.src().getName().contains("init")
											&& !edgeOut.tgt().getName().contains("init")
											&& !edgeOut.src().getDeclaringClass().getName().contains("error")
											&& !edgeOut.tgt().getDeclaringClass().getName().contains("error")) {
										// we add edges and vertex
										g.addVertex(edgeOut.src());
										g.addVertex(edgeOut.tgt());
										g.addEdge(edgeOut.tgt(), edgeOut.src());
									}
								}
								while (edgesInto.hasNext()) {
									soot.jimple.toolkits.callgraph.Edge edgeIn = edgesInto.next();
									if (!edgeIn.src().getName().contains("init")
											&& !edgeIn.tgt().getName().contains("init")
											&& !edgeIn.src().getDeclaringClass().getName().contains("error")
											&& !edgeIn.tgt().getDeclaringClass().getName().contains("error")) {
										g.addVertex(edgeIn.src());
										g.addVertex(edgeIn.tgt());
										g.addEdge(edgeIn.tgt(), edgeIn.src());
									}
								}
							}
						}
						if (!g.edgeSet().isEmpty()) {
							for (SootMethod source : basicSource) {
								Boolean start = g.vertexSet().contains(source);
								if (start) {
									System.out.println("Source found in the callgraph: " + source + "...");
									System.out.println("Start traversal: " + "\n");
									traverseHrefGraph(g, source);
								}
							}
						}
					}
					hasSource = false;
				}

				for (String className : testClasses) {
					SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
					sc.setApplicationClass();

					for (SootMethod sm : sc.getMethods()) {

						if (sm.isConcrete() && BIM.contains(sm.toString())) {
							basicSink.add(sm);
						}

						if (sm.isConcrete() && EIM.contains(sm.toString())) {
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
											basicSink.add(sm);
										}
									}
								}
							}
						}
					}

					for (SootMethod sm : sc.getMethods()) {
						try {
							Map<Value, SootMethod> paramVals = new LinkedHashMap<Value, SootMethod>();
							// Set<Value> paramVals = new HashSet<Value>();
							if (!sm.getName().contains("main") && sm.isConcrete()) {
								for (Unit u : sm.retrieveActiveBody().getUnits()) {
									/*
									 * if (((Stmt) u).containsInvokeExpr()) {
									 * InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
									 * Value leftOp = null;
									 * //if (u instanceof AssignStmt) leftOp = ((AssignStmt) u).getLeftOp();
									 * //if (leftOp != null) paramVals.add(leftOp);
									 * //if (basicSource.contains(invokeExpr.getMethod())) {
									 * if (basicSource.contains(sm) || basicSource.contains(invokeExpr.getMethod()))
									 * {
									 * for (int i = 0; i < invokeExpr.getArgCount(); i++) {
									 * paramVals.put(invokeExpr.getArg(i),invokeExpr.getMethod());
									 * }
									 * }
									 * }
									 */
								}

								for (Unit u : sm.retrieveActiveBody().getUnits()) {
									/*
									 * if (u instanceof AssignStmt) {
									 * 
									 * Value leftOp = ((AssignStmt) u).getLeftOp();
									 * Value rightOp = ((AssignStmt) u).getRightOp();
									 * 
									 * //if (!((AssignStmt) u).containsInvokeExpr()) {
									 * if (paramVals.containsKey(leftOp)) paramVals.remove(leftOp);
									 * if (paramVals.containsKey(rightOp)) {
									 * paramVals.put(leftOp, sm);
									 * if ((((AssignStmt) u).containsFieldRef())) {
									 * //System.out.println("Value: "+rightOp+
									 * " from BOM: "+paramVals.get(rightOp)+"flows to a field: "+((AssignStmt)
									 * u).getFieldRef());
									 * //flow2FieldM.add(sm);
									 * //flow2FieldC.add(sc);
									 * }
									 * }
									 * }
									 */
								}

								for (Unit u : sm.retrieveActiveBody().getUnits()) {
									/*
									 * if (u instanceof ReturnStmt) {
									 * ReturnStmt stmt = (ReturnStmt) u;
									 * //if (paramVals.contains(stmt.getOp())) {
									 * if (paramVals.containsKey(stmt.getOp())
									 * && !stmt.getOp().getType().toString().toLowerCase().contains("bool")
									 * && !stmt.getOp().getType().toString().toLowerCase().contains("int")
									 * && !stmt.getOp().getType().toString().toLowerCase().contains("void")
									 * ) {
									 * //System.out.println("Value: "+stmt.getOp()+
									 * " from BOM: "+paramVals.get(stmt.getOp())+"flows to a field: "+stmt.getOp().
									 * getType());
									 * flow2Return.add(sm);
									 * }
									 * }
									 */
								}

								/*
								 * for (Unit u : sm.retrieveActiveBody().getUnits()) {
								 * 
								 * if (((Stmt) u).containsInvokeExpr()) {
								 * InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
								 * //if (basicSink.contains(invokeExpr.getMethod())) {
								 * if (basicSink.contains(sm) || basicSink.contains(invokeExpr.getMethod())) {
								 * //if (invokeExpr.getMethod().getName().toLowerCase().contains("print")) {
								 * for (Value arg : invokeExpr.getArgs())
								 * if (paramVals.contains(arg)) flow2Sink.add(sm);
								 * }
								 * }
								 * }
								 */

							}
						} catch (Exception ex) {
							System.err.println("Something went wrong:");
							ex.printStackTrace();
						}

						String sig = sm.getSignature();
						sig = sig.substring(sig.indexOf(": ") + 2, sig.length());

						String returnType = sig.substring(0, sig.indexOf(" "));

						String methodName = sig.substring(sig.indexOf(" ") + 1, sig.indexOf("("));

						List<String> parameters = new ArrayList<String>();
						for (String parameter : sig
								.substring(sig.indexOf("(") + 1, sig.indexOf(")"))
								.split(",")) {
							if (!parameter.trim().isEmpty())
								parameters.add(parameter.trim());
						}

						Method newMethod = new Method(methodName, parameters, returnType, className);

						methods.add(newMethod);
						// }
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

		// List<String> distinctBasicSource =
		// basicSource.stream().distinct().collect(Collectors.toList());
		// System.out.println(distinctBasicSource);

		try {

			PrintWriter bsWriter = new PrintWriter("source.txt", "UTF-8");
			for (SootMethod m : basicSource) {
				// bsWriter.println(m+" -> _SOURCE_");
				bsWriter.println(m);
			}
			// bsWriter.println("Finished.");
			bsWriter.close();

			PrintWriter bkWriter = new PrintWriter("sink.txt", "UTF-8");
			for (SootMethod m : basicSink) {
				// bkWriter.println(m+" -> _SINK_");
				bkWriter.println(m);
			}
			// bkWriter.println("Finished.");
			bkWriter.close();
			/*
			 * PrintWriter baWriter = new PrintWriter("sinkall.txt", "UTF-8");
			 * for (SootMethod m : basicSinkAll) {
			 * //bkWriter.println(m+" -> _SINK_");
			 * baWriter.println(m);
			 * }
			 * //bkWriter.println("Finished.");
			 * bkWriter.close();
			 * 
			 * PrintWriter frWriter = new PrintWriter("F2R.txt", "UTF-8");
			 * for (SootMethod m : flow2Return) {
			 * frWriter.println(m+" -> _SOURCE_");
			 * }
			 * //frWriter.println("Finished.");
			 * frWriter.close();
			 * 
			 * PrintWriter fsWriter = new PrintWriter("F2S.txt", "UTF-8");
			 * for (SootMethod m : flow2Sink) {
			 * fsWriter.println(m+" -> _DEFECT_");
			 * }
			 * //fsWriter.println("Finished.");
			 * fsWriter.close();
			 * 
			 * PrintWriter ffWriter = new PrintWriter("F2FC.txt", "UTF-8");
			 * for (SootClass c : flow2FieldC) {
			 * ffWriter.println(c+" -> _CLASS_");
			 * }
			 * //ffWriter.println("Finished.");
			 * ffWriter.close();
			 * 
			 * PrintWriter ffmWriter = new PrintWriter("F2FM.txt", "UTF-8");
			 * for (SootMethod m : flow2FieldM) {
			 * ffmWriter.println(m+" -> _SOURCE_");
			 * }
			 * //ffmWriter.println("Finished.");
			 * ffmWriter.close();
			 * 
			 * PrintWriter fdWriter = new PrintWriter("SourcesAndSinks.txt", "UTF-8");
			 * for (SootMethod m1 : flow2FieldM) {
			 * fdWriter.println(m1+" -> _SOURCE_");
			 * }
			 * 
			 * for (SootMethod m2 : flow2Return) {
			 * fdWriter.println(m2+" -> _SOURCE_");
			 * }
			 * 
			 * for (SootMethod m3 : basicSink) {
			 * fdWriter.println(m3+" -> _SINK_");
			 * }
			 * 
			 * for (SootMethod m4 : basicSource) {
			 * fdWriter.println(m4+" -> _SOURCE_");
			 * }
			 * fdWriter.close();
			 */
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		System.out.println("Loaded " + (methods.size() - methodCount) + " methods from JAR files. \n");
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