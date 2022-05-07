package pribm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.traverse.DepthFirstIterator;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;

public class CallGraphBuilder {

	public static Graph<SootMethod, DefaultEdge> traverseHrefGraphIntern(Graph<SootMethod, DefaultEdge> hrefGraph,
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
	public static void renderHrefGraph(Graph<SootMethod, DefaultEdge> hrefGraph, SootMethod start)
			throws ExportException, IOException {
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
			// File file = new File(String.format(start.toString(),".txt"));
			// if (!file.getParentFile().mkdirs()) throw new IOException("Unable to create "
			// + file.getParentFile());
			// fWriter = new FileWriter(file, true);
			String fileName = start.getName() + ".txt";
			dotWriter = new PrintWriter(String.format("dot\\%s", fileName));
			dotWriter.println(writer.toString());
			dotWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Graph<SootMethod, DefaultEdge> getCG(SootClass sc) {
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
}
