package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import org.jebtk.bioinformatics.genomic.DNA;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.modern.UI;
import org.jebtk.modern.combobox.ModernComboBox2;

public class MaskCombo extends ModernComboBox2 {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public MaskCombo() {
    addMenuItem(UI.ASSET_UPPERCASE);
    addMenuItem(UI.ASSET_LOWERCASE);
    addMenuItem(DNA.N);

    setSelectedIndex(0); // 1);
  }

  public RepeatMaskType getRepeatMaskType() {
    switch (getSelectedIndex()) {
    case 0:
      return RepeatMaskType.UPPERCASE;
    case 2:
      return RepeatMaskType.N;
    default:
      return RepeatMaskType.LOWERCASE;
    }
  }

}
