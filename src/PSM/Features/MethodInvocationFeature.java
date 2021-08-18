package PSM.Features;

import PSM.Info.Method;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;

public class MethodInvocationFeature extends AbstractSootFeature{
	private final String methodName;

	  public MethodInvocationFeature(String cp, String methodName) {
	    super(cp);
	    this.methodName = methodName;
	  }

	  @Override
	  public Type appliesInternal(Method method) {
	    SootMethod sm = getSootMethod(method);

	    if (sm == null) {
	      System.err.println("Method not declared: " + method);
	      return Type.NOT_SUPPORTED;
	    }

	    if (!sm.isConcrete()) return Type.NOT_SUPPORTED;

	    try {
	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        // Check for invocations
	        if (u instanceof Stmt) {
	          Stmt stmt = (Stmt) u;
	          if (stmt.containsInvokeExpr())
	            if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
	            InstanceInvokeExpr iinv = (InstanceInvokeExpr) stmt.getInvokeExpr();
	            if (iinv.getMethod().getName().contains(methodName)) return Type.TRUE;
	            }
	        }
	      }
	      return Type.FALSE;
	    } catch (Exception ex) {
	      // System.err.println("Something went wrong:");
	      // ex.printStackTrace();
	      return Type.NOT_SUPPORTED;
	    }
	  }

	  @Override
	  public String toString() {
	    return "<Method " + methodName + " invoked>";
	  }

	@Override
	public boolean check(Method method) {
	    SootMethod sm = getSootMethod(method);

	    if (sm == null) {
	      System.err.println("Method not declared: " + method);
	      return false;
	    }

	    if (!sm.isConcrete()) return false;

	    try {
	      for (Unit u : sm.retrieveActiveBody().getUnits()) {
	        // Check for invocations
	        if (u instanceof Stmt) {
	          Stmt stmt = (Stmt) u;
	          if (stmt.containsInvokeExpr())
	            if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
	            InstanceInvokeExpr iinv = (InstanceInvokeExpr) stmt.getInvokeExpr();
	            if (iinv.getMethod().getName().contains(methodName)) return true;
	            }
	        }
	      }
	      return false;
	    } catch (Exception ex) {
	      // System.err.println("Something went wrong:");
	      // ex.printStackTrace();
	      return false;
	    }
	}

}
