package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jebtk.bioinformatics.dna.WebSequenceReader;
import org.jebtk.bioinformatics.genomic.GenesDB;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.bioinformatics.genomic.SequenceRegion;
import org.jebtk.bioinformatics.genomic.WebGenes;
import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.bioinformatics.ui.groups.Group;
import org.jebtk.core.Mathematics;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.statistics.Hypergeometric;
import org.jebtk.math.statistics.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class ParameterOptimization {
  private static final Logger LOG = LoggerFactory.getLogger(ParameterOptimization.class);

  public static void search(Motif motif, Group foregroundGroup, Group backgroundGroup, int ext5p, int ext3p, String db,
      Genome genome, SequenceReader assembly, GenesDB genesDb, Path file) throws IOException {
    boolean mainVariants = false;
    boolean peakWidths = false;

    List<SearchRegion> foregroundRegions = MotifSearch.getSearchRegions(genome, foregroundGroup, ext5p, ext3p,
        mainVariants, peakWidths, genesDb);

    // System.err.println("sdf " + sortedPeaks.size() + " " +
    // foregroundRegions.size() + " "+ mPeakCount);
    // System.exit(0);

    List<SequenceRegion> foregroundSequences = SearchRegion.getSequences(genome, assembly, foregroundRegions);

    List<SequenceRegion> foregroundRevCompSeqs = SequenceRegion.reverseComplementRegion(foregroundSequences);

    List<SearchRegion> backgroundRegions = MotifSearch.getSearchRegions(genome, backgroundGroup, ext5p, ext3p,
        mainVariants, peakWidths, genesDb);

    List<SequenceRegion> backgroundSequences = SearchRegion.getSequences(genome, assembly, backgroundRegions);

    List<SequenceRegion> backgroundRevCompSeqs = SequenceRegion.reverseComplementRegion(backgroundSequences);

    boolean[] goldStandard = MotifSearch.createGoldStandard(foregroundSequences.size(), backgroundSequences.size());

    int numSequences = foregroundSequences.size() + backgroundSequences.size();

    double[] bestScores = new double[numSequences];

    Hypergeometric hyg = new Hypergeometric();

    System.err.println(motif.getName());

    // lets set the threshold to be at least half the max
    // score of the motif

    MotifSearch ms = new MotifSearch();

    BufferedWriter writer = FileUtils.newBufferedWriter(file);

    try {
      for (double threshold = 0; threshold < 1.1; threshold += 0.1) {
        writer.write(Double.toString(threshold));
        writer.newLine();

        // for (double minSensitivity = 0; minSensitivity < 1.1; minSensitivity
        // += 0.1)
        // {
        // for (double minSpecificity = 0; minSpecificity < 1.1; minSpecificity
        // += 0.1)
        // {

        double t = MotifSearch.getMaxScore(motif) * threshold;

        int w = motif.getBaseCount();

        ms.bestScores(motif, w, foregroundSequences, foregroundRevCompSeqs, t, 0, bestScores);

        ms.bestScores(motif, w, backgroundSequences, backgroundRevCompSeqs, t, foregroundSequences.size(), bestScores);

        List<Stats> allStats = MotifSearch.enrichment(bestScores, goldStandard);

        for (Stats stats : allStats) {
          writer.write(Double.toString(stats.threshold));
          writer.write(TextUtils.TAB_DELIMITER);
          writer.write(Double.toString(stats.specificity));
          writer.write(TextUtils.TAB_DELIMITER);
          writer.write(Double.toString(stats.sensitivity));
          writer.newLine();

          LOG.debug("tp: {}, tn: {}, fp: {}, fn: {}, n: {}", stats.truePositive, stats.trueNegative,
              stats.falsePositive, stats.falseNegative, numSequences);

          // double p = hyg.cdfTwoTail(stats.truePositive,
          // numSequences / 2,
          // stats.truePositive + stats.falsePositive,
          // numSequences);

          double p = hyg.cdfOneTail(stats.truePositive, foregroundSequences.size(),
              stats.truePositive + stats.falsePositive, numSequences);

          double minusLog10P = Statistics.minusLog10P(p);

          LOG.debug("HygP: {} {}", p, minusLog10P);
        }
      }
    } finally {
      writer.close();
    }
  }

  public static void search2(Motif motif, Group foregroundGroup, Group backgroundGroup, int ext5p, int ext3p,
      Genome genome, SequenceReader assembly, GenesDB genesDb) throws IOException, ParseException {
    boolean mainVariants = false;
    boolean peakWidths = false;

    List<SearchRegion> foregroundRegions = MotifSearch.getSearchRegions(genome, foregroundGroup, ext5p, ext3p,
        mainVariants, peakWidths, genesDb);

    // System.err.println("sdf " + sortedPeaks.size() + " " +
    // foregroundRegions.size() + " "+ mPeakCount);
    // System.exit(0);

    List<SequenceRegion> foregroundSequences = SearchRegion.getSequences(genome, assembly, foregroundRegions);

    List<SequenceRegion> foregroundRevCompSeqs = SequenceRegion.reverseComplementRegion(foregroundSequences);

    List<SearchRegion> backgroundRegions = MotifSearch.getSearchRegions(genome, backgroundGroup, ext5p, ext3p,
        mainVariants, peakWidths, genesDb);

    List<SequenceRegion> backgroundSequences = SearchRegion.getSequences(genome, assembly, backgroundRegions);

    List<SequenceRegion> backgroundRevCompSeqs = SequenceRegion.reverseComplementRegion(backgroundSequences);

    boolean[] goldStandard = MotifSearch.createGoldStandard(foregroundSequences.size(), backgroundSequences.size());

    int ns = foregroundSequences.size() + backgroundSequences.size();

    double[] bestScores = new double[ns];

    System.err.println(motif.getName() + " " + ns);

    int w = motif.getBaseCount();

    MotifSearch ms = new MotifSearch();

    Hypergeometric hyg = new Hypergeometric();

    double[][] errors = Mathematics.zeros(11, 11);

    for (int t = 0; t < 11; ++t) {
      double threshold = MotifSearch.getMaxScore(motif) * t / 10.0;

      Stats minStats = new Stats();

      System.err.println("t\t" + t);

      for (int minSpec = 1; minSpec < 11; ++minSpec) {
        double minSpecificity = minSpec / 10.0;

        for (int minSens = 1; minSens < 11; ++minSens) {
          double minSensitivity = minSens / 10.0;

          ms.bestScores(motif, w, foregroundSequences, foregroundRevCompSeqs, threshold, bestScores);

          ms.bestScores(motif, w, backgroundSequences, backgroundRevCompSeqs, threshold, foregroundSequences.size(),
              bestScores);

          Stats stats = MotifSearch.enrichmentByMinError(bestScores, goldStandard, minSensitivity, minSpecificity);

          // double p = hyg.cdfTwoTail(stats.truePositive, ns / 2,
          // stats.truePositive +
          // stats.falsePositive, ns);
          double p = hyg.cdfOneTail(stats.truePositive, ns / 2, stats.truePositive + stats.falsePositive, ns);

          errors[minSens][minSpec] = -Mathematics.log10(p); // stats.error;

          if (stats.error < minStats.error) {
            // if (p < minStats.p) {
            minStats = stats;
            minStats.threshold = threshold;
            minStats.p = p;
          }

          // LOG.debug("Test: {} {} {} {}", c,
          // threshold,
          // minSensitivity,
          // minSpecificity);
        }
      }

      /*
       * System.err.println("threshold\t" + threshold); System.err.println("p\t" +
       * minStats.p); System.err.println("error\t" + minStats.error);
       * System.err.println("specificity\t" + minStats.specificity);
       * System.err.println("sensitivity\t" + minStats.sensitivity);
       * System.err.println("tp\t" + minStats.truePositive); System.err.println("fp\t"
       * + minStats.falsePositive); System.err.println("tn\t" +
       * minStats.trueNegative); System.err.println("fn\t" + minStats.falseNegative);
       */

      BufferedWriter writer = FileUtils.newBufferedWriter(
          PathUtils.getPath(Motif.sanitize(TextUtils.cat("_", motif.getId(), "error", "threshold", t) + ".txt")));

      try {
        writer.write("Sensitivity");

        for (int i = 0; i < errors[0].length; ++i) {
          double minSpecificity = i / 10.0;

          writer.write(TextUtils.TAB_DELIMITER);
          writer.write(Double.toString(minSpecificity));
        }

        writer.newLine();

        for (int i = 0; i < errors.length; ++i) {
          double minSensitivity = i / 10.0;

          writer.write(Double.toString(minSensitivity));

          for (int j = 0; j < errors[0].length; ++j) {
            writer.write(TextUtils.TAB_DELIMITER);
            writer.write(Double.toString(errors[i][j]));
          }

          writer.newLine();
        }
      } finally {
        writer.close();
      }
    }

    /*
     * System.err.println("Best Stats"); System.err.println("error " +
     * minStats.error); System.err.println("threshold " + minStats.threshold);
     * System.err.println("specificity " + minStats.specificity);
     * System.err.println("sensitivity " + minStats.sensitivity);
     * System.err.println("tp " + minStats.truePositive); System.err.println("fp " +
     * minStats.falsePositive); System.err.println("tn " + minStats.trueNegative);
     * System.err.println("fn " + minStats.falseNegative);
     */
  }

  public static void main(String[] args)
      throws SAXException, IOException, ParserConfigurationException, ParseException {
    int ext5p = 200;
    int ext3p = 200;

    Motif motif = Motif.parseMotif(
        PathUtils.getPath("/ifs/home/cancer/Lab_RDF/Personal/Antony/motifs/database/Database/RDF/bcl6bs.motif"),
        "test");

    GenesDB genesDb = new WebGenes(SettingsService.getInstance().getSetting("motifs.genome.remote-url").getUrl());

    SequenceReader assembly = new WebSequenceReader(
        new URL(SettingsService.getInstance().getString("motifs.dna.remote-url")));

    List<Group> groups = Group
        .loadGroups(PathUtils.getPath("/ifs/home/cancer/Lab_RDF/Personal/Antony/motifs/groups.mgrpx"));

    Group foregroundGroup = groups.get(0);
    Group backgroundGroup = groups.get(1);

    search2(motif, foregroundGroup, backgroundGroup, ext5p, ext3p, Genome.HG19, assembly, genesDb);
  }
}
