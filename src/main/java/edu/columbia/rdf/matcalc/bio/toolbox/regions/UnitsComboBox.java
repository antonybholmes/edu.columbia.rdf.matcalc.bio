package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import org.jebtk.modern.combobox.ModernComboBox;

public class UnitsComboBox extends ModernComboBox {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnitsComboBox() {
    addMenuItem("bp");
    addMenuItem("kb");
    addMenuItem("Mb");
  }

}
