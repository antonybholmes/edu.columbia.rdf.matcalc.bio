package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.Region;
import org.jebtk.bioinformatics.genomic.Sequence;
import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.core.text.Formatter;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.dialog.ModernMessageDialog;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.SearchSequence;
import edu.columbia.rdf.matcalc.bio.SearchSequenceType;
import edu.columbia.rdf.matcalc.bio.SequenceUtils;

public class MotifSearchTask extends SwingWorker<Void, Void> {

  private DataFrame mNewModel;
  private final List<Motif> mMotifs;
  private final double mThreshold;
  private final MainMatCalcWindow mParent;
  private final Genome mGenome;

  public static class SearchResult {
    public SearchRegion region;
    public Motif motif;
    public BindingSite site;
    public int offset;
    public Region motifLocation;
    public int index;
  }

  public MotifSearchTask(MainMatCalcWindow parent, Genome genome, List<Motif> motifs, double threshold) {
    mParent = parent;
    mGenome = genome;
    mMotifs = motifs;
    mThreshold = threshold;
  }

  @Override
  public Void doInBackground() {
    try {
      mNewModel = motifs();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public void done() {
    if (mNewModel == null) {
      ModernMessageDialog.createWarningDialog(mParent, "No motifs were found.");

      return;
    }

    mParent.history().addToHistory("Motif Search", mNewModel);
  }

  private DataFrame motifs() throws IOException {
    DataFrame m = mParent.getCurrentMatrix();

    List<SearchSequence> sequences = SequenceUtils.matrixToSequences(mGenome, m);

    if (sequences.isEmpty()) {
      ModernMessageDialog.createWarningDialog(mParent, "There are no suitable DNA sequences in the table.");

      return null;
    }

    List<SearchSequence> revCompSeqs = SearchSequence.reverseComplement(sequences);

    return createMotifsTable(mMotifs, m, sequences, revCompSeqs, mThreshold);
  }

  private static DataFrame createMotifsTable(List<Motif> motifs, DataFrame matrix, List<SearchSequence> sequences,
      List<SearchSequence> revCompSeqs, double threshold) throws IOException {

    /*
     * // The header cell = row.createCell(c++); cell.setCellValue("Feature"); cell
     * = row.createCell(c++); cell.setCellValue("Feature Strand"); cell =
     * row.createCell(c++); cell.setCellValue("Feature Region"); cell =
     * row.createCell(c++); cell.setCellValue("Reference Location"); cell =
     * row.createCell(c++); cell.setCellValue("5' Offset"); cell =
     * row.createCell(c++); cell.setCellValue("3' Offset"); cell =
     * row.createCell(c++); cell.setCellValue("Search Region"); cell =
     * row.createCell(c++); cell.setCellValue("Motif Name"); cell =
     * row.createCell(c++); cell.setCellValue("Motif ID"); cell =
     * row.createCell(c++); cell.setCellValue("Motif Database"); cell =
     * row.createCell(c++); cell.setCellValue("5' Sequence"); cell =
     * row.createCell(c++); cell.setCellValue("3' Sequence"); cell =
     * row.createCell(c++); cell.setCellValue("Score"); cell = row.createCell(c++);
     * cell.setCellValue("Max Score"); cell = row.createCell(c++);
     * cell.setCellValue("Strand"); cell = row.createCell(c++);
     * cell.setCellValue("Offset From Reference"); cell = row.createCell(c++);
     * cell.setCellValue("Motif Location");
     */

    // Use a buffer that can accommodate all scores regardless of
    // of sequence length. 4096 should cover most instances

    double[] forwardScores = new double[MotifSearch.BUFFER_SIZE];
    int[] forwardIds = new int[MotifSearch.BUFFER_SIZE];
    double[] revScores = new double[MotifSearch.BUFFER_SIZE];
    int[] revIds = new int[MotifSearch.BUFFER_SIZE];

    // lets keep all motifs for the moment

    MotifSearch ms = new MotifSearch();

    List<SearchResult> searchResults = new ArrayList<>();

    // add up to the
    for (int i = 0; i < sequences.size(); ++i) {
      SearchSequence s = sequences.get(i);

      char[] seq = s.getDna().toArray();
      // char[] revCompSeq = revCompSeqs.get(i).getArray();

      byte[] iSeq = s.getDna().toIndex();
      byte[] iRevCompSeq = revCompSeqs.get(i).getDna().toIndex();

      int n = seq.length;

      for (Motif motif : motifs) {
        // System.err.println(motif.getName());

        double t = threshold * MotifSearch.getMaxScore(motif);

        int w = motif.getBaseCount();

        double bgscore = motif.getBgPwm(); // Mathematics.repeatArray(m.getBgPwm(),
                                           // n);

        double[][] pwm = motif.getPwm();

        // SearchRegion region = searchRegions.get(i);

        ms.search(iSeq, iRevCompSeq, n, pwm, bgscore, w, t, forwardScores, forwardIds, revScores, revIds);

        List<BindingSite> sites = MotifSearch.getBindingSites(seq, n, w, t, forwardScores, forwardIds, revScores,
            revIds);

        sites = BindingSite.sortByScore(sites);

        for (BindingSite site : sites) {
          // Sequence showSequence = MotifSearch.showScores(sequence,
          // site,
          // n);

          // The genomic location of the motif start

          // int motifStart =
          // region.getSearchRegion().getStart() + site.getOffset();

          // The offset from the reference/mid point
          // int offsetFromReference =
          // motifStart - region.getReferencePoint().getStart();

          Region motifLocation = null;

          if (s.getType() == SearchSequenceType.GENOMIC) {
            GenomicRegion r = s.getRegion();

            motifLocation = new GenomicRegion(r.getChr(), r.getStart() + site.getOffset(),
                r.getStart() + site.getOffset() + site.getSequence().length() - 1);
          } else {
            motifLocation = new Region(site.getOffset(), site.getOffset() + site.getSequence().length() - 1);
          }

          SearchResult sr = new SearchResult();

          // sr.region = region;
          sr.index = i;
          sr.motif = motif;
          sr.site = site;
          // sr.offset = offsetFromReference;
          sr.motifLocation = motifLocation;

          searchResults.add(sr);

          /*
           * cell = row.createCell(c++); cell.setCellValue(region.getName()); cell =
           * row.createCell(c++);
           * cell.setCellValue(Character.toString(region.getStrand())); cell =
           * row.createCell(c++); cell.setCellValue(region.getRegion().toString()); cell =
           * row.createCell(c++);
           * cell.setCellValue(region.getReferencePoint().toString()); cell =
           * row.createCell(c++); cell.setCellValue(region.getExt5p()); cell =
           * row.createCell(c++); cell.setCellValue(region.getExt3p()); cell =
           * row.createCell(c++); cell.setCellValue(region.getSearchRegion().toString());
           * cell = row.createCell(c++); cell.setCellValue(m.getName()); cell =
           * row.createCell(c++); cell.setCellValue(m.getId()); cell =
           * row.createCell(c++); cell.setCellValue(m.getDatabase()); cell =
           * row.createCell(c++); cell.setCellValue(site.getSequence()); cell =
           * row.createCell(c++);
           * cell.setCellValue(Sequence.reverseComplement(site.getSequence())); cell =
           * row.createCell(c++); cell.setCellValue(TextUtils.format2DP(site.getScore()));
           * cell = row.createCell(c++);
           * cell.setCellValue(TextUtils.format2DP(MotifSearch.getMaxScore(m))); cell =
           * row.createCell(c++); cell.setCellValue(Character.toString(site.getStrand()));
           * cell = row.createCell(c++); cell.setCellValue(offsetFromReference); cell =
           * row.createCell(c++); cell.setCellValue(motifLocation.toString()); //cell =
           * row.createCell(15); //cell.setCellValue(showSequence.getBases());
           * 
           * ++r;
           */
        }
      }
    }

    if (searchResults.isEmpty()) {
      return null;
    }

    DataFrame ret = DataFrame.createDataFrame(searchResults.size(), matrix.getCols() + 10);

    DataFrame.copyColumnNames(matrix, ret);

    int c = matrix.getCols();

    // ret.setColumnName(0, "Feature");
    // ret.setColumnName(1, "Feature Strand");
    // ret.setColumnName(2, "Feature Region");
    // ret.setColumnName(3, "Reference Location");
    // ret.setColumnName(4, "5' Offset");
    // ret.setColumnName(5, "3' Offset");
    // ret.setColumnName(6, "Search Region");
    ret.setColumnName(c++, "Motif Name");
    ret.setColumnName(c++, "Motif ID");
    ret.setColumnName(c++, "Motif Database");
    ret.setColumnName(c++, "5' Sequence");
    ret.setColumnName(c++, "3' Sequence");
    ret.setColumnName(c++, "Motif Location");
    ret.setColumnName(c++, "Strand");
    ret.setColumnName(c++, "Score");
    ret.setColumnName(c++, "Max Score");
    ret.setColumnName(c++, "Offset From Reference");
    // ret.setColumnName(16, "Motif Location");

    int r = 0;

    for (SearchResult sr : searchResults) {
      // ret.set(r, 0, sr.region.getName());
      // ret.set(r, 1, Character.toString(sr.region.getStrand()));
      // ret.set(r, 2, sr.region.getRegion().toString());
      // ret.set(r, 3, sr.region.getReferencePoint().toString());
      // ret.set(r, 4, sr.region.getExt5p());
      // ret.set(r, 5, sr.region.getExt3p());
      // ret.set(r, 6, sr.region.getSearchRegion().toString());

      ret.copyRow(matrix, sr.index, r);

      // System.err.println(sr.site.getScore() + " " +
      // Formatter.decimal().dp(2).format(sr.site.getScore()));

      c = matrix.getCols();
      ret.set(r, c++, sr.motif.getName());
      ret.set(r, c++, sr.motif.getId());
      ret.set(r, c++, sr.motif.getDatabase());
      ret.set(r, c++, sr.site.getSequence());
      ret.set(r, c++, Sequence.reverseComplement(sr.site.getSequence()));
      ret.set(r, c++, sr.motifLocation.toString());
      ret.set(r, c++, Character.toString(sr.site.getStrand()));
      ret.set(r, c++, Formatter.decimal().dp(2).format(sr.site.getScore()));
      ret.set(r, c++, Formatter.decimal().dp(2).format(MotifSearch.getMaxScore(sr.motif)));
      ret.set(r, c++, sr.site.getOffset());

      ++r;
    }

    return ret;
  }
}