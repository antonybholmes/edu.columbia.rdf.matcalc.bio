package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
public class Log10TssSubFigure extends SubFigure {

  private static final long serialVersionUID = 1L;

  /** GRAPH must show Y axis with some minimum value */
  public static final double MIN_Y = 10;

  public Log10TssSubFigure(String title, String xPrefix, List<HistBin> log10TssHist) {
    super(title + " Log10 Distance");

    Axes axes = currentAxes();

    Plot plot = axes.currentPlot();

    plot.setBarWidth(1);

    DataFrame m = DataFrame.createNumericalMatrix(log10TssHist.size(), 2);

    m.setColumnNames("x", "y");

    int r = 0;
    for (HistBin hist : log10TssHist) {
      m.set(r, 0, hist.getX());
      m.set(r, 1, hist.getCount());

      // System.err.println("r " + r + " " + hist.getX() + " " +
      // hist.getCount());

      ++r;
    }

    plot.setMatrix(m);

    XYSeries series = new XYSeries("Log10 Distance");
    series.addRegex("x", "y");

    series.getStyle().getLineStyle().setColor(Color.RED);
    series.getStyle().getFillStyle().setColor(Color.RED);
    series.getMarkerStyle().setVisible(false);
    // series.getShape().getStyle().getFillStyle().setColor(Color.RED);
    // series.getShape().getStyle().getLineStyle().setColor(Color.RED);

    axes.currentPlot().getAllSeries().add(series);

    // set the labels to be in logs

    DecimalFormat formatter = new DecimalFormat("0E0");

    List<String> logScale = new ArrayList<String>();

    for (int i = 0; i <= 8; ++i) {
      logScale.add(formatter.format((int) Math.pow(10, i)));
    }

    axes.getTitle().setText(getName());

    axes.getX1Axis().setLimits(0, 8, 1);
    axes.getX1Axis().getTicks().getMajorTicks().setLabels(logScale);

    if (!TextUtils.isNullOrEmpty(xPrefix)) {
      axes.getX1Axis().getTitle().setText(xPrefix + " Absolute Distance (bp)");
    } else {
      axes.getX1Axis().getTitle().setText("Absolute Distance (bp)");
    }

    axes.getY1Axis().setLimitsAutoRound(0, Math.max(MIN_Y, XYSeries.getYMax(m, series)));
    axes.getY1Axis().getTitle().setText("Count");

    axes.setInternalSize(800, 400);

    axes.setMargins(100);
  }
}
