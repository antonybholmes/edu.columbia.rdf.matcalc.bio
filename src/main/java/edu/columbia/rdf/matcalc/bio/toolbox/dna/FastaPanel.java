package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.util.List;

import org.jebtk.bioinformatics.genomic.Sequence;
import org.jebtk.modern.panel.ModernPanel;
import org.jebtk.modern.scrollpane.ModernScrollPane;

public class FastaPanel extends ModernPanel {
  private static final long serialVersionUID = 1L;

  public <X extends Sequence> FastaPanel(List<X> sequences) {
    ModernScrollPane scrollPane = new ModernScrollPane(
        new FastaTextArea(sequences));

    // scrollPane.setVerticalScrollBarPolicy(ScrollBarPolicy.ALWAYS);

    add(scrollPane);

    setBorder(BORDER);
  }
}
