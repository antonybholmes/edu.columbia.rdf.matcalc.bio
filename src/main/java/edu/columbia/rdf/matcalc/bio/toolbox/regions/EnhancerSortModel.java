package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import org.jebtk.modern.search.SortModel;

/**
 * Allows sort objects to be shared between entities that control how samples
 * and experiments are sorted.
 * 
 * @author Antony Holmes
 *
 */
public class EnhancerSortModel extends SortModel<Enhancer> {
  private static final long serialVersionUID = 1L;

  public EnhancerSortModel() {
    add(new SortSamplesByName());

    setDefault("ChIP-seq Type");
  }
}
