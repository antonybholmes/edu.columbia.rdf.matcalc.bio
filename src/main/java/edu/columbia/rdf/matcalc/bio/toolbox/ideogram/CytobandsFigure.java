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

import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jebtk.bioinformatics.ext.ucsc.Cytobands;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Human;
import org.jebtk.bioinformatics.ui.external.ucsc.CytobandsLayer;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.graphplot.PlotFactory;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.FigureVertAlignment;
import org.jebtk.graphplot.figure.LabelAxesLayer;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.graphplot.plotbox.PlotBoxGridLayout;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.DoubleMatrix;

/**
 * Layout out peak plots in a column.
 * 
 * @author Antony Holmes
 *
 */
public class CytobandsFigure extends Figure {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private static final double PLOT_WIDTH = SettingsService.getInstance()
      .getInt("ideogram.plot-width");

  private static final int PLOT_ROW_HEIGHT = SettingsService.getInstance()
      .getInt("ideogram.separation");

  private Axes axes;

  // private static final Color GAINS_COLOR =
  // SettingsService.getInstance().getColor("ideogram.gains.color");

  // private static final Color LOSSES_COLOR =
  // SettingsService.getInstance().getColor("ideogram.losses.color");

  private static final int OFFSET = 10;

  /**
   * Sets the view.
   *
   * @param view the new view
   */
  public CytobandsFigure(Genome genome,
      final Cytobands cytobands,
      final Color gainColor,
      final Map<Chromosome, DataFrame> matrixMapGain, final Color lossColor,
      final Map<Chromosome, DataFrame> matrixMapLoss) {
    super("Cytobands Figure", new PlotBoxGridLayout(36, 2));

    // lets see which genome is longest

    int maxLength = ChromosomeService.getInstance().size(genome, ChromosomeService.getInstance().chr(genome, "chr1"));
    // Chromosome longestChr = Chromosome.CHR1;

    Chromosome chr;

    for (int i = 0; i < 12; ++i) {
      // Gains
      chr = Human.CHROMOSOMES[i];

      createGainPlot(cytobands,
          genome,
          chr,
          maxLength,
          gainColor,
          matrixMapGain);

      chr = Human.CHROMOSOMES[i + 12];

      createGainPlot(cytobands,
          genome,
          chr,
          maxLength,
          gainColor,
          matrixMapGain);

      //
      // Cytobands
      //

      chr = Human.CHROMOSOMES[i];

      createBands(cytobands, genome, chr, maxLength);

      chr = Human.CHROMOSOMES[i + 12];

      createBands(cytobands, genome, chr, maxLength);

      //
      // Losses
      //

      chr = Human.CHROMOSOMES[i];

      createLossPlot(cytobands,
          genome,
          chr,
          maxLength,
          lossColor,
          matrixMapLoss);

      chr = Human.CHROMOSOMES[i + 12];

      createLossPlot(cytobands,
          genome,
          chr,
          maxLength,
          lossColor,
          matrixMapLoss);
    }
  }

  private void createGainPlot(final Cytobands cytobands,
      final Genome genome,
      final Chromosome chr,
      int maxLength,
      final Color gainColor,
      final Map<Chromosome, DataFrame> matrixMap) {

    DataFrame m = matrixMap.get(chr);

    axes = createPlot("Gains",
        cytobands,
        genome,
        chr,
        maxLength,
        matrixMap,
        gainColor,
        FigureVertAlignment.BOTTOM);

    if (matrixMap.containsKey(chr)) {
      axes.getY1Axis().setLimits(0, DoubleMatrix.maxInColumn(m, 1) + 1);
    }

    // axes.getTitle().setText(chr.toString());
    // axes.getMargins().setTop(0);
    // axes.getMargins().setBottom(OFFSET);
  }

  private void createLossPlot(final Cytobands cytobands,
      final Genome genome,
      final Chromosome chr,
      int maxLength,
      final Color lossColor,
      final Map<Chromosome, DataFrame> matrixMap) {

    DataFrame m = matrixMap.get(chr);

    axes = createPlot("Losses",
        cytobands,
        genome,
        chr,
        maxLength,
        matrixMap,
        lossColor,
        FigureVertAlignment.TOP);

    // Axes.enableAllFeatures(axes);

    if (matrixMap.containsKey(chr)) {
      axes.getY1Axis().setLimits(DoubleMatrix.minInColumn(m, 1) - 1, 0);
    }

    // axes.getMargins().setTop(OFFSET);
    axes.setBottomMargin(OFFSET);
  }

  private Axes createPlot(String name,
      Cytobands cytobands,
      Genome genome,
      Chromosome chr,
      int maxLength,
      Map<Chromosome, DataFrame> matrixMap,
      Color color,
      FigureVertAlignment alignment) {

    int size = ChromosomeService.getInstance().size(genome, chr);

    //
    // Gains
    //

    SubFigure subFigure = newSubFigure(); // SubFigureService.getInstance().createNewFigure();

    subFigure.setVertAlignment(alignment);

    Axes axes = subFigure.newAxes();

    if (matrixMap.containsKey(chr)) {
      DataFrame m = matrixMap.get(chr);

      XYSeries series = new XYSeries(name);

      series.getStyle().getFillStyle().setColor(color);
      series.getStyle().getLineStyle().setColor(color);
      series.getStyle().getLineStyle().setStroke(2);

      PlotFactory.createSegmentsPlot(m, axes, series);

      axes.getX1Axis().setLimits(0, size);

      int h = getUniqueY(m);

      axes.setInternalSize((int) (PLOT_WIDTH * (double) size / maxLength),
          PLOT_ROW_HEIGHT * h);
    } else {
      axes.setInternalSize((int) (PLOT_WIDTH * (double) size / maxLength),
          PLOT_ROW_HEIGHT);
    }

    Axes.disableAllFeatures(axes);

    axes.setMargins(0, 100, 0, OFFSET);

    // getSubFigureZModel().setZ(figure);

    return axes;
  }

  /**
   * See how many unique y's and therefore rows we need.
   * 
   * @param m
   * @return
   */
  private static int getUniqueY(final DataFrame m) {
    Set<Double> set = new HashSet<Double>();

    for (int i = 0; i < m.getRows(); ++i) {
      set.add(m.getValue(i, 1));
    }

    return set.size();
  }

  private void createBands(Cytobands cytobands,
      Genome genome,
      Chromosome chr,
      int maxLength) {

    int size = ChromosomeService.getInstance().size(genome, chr);

    //
    // Cytobands
    //

    SubFigure figure = newSubFigure(); // SubFigureService.getInstance().createNewFigure();

    Axes axes = figure.newAxes();

    CytobandsLayer layer = new CytobandsLayer(cytobands);

    axes.addChild(layer);
    axes.getX1Axis().setLimits(0, size);
    axes.getY1Axis().setLimits(0, 1);
    axes.setMargins(0, 100, 0, OFFSET);

    axes.addChild(new LabelAxesLayer(chr.toString(), 0, 0, -60, -5));

    axes.setInternalSize((int) (PLOT_WIDTH * (double) size / maxLength), 24);

    Axes.disableAllFeatures(axes);

    // axes.getTitle().setText(chr.toString());

    // getSubFigureZModel().setZ(figure);
  }
}
