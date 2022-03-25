package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.jebtk.bioinformatics.gapsearch.BinaryGapSearch;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.core.io.Io;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.Annotation;

public class DistancePlotTask extends SwingWorker<Void, Void> {

  private BinaryGapSearch<Annotation> mSearch;
  private double mStart;
  private double mEnd;
  private int mUnits;
  private double mBinSize;
  private int mBinUnits;
  private MainMatCalcWindow mWindow;
  private Genome mGenome;

  public DistancePlotTask(MainMatCalcWindow window, Genome genome, BinaryGapSearch<Annotation> gappedSearch,
      double start, double end, int units, double binSize, int binUnits) {
    mWindow = window;
    mGenome = genome;
    mSearch = gappedSearch;

    mStart = start;
    mEnd = end;
    mUnits = units;
    mBinSize = Math.abs(binSize);
    mBinUnits = binUnits;
  }

  @Override
  public Void doInBackground() {
    plot();

    return null;
  }

  public void plot() {
    DataFrame model = mWindow.getCurrentMatrix();

    List<Double> tssPoints = new ArrayList<Double>();

    for (int i = 0; i < model.getRows(); ++i) {
      GenomicRegion region = null;

      if (Io.isEmptyLine(model.getText(i, 0))) {
        continue;
      }

      if (model.getText(i, 0).contains(TextUtils.NA)) {
        continue;
      }

      if (GenomicRegion.isGenomicRegion(model.getText(i, 0))) {
        region = GenomicRegion.parse(mGenome, model.getText(i, 0));
      } else {
        // three column format

        region = new GenomicRegion(ChromosomeService.getInstance().chr(mGenome, model.getText(i, 0)),
            model.getInt(i, 1), model.getInt(i, 2));
      }

      // System.err.println("region: " + region);

      GenomicRegion midPoint = GenomicRegion.midRegion(region);

      // Find Gene TSS near the midpoint
      List<Annotation> results = mSearch.getClosestFeatures(midPoint);

      if (results != null) {
        double min = Double.MAX_VALUE;
        double d;
        // region is from file 1
        for (Annotation annotation : results) {
          d = GenomicRegion.midDist(region, annotation.getRegion()); // ,
                                                                     // region);

          if (Math.abs(d) < Math.abs(min)) {
            min = d;
          }

        }

        System.err.println("tss " + min);

        tssPoints.add(min);
      }
    }

    List<Double> plotTssPoints = new ArrayList<Double>();

    double s = mStart * mUnits;
    double e = mEnd * mUnits;

    System.err.println("s " + s + " " + e + " " + (mBinSize * mBinUnits / mUnits));

    for (double x : tssPoints) {
      if (x < s || x > e) {
        continue;
      }

      plotTssPoints.add(x / mUnits);
    }

    TssPlotTask.plot(mWindow, tssPoints, mStart, mEnd, mUnits, mBinSize, mBinUnits);

    /*
     * List<HistBin> tssHist = Statistics.histogram(plotTssPoints, mStart, mEnd,
     * mBinSize * mBinUnits / mUnits);
     * 
     * TssPlotCanvas tssCanvas = new
     * TssPlotCanvas(TextUtils.truncate(PathUtils.getName(mFile2), 50), null,
     * tssHist, mStart, mEnd, Math.pow(10,
     * Math.floor(Math.log10(Math.abs(mStart)))));
     * 
     * 
     * 
     * List<Double> log10TssPoints = new ArrayList<Double>();
     * 
     * for (double x : tssPoints) { double v = Mathematics.log10(Math.abs(x) + 1);
     * 
     * if (v <= 8) { log10TssPoints.add(v); } }
     * 
     * List<HistBin> log10TssHist = Statistics.histogram(log10TssPoints, 0, 8, 0.1);
     * 
     * System.err.println(log10TssHist.size());
     * 
     * Log10TssPlotCanvas tssLogCanvas = new
     * Log10TssPlotCanvas(TextUtils.truncate(PathUtils.getName(mFile2), 50), null,
     * log10TssHist);
     * 
     * Figure figure = new Figure(new FigureLayoutGrid(1, 2));
     * figure.getSubFigureZModel().addChild(tssCanvas);
     * figure.getSubFigureZModel().addChild(tssLogCanvas);
     * 
     * Graph2dWindow plotWindow = new Graph2dWindow(mWindow, figure);
     * 
     * plotWindow.getStyle().set(PlotStyle.BARS);
     * 
     * plotWindow.setVisible(true);
     */
  }
}