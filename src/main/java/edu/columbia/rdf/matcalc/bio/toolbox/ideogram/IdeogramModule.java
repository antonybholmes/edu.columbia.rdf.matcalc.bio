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
package edu.columbia.rdf.matcalc.bio.toolbox.ideogram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jebtk.bioinformatics.ext.ucsc.CytobandsService;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomeService;
import org.jebtk.bioinformatics.genomic.Human;
import org.jebtk.core.Resources;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.tooltip.ModernToolTip;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.figure.graph2d.Graph2dWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

/**
 * Merges designated segments together using the merge column. Consecutive rows
 * with the same merge id will be merged together. Coordinates and copy number
 * will be adjusted but genes, cytobands etc are not.
 *
 * @author Antony Holmes
 *
 */
public class IdeogramModule extends Module implements ModernClickListener {

  public static final File RES_DIR = new File("res/modules/ideogram/genomes");

  private static final String NAME = "Ideogram";

  /**
   * The member button from human.
   */
  private RibbonLargeButton mButtonIdeogram = new RibbonLargeButton("Ideogram",
      AssetService.getInstance().loadIcon(IdeogramIcon.class, 24));

  /**
   * The member window.
   */
  private MainMatCalcWindow mWindow;

  private Graph2dWindow mGraphWindow;

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.NameProperty#getName()
   */
  @Override
  public String getName() {
    return NAME;
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
    mButtonIdeogram.setToolTip(
        new ModernToolTip(NAME, "Generate ideogram for losses and gains."));
    mWindow.getRibbon().getHomeToolbar().getSection("Tools")
        .add(mButtonIdeogram);

    mButtonIdeogram.addClickListener(this);
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
      ideogram();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  private void ideogram() throws IOException {
    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      showLoadMatrixError(mWindow);

      return;
    }

    Map<String, Integer> idColumns = findColumns(mWindow,
        m,
        "id",
        "chr",
        "start",
        "end",
        "mean");

    if (idColumns == null) {
      return;
    }

    IdeogramDialog dialog = new IdeogramDialog(mWindow,
        SettingsService.getInstance().getColor("ideogram.gains.color"),
        SettingsService.getInstance().getColor("ideogram.losses.color"));

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    // Save the colors as settings
    SettingsService.getInstance().update("ideogram.gains.color",
        dialog.getGainColor());

    SettingsService.getInstance().update("ideogram.losses.color",
        dialog.getLossColor());

    Genome genome = GenomeService.getInstance().guessGenome(dialog.getGenome());

    loadGenomeData(genome);

    Map<Chromosome, Integer> sampleYGain = new HashMap<Chromosome, Integer>();

    Map<Chromosome, Integer> sampleYLoss = new HashMap<Chromosome, Integer>();

    Map<Chromosome, Map<String, Integer>> yMapGain = new HashMap<Chromosome, Map<String, Integer>>();

    Map<Chromosome, Integer> rowCountGain = new HashMap<Chromosome, Integer>();

    Map<Chromosome, Map<String, Integer>> yMapLoss = new HashMap<Chromosome, Map<String, Integer>>();

    Map<Chromosome, Integer> rowCountLoss = new HashMap<Chromosome, Integer>();

    for (Chromosome chr : Human.CHROMOSOMES) {
      sampleYGain.put(chr, 1);
      sampleYLoss.put(chr, 1);
    }

    for (int i = 0; i < m.getRows(); ++i) {
      Chromosome chr = ChromosomeService.getInstance()
          .chr(genome, m.getText(i, idColumns.get("chr")));

      double mean = m.getValue(i, idColumns.get("mean"));

      if (mean >= 0) {
        if (!rowCountGain.containsKey(chr)) {
          rowCountGain.put(chr, 0);
        }

        rowCountGain.put(chr, rowCountGain.get(chr) + 1);
      } else {
        if (!rowCountLoss.containsKey(chr)) {
          rowCountLoss.put(chr, 0);
        }

        rowCountLoss.put(chr, rowCountLoss.get(chr) + 1);
      }
    }

    // create some ms

    Map<Chromosome, DataFrame> matrixMapGain = new HashMap<Chromosome, DataFrame>();

    for (Chromosome chr : Human.CHROMOSOMES) {
      if (rowCountGain.containsKey(chr)) {
        // The matrix must hold all gains.
        DataFrame matrix = DataFrame
            .createNumericalMatrix(rowCountGain.get(chr), 4);

        matrix.setColumnName(0, "Gains x1");
        matrix.setColumnName(1, "Gains y1");
        matrix.setColumnName(2, "Gains x2");
        matrix.setColumnName(3, "Gains y2");

        matrixMapGain.put(chr, matrix);
      }
    }

    Map<Chromosome, DataFrame> matrixMapLoss = new HashMap<Chromosome, DataFrame>();

    for (Chromosome chr : Human.CHROMOSOMES) {
      if (rowCountLoss.containsKey(chr)) {
        DataFrame matrix = DataFrame
            .createNumericalMatrix(rowCountLoss.get(chr), 4);

        matrix.setColumnName(0, "Losses x1");
        matrix.setColumnName(1, "Losses y1");
        matrix.setColumnName(2, "Losses x2");
        matrix.setColumnName(3, "Losses y2");

        matrixMapLoss.put(chr, matrix);
      }
    }

    // Reset the counters

    for (Chromosome chr : rowCountGain.keySet()) {
      rowCountGain.put(chr, 0);
    }

    for (Chromosome chr : rowCountLoss.keySet()) {
      rowCountLoss.put(chr, 0);
    }

    // sort by length

    Map<Chromosome, Map<Integer, List<Integer>>> orderMapGain = new TreeMap<Chromosome, Map<Integer, List<Integer>>>();

    Map<Chromosome, Map<Integer, List<Integer>>> orderMapLoss = new TreeMap<Chromosome, Map<Integer, List<Integer>>>();

    for (int i = 0; i < m.getRows(); ++i) {
      Chromosome chr = ChromosomeService.getInstance()
          .chr(genome, m.getText(i, idColumns.get("chr")));
      int start = (int) m.getValue(i, idColumns.get("start"));
      int end = (int) m.getValue(i, idColumns.get("end"));
      double mean = m.getValue(i, idColumns.get("mean"));
      int l = end - start + 1;

      if (mean >= 0) {
        if (!orderMapGain.containsKey(chr)) {
          orderMapGain.put(chr, new TreeMap<Integer, List<Integer>>());
        }

        if (!orderMapGain.get(chr).containsKey(l)) {
          orderMapGain.get(chr).put(l, new ArrayList<Integer>());
        }

        orderMapGain.get(chr).get(l).add(i);
      } else {
        if (!orderMapLoss.containsKey(chr)) {
          orderMapLoss.put(chr, new TreeMap<Integer, List<Integer>>());
        }

        if (!orderMapLoss.get(chr).containsKey(l)) {
          orderMapLoss.get(chr).put(l, new ArrayList<Integer>());
        }

        orderMapLoss.get(chr).get(l).add(i);
      }
    }

    for (Chromosome chr : orderMapGain.keySet()) {
      // order largest to smallest
      List<Integer> lorder = CollectionUtils
          .reverse(CollectionUtils.sort(orderMapGain.get(chr).keySet()));

      for (int l : lorder) {
        for (int i : orderMapGain.get(chr).get(l)) {
          String id = m.getText(i, idColumns.get("id"));
          int start = (int) m.getValue(i, idColumns.get("start"));
          int end = (int) m.getValue(i, idColumns.get("end"));

          if (!yMapGain.containsKey(chr)) {
            yMapGain.put(chr, new HashMap<String, Integer>());
          }

          if (!yMapGain.get(chr).containsKey(id)) {
            // allocate the next available row to a sample
            yMapGain.get(chr).put(id, sampleYGain.get(chr));

            // Set the next available row one higher
            sampleYGain.put(chr, sampleYGain.get(chr) + 1);
          }

          int y = yMapGain.get(chr).get(id);

          int r = rowCountGain.get(chr);

          matrixMapGain.get(chr).set(r, 0, start);
          matrixMapGain.get(chr).set(r, 1, y);
          matrixMapGain.get(chr).set(r, 2, end);
          matrixMapGain.get(chr).set(r, 3, y);

          rowCountGain.put(chr, rowCountGain.get(chr) + 1);

        }
      }
    }

    for (Chromosome chr : orderMapLoss.keySet()) {
      List<Integer> lorder = CollectionUtils
          .reverse(CollectionUtils.sort(orderMapLoss.get(chr).keySet()));

      for (int len : lorder) {
        for (int i : orderMapLoss.get(chr).get(len)) {
          String id = m.getText(i, idColumns.get("id"));
          int start = (int) m.getValue(i, idColumns.get("start"));
          int end = (int) m.getValue(i, idColumns.get("end"));

          if (!yMapLoss.containsKey(chr)) {
            yMapLoss.put(chr, new HashMap<String, Integer>());
          }

          if (!yMapLoss.get(chr).containsKey(id)) {
            yMapLoss.get(chr).put(id, sampleYLoss.get(chr));

            sampleYLoss.put(chr, sampleYLoss.get(chr) + 1);
          }

          int y = -yMapLoss.get(chr).get(id);

          int r = rowCountLoss.get(chr);

          matrixMapLoss.get(chr).set(r, 0, start);
          matrixMapLoss.get(chr).set(r, 1, y);
          matrixMapLoss.get(chr).set(r, 2, end);
          matrixMapLoss.get(chr).set(r, 3, y);

          rowCountLoss.put(chr, rowCountLoss.get(chr) + 1);
        }
      }
    }

    Figure figure = new CytobandsFigure(genome,
        CytobandsService.getInstance().getCytobands(genome),
        dialog.getGainColor(), matrixMapGain, dialog.getLossColor(),
        matrixMapLoss);

    mGraphWindow = new Graph2dWindow(mWindow, figure, false).removeFormatPane();
    mGraphWindow.setVisible(true);
  }

  private static void loadGenomeData(Genome genome) throws IOException {
    File dir = new File(RES_DIR, genome.getAssembly());

    CytobandsService.getInstance().load(genome,
        Resources.getGzipReader(
            new File(dir, "ucsc_cytobands_" + genome + ".txt.gz")));
  }
}