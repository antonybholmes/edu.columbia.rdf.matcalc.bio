package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.gapsearch.FixedGapSearch;
import org.jebtk.bioinformatics.gapsearch.GappedSearchFeatures;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.ui.groups.Group;
import org.jebtk.core.Mathematics;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.DefaultTreeMapCreator;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.TreeSetCreator;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.Annotation;
import edu.columbia.rdf.matcalc.toolbox.core.venn.CircleStyle;
import edu.columbia.rdf.matcalc.toolbox.core.venn.MainVennWindow;

/**
 * Overlap segments.
 * 
 * @author Antony Holmes
 */
public class NWayOverlapTask extends SwingWorker<Void, Void> {

  private List<Path> mFiles;

  private List<FixedGapSearch<Annotation>> mAnnotations;

  private MainMatCalcWindow mWindow;

  private boolean mVenn;

  public NWayOverlapTask(MainMatCalcWindow window, List<Path> files, boolean venn)
      throws InvalidFormatException, IOException {
    mWindow = window;
    mFiles = files;
    // Venn diagram is drawn only when there are two files involved
    mVenn = venn && files.size() > 1;

    mAnnotations = new ArrayList<FixedGapSearch<Annotation>>(files.size());

    // Load all of the files into annotations
    for (Path file : files) {
      FixedGapSearch<Annotation> gappedSearch = Annotation.parseFixed(file);

      mAnnotations.add(gappedSearch);
    }
  }

  @Override
  public Void doInBackground() {
    overlap();

    return null;
  }

