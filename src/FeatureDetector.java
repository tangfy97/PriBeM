import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeatureDetector {

  private final String cp;
  private Map<Category, Set<IFeature>> featuresMap;

  public FeatureDetector(String cp) {
    this.cp = cp;
  }

  public Map<Category, Set<IFeature>> features() {
    return featuresMap;
  }

  private void addFeature(IFeature feature,
      Set<Category> categoriesForFeature) {
    for (Category category : categoriesForFeature) {
      Set<IFeature> typeFeatures = featuresMap.get(category);
      typeFeatures.add(feature);
      featuresMap.put(category, typeFeatures);
    }
  }

  /**
   * Initialises the set of features for classifying methods in the categories.
   * 
   * @param disable indicates the id of the feature instance that should be excluded. 
   * Used for the One-at-a-time analysis. 0 is for all features enables. 
   */
  public void initializeFeatures(int disable) {
    featuresMap = new HashMap<Category, Set<IFeature>>();
    for (Category category : Category.values())
      featuresMap.put(category, new HashSet<IFeature>());

    if(disable != 3) {
    	IFeature classNameContainsUser = new MethodClassContainsNameFeature(
    	        "User");
        //((WeightedFeature)classNameContainsSaniti).setWeight(2);
    	    addFeature(classNameContainsUser,
    	        new HashSet<>(Arrays.asList(Category.PPI, Category.SOURCE)));
    }
    
    System.out.println("Initialised " + getFeaturesSize() + " features.");
  }

  private int getFeaturesSize() {
    int count = 0;
    for (Category c : featuresMap.keySet())
      count += featuresMap.get(c).size();
    return count;
  }

}