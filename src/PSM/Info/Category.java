package PSM.Info;
public enum Category {
    SOURCE(Constants.SOURCE, false), SINK(Constants.SINK, false), AUTHENTICATION_TO_HIGH(
            Constants.AUTHENTICATION_SAFE, false), AUTHENTICATION_TO_LOW(
            Constants.AUTHENTICATION_UNSAFE, false), AUTHENTICATION_NEUTRAL(
            Constants.AUTHENTICATION_NOCHANGE, false), SANITIZER(Constants.SANITIZER,
            false), NONE(Constants.NONE, false), PPI(Constants.PPI, false),
    CWE359("CWE359", true), CWE200("CWE200", true), CWE202("CWE202", 
      true), CWE213("CWE213", true), CWE201("CWE201", 
          true), CWE538("CWE538", true), CWE_NONE("none", true);

  private final String id;
  private final boolean cwe;

  private Category(String id, boolean cwe) {
    this.id = id;
    this.cwe = cwe;
  }

  public boolean isCwe() {
    return cwe;
  }

  @Override
  public String toString() {
    return id;
  }

  public static Category getCategoryForCWE(String cweName) {
    for (Category c : Category.values())
      if (c.id.toLowerCase().equals(cweName.toLowerCase()))
        return c;
    return null;
  }
}
