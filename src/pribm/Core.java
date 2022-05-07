package pribm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.*;
import org.jgrapht.traverse.*;
import pribm.Features.AbstractSootFeature;
import pribm.Info.Method;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;

public class Core {

	public static String sourceDirectory = System.getProperty("user.dir");
	public static String jarDirectory = System.getProperty("user.dir") + "/data";
	public static String sourcePath = System.getProperty("user.dir") + "/source.txt";
	public static String sinkPath = System.getProperty("user.dir") + "/sink.txt";
	public Set<String> EOM = new HashSet<String>();
	public Set<String> EIM = new HashSet<String>();
	public Set<String> BOM = new HashSet<String>();
	public Set<String> BIM = new HashSet<String>();
	public Set<String> source = new HashSet<String>();
	public Set<String> sink = new HashSet<String>();
	public Set<SootClass> visited = new HashSet<SootClass>();
	public Set<Method> methods = new HashSet<Method>();
	public String testCp;
	public Set<SootMethod> basicSource = new HashSet<SootMethod>();
	public Set<SootMethod> invokeSource = new HashSet<SootMethod>();
	public Set<SootMethod> basicSink = new HashSet<SootMethod>();

	public Core(String testCp) {
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

	@SuppressWarnings("serial")
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

	/**
	 * Traverse a graph in depth-first order and print the vertices.
	 *
	 * @param hrefGraph a graph based on URI objects
	 *
	 * @param start     the vertex where the traversal should start
	 * @throws IOException
	 * @throws ExportException
	 */

	public void traverseHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph, SootMethod start)
			throws ExportException, IOException {
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
					if (basicSink.contains(caller)) {
						System.out.println("The above invocation flows into a sink.");
					}
					if ((caller.getDeclaringClass() != callee.getDeclaringClass()) && count > 0) {
						System.out.println("Global flow detected: " + callee + " -> " + caller + "\n");
						System.out.println("Adding connections to callgraphs in class: " + caller.getDeclaringClass());
						if (!visited.contains(caller.getDeclaringClass())) {
							visited.add(caller.getDeclaringClass());
							cgg = CallGraphBuilder.traverseHrefGraphIntern(
									CallGraphBuilder.getCG(caller.getDeclaringClass()), caller);
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
		CallGraphBuilder.renderHrefGraph(cg, start);
	}

	private void loadMethodsFromTestLib(final Set<String> testClasses) throws Exception {

		int methodCount = methods.size();
		BOM = Preprocess.loadBOM();
		BIM = Preprocess.loadBIM();
		EOM = Preprocess.loadEOM();
		EIM = Preprocess.loadEIM();

		new AbstractSootFeature(testCp) {
			@Override
			public Type appliesInternal(Method method) throws Exception {
				System.out.println("Start looking for sources and sinks: ");
				Preprocess.setSparkPointsToAnalysis();

				for (String className : testClasses) {
					SootClass sc = Scene.v().forceResolve(className, SootClass.BODIES);
					sc.setApplicationClass();
					CallGraph callGraph = Scene.v().getCallGraph();
					Set<SootMethod> visitedSource = new HashSet<SootMethod>();
					boolean hasSource = false;

					for (SootMethod m : sc.getMethods()) {
						if (m.isConcrete() && !m.getName().toLowerCase().contains("init")
								&& !m.getName().toLowerCase().contains("main")) {
							if (EOM.contains(m.toString()) || BOM.contains(m.toString())) {
								basicSource.add(m);
								hasSource = true;
							}

							for (Unit u : m.retrieveActiveBody().getUnits()) {
								SootMethod methodBOM = null;
								if (u instanceof AssignStmt) {
									if (((AssignStmt) u).containsInvokeExpr()) {
										InvokeExpr invok = ((AssignStmt) u).getInvokeExpr();
										for (String s : BOM) {
											if ((s.equals(invok.getMethod().toString()))) {
												methodBOM = invok.getMethod();
												invokeSource.add(m);
												basicSource.add(methodBOM);
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
										if (s.equals(invokeExpr.getMethod().toString())
												&& !(invokeExpr.getMethod().getName().contains("init"))
												&& !(invokeExpr.getMethod().getName().contains("error"))
												&& !(invokeExpr.getMethod().getName().contains("bug"))
												&& !(invokeExpr.getMethod().getName().contains("abort"))) {
											basicSink.add(sm);
											basicSink.add(invokeExpr.getMethod());
										}
									}
								}
							}
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

		try {
			PrintWriter bsWriter = new PrintWriter("source.txt", "UTF-8");
			for (SootMethod m : basicSource) {
				bsWriter.println(m);
			}
			bsWriter.close();

			PrintWriter bkWriter = new PrintWriter("sink.txt", "UTF-8");
			for (SootMethod m : basicSink) {
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