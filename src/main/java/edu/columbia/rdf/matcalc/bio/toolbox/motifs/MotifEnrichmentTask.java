package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.core.Mathematics;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.statistics.Hypergeometric;
import org.jebtk.modern.dialog.ModernMessageDialog;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.SearchSequence;
import edu.columbia.rdf.matcalc.bio.SequenceUtils;

public class MotifEnrichmentTask extends SwingWorker<Void, Void> {

  private DataFrame mNewModel = null;
  private final List<Motif> mMotifs;
  private final double mMinSensitivity;
  private final double mMinSpecificity;
  private final double mThreshold;
  private final MainMatCalcWindow mBackgroundGroup;
  private final MainMatCalcWindow mForegroundGroup;
  private final MainMatCalcWindow mParent;
  private final Genome mGenome;

  private static class SearchResult implements Comparable<SearchResult> {
    public double q;
    public Motif m;
    public Stats stats;
    public double p;

    @Override
    public int compareTo(SearchResult s) {
      if (p < s.p) {
        return -1;
      } else if (p > s.p) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /**
   * Create a new Enrichment task.
   * 
   * @param parent
   * @param genome
   * @param foregroundGroup
   * @param backgroundGroup
   * @param threshold
   * @param motifs
   * @param minSensitivity
   * @param minSpecificity
   */
  public MotifEnrichmentTask(MainMatCalcWindow parent, Genome genome, List<Motif> motifs,
      MainMatCalcWindow foregroundGroup, MainMatCalcWindow backgroundGroup, double threshold, double minSensitivity,
      double minSpecificity) {
    mParent = parent;
    mGenome = genome;
    mMotifs = motifs;
    mForegroundGroup = foregroundGroup;
    mBackgroundGroup = backgroundGroup;
    mThreshold = threshold;
    mMinSensitivity = minSensitivity;
    mMinSpecificity = minSpecificity;
  }

  @Override
  public Void doInBackground() {
    mNewModel = motifs();

    mNewModel.setName("Motif Enrichment");

    if (mNewModel != null && mNewModel.getRows() > 0) {
      mParent.openMatrices().open(mNewModel);
    } else {
      ModernMessageDialog.createWarningDialog(mParent, "There were no enriched motifs.");
    }

    return null;
  }

  private DataFrame motifs() {
    System.err.println("Search for motifs in foreground regions...");

    List<SearchSequence> foregroundSequences = SequenceUtils.matrixToSequences(mGenome,
        mForegroundGroup.getCurrentMatrix());

    List<SearchSequence> backgroundSequences = SequenceUtils.matrixToSequences(mGenome,
        mBackgroundGroup.getCurrentMatrix());

    return enrichmentMotifs(mThreshold, mMinSpecificity, mMinSensitivity, mMotifs, foregroundSequences,
        backgroundSequences);
  }

  public static DataFrame enrichmentMotifs(double threshold, double minSpecificity, double minSensitivity,
      List<Motif> motifs, List<SearchSequence> foregroundSequences, List<SearchSequence> backgroundSequences) {
    List<SearchSequence> foregroundRevCompSeqs = SearchSequence.reverseComplement(foregroundSequences);

    List<SearchSequence> backgroundRevCompSeqs = SearchSequence.reverseComplement(backgroundSequences);

    byte[][] iSeqs = SearchSequence.toIndex(foregroundSequences);
    byte[][] iRevCompSeqs = SearchSequence.toIndex(foregroundRevCompSeqs);

    // Map<Integer, Map<Integer, Collection<Integer>>> tripletMap =
    // MotifSearch.mapTriplets(iSeqs);

    // Map<Integer, Map<Integer, Collection<Integer>>> revTripletMap =
    // MotifSearch.mapTriplets(iRevCompSeqs);

    byte[][] iBackSeqs = SearchSequence.toIndex(backgroundSequences);
    byte[][] iBackRevCompSeqs = SearchSequence.toIndex(backgroundRevCompSeqs);

    // Map<Integer, Map<Integer, Collection<Integer>>> tripletMapBack =
    // MotifSearch.mapTriplets(iBackSeqs);

    // Map<Integer, Map<Integer, Collection<Integer>>> revTripletMapBack =
    // MotifSearch.mapTriplets(iBackRevCompSeqs);

    MotifsModule.LOG.info("Calculating enrichment...");

    boolean[] goldStandard = MotifSearch.createGoldStandard(foregroundSequences.size(), backgroundSequences.size());

    int numSequences = foregroundSequences.size() + backgroundSequences.size();

    // Store the max score associated with sequence for a given motif
    double[] bestScores = new double[numSequences];

    Hypergeometric hyg = new Hypergeometric();

    List<SearchResult> results = new ArrayList<SearchResult>();

    int c = 1;

    MotifSearch ms = new MotifSearch();

    for (Motif m : motifs) {
      // Set the threshold to be at least half the max score of the motif
      double t = MotifSearch.getMaxScore(m) * threshold;

      int w = m.getBaseCount();

      // double[][] pwm = m.getPwm();

      ms.bestScores(m, w, iSeqs, iRevCompSeqs, t, 0, bestScores);

      /*
       * MotifSearch.bestScores(m, w, iSeqs, tripletMap, iRevCompSeqs, revTripletMap,
       * t, 0, bestScores);
       */

      /*
       * MotifSearch.bestScores(m, w, foregroundSequences, foregroundRevCompSeqs, t,
       * 0, bestScores);
       */

      ms.bestScores(m, w, iBackSeqs, iBackRevCompSeqs, t, foregroundSequences.size(), bestScores);

      /*
       * MotifSearch.bestScores(m, w, iBackSeqs, tripletMapBack, iBackRevCompSeqs,
       * revTripletMapBack, t, foregroundSequences.size(), bestScores);
       */

      /*
       * MotifSearch.bestScores(m, w, backgroundSequences, backgroundRevCompSeqs, t,
       * foregroundSequences.size(), bestScores);
       */

      Stats stats = MotifSearch.enrichmentByMinError(bestScores, goldStandard, minSensitivity, minSpecificity);

      // Once the error has been minimized, we can calculate the rest of the
      // stats
      if (stats.error < 1) {
        double p = hyg.cdfOneTail(stats.truePositive, foregroundSequences.size(),
            stats.truePositive + stats.falsePositive, numSequences);

        SearchResult sr = new SearchResult();

        sr.m = m;
        sr.stats = stats;
        sr.p = Mathematics.bound(p, 0, 1);

        results.add(sr);
      }

      if (c % 100 == 0) {
        MotifsModule.LOG.info("Processed {} motifs.", c);
      }

      ++c;
    }

    // Sort by p-value
    Collections.sort(results);

    // Calculate BH FDR

    for (int i = 0; i < results.size(); ++i) {
      double q = results.get(i).p * results.size() / (i + 1);

      results.get(i).q = Mathematics.bound(q, 0, 1);
    }

    DataFrame matrix = DataFrame.createDataFrame(results.size(), 12);

    // The header
    matrix.setColumnName(0, "Motif Name (threshold=" + Double.toString(threshold) + ")");
    matrix.setColumnName(1, "Motif ID");
    matrix.setColumnName(2, "Motif Database");
    matrix.setColumnName(3, "-Log10(P (X >= TP))");
    matrix.setColumnName(4, "-Log10(Q (X >= TP))");
    matrix.setColumnName(5, "True Positive");
    matrix.setColumnName(6, "False Negative");
    matrix.setColumnName(7, "False Positive");
    matrix.setColumnName(8, "True Negative");
    matrix.setColumnName(9, "Sensitivity (min=" + Double.toString(minSensitivity) + ")");
    matrix.setColumnName(10, "Specificity (min=" + Double.toString(minSpecificity) + ")");
    matrix.setColumnName(11, "Error");

    int r = 0;

    for (SearchResult sr : results) {
      matrix.set(r, 0, sr.m.getName());
      matrix.set(r, 1, sr.m.getId());
      matrix.set(r, 2, sr.m.getDatabase());
      matrix.set(r, 3, -Mathematics.log10(sr.p));
      matrix.set(r, 4, -Mathematics.log10(sr.q));
      matrix.set(r, 5, sr.stats.truePositive);
      matrix.set(r, 6, sr.stats.falseNegative);
      matrix.set(r, 7, sr.stats.falsePositive);
      matrix.set(r, 8, sr.stats.trueNegative);
      // true positive rate
      matrix.set(r, 9, sr.stats.sensitivity);
      // true negative rate
      matrix.set(r, 10, sr.stats.specificity);
      matrix.set(r, 11, sr.stats.error);

      ++r;
    }

    return matrix;
  }
}
