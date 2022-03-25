package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.util.List;

import org.jebtk.bioinformatics.genomic.Sequence;
import org.jebtk.modern.text.ModernClipboardTextArea;

public class FastaTextArea extends ModernClipboardTextArea {

  private static final long serialVersionUID = 1L;

  public <X extends Sequence> FastaTextArea(List<X> sequences) {
    setEditable(false);

    setSequences(sequences);
  }

  public <X extends Sequence> void setSequences(List<X> sequences) {
    StringBuilder buffer = new StringBuilder();

    for (Sequence sequence : sequences) {
      buffer.append(">").append(sequence.getName()).append("\n");
      buffer.append(sequence.toString()).append("\n");
    }

    setText(buffer.toString());
  }

}
