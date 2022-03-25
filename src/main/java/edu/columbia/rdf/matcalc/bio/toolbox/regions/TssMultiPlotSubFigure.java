package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.util.List;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.text.TextUtils;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.statistics.HistBin;

/**
 * Draw peaks.
 * 
 * @author Antony Holmes
 *
 */
public class TssMultiPlotSubFigure extends SubFigure {

  private static final long serialVersionUID = 1L;

  public TssMultiPlotSubFigure(String title, String xPrefix, List<List<HistBin>> tssHists, double start, double end,
      double step) {
    super(title + " Distance");

    // set the graph limits

    // Create a series for each bedgraph in the group

    for (List<HistBin> tssHist : tssHists) {
      Axes axes = currentAxes();

      Plot plot = axes.currentPlot();

      plot.setBarWidth(1);

      DataFrame m = DataFrame.createDataFrame(tssHist.size(), 2);

      m.setColumnNames(CollectionUtils.toString("x", "y"));

      int r = 0;
      for (HistBin hist : tssHist) {
        m.set(r, 0, hist.getX());
        m.set(r, 1, hist.getCount());

        ++r;
      }

      plot.setMatrix(m);

      XYSeries series = new XYSeries("Distance");

      series.getStyle().getLineStyle().setColor(Color.RED);
      series.getStyle().getFillStyle().setColor(Color.RED);
      // series.getShape().getStyle().getFillStyle().setColor(Color.RED);
      // series.getShape().getLineStyle().setColor(Color.RED);

      currentAxes().currentPlot().getAllSeries().add(series);
    }

    // set the labels to be in logs

    currentAxes().getTitle().setText(getName());

    currentAxes().getX1Axis().setLimits(start, end, step);
    // getGraphSpace().getGraphProperties().getXAxisProperties().getMajorTicks().set(labels);

    if (!TextUtils.isNullOrEmpty(xPrefix)) {
      currentAxes().getX1Axis().getTitle().setText(xPrefix + " Distance (kb)");
    } else {
      currentAxes().getX1Axis().getTitle().setText("Distance (kb)");
    }

    currentAxes().setY1AxisLimitAutoRound();
    currentAxes().getY1Axis().getTitle().setText("Count");

    currentAxes().setInternalSize(600, 400);
  }
}
