package PSM.Features;

import PSM.IFeature;
import PSM.IFeature.Type;
import PSM.Info.Method;

public class MethodClassContainsNameFeature  implements IFeature {

  private final String partOfName;

  public MethodClassContainsNameFeature(String partOfName) {
    this.partOfName = partOfName.toLowerCase();
  }

  @Override
  public Type applies(Method method) {
    return (method.getClassName().toLowerCase().contains(partOfName) ? Type.TRUE
        : Type.FALSE);
  }
  
  @Override
  public boolean check(Method method) {
	  return (method.getClassName().toLowerCase().contains(partOfName) ? true : false);
  }

  @Override
  public String toString() {
    return "<Method is part of class that contains the name " + partOfName
        + ">";
  }
}
