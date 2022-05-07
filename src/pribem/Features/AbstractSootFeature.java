package pribem.Features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pribem.IFeature;
import pribem.Info.Method;
import soot.G;
import soot.Hierarchy;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public abstract class AbstractSootFeature implements IFeature {

	private static boolean SOOT_INITIALIZED = false;

	private Map<Method, Type> resultCache = new HashMap<Method, Type>();

	public AbstractSootFeature(String cp) {
		initializeSoot(cp);
	}

	private void initializeSoot(String cp) {
		if (SOOT_INITIALIZED)
			return;
		G.reset();

		// Call-graph options
		Options.v().setPhaseOption("cg", "safe-newinstance:true");
		Options.v().setPhaseOption("cg.cha", "enabled:false");

		// Enable SPARK call-graph construction
		Options.v().setPhaseOption("cg.spark", "enabled:true");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_process_dir(Collections.singletonList(cp));
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
		Options.v().set_include_all(true);
		Options.v().set_soot_classpath(cp);
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_output_dir(System.getProperty("user.dir") + "/sootOutput");
		SOOT_INITIALIZED = true;
	}

	protected SootMethod getSootMethod(Method method) {
		return getSootMethod(method, true);
	}

	protected SootMethod getSootMethod(Method method, boolean lookInHierarchy) {

		SootClass c = Scene.v().forceResolve(method.getClassName(), SootClass.BODIES);

		if (c == null || c.isPhantom()) {
			System.err.println("Class " + method.getClassName() + " not found");
			return null;
		}

		c.setApplicationClass();
		if (c.isInterface())
			return null;

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
		if (!(type instanceof RefType))
			return false;

		// Check for a direct match
		RefType refType = (RefType) type;
		if (refType.getSootClass().getName().equals(typeName))
			return true;

		// interface treatment
		if (refType.getSootClass().isInterface())
			return false;

		// class treatment
		Hierarchy h = Scene.v().getActiveHierarchy();
		List<SootClass> ancestors = h.getSuperclassesOf(refType.getSootClass());
		for (SootClass ancestor : ancestors) {
			if (ancestor.getName().equals(typeName))
				return true;
			for (SootClass sc : ancestor.getInterfaces())
				if (sc.getName().equals(typeName))
					return true;
		}
		return false;
	}

	public Type applies(Method method) throws Exception {
		if (this.resultCache.containsKey(method))
			return this.resultCache.get(method);
		else {
			Type tp = this.appliesInternal(method);
			this.resultCache.put(method, tp);
			return tp;
		}
	}

	public abstract Type appliesInternal(Method method) throws Exception;

}