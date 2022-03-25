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
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.statistics.HistBin;
import org.jebtk.math.statistics.Statistics;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.SearchSequence;
import edu.columbia.rdf.matcalc.bio.SequenceUtils;

public class GCBackgroundTask extends SwingWorker<Void, Void> {

  private static final int MAX_ATTEMPTS = 10000;
  private DataFrame mNewModel = null;
  private final SequenceReader mAssembly;

  private final MainMatCalcWindow mParent;
  private final Genome mGenome;

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
  public GCBackgroundTask(MainMatCalcWindow parent, Genome genome, SequenceReader assembly) {
    mParent = parent;
    mGenome = genome;
    mAssembly = assembly;
  }

  @Override
  public Void doInBackground() {
    try {
      mNewModel = motifs(mGenome);

      mNewModel.setName("GC Background Sequences");

      mParent.openMatrices().newWindow().open(mNewModel);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private DataFrame motifs(Genome genome) throws Exception {
    MotifsModule.LOG.info("Searching for motifs in foreground regions...");

    List<SearchSequence> foregroundSequences = SequenceUtils.matrixToSequences(genome, mParent.getCurrentMatrix());

    //
    // Determine the GC content of the sequences
    //

    MotifsModule.LOG.info("Assessing GC content in foreground...");

    List<Double> gcs = new ArrayList<Double>();

    for (SearchSequence sr : foregroundSequences) {
      double gc = Sequence.gcContent(sr.getDna());

      gcs.add(gc);
    }

    //
    // Bin GC into histogram
    //

    MotifsModule.LOG.info("Creating GC distribution...");

    double d = 0.05;

    List<HistBin> gcHist = Statistics.histogram(gcs, 0, 1, d);

    for (int i = 0; i < gcHist.size(); ++i) {
      MotifsModule.LOG.info("GC {} {} {}", i, i * d, gcHist.get(i).getCount());
    }

    //
    // Now to make a random dist of sequences to match the gc
    // content of our foreground sequences.
    //

    MotifsModule.LOG.info("Creating GC matched distribution of random sequences...");

    // Index foreground reads
    BinaryGapSearch<SearchSequence> foregroundGapped = new BinaryGapSearch<SearchSequence>();

    int l = 0;

    for (int i = 0; i < foregroundSequences.size(); ++i) {
      foregroundGapped.add(foregroundSequences.get(i).getRegion(), foregroundSequences.get(i));

      l += foregroundSequences.get(i).getDna().length();
    }

    // Use the average length to generate the background
    int sequenceLength = l / foregroundSequences.size(); // foregroundSequences.get(0).getLength();

    List<SequenceRegion> backgroundSequences = new ArrayList<SequenceRegion>();

    for (int i = 0; i < gcHist.size(); ++i) {
      if (gcHist.get(i).getCount() == 0) {
        continue;
      }

      MotifsModule.LOG.info("Creating GC matched sequences for bin {}, size {}...", i, gcHist.get(i).getCount());

      double refGC = i * d;

      // For each bin, fill it up with as many sequences as required

      // int j = 0;

      // while (j < gcHist.get(i).getCount()) {

      for (int j = 0; j < gcHist.get(i).getCount(); ++j) {
        SequenceRegion rs = null;

        // Find the closest sequence by gc content even if its not
        // what is required
        SequenceRegion crs = null;

        // The closest non overlapping sequence we could find
        SequenceRegion crsNoOverlap = null;

        boolean found = false;

        double minGC = Double.MAX_VALUE;

        for (int t = 0; t < MAX_ATTEMPTS; ++t) {
          // Random sequence
          rs = Sequence.getRandomSequence(genome, mAssembly, sequenceLength);

          double gc = Sequence.gcContent(rs.getSequence());

          double gcd = Math.abs(refGC - gc);

          //
          // Make sure random sequence does not overlap the
          // foreground sequences
          //

          List<SearchSequence> checkSequences = foregroundGapped.getClosestFeatures(rs);

          boolean overlap = false;

          if (checkSequences != null) {
            for (SearchSequence s : checkSequences) {
              if (GenomicRegion.overlap(s.getRegion(), rs) != null) {
                overlap = true;
                break;
              }
            }
          }

          if (gcd < minGC) {
            minGC = gcd;

            crs = rs;

            if (!overlap) {
              crsNoOverlap = rs;
            }
          }

          //
          // Match the GC content to the current bin we are
          // trying to replicate with random sequences. Keep
          // sampling until we get enough sequences.
          //

          // MotifsModule.LOG.info("gc {} {}", gc, (int)(gc / d));

          if (!overlap) {
            int bin = (int) (gc / d);

            if (bin == i) {
              backgroundSequences.add(rs);

              // LOG.info("GC {} {} {} {} {} {}", gc, bin, i, j, gcHist.get(i),
              // mBackgroundSequences.size());

              found = true;

              break;
            }
          }
        }

        if (!found) {
          // If nothing was found after 10000 iterations, use
          // the best we found

          if (crsNoOverlap != null) {
            // Preferentially add the sequence that was not
            // overlapping
            MotifsModule.LOG.info("GC wrong, no overlap: {} {}...", refGC, minGC);

            backgroundSequences.add(crsNoOverlap);
          } else {
            MotifsModule.LOG.info("GC wrong: {} {}...", refGC, minGC);

            // As a last resort, add the closest sequence regardless
            // of whether it was overlapping
            backgroundSequences.add(crs);
          }
        }
      }

    }

    DataFrame ret = DataFrame.createTextMatrix(backgroundSequences.size(), 2);

    ret.setColumnNames("DNA Location", "DNA sequence");

    for (int i = 0; i < backgroundSequences.size(); ++i) {
      SequenceRegion seq = backgroundSequences.get(i);

      ret.set(i, 0, seq.getLocation());
      ret.set(i, 1, seq.getSequence());
    }

    return ret;
  }
}
