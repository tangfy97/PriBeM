package PSM.Features;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import PSM.IFeature;
import PSM.Info.Method;
import soot.G;
import soot.Hierarchy;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.options.Options;

public abstract class AbstractSootFeature implements IFeature {

  private static boolean SOOT_INITIALIZED = false;

  private Map<Method, Type> resultCache = new HashMap<Method, Type>();

  public AbstractSootFeature(String cp) {
    initializeSoot(cp);
  }

  private void initializeSoot(String cp) {
    if (SOOT_INITIALIZED) return;
    G.reset();

    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_whole_program(true);
    Options.v().set_include_all(true);
    Options.v().set_soot_classpath(cp);

    Scene.v().loadNecessaryClasses();
    SOOT_INITIALIZED = true;
  }

  protected SootMethod getSootMethod(Method method) {
    return getSootMethod(method, true);
  }

  protected SootMethod getSootMethod(Method method, boolean lookInHierarchy) {

    SootClass c =
        Scene.v().forceResolve(method.getClassName(), SootClass.BODIES);

    if (c == null || c.isPhantom()) {
      System.err.println("Class " + method.getClassName() + " not found");
      return null;
    }

    c.setApplicationClass();
    if (c.isInterface()) return null;

    while (c != null) {
      // Does the current class declare the method we are looking for?
      if (method.getReturnType().isEmpty()) {
        if (c.declaresMethodByName(method.getMethodName()))
          return c.getMethodByName(method.getMethodName());
      } else {
        if (c.declaresMethod(method.getSubSignature()))
          return c.getMethod(method.getSubSignature());
      }

      // Continue our search up the class hierarchy
      if (lookInHierarchy && c.hasSuperclass())
        c = c.getSuperclass();
      else
        c = null;
    }
    return null;
  }

  protected boolean isOfType(soot.Type type, String typeName) {
    if (!(type instanceof RefType)) return false;

    // Check for a direct match
    RefType refType = (RefType) type;
    if (refType.getSootClass().getName().equals(typeName)) return true;

    // interface treatment
    if (refType.getSootClass().isInterface()) return false;

    // class treatment
    Hierarchy h = Scene.v().getActiveHierarchy();
    List<SootClass> ancestors = h.getSuperclassesOf(refType.getSootClass());
    for (SootClass ancestor : ancestors) {
      if (ancestor.getName().equals(typeName)) return true;
      for (SootClass sc : ancestor.getInterfaces())
        if (sc.getName().equals(typeName)) return true;
    }
    return false;
  }

  public Type applies(Method method) {
    if (this.resultCache.containsKey(method))
      return this.resultCache.get(method);
    else {
      Type tp = this.appliesInternal(method);
      this.resultCache.put(method, tp);
      return tp;
    }
  }

  public abstract Type appliesInternal(Method method);

}