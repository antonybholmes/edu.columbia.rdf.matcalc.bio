package edu.columbia.rdf.matcalc.bio.toolbox.decisiontree;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.jebtk.core.event.ChangeListeners;
import org.jebtk.core.xml.XmlRepresentation;
import org.jebtk.math.machine.learning.DecisionTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClassifierService extends ChangeListeners
    implements Iterable<String>, XmlRepresentation {
  private static final long serialVersionUID = 1L;

  private static class ClassifierHelper {
    private static final ClassifierService INSTANCE = new ClassifierService();
  }

  public static ClassifierService getInstance() {
    return ClassifierHelper.INSTANCE;
  }

  private Map<String, DecisionTree> mClassifierMap = new TreeMap<String, DecisionTree>();

  private ClassifierService() {
    // Do nothing
  }

  public void add(DecisionTree classifier) {
    mClassifierMap.put(classifier.getName(), classifier);

    fireChanged();
  }

  @Override
  public Iterator<String> iterator() {
    return mClassifierMap.keySet().iterator();
  }

  @Override
  public Element toXml(Document doc) {
    Element e = doc.createElement("classifiers");

    for (DecisionTree c : mClassifierMap.values()) {
      e.appendChild(c.toXml(doc));
    }

    return e;
  }

  public DecisionTree get(String name) {
    return mClassifierMap.get(name);
  }

  public int getCount() {
    return mClassifierMap.size();
  }

  public void clear() {
    mClassifierMap.clear();

    fireChanged();
  }
}
