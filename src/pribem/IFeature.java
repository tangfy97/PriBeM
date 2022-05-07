package pribem;

import pribem.Info.Method;

public interface IFeature {

  enum Type {
    TRUE, FALSE, NOT_SUPPORTED
  }

  Type applies(Method method) throws Exception;
  boolean check(Method method);

  String toString();

}