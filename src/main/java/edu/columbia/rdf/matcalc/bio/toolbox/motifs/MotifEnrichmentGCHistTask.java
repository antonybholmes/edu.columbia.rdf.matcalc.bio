package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.jebtk.bioinformatics.gapsearch.BinaryGapSearch;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.Sequence;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.bioinformatics.genomic.SequenceRegion;
import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.statistics.HistBin;
import org.jebtk.math.statistics.Statistics;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.SearchSequence;
import edu.columbia.rdf.matcalc.bio.SequenceUtils;

public class MotifEnrichmentGCHistTask extends SwingWorker<Void, Void> {

  private DataFrame mNewModel = null;
  private List<Motif> mMotifs;
  private double mMinSensitivity;
  private double mMinSpecificity;
  private double mThreshold;
  private MainMatCalcWindow mForegroundSeqWindow;
  private SequenceReader mAssembly;
  private List<SearchSequence> mBackgroundSequences;
  private MainMatCalcWindow mParent;
  private Genome mGenome;

  /**
   * Create a new Enrichment task.
   * 
   * @param parent
   * @param statusModel
   * @param motifs
   * @param peaks
   * @param extension
   * @param minSensitivity
   * @param minSpecificity
   */
  public MotifEnrichmentGCHistTask(MainMatCalcWindow parent, Genome genome, SequenceReader assembly, List<Motif> motifs,
      MainMatCalcWindow foregroundSeqWindow, double threshold, double minSensitivity, double minSpecificity) {
    mParent = parent;
    mGenome = genome;
    mAssembly = assembly;
    mMotifs = motifs;
    mForegroundSeqWindow = foregroundSeqWindow;
    mThreshold = threshold;
    mMinSensitivity = minSensitivity;
    mMinSpecificity = minSpecificity;
  }

  @Override
  public Void doInBackground() {
    try {
      mNewModel = motifs();

      mNewModel.setName("Motif Enrichment");

      mParent.openMatrices().open(mNewModel);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private DataFrame motifs() throws Exception {
    System.err.println("Searching for motifs in foreground regions...");

    List<SearchSequence> foregroundSequences = SequenceUtils.matrixToSequences(mGenome,
        mForegroundSeqWindow.getCurrentMatrix());

    //
    // Determine the GC content of the sequences
    //

    System.err.println("Assessing GC content in foreground...");

    List<Double> gcs = new ArrayList<Double>();

    for (SearchSequence sr : foregroundSequences) {
      double gc = Sequence.gcContent(sr.getDna());

      gcs.add(gc);
    }

    //
    // Bin GC into histogram
    //

    System.err.println("Creating GC distribution...");

    double d = 0.05;

    List<HistBin> gcHist = Statistics.histogram(gcs, 0, 1, d);

    for (int i = 0; i < gcHist.size(); ++i) {
      MotifsModule.LOG.info("GC {} {} {}", i, i * d, gcHist.get(i).getCount());
    }

    //
    // Now to make a random dist of sequences to match the gc
    // content of our foreground sequnces.
    //

    MotifsModule.LOG.info("Creating GC matched distribution of random sequences...");

    // Index foreground reads
    BinaryGapSearch<SearchSequence> foregroundGapped = new BinaryGapSearch<SearchSequence>();

    for (int i = 0; i < foregroundSequences.size(); ++i) {
      GenomicRegion r = foregroundSequences.get(i).getRegion();

      foregroundGapped.add(r, foregroundSequences.get(i));
    }

    int sequenceLength = foregroundSequences.get(0).getDna().length();

    mBackgroundSequences = new ArrayList<SearchSequence>();

    for (int i = 0; i < gcHist.size(); ++i) {
      if (gcHist.get(i).getCount() == 0) {
        continue;
      }

      int j = 0;

      MotifsModule.LOG.info("GC {} {}", i, gcHist.get(i).getCount());

      while (j < gcHist.get(i).getCount()) {
        // Get a random sequence
        SequenceRegion rs = Sequence.getRandomSequence(mGenome, mAssembly, sequenceLength);

        //
        // Make sure random sequence does not overlap the
        // foreground sequences
        //

        List<SearchSequence> checkSequences = foregroundGapped.getClosestFeatures(rs);

        if (checkSequences != null) {
          boolean overlap = false;

          for (SearchSequence region : checkSequences) {
            if (GenomicRegion.overlap(region.getRegion(), rs) != null) {
              overlap = true;
              break;
            }
          }

          if (overlap) {
            continue;
          }
        }

        //
        // Match the GC content to the current bin we are
        // trying to replicate with random sequences. Keep
        // sampling until we get enough sequences.
        //

        double gc = Sequence.gcContent(rs.getSequence());

        // MotifsModule.LOG.info("gc {} {}", gc, (int)(gc / d));

        int bin = (int) (gc / d);

        if (bin != i) {
          continue;
        }

        mBackgroundSequences.add(new SearchSequence(rs, rs.getSequence()));

        // LOG.info("GC {} {} {} {} {} {}", gc, bin, i, j, gcHist.get(i),
        // mBackgroundSequences.size());

        ++j;
      }
    }

    return MotifEnrichmentTask.enrichmentMotifs(mThreshold, mMinSpecificity, mMinSensitivity, mMotifs,
        foregroundSequences, mBackgroundSequences);
  }
}
