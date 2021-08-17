package PSM;
public class MethodNameContainsFeature implements IFeature {

  private final String contains;
  private final String doesNotContain;

  public MethodNameContainsFeature(String contains) {
    this.contains = contains.toLowerCase();
    this.doesNotContain = null;
  }

  public MethodNameContainsFeature(String contains, String doesNotContain) {
    this.contains = contains.toLowerCase();
    this.doesNotContain = doesNotContain.toLowerCase();
  }

  @Override
  public Type applies(Method method) {
    if (doesNotContain != null
        && method.getMethodName().toLowerCase().contains(doesNotContain))
      return Type.FALSE;
    return (method.getMethodName().toLowerCase().contains(contains) ? Type.TRUE
        : Type.FALSE);
  }

  @Override
  public String toString() {
    String s = "<Method name contains " + this.contains;
    if (doesNotContain != null) s += " but not " + this.doesNotContain;
    return s + ">";
  }

@Override
public boolean check(Method method) {
	if (doesNotContain != null
	        && method.getMethodName().toLowerCase().contains(doesNotContain))
	      return false;
	    return (method.getMethodName().toLowerCase().contains(contains) ? true
	        : false);
}

}