  private void overlap() {
    // First get all the regions to search into one sorted map

    Map<Chromosome, IterMap<GenomicRegion, IterMap<Path, Set<GenomicRegion>>>> locationCoreMap = DefaultTreeMap
        .create(new DefaultTreeMapCreator<GenomicRegion, IterMap<Path, Set<GenomicRegion>>>(
            new DefaultTreeMapCreator<Path, Set<GenomicRegion>>(new TreeSetCreator<GenomicRegion>())));

    // Set<GenomicRegion> allocatedRegions = new HashSet<GenomicRegion>();

    for (int i = 0; i < mAnnotations.size(); ++i) {
      FixedGapSearch<Annotation> gs = mAnnotations.get(i);
      Path file = mFiles.get(i);

      for (Chromosome chr : gs) {
        for (Annotation an : gs.getFeatures(chr)) {
          GenomicRegion region = an.getRegion();

          // Keep track of all the regions other than this one
          // which are overlapping this region.
          Set<GenomicRegion> used = new HashSet<GenomicRegion>();

          boolean exhausted = false;
          boolean allocated = false;

          while (!exhausted) {
            Map<Path, Set<GenomicRegion>> overlaps = DefaultTreeMap.create(new TreeSetCreator<GenomicRegion>());

            overlaps.get(file).add(region);

            // int start = region.getStart();
            // int end = region.getEnd();

            GenomicRegion consensusRegion = region; // GenomicRegion.create(genome,
                                                    // chr,
                                                    // region.getStart(),
                                                    // region.getEnd())

            // Test all files to see if we can find overlaps
            for (int j = 0; j < mAnnotations.size(); ++j) {
              FixedGapSearch<Annotation> gs2 = mAnnotations.get(j);
              Path file2 = mFiles.get(j);

              for (GappedSearchFeatures<Annotation> gsf2 : gs2.getFeatures(consensusRegion)) {
                for (Entry<GenomicRegion, List<Annotation>> r : gsf2) {
                  for (Annotation an2 : r.getValue()) {

                    GenomicRegion testRegion = an2.getRegion();

                    // Don't test the region on itself
                    if (testRegion.equals(region)) {
                      continue;
                    }

                    // if (allocatedRegions.contains(testRegion)) {
                    // continue;
                    // }

                    if (used.contains(testRegion)) {
                      continue;
                    }

                    GenomicRegion newConsensus = GenomicRegion.overlap(consensusRegion, testRegion);

                    if (newConsensus != null) {
                      // Adjust the size of the overlap

                      consensusRegion = newConsensus;

                      overlaps.get(file2).add(testRegion);

                      used.add(testRegion);
                    }
                  }
                }
              }
            }

            // If there are multiple overlaps then there must be
            // more than two reads in the group. If allocated is
            // not set, it means the read is isolated and nothing
            // overlaps it.
            if (overlaps.size() > 1 || !allocated) {
              // GenomicRegion overlap = GenomicRegion.create(genome, chr,
              // start, end);

              for (Path f : overlaps.keySet()) {
                for (GenomicRegion r : overlaps.get(f)) {
                  locationCoreMap.get(chr).get(consensusRegion).get(f).add(r);
                }
              }

              allocated = true;
            } else {
              exhausted = true;
              break;
            }
          }

          // Now that we have finished with this region, mark
          // all the regions we used as being allocated so we
          // don't use them again
          // allocatedRegions.add(region);
          // allocatedRegions.addAll(used);
        }
      }
    }

    int rowCount = 0;

    for (Chromosome chr : locationCoreMap.keySet()) {
      rowCount += locationCoreMap.get(chr).size();
    }

    DataFrame matrix = DataFrame.createDataFrame(rowCount, 3 + 3 * mFiles.size());

    int c = 0;

    matrix.setColumnName(c++, "Overlap Region");
    matrix.setColumnName(c++, "Overlap Width");
    matrix.setColumnName(c++, "Number Of Overlapping Regions");

    for (Path file : mFiles) {
      String name = PathUtils.getNameNoExt(file);

      matrix.setColumnName(c++, name + " Region");
      matrix.setColumnName(c++, name + " Region Width");
      matrix.setColumnName(c++, name + " % Overlap");
    }

    int r = 0;

    Group group1 = null;
    Group group2 = null;
    Group group3 = null;

    if (mVenn) {
      if (mFiles.size() > 0) {
        group1 = new Group(PathUtils.getNameNoExt(mFiles.get(0)), Color.RED);
      }

      if (mFiles.size() > 1) {
        group2 = new Group(PathUtils.getNameNoExt(mFiles.get(1)), Color.BLUE);
      }

      if (mFiles.size() > 2) {
        group3 = new Group(PathUtils.getNameNoExt(mFiles.get(2)), Color.GREEN);
      }
    }

    for (Chromosome chr : locationCoreMap.keySet()) {
      for (GenomicRegion overlapRegion : locationCoreMap.get(chr).keySet()) {

        c = 0;

        matrix.set(r, c++, overlapRegion.getLocation());
        matrix.set(r, c++, overlapRegion.getLength());
        matrix.set(r, c++, locationCoreMap.get(chr).get(overlapRegion).size());

        for (int i = 0; i < mAnnotations.size(); ++i) {
          Path file = mFiles.get(i);

          if (locationCoreMap.get(chr).get(overlapRegion).containsKey(file)) {
            // If we are generating a venn diagram

            if (mVenn) {
              switch (i) {
              case 0:
                group1.add(overlapRegion.getLocation());
                break;
              case 1:
                group2.add(overlapRegion.getLocation());
                break;
              case 2:
                group3.add(overlapRegion.getLocation());
                break;
              }
            }

            // Locations

            List<GenomicRegion> locations = CollectionUtils.sort(locationCoreMap.get(chr).get(overlapRegion).get(file));

            List<String> items = new ArrayList<String>();

            for (GenomicRegion region : locations) {
              items.add(region.getLocation());
            }

            matrix.set(r, c++, Join.onSemiColon().values(items));

            // Lengths

            items.clear();

            for (GenomicRegion region : locations) {
              items.add(Integer.toString(region.getLength()));
            }

            matrix.set(r, c++, Join.onSemiColon().values(items));

            // Overlaps

            items.clear();

            for (GenomicRegion region : locations) {
              double p = 100.0 * Math.min(1.0, GenomicRegion.p(overlapRegion, region));

              items.add(Double.toString(Mathematics.round(p, 2)));
            }

            matrix.set(r, c++, Join.onSemiColon().values(items));
          } else {
            matrix.set(r, c++, TextUtils.NA);
            matrix.set(r, c++, TextUtils.NA);
            matrix.set(r, c++, TextUtils.NA);
          }
        }

        ++r;
      }
    }

    // mWindow.history().addToHistory("Overlap", matrix);
    mWindow.openMatrices().open(matrix.setName(mFiles.size() + " Way Overlap"));

    if (mVenn) {
      if (mFiles.size() == 2) {
        MainVennWindow vennWindow = new MainVennWindow(group1, group2, CircleStyle.UNIFORM);

        vennWindow.setVisible(true);
      } else if (mFiles.size() == 3) {
        MainVennWindow vennWindow = new MainVennWindow(group1, group2, group3, CircleStyle.UNIFORM);

        vennWindow.setVisible(true);
      } else {
        // Do nothing
      }
    }
  }
}
