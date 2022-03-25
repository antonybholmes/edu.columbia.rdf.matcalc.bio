/**
 * Copyright 2016 Antony Holmes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingWorker;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.gapsearch.FixedGapSearch;
import org.jebtk.bioinformatics.gapsearch.GappedSearchFeatures;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.OverlapType;
import org.jebtk.core.io.Io;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.stream.Stream;
import org.jebtk.core.stream.StringMapFunction;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.Annotation;

/**
 * Performs a one way overlap between two files. The structure of the first file
 * is preserved and we annotate whether features of the second file overlap
 * those in the first.
 */
public class OneWayOverlapTask extends SwingWorker<Void, Void> {

  private static final int NEW_COLUMN_COUNT = 7;

  /** The m add beginning. */
  private boolean mAddBeginning;

  /** The m window. */
  private MainMatCalcWindow mWindow;

  /** The m files. */
  private List<Path> mFiles;

  /** The m annotations. */
  private ArrayList<FixedGapSearch<Annotation>> mAnnotations;

  private boolean mSimpleMode;

  private Genome mGenome;

  /**
   * Instantiates a new one way overlap task.
   *
   * @param window       the window
   * @param files        the files
   * @param addBeginning the add beginning
   * @throws InvalidFormatException the invalid format exception
   * @throws ParseException         the parse exception
   * @throws IOException            Signals that an I/O exception has occurred.
   */
  public OneWayOverlapTask(MainMatCalcWindow window, Genome genome, List<Path> files, boolean addBeginning,
      boolean simpleMode) throws InvalidFormatException, IOException {
    mWindow = window;
    mGenome = genome;
    mAddBeginning = addBeginning;
    mFiles = files;
    mSimpleMode = simpleMode;

    mAnnotations = new ArrayList<FixedGapSearch<Annotation>>(files.size());

    // Load all of the files into annotations
    for (Path file : files) {
      FixedGapSearch<Annotation> gappedSearch = Annotation.parseFixed(file);

      mAnnotations.add(gappedSearch);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
    try {
      overlap();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Overlap.
   *
   * @throws Exception the exception
   */
  private void overlap() throws Exception {
    if (mFiles.size() < 2) {
      return;
    }

    Path file = mFiles.get(0);

    DataFrame matrix = Annotation.toMatrix(file);

    DataFrame outputMatrix = DataFrame.createDataFrame(matrix.getRows(), matrix.getCols() + NEW_COLUMN_COUNT);

    int c = 0;

    if (mAddBeginning) {
      c = 0;
    } else {
      c = outputMatrix.getCols() - NEW_COLUMN_COUNT;
    }

    String name = PathUtils.getNameNoExt(mFiles.get(1));

    outputMatrix.setColumnName(c + 0, "Overlap Region");
    outputMatrix.setColumnName(c + 1, "Overlap Source");
    outputMatrix.setColumnName(c + 2, "Overlap Count");
    outputMatrix.setColumnName(c + 3, "Overlap %");
    outputMatrix.setColumnName(c + 4, "Overlap Type");
    outputMatrix.setColumnName(c + 5, "Overlap Feature");
    outputMatrix.setColumnName(c + 6, "Overlap Location");

    if (mAddBeginning) {
      c = NEW_COLUMN_COUNT;
    } else {
      c = 0;
    }

    for (int i = 0; i < matrix.getCols(); ++i) {
      outputMatrix.copyColumn(matrix, i, c + i);
    }

    if (mAddBeginning) {
      c = 0;
    } else {
      c = outputMatrix.getCols() - NEW_COLUMN_COUNT;
    }

    for (int i = 0; i < matrix.getRows(); ++i) {
      GenomicRegion region = null;

      if (Io.isEmptyLine(matrix.getText(i, 0))) {
        region = null;
      } else if (matrix.getText(i, 0).contains(TextUtils.NA)) {
        region = null;
      } else if (GenomicRegion.isGenomicRegion(matrix.getText(i, 0))) {
        region = GenomicRegion.parse(mGenome, matrix.getText(i, 0));
      } else {
        // three column format

        region = new GenomicRegion(ChromosomeService.getInstance().chr(mGenome, matrix.getText(i, 0)),
            TextUtils.parseInt(matrix.getText(i, 1)), TextUtils.parseInt(matrix.getText(i, 2)));
      }

      // Use the next file to overlap with
      FixedGapSearch<Annotation> gappedSearch = mAnnotations.get(1);

      // Find the closest feature
      List<GappedSearchFeatures<Annotation>> results = gappedSearch.getFeatures(region);

      List<Double> maxOverlapWidth = new ArrayList<Double>();
      List<GenomicRegion> maxOverlap = new ArrayList<GenomicRegion>();
      List<Annotation> maxAnnotation = new ArrayList<Annotation>();
      List<OverlapType> maxOverlapType = new ArrayList<OverlapType>();

      if (results != null) {
        double l = (double) region.getLength();
        double maxP = 0;

        for (GappedSearchFeatures<Annotation> features : results) {
          for (Entry<GenomicRegion, List<Annotation>> r : features) {
            for (Annotation annotation : r.getValue()) {
              // System.err.println("Comp " + region + " " +
              // annotation.getRegion());

              GenomicRegion overlap = GenomicRegion.overlap(region, annotation.getRegion());

              if (overlap != null) {
                double p = 100.0 * Math.min(1.0, overlap.getLength() / l);

                if (mSimpleMode) {
                  if (p > maxP) {
                    maxP = p;

                    maxOverlapWidth.clear();
                    maxOverlap.clear();
                    maxAnnotation.clear();
                    maxOverlapType.clear();

                    maxOverlapWidth.add(p);
                    maxOverlap.add(overlap);
                    maxAnnotation.add(annotation);
                    maxOverlapType.add(GenomicRegion.overlapType(region, annotation.getRegion()));
                  }
                } else {
                  maxOverlapWidth.add(p);
                  maxOverlap.add(overlap);
                  maxAnnotation.add(annotation);
                  maxOverlapType.add(GenomicRegion.overlapType(region, annotation.getRegion()));
                }
              }
            }
          }
        }
      }

      if (maxOverlap.size() > 0) {
        outputMatrix.set(i, c, Stream.of(maxOverlap).join(";")); // Join.onSemiColon().values(maxOverlap).toString());
        outputMatrix.set(i, c + 1, name);
        outputMatrix.set(i, c + 2, maxOverlap.size());
        outputMatrix.set(i, c + 3, Stream.of(maxOverlapWidth).asDouble().round(2).join(";")); // Join.onSemiColon().values(Mathematics.round(maxOverlapWidth,
                                                                                              // 2)).toString()
        outputMatrix.set(i, c + 4, Stream.of(maxOverlapType).join(";")); // Join.onSemiColon().values(maxOverlapType).toString());
        outputMatrix.set(i, c + 5, Stream.of(maxAnnotation).map(new StringMapFunction<Annotation>() {
          @Override
          public String apply(Annotation a) {
            return a.getName();
          }
        }).join(";"));

        outputMatrix.set(i, c + 6, Stream.of(maxAnnotation).map(new StringMapFunction<Annotation>() {
          @Override
          public String apply(Annotation a) {
            return a.getRegion().getLocation();
          }
        }).join(";"));
      } else {
        outputMatrix.set(i, c, TextUtils.NA);
        outputMatrix.set(i, c + 1, name);
        outputMatrix.set(i, c + 2, 0);
        outputMatrix.set(i, c + 3, TextUtils.NA);
        outputMatrix.set(i, c + 4, TextUtils.NA);
        outputMatrix.set(i, c + 5, TextUtils.NA);
        outputMatrix.set(i, c + 6, TextUtils.NA);
      }
    }

    mWindow.openMatrices().open(outputMatrix.setName("One Way Overlap"));
  }
}