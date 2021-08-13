
/**
 * Common interface for all features in the probabilistic model
 *
 * @author Steven Arzt
 *
 */
public interface IFeature {

  enum Type {
    TRUE, FALSE, NOT_SUPPORTED
  }

  Type applies(Method method);

  String toString();

}