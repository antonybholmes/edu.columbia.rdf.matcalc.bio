/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.mutations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.Mutation;
import org.jebtk.math.external.microsoft.Excel;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.ui.external.microsoft.ExcelUI;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.ribbon.RibbonLargeButton;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.SearchSequence;
import edu.columbia.rdf.matcalc.bio.SequenceUtils;
import edu.columbia.rdf.matcalc.toolbox.Module;

/**
 * Map probes to genes.
 *
 * @author Antony Holmes
 *
 */
public class MutationsModule extends Module implements ModernClickListener {

  /**
   * The member convert button.
   */
  private RibbonLargeButton mConvertButton = new RibbonLargeButton("Mutations",
      AssetService.getInstance().loadIcon(MutationsIcon.class, 24));

  /**
   * The member window.
   */
  private MainMatCalcWindow mWindow;

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.NameProperty#getName()
   */
  @Override
  public String getName() {
    return "Mutations";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.columbia.rdf.apps.matcalc.modules.Module#init(edu.columbia.rdf.apps.
   * matcalc.MainMatCalcWindow)
   */
  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    // home
    mWindow.getRibbon().getToolbar("DNA").getSection("Mutations")
        .add(mConvertButton);

    mConvertButton.addClickListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.abh.lib.ui.modern.event.ModernClickListener#clicked(org.abh.lib.ui.
   * modern .event.ModernClickEvent)
   */
  @Override
  public final void clicked(ModernClickEvent e) {
    try {
      mutations();
    } catch (IOException | InvalidFormatException e1) {
      e1.printStackTrace();
    }
  }

  private void mutations() throws IOException, InvalidFormatException {
    Genome genome = Genome.HG19;
    
    DataFrame m = mWindow.getCurrentMatrix();

    List<SearchSequence> sequences = SequenceUtils.matrixToSequences(genome, m);

    if (sequences.size() == 0) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "There are no suitable DNA sequences in the table.");

      return;
    }

    Path mutationFile = ExcelUI.openExcelFileDialog(mWindow,
        RecentFilesService.getInstance().getPwd());

    String[] lines = Excel.getTextFromFile(mutationFile, true);

    List<Mutation> mutations = Mutation.parse(lines);

    // Check output
    Mutation.toString(mutations, System.err);

    MutationsDialog dialog = new MutationsDialog(mWindow, mutations);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    int rows = sequences.size() * lines.length;

    boolean allMutationsMode = dialog.all();

    if (allMutationsMode) {
      rows += 1;
    }

    boolean wildMode = dialog.wild();

    if (wildMode) {
      rows += 1;
    }

    DataFrame m2 = DataFrame.createDataFrame(rows, 3);

    int r = 0;

    int trim5p = dialog.getTrim5p();
    int trim3p = dialog.getTrim3p();

    for (SearchSequence sequence : sequences) {
      String dna = sequence.getDna().toString();

      int l = dna.length();

      int trimL = l - trim5p - trim3p;

      char[] all = dna.toCharArray();

      for (Mutation mutation : mutations) {
        int index = mutation.getIndex();

        if (index < 0 || index >= l) {
          continue;
        }

        char[] bases = dna.toCharArray();

        char to = mutation.getTo().charAt(0);

        bases[index] = to;
        all[index] = to;

        m2.set(r, 0, sequence.getId());
        m2.set(r, 1, sequence.getId() + "_" + mutation.toString());
        m2.set(r, 2, new String(bases, trim5p, trimL));

        ++r;
      }

      if (allMutationsMode) {
        m2.set(r, 0, sequence.getId());
        m2.set(r, 1, sequence.getId() + "_all");
        m2.set(r, 2, new String(all, trim5p, trimL));

        ++r;
      }

      if (wildMode) {
        m2.set(r, 0, sequence.getId());
        m2.set(r, 1, sequence.getId());
        m2.set(r, 2, dna.substring(trim5p, l - trim3p));

        ++r;
      }
    }

    m2.setColumnName(0, "Id");
    m2.setColumnName(1, "Mutation");
    m2.setColumnName(2, "DNA Sequence");

    mWindow.history().addToHistory("Mutations", m2);
  }
}
