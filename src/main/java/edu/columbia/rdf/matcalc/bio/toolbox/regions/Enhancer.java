package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import org.jebtk.core.NameGetter;

public class Enhancer implements NameGetter {

  private String mName;

  public Enhancer(String name) {
    mName = name;
  }

  @Override
  public String getName() {
    return mName;
  }

}