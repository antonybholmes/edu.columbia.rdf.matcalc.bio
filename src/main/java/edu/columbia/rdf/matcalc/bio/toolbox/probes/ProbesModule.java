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
package edu.columbia.rdf.matcalc.bio.toolbox.probes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.jebtk.bioinformatics.ext.ucsc.ChipFile;
import org.jebtk.bioinformatics.genomic.ProbeGene;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

/**
 * Map probes to genes.
 *
 * @author Antony Holmes
 *
 */
public class ProbesModule extends Module implements ModernClickListener {

  /**
   * The member convert button.
   */
  private RibbonLargeButton mConvertButton = new RibbonLargeButton("Probes",
      AssetService.getInstance().loadIcon(ProbesIcon.class, 24));

  public static final Path DIR = PathUtils.getPath("res/modules/probes");

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
    return "Probes";
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
    mWindow.getRibbon().getToolbar("Bioinformatics").getSection("Probes")
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
      mapProbes();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  private void mapProbes() throws IOException {
    DataFrame m = mWindow.getCurrentMatrix();

    int c = mWindow.getSelectedColumn();

    if (c == Integer.MIN_VALUE) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must select a column of probe ids.");

      return;
    }

    System.err.println("c " + c);

    ProbesDialog dialog = new ProbesDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    Path file = dialog.getFile();

    int cols = m.getCols();

    DataFrame m2 = DataFrame.createDataFrame(m.getRows(), cols + 2);

    DataFrame.copyColumns(m, m2);

    // load the chip map
    Map<String, ProbeGene> probeGeneMap = ChipFile.parseChipFile(file);

    for (int i = 0; i < m.getRows(); ++i) {
      String name = m.getText(i, c);

      if (probeGeneMap.containsKey(name)) {
        m2.set(i, cols, probeGeneMap.get(name).getGeneSymbol());
        m2.set(i, cols + 1, probeGeneMap.get(name).getDescription());
      } else {
        m2.set(i, cols, TextUtils.NA);
        m2.set(i, cols + 1, TextUtils.NA);
      }
    }

    m2.setColumnName(cols, "Gene Symbol");
    m2.setColumnName(cols + 1, "Description");

    mWindow.history().addToHistory("Probes to genes", m2);
  }
}
