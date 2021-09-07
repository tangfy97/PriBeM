package PSM;

import PSM.Info.Method;

public interface IFeature {

  enum Type {
    TRUE, FALSE, NOT_SUPPORTED
  }

  Type applies(Method method);
  boolean check(Method method);

  String toString();

}