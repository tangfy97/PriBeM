package PSM.Features;

import java.util.ArrayList;
import java.util.List;

import PSM.Info.Method;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

/**
 * Feature which checks whether the current method (indirectly) calls another
 * one
 *
 * @author Steven Arzt, Siegfried Rasthofer
 *
 */
public class MethodCallsAnotherMethod extends AbstractSootFeature {

  private final String className;
  private final String methodName;
  private final boolean substringMatch;

  public MethodCallsAnotherMethod(String cp, String methodName) {
    this(cp, "", methodName);
  }

  public MethodCallsAnotherMethod(String cp, String className,
      String methodName) {
    this(cp, className, methodName, false);
  }

  public MethodCallsAnotherMethod(String cp, String className,
      String methodName, boolean substringMatch) {
    super(cp);
    this.className = className;
    this.methodName = methodName;
    this.substringMatch = substringMatch;
  }

  @Override
  public Type appliesInternal(Method method) {
    try {
      SootMethod sm = getSootMethod(method);
      if (sm == null) {
        System.err.println("Method not declared: " + method);
        return Type.NOT_SUPPORTED;
      }
      return checkMethod(sm, new ArrayList<SootMethod>());
    } catch (Exception ex) {
      System.err.println("Something went wrong:");
      ex.printStackTrace();
      return Type.NOT_SUPPORTED;
    }
  }
  
  public Type checkMethod(SootMethod method, List<SootMethod> doneList) {
	    if (doneList.contains(method))
	      return Type.NOT_SUPPORTED;
	    if (!method.isConcrete())
	      return Type.NOT_SUPPORTED;
	    doneList.add(method);

	    try {
	      Body body = null;
	      try {
	        body = method.retrieveActiveBody();
	      } catch (Exception ex) {
	        return Type.NOT_SUPPORTED;
	      }

	      for (Unit u : body.getUnits()) {
	        if (!(u instanceof Stmt))
	          continue;
	        Stmt stmt = (Stmt) u;
	        if (!stmt.containsInvokeExpr())
	          continue;

	        InvokeExpr inv = stmt.getInvokeExpr();
	        if ((substringMatch
	            && inv.getMethod().getName().contains(this.methodName))
	            || inv.getMethod().getName().startsWith(this.methodName)) {
	          if (this.className.isEmpty() || this.className
	              .equals(inv.getMethod().getDeclaringClass().getName()))
	            return Type.TRUE;
	        } else if (checkMethod(inv.getMethod(), doneList) == Type.TRUE)
	          return Type.TRUE;
	      }
	      return Type.FALSE;
	    } catch (Exception ex) {
	      System.err.println("Oops: " + ex);
	      return Type.NOT_SUPPORTED;
	    }
	  }

  public boolean checkMethods(SootMethod method, List<SootMethod> doneList) {
    if (doneList.contains(method))
      return false;
    if (!method.isConcrete())
      return false;
    doneList.add(method);

    try {
      Body body = null;
      try {
        body = method.retrieveActiveBody();
      } catch (Exception ex) {
        return false;
      }

      for (Unit u : body.getUnits()) {
        if (!(u instanceof Stmt))
          continue;
        Stmt stmt = (Stmt) u;
        if (!stmt.containsInvokeExpr())
          continue;

        InvokeExpr inv = stmt.getInvokeExpr();
        if ((substringMatch
            && inv.getMethod().getName().contains(this.methodName))
            || inv.getMethod().getName().startsWith(this.methodName)) {
          if (this.className.isEmpty() || this.className
              .equals(inv.getMethod().getDeclaringClass().getName()))
            return true;
        } else if (checkMethods(inv.getMethod(), doneList) == true)
          return true;
      }
      return false;
    } catch (Exception ex) {
      System.err.println("Oops: " + ex);
      return false;
    }
  }

  @Override
  public String toString() {
    return "Method starting with '" + this.methodName + "' invoked";
  }

@Override
public boolean check(Method method) {
	try {
	      SootMethod sm = getSootMethod(method);
	      if (sm == null) {
	        System.err.println("Method not declared: " + method);
	        return false;
	      }
	      return checkMethods(sm, new ArrayList<SootMethod>());
	    } catch (Exception ex) {
	      System.err.println("Something went wrong:");
	      ex.printStackTrace();
	      return false;
	    }
}

}
