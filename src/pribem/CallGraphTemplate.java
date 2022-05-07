package pribem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.SootMethod;

public class CallGraphTemplate {
	
	static class Vertex {
	    SootMethod label;
	    Vertex(SootMethod label) {
	        this.label = label;
	    }

	    // equals and hashCode
	}
	
	static class LocalGraph {
	    private Map<Vertex, List<Vertex>> adjVertices;
	    
	    // standard constructor, getters, setters
	    
	    boolean hasNeighbour(SootMethod label) {
	    	if(adjVertices.get(new Vertex(label)).isEmpty()) {
	    		return false;
	    	} else {
	    		return true;
	    	}
	    }
	    
	    void addVertex(SootMethod label) {
	        adjVertices.putIfAbsent(new Vertex(label), new ArrayList<>());
	    }

	    void removeVertex(SootMethod label) {
	        Vertex v = new Vertex(label);
	        adjVertices.values().stream().forEach(e -> e.remove(v));
	        adjVertices.remove(new Vertex(label));
	    }
	    
	    void addEdge(SootMethod label1, SootMethod label2) {
	        Vertex v1 = new Vertex(label1);
	        Vertex v2 = new Vertex(label2);
	        adjVertices.get(v1).add(v2);
	        adjVertices.get(v2).add(v1);
	    }
	    
	    void removeEdge(SootMethod label1, SootMethod label2) {
	        Vertex v1 = new Vertex(label1);
	        Vertex v2 = new Vertex(label2);
	        List<Vertex> eV1 = adjVertices.get(v1);
	        List<Vertex> eV2 = adjVertices.get(v2);
	        if (eV1 != null)
	            eV1.remove(v2);
	        if (eV2 != null)
	            eV2.remove(v1);
	    }
	    
	    List<Vertex> getAdjVertices(SootMethod label) {
	        return adjVertices.get(new Vertex(label));
	    }
	}
	
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
	
	
}
