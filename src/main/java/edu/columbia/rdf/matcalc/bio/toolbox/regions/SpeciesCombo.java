package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.io.IOException;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.modern.UI;
import org.jebtk.modern.combobox.ModernComboBox;

import edu.columbia.rdf.matcalc.bio.AnnotationService;

public class SpeciesCombo extends ModernComboBox {

  private static final long serialVersionUID = 1L;

  public SpeciesCombo() throws IOException {
    for (Genome g : AnnotationService.getInstance().genomes()) {
      addMenuItem(g.mName);
    }

    UI.setSize(this, VERY_LARGE_SIZE);
  }

}
