package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.text.TextUtils;

/**
 * Describes the location and match for a given motif.
 * 
 * @author Antony Holmes
 *
 */
public class BindingSite {
  private final String mSequence;
  private final int mOffset;
  private final double mScore;
  private final char mStrand;

  public BindingSite(String sequence, int offset, double score, char strand) {
    mSequence = sequence.toUpperCase();
    mOffset = offset;
    mScore = Math.abs(score);
    mStrand = strand;
  }

  @Override
  public String toString() {
    return new StringBuilder(mSequence).append(TextUtils.TAB_DELIMITER).append(mStrand).append(TextUtils.TAB_DELIMITER)
        .append(Integer.toString(mOffset)).append(TextUtils.TAB_DELIMITER).append(Double.toString(mScore)).toString();
  }

  public double getScore() {
    return mScore;
  }

  public int getOffset() {
    return mOffset;
  }

  public char getStrand() {
    return mStrand;
  }

  public String getSequence() {
    return mSequence;
  }

  public static List<BindingSite> sortByScore(List<BindingSite> sites) {
    Map<Double, List<BindingSite>> scoreMap = new HashMap<Double, List<BindingSite>>();

    for (BindingSite site : sites) {
      if (!scoreMap.containsKey(site.getScore())) {
        scoreMap.put(site.getScore(), new ArrayList<BindingSite>());
      }

      scoreMap.get(site.getScore()).add(site);
    }

    List<BindingSite> ret = new ArrayList<BindingSite>();

    for (double score : CollectionUtils.reverse(CollectionUtils.sort(scoreMap.keySet()))) {
      for (BindingSite site : sortByDistance(scoreMap.get(score))) {
        ret.add(site);
      }
    }

    return ret;
  }

  public static List<BindingSite> sortByDistance(List<BindingSite> sites) {
    Map<Integer, List<BindingSite>> offsetMap = new HashMap<Integer, List<BindingSite>>();

    for (BindingSite site : sites) {
      if (!offsetMap.containsKey(Math.abs(site.getOffset()))) {
        offsetMap.put(Math.abs(site.getOffset()), new ArrayList<BindingSite>());
      }

      offsetMap.get(Math.abs(site.getOffset())).add(site);
    }

    List<BindingSite> ret = new ArrayList<BindingSite>();

    for (int offset : CollectionUtils.sort(offsetMap.keySet())) {
      for (BindingSite site : offsetMap.get(offset)) {
        ret.add(site);
      }
    }

    return ret;
  }
}
