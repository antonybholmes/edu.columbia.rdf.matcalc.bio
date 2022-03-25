package edu.columbia.rdf.matcalc.bio.toolbox.decisiontree;

import java.util.List;
import java.util.Map;

import org.jebtk.core.NameGetter;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.xml.XmlRepresentation;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.MatrixGroup;
import org.jebtk.math.matrix.utils.MatrixOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Classifier implements Comparable<Classifier>, NameGetter, XmlRepresentation {
  private String mName;
  private DataFrame mPhenM;
  private DataFrame mControlM;
  private Map<String, Integer> mGeneMap;
  private String mPhenotype;
  private String mControl;
  private String[] mGenes;

  public Classifier(String name, DataFrame m, XYSeries phenotypeGroup,
      XYSeries controlGroup, String annotation) {
    mName = name;

    mPhenotype = phenotypeGroup.getName();
    mControl = controlGroup.getName();

    List<Integer> phenotypeIndices = MatrixGroup.findColumnIndices(m,
        phenotypeGroup);

    List<Integer> controlIndices = MatrixGroup.findColumnIndices(m,
        controlGroup);

    mGenes = m.getIndex().getText(annotation);

    mGeneMap = CollectionUtils.toIndexMap(mGenes);

    mPhenM = DataFrame.createNumericalMatrix(mGenes.length,
        phenotypeIndices.size());

    mControlM = DataFrame.createNumericalMatrix(mGenes.length,
        controlIndices.size());

    int c = 0;

    for (int i : phenotypeIndices) {
      mPhenM.copyColumn(m, i, c++);
    }

    c = 0;

    for (int i : controlIndices) {
      mControlM.copyColumn(m, i, c++);
    }
  }

  public Classifier(String name, String phenotype, DataFrame phenM,
      String control, DataFrame controlM, String[] genes) {
    mName = name;

    mPhenotype = phenotype;
    mControl = control;

    mGenes = genes;
    mGeneMap = CollectionUtils.toIndexMap(genes);

    mPhenM = phenM;

    mControlM = controlM;
  }

  @Override
  public String getName() {
    return mName;
  }

  public int getGeneCount() {
    return mGenes.length;
  }

  public String getGene(int g) {
    return mGenes[g];
  }

  public double getPhenotypeMean(String gene) {
    return MatrixOperations.mean(mPhenM, mGeneMap.get(gene));
  }

  public DataFrame getPhenotype() {
    return mPhenM;
  }

  public String getPhenotypeName() {
    return mPhenotype;
  }

  public double getControlMean(String gene) {
    return MatrixOperations.mean(mControlM, mGeneMap.get(gene));
  }

  public DataFrame getControl() {
    return mControlM;
  }

  public String getControlName() {
    return mControl;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Classifier) {
      return compareTo((Classifier) o) == 0;
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(Classifier c) {
    return mName.compareTo(c.mName);
  }

  @Override
  public int hashCode() {
    return mName.hashCode();
  }

  public static Classifier create(String name,
      DataFrame m,
      XYSeries phenotypeGroup,
      XYSeries controlGroup,
      String annotation) {
    return new Classifier(name, m, phenotypeGroup, controlGroup, annotation);
  }

  public static Classifier create(String name,
      String phenotype,
      DataFrame phenM,
      String control,
      DataFrame controlM,
      String[] genes) {
    return new Classifier(name, phenotype, phenM, control, controlM, genes);
  }

  @Override
  public Element toXml(Document doc) {
    Element e = doc.createElement("classifier");
    e.setAttribute("name", mName);
    e.setAttribute("phenotype", mPhenotype);
    e.setAttribute("phenotype-size", Integer.toString(mPhenM.getCols()));
    e.setAttribute("control", mControl);
    e.setAttribute("control-size", Integer.toString(mControlM.getCols()));
    e.setAttribute("size", Integer.toString(mGenes.length));

    // XmlElement gse = new XmlElement("genes");

    for (String name : CollectionUtils.sort(mGenes)) {
      Element ge = doc.createElement("gene");

      ge.setAttribute("name", name);

      Element pe = doc.createElement("phenotype");

      ge.appendChild(pe);

      for (int i = 0; i < mPhenM.getCols(); ++i) {
        Element se = doc.createElement("sample");
        // se.setAttribute("name", mPhenM.getColumnName(i));
        se.setAttribute("value", mPhenM.getText(mGeneMap.get(name), i));
        pe.appendChild(se);
      }

      Element ce = doc.createElement("control");

      ge.appendChild(ce);

      for (int i = 0; i < mControlM.getCols(); ++i) {
        Element se = doc.createElement("sample");
        // se.setAttribute("name", mControlM.getColumnName(i));
        se.setAttribute("value", mControlM.getText(mGeneMap.get(name), i));
        ce.appendChild(se);
      }

      e.appendChild(ge);
    }

    return e;
  }

  public String[] getGenes() {
    return mGenes;
  }
}
