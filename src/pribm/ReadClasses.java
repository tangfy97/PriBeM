package pribm;

import com.google.common.collect.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.*;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import pribm.CallGraphTemplate.LocalGraph;
import pribm.CallGraphTemplate.Vertex;
import pribm.Features.AbstractSootFeature;
import pribm.Info.Method;
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
	public static String dotPath = System.getProperty("user.dir") + "/dot/";
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
	public Set<SootMethod> invokeSource = new HashSet<SootMethod>();
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

	public static <T> Set<T> mergeSet(Set<T> a, Set<T> b) {

		// Adding all elements of respective Sets
		// using addAll() method
		return new HashSet<T>() {
			{
				addAll(a);
				addAll(b);
			}
		};
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
	 * @throws IOException 
	 * @throws ExportException 
	 */

	public void traverseHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph, SootMethod start) throws ExportException, IOException {
		int count = 0;
		Graph<SootMethod, DefaultEdge> cg = new DefaultDirectedGraph<>(DefaultEdge.class);
		Graph<SootMethod, DefaultEdge> cgg = new DefaultDirectedGraph<>(DefaultEdge.class);
		Iterator<SootMethod> iterator = new DepthFirstIterator<>(hrefGraph, start);
		SootMethod callee = null;

		while (iterator.hasNext()) {
			SootMethod caller = iterator.next();
			if (callee != null) {
				cg.addVertex(callee);
				cg.addVertex(caller);
				cg.addEdge(callee, caller);
				System.out.println("<" + count + ": " + callee + " -> " + caller + ">");
				if (mergeSet(basicSource, invokeSource).contains(caller)) {
					System.out.println("The above invocation flows into a source.");
				} else {
					if ((caller.getDeclaringClass() != callee.getDeclaringClass()) && count > 0
					/* && (!caller.getDeclaringClass().toString().contains("java")) */) {
						System.out.println("Global flow detected: " + callee + " -> " + caller + "\n");
						System.out.println("Adding connections to callgraphs in class: " + caller.getDeclaringClass());
						if (!visited.contains(caller.getDeclaringClass())) {
							visited.add(caller.getDeclaringClass());
							cgg = traverseHrefGraphIntern(getCG(caller.getDeclaringClass()), caller);
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
		System.out.println("*************************************");
		System.out.println("*************************************");
		System.out.println("\n");
		Graphs.addGraph(cg, cgg);
		renderHrefGraph(cg, start);
	}

	public Graph<SootMethod, DefaultEdge> traverseHrefGraphIntern(Graph<SootMethod, DefaultEdge> hrefGraph,
			SootMethod start) {

		Graph<SootMethod, DefaultEdge> cg = new DefaultDirectedGraph<>(DefaultEdge.class);
		Iterator<SootMethod> iterator = new DepthFirstIterator<>(hrefGraph, start);
		SootMethod callee = null;

		while (iterator.hasNext()) {
			SootMethod caller = iterator.next();
			if (callee != null) {
				cg.addVertex(callee);
				cg.addVertex(caller);
				cg.addEdge(callee, caller);
			}
			if (callee == null) {
				System.out.println("Continue with method: " + caller);
			}
			callee = caller;
		}
		System.out.println("\n");
		return cg;
	}

	/**
	 * Render a graph in DOT format.
	 *
	 * @param hrefGraph a graph based on URI objects
	 * @throws IOException 
	 */
	public void renderHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph, SootMethod start) throws ExportException, IOException {
		DOTExporter<SootMethod, DefaultEdge> exporter = new DOTExporter<>();
		exporter.setVertexAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", DefaultAttribute.createAttribute(v.toString()));
			return map;
		});
		Writer writer = new StringWriter();
		exporter.exportGraph(hrefGraph, writer);
		System.out.println(writer.toString());
		PrintWriter dotWriter;
		try {
			//File file = new File(String.format(start.toString(),".txt"));
			//if (!file.getParentFile().mkdirs()) throw new IOException("Unable to create " + file.getParentFile());
			//fWriter = new FileWriter(file, true);
			String fileName = start.getName()+".txt";
			dotWriter = new PrintWriter(String.format("dot\\%s", fileName));
			dotWriter.println(writer.toString());
			dotWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		System.out.println("[SPARK] Starting analysis ...");

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

		System.out.println("[SPARK] Done! \n\n");
	}

	public Graph<SootMethod, DefaultEdge> getCG(SootClass sc) {
		sc.setApplicationClass();
		CallGraph callGraph = Scene.v().getCallGraph();

		System.out.println("\n");
		System.out.println("------------------------------------");
		System.out.println("------------------------------------");
		System.out.println("Now we build call graphs for class: " + sc);
		Graph<SootMethod, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (SootMethod sm : sc.getMethods()) {
			if (!sm.getName().contains("init")) {
				Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
				Iterator<soot.jimple.toolkits.callgraph.Edge> edgesInto = callGraph.edgesInto(sm);

				while (edgesOut.hasNext()) {
					soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next();
					// System.out.println(edgesOut);
					if (!edgeOut.src().getName().contains("init") && !edgeOut.tgt().getName().contains("init")
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
					if (!edgeIn.src().getName().contains("init") && !edgeIn.tgt().getName().contains("init")
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
		//Map<Value, SootMethod> map = new LinkedHashMap<Value, SootMethod>();
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
					Set<SootMethod> visitedSource = new HashSet<SootMethod>();
					// Set<Value> valueSet = new HashSet<Value>();
					boolean hasSource = false;

					for (SootMethod m : sc.getMethods()) {
						if (m.isConcrete() && !m.getName().toLowerCase().contains("init")
								&& !m.getName().toLowerCase().contains("main")) {
							if (EOM.contains(m.toString()) || BOM.contains(m.toString())) {
								basicSource.add(m);
								hasSource = true;
							}

							for (Unit u : m.retrieveActiveBody().getUnits()) {
								// Value value = null;

								SootMethod methodBOM = null;

								if (u instanceof AssignStmt) {
									if (((AssignStmt) u).containsInvokeExpr()) {
										InvokeExpr invok = ((AssignStmt) u).getInvokeExpr();
										for (String s : BOM) {
											if ((s.equals(invok.getMethod().toString()))) {
												// if(invokeSource.getMethod().getName().toLowerCase().contains("next"))
												// {
												methodBOM = invok.getMethod();
												// value = ((AssignStmt) u).getLeftOp();
												// map.put(value, methodBOM);
												invokeSource.add(m);
												basicSource.add(methodBOM);
												// valueSet.add(value);
												hasSource = true;
											}
										}
									}
								}
							}
						}
					}

					if (hasSource == true) {
						Graph<SootMethod, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

						for (SootMethod sm : sc.getMethods()) {
							if (!sm.getName().contains("init")) {
								Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph.edgesOutOf(sm);
								Iterator<soot.jimple.toolkits.callgraph.Edge> edgesInto = callGraph.edgesInto(sm);

								while (edgesOut.hasNext()) {
									soot.jimple.toolkits.callgraph.Edge edgeOut = edgesOut.next();
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

						// renderHrefGraph(g);
						if (!g.edgeSet().isEmpty()) {
							for (SootMethod source : basicSource) {
								Boolean checkSource = g.vertexSet().contains(source);
								Boolean diffSource = !visitedSource.contains(source);
								if (checkSource && diffSource) {
									System.out.println("Source found in the callgraph: " + source + "...");
									System.out.println("In class: " + sc);

									if (!source.getName().contains("init")) {
										Iterator<soot.jimple.toolkits.callgraph.Edge> edgesOut = callGraph
												.edgesOutOf(source);
										Iterator<soot.jimple.toolkits.callgraph.Edge> edgesInto = callGraph
												.edgesInto(source);

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

									System.out.println("Start traversal: " + "\n");
									visitedSource.add(source);
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
									 * if (((Stmt) u).containsInvokeExpr()) { InvokeExpr invokeExpr = ((Stmt)
									 * u).getInvokeExpr(); Value leftOp = null; //if (u instanceof AssignStmt)
									 * leftOp = ((AssignStmt) u).getLeftOp(); //if (leftOp != null)
									 * paramVals.add(leftOp); //if (basicSource.contains(invokeExpr.getMethod())) {
									 * if (basicSource.contains(sm) || basicSource.contains(invokeExpr.getMethod()))
									 * { for (int i = 0; i < invokeExpr.getArgCount(); i++) {
									 * paramVals.put(invokeExpr.getArg(i),invokeExpr.getMethod()); } } }
									 */
								}

								for (Unit u : sm.retrieveActiveBody().getUnits()) {
									/*
									 * if (u instanceof AssignStmt) {
									 *
									 * Value leftOp = ((AssignStmt) u).getLeftOp(); Value rightOp = ((AssignStmt)
									 * u).getRightOp();
									 *
									 * //if (!((AssignStmt) u).containsInvokeExpr()) { if
									 * (paramVals.containsKey(leftOp)) paramVals.remove(leftOp); if
									 * (paramVals.containsKey(rightOp)) { paramVals.put(leftOp, sm); if
									 * ((((AssignStmt) u).containsFieldRef())) {
									 * //System.out.println("Value: "+rightOp+
									 * " from BOM: "+paramVals.get(rightOp)+"flows to a field: "+((AssignStmt)
									 * u).getFieldRef()); //flow2FieldM.add(sm); //flow2FieldC.add(sc); } } }
									 */
								}

								for (Unit u : sm.retrieveActiveBody().getUnits()) {
									/*
									 * if (u instanceof ReturnStmt) { ReturnStmt stmt = (ReturnStmt) u; //if
									 * (paramVals.contains(stmt.getOp())) { if (paramVals.containsKey(stmt.getOp())
									 * && !stmt.getOp().getType().toString().toLowerCase().contains("bool") &&
									 * !stmt.getOp().getType().toString().toLowerCase().contains("int") &&
									 * !stmt.getOp().getType().toString().toLowerCase().contains("void") ) {
									 * //System.out.println("Value: "+stmt.getOp()+
									 * " from BOM: "+paramVals.get(stmt.getOp())+"flows to a field: "+stmt.getOp().
									 * getType()); flow2Return.add(sm); } }
									 */
								}
								/*
								 * for (Unit u : sm.retrieveActiveBody().getUnits()) {
								 *
								 * if (((Stmt) u).containsInvokeExpr()) { InvokeExpr invokeExpr = ((Stmt)
								 * u).getInvokeExpr(); //if (basicSink.contains(invokeExpr.getMethod())) { if
								 * (basicSink.contains(sm) || basicSink.contains(invokeExpr.getMethod())) { //if
								 * (invokeExpr.getMethod().getName().toLowerCase().contains("print")) { for
								 * (Value arg : invokeExpr.getArgs()) if (paramVals.contains(arg))
								 * flow2Sink.add(sm); } } }
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
						for (String parameter : sig.substring(sig.indexOf("(") + 1, sig.indexOf(")")).split(",")) {
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
			bsWriter.close();

			PrintWriter bkWriter = new PrintWriter("sink.txt", "UTF-8");
			for (SootMethod m : basicSink) {
				// bkWriter.println(m+" -> _SINK_");
				bkWriter.println(m);
			}
			bkWriter.close();

			PrintWriter isWriter = new PrintWriter("invokeSource.txt", "UTF-8");
			for (SootMethod m : invokeSource) {
				isWriter.println(m);
			}
			isWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		System.out.println("Loaded " + (methods.size() - methodCount) + " methods from JAR files. \n");
		System.out.println("Found " + basicSource.size() + " Source Methods.");
		System.out.println("Found " + basicSink.size() + " Sink Methods.");
		System.out.println("Found " + invokeSource.size() + " Methods Invoking a Basic Source.");
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