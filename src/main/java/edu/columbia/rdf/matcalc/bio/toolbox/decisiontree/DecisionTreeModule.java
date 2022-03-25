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
package edu.columbia.rdf.matcalc.bio.toolbox.decisiontree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jebtk.core.ColorUtils;
import org.jebtk.core.Mathematics;
import org.jebtk.core.xml.XmlUtils;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Axis;
import org.jebtk.graphplot.figure.BoxWhiskerScatterLayer2;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.LabelPlotLayer;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.graphplot.figure.series.XYSeriesGroup;
import org.jebtk.graphplot.icons.ShapeStyle;
import org.jebtk.math.Linspace;
import org.jebtk.math.machine.learning.C45;
import org.jebtk.math.machine.learning.DecisionTree;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.MatrixGroup;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.PlusVectorIcon;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.io.FileDialog;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
public class DecisionTreeModule extends Module {

  /**
   * The member convert button.
   */
  private RibbonLargeButton mClassifyButton = new RibbonLargeButton(
      "Decision Tree",
      AssetService.getInstance().loadIcon(RunVectorIcon.class, 24));

  private RibbonLargeButton mAddButton = new RibbonLargeButton(
      "Add Decision Tree",
      AssetService.getInstance().loadIcon(PlusVectorIcon.class, 24));

  private final static Logger LOG = LoggerFactory
      .getLogger(DecisionTreeModule.class);

  private static final int CLASSIFIER_PLOT_WIDTH = 100;

  // private RibbonLargeButton2 mExportButton = new RibbonLargeButton2("Export",
  // UIResources.getInstance().loadScalableIcon(SaveVectorIcon.class, 24));

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
    return "Classifier";
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
    mWindow.getRibbon().getToolbar("Classification").getSection("Decision Tree")
        .add(mClassifyButton);
    mWindow.getRibbon().getToolbar("Classification").getSection("Decision Tree")
        .add(mAddButton);
    // mWindow.getRibbon().getToolbar("Statistics").getSection("Classifier").add(mExportButton);

    mClassifyButton.addClickListener(new ModernClickListener() {
      @Override
      public void clicked(ModernClickEvent e) {
        classify();
      }
    });

    mAddButton.addClickListener(new ModernClickListener() {
      @Override
      public void clicked(ModernClickEvent e) {
        addDecisionTree();
      }
    });

    /*
     * mExportButton.addClickListener(new ModernClickListener() {
     * 
     * @Override public void clicked(ModernClickEvent e) { try { export(); }
     * catch (IOException e1) { e1.printStackTrace(); } }} );
     */
  }

  private void classify() {
    ClassifierDialog dialog = new ClassifierDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    DataFrame m = mWindow.getCurrentMatrix();

    DecisionTree tree = dialog.getDecisionTree();

    double[] values;

    DataFrame resultsM = DataFrame.createNumericalMatrix(m.getCols(), 1);

    for (int queryColumn = 0; queryColumn < m.getCols(); ++queryColumn) {
      // Score each sample in the query set

      values = m.columnToDouble(queryColumn);

      String classification = tree.classify(values);

      resultsM.setRowName(queryColumn, m.getColumnName(queryColumn));
      resultsM.set(queryColumn, 0, classification);
    }

    mWindow.history().addToHistory("Run classifier", resultsM);

    // plot(m, mWindow.getGroups(), classifiers, resultsM, summaries);
  }

  private void plot(DataFrame m,
      XYSeriesGroup groups,
      List<Classifier> classifiers,
      DataFrame resultsM,
      List<DataFrame> summaries) {

    // We need to create some series for each classifier

    Figure figure = Figure.createFigure();

    // SubFigure to hold new plot
    SubFigure subFigure = figure.newSubFigure();

    // We will use one set of axes for the whole plot
    Axes axes = subFigure.newAxes();

    // We add multiple plots to the figure, one for each classifier

    double max = 0;

    for (int ci = 0; ci < classifiers.size(); ++ci) {
      // Classifier classifier = classifiers.get(ci);

      DataFrame summaryM = summaries.get(ci);

      //
      // Summary
      //

      Plot plot = axes.newPlot();

      double confMin = summaryM.getValue(0, 4);
      double confMax = summaryM.getValue(0, 5);

      System.err.println("Conf " + confMin + " " + confMax);

      plot.addChild(new ConfidenceLayer(ci + 0.5, confMin, confMax));

      plot = axes.newPlot();
      plot.addChild(new BoxWhiskerScatterLayer2(ci + 0.5, 0.8));

      // Plot for each group

      for (XYSeries g : groups) {
        XYSeries series = new XYSeries(g.getName(), g.getColor());

        series.setMarker(ShapeStyle.CIRCLE);
        series.getMarkerStyle().getLineStyle().setColor(g.getColor());
        series.getMarkerStyle().getFillStyle()
            .setColor(ColorUtils.getTransparentColor50(g.getColor()));
        // series.getMarker().setSize(size);

        List<Integer> indices = MatrixGroup.findColumnIndices(m, g);

        DataFrame cm = DataFrame.createNumericalMatrix(indices.size(), 1);

        for (int i = 0; i < indices.size(); ++i) {
          double v = resultsM.getValue(indices.get(i), ci);

          cm.set(i, 0, v);

          max = Math.max(max, Math.abs(v));
        }

        series.setMatrix(cm);

        plot.getAllSeries().add(series);
      }
    }

    //
    // The axis
    //

    Axis axis = axes.getX1Axis();

    axis.setLimits(0, classifiers.size(), 1);
    axis.getTicks()
        .setTicks(Linspace.evenlySpaced(0.5, classifiers.size() - 0.5, 1));
    axis.getTicks().getMajorTicks().setRotation(-Mathematics.HALF_PI);

    // Don't render the axis, instead use labels to indicate the
    // phenotype and control
    axis.setVisible(false);
    axis.getTicks().getMajorTicks().getLineStyle().setVisible(false);
    axis.getTicks().getMinorTicks().getLineStyle().setVisible(false);
    axis.getLineStyle().setVisible(false);
    axis.getTitle().setVisible(false);

    // The labels are the series names

    List<String> labels = new ArrayList<String>(classifiers.size());

    for (Classifier c : classifiers) {
      labels.add(c.getName());
    }

    axis.getTicks().getMajorTicks().setLabels(labels);

    //
    // The y axis
    //

    axis = axes.getY1Axis();
    axis.setLimitsAutoRound(-max, max);
    axis.setShowZerothLine(true);
    axis.getTitle().setText("Classifier Score");

    // Add some classifier labels to indicate what the score means

    // Get the graph max after adjustments
    max = axis.getLimits().getMax();

    for (int ci = 0; ci < classifiers.size(); ++ci) {
      Classifier classifier = classifiers.get(ci);

      Plot plot = axes.newPlot();

      plot.addChild(new LabelPlotLayer(classifier.getPhenotypeName(), ci + 0.5,
          max, true, true, 0, -40));
      plot.addChild(new LabelPlotLayer(classifier.getControlName(), ci + 0.5,
          -max, true, true, 0, 40));
    }

    axes.setMargins(100);
    // axes.setBottomMargin(PlotFactory.autoSetX1LabelMargin(axes));

    axes.setInternalSize(classifiers.size() * CLASSIFIER_PLOT_WIDTH, 600);

    subFigure.setMargins(100);

    Graph2dWindow window = new Graph2dWindow(mWindow, figure, false);

    window.setVisible(true);
  }

  private void addDecisionTree() {
    if (mWindow.getGroups().size() == 0) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must create some groups.");

      return;
    }

    DataFrame m = mWindow.getCurrentMatrix();

    AddClassifierDialog dialog = new AddClassifierDialog(mWindow, m);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    DecisionTree decisionTree = C45.parseDouble(m, mWindow.getGroups());

    ClassifierService.getInstance().add(decisionTree);

    ModernMessageDialog.createInformationDialog(mWindow,
        "The classifier was created.");
  }

  private void export()
      throws TransformerException, ParserConfigurationException {
    writeXml(FileDialog.save(mWindow).filter(new ClassifierGuiFileFilter())
        .getFile(RecentFilesService.getInstance().getPwd()));
  }

  public final void writeXml(Path file)
      throws TransformerException, ParserConfigurationException {
    Document doc = XmlUtils.createDoc();

    doc.appendChild(ClassifierService.getInstance().toXml(doc));

    XmlUtils.writeXml(doc, file);

    // LOG.info("Wrote settings to {}", Path.getAbsoluteFile());
  }
}
