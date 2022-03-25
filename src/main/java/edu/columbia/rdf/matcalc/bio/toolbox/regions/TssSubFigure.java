package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.util.List;

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
public class TssSubFigure extends SubFigure {

  private static final long serialVersionUID = 1L;

  public TssSubFigure(String title, String xPrefix, List<HistBin> tssHist, double start, double end, double step) {
    super(title);

    // set the graph limits

    Axes axes = currentAxes();

    Plot plot = axes.currentPlot();

    plot.setBarWidth(1);

    DataFrame m = DataFrame.createNumericalMatrix(tssHist.size(), 2);

    m.setColumnNames("x", "y");

    int r = 0;
    for (HistBin hist : tssHist) {
      m.set(r, 0, hist.getX());
      m.set(r, 1, hist.getCount());

      // System.err.println("r " + r + " " + hist.getX() + " " +
      // hist.getCount());

      ++r;
    }

    plot.setMatrix(m);

    XYSeries series = new XYSeries("Distance");
    series.addRegex("x", "y");

    series.getStyle().getLineStyle().setColor(Color.RED);
    series.getStyle().getFillStyle().setColor(Color.RED);
    series.getMarkerStyle().setVisible(false);
    // series.getShape().getStyle().getFillStyle().setColor(Color.RED);
    // series.getShape().getLineStyle().setColor(Color.RED);

    plot.getAllSeries().add(series);

    // set the labels to be in logs

    if (title != null) {
      axes.getTitle().setText(title + " Distance");
    } else {
      axes.getTitle().setText("Distance");
    }

    System.err.println("ticks " + start + " " + end + " " + step);

    axes.getX1Axis().setLimits(start, end, step);
    // getGraphSpace().getGraphProperties().getXAxisProperties().getMajorTicks().set(labels);

    if (xPrefix != null) {
      axes.getX1Axis().getTitle().setText(xPrefix + " Distance (kb)");
    } else {
      axes.getX1Axis().getTitle().setText("Distance (kb)");
    }

    axes.getY1Axis().setLimitsAutoRound(0, Math.max(Log10TssSubFigure.MIN_Y, XYSeries.getYMax(m, series)));
    axes.getY1Axis().getTitle().setText("Count");

    axes.setInternalSize(600, 400);

    axes.setMargins(100);
  }
}
