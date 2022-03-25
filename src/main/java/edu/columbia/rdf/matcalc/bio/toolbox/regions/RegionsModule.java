package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.conservation.ConservationAssembly;
import org.jebtk.bioinformatics.conservation.ConservationAssemblyWeb;
import org.jebtk.bioinformatics.ext.ucsc.Bed;
import org.jebtk.bioinformatics.gapsearch.BinaryGapSearch;
import org.jebtk.bioinformatics.gapsearch.FixedGapSearch;
import org.jebtk.bioinformatics.gapsearch.GappedSearchFeatures;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.GenesDB;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicElement;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.GenomicType;
import org.jebtk.bioinformatics.ui.BioInfDialog;
import org.jebtk.bioinformatics.ui.Bioinformatics;
import org.jebtk.bioinformatics.ui.external.ucsc.BedGraphGuiFileFilter;
import org.jebtk.bioinformatics.ui.external.ucsc.BedGuiFileFilter;
import org.jebtk.bioinformatics.ui.groups.Group;
import org.jebtk.core.Mathematics;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.io.Io;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.statistics.Statistics;
import org.jebtk.math.ui.external.microsoft.XlsxGuiFileFilter;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.button.ModernClickWidget;
import org.jebtk.modern.dataview.ModernDataModel;
import org.jebtk.modern.dialog.MessageDialogTaskGlassPane;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.help.GuiAppInfo;
import org.jebtk.modern.io.FileDialog;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.io.TxtGuiFileFilter;
import org.jebtk.modern.menu.ModernPopupMenu2;
import org.jebtk.modern.menu.ModernTwoLineMenuItem;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.ribbon.RibbonLargeDropDownButton2;
import org.jebtk.modern.tooltip.ModernToolTip;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.Annotation;
import edu.columbia.rdf.matcalc.bio.AnnotationService;
import edu.columbia.rdf.matcalc.toolbox.Module;
import edu.columbia.rdf.matcalc.toolbox.core.venn.CircleStyle;
import edu.columbia.rdf.matcalc.toolbox.core.venn.MainVennWindow;

public class RegionsModule extends Module implements ModernClickListener {
  private MainMatCalcWindow mWindow;

  private ConservationAssembly mConservationAssembly;

  private ConservationAssembly mMouseConservationAssembly;

  private class EnhancerTask extends SwingWorker<Void, Void> {

    private Map<String, BinaryGapSearch<Annotation>> mGappedSearch;
    private DataFrame mNewModel;
    private boolean mOverlapMode;
    private MessageDialogTaskGlassPane mSearchScreen;

    public EnhancerTask(Map<String, BinaryGapSearch<Annotation>> gappedSearch, boolean overlapMode) {

      mGappedSearch = gappedSearch;
      mOverlapMode = overlapMode;
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Overlapping...");

      try {
        mNewModel = enhancers(Genome.HG19);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Add Super Enhancers", mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame enhancers(Genome genome) throws IOException {
      DataFrame model = getCurrentModel();

      List<String> annotations = CollectionUtils.sortKeys(mGappedSearch);

      DataFrame matrix = DataFrame.createDataFrame(model.getRows(), model.getCols() + annotations.size());

      for (int i = 0; i < model.getCols(); ++i) {
        matrix.setColumnName(i, model.getColumnHeader().getHeader(i));
      }

      int c = model.getCols();

      for (int i = 0; i < annotations.size(); ++i) {
        matrix.setColumnName(c + i, annotations.get(i) + " Super Enhancers");
      }

      for (int i = 0; i < model.getRows(); ++i) {

        // Copy the existing data
        matrix.copyRow(model, i, i);

        GenomicRegion region = null;

        if (Io.isEmptyLine(model.getText(i, 0))) {
          region = null;
        } else if (model.getText(i, 0).contains(TextUtils.NA)) {
          region = null;
        } else if (GenomicRegion.isGenomicRegion(model.getText(i, 0))) {
          region = GenomicRegion.parse(genome, model.getText(i, 0));
        } else {
          // three column format

          region = new GenomicRegion(ChromosomeService.getInstance().chr(genome, model.getText(i, 0)),
              Integer.parseInt(model.getText(i, 1)), Integer.parseInt(model.getText(i, 2)));
        }

        c = model.getCols();

        for (int j = 0; j < annotations.size(); ++j) {
          String type = annotations.get(j);

          Set<String> enhancers = new HashSet<String>();

          List<GappedSearchFeatures<Annotation>> results = mGappedSearch.get(type).getFeatures(region);

          if (results != null) {
            if (mOverlapMode) {
              for (GappedSearchFeatures<Annotation> features : results) {
                for (Entry<GenomicRegion, List<Annotation>> r : features) {
                  for (Annotation annotation : r.getValue()) {

                    GenomicRegion overlap = GenomicRegion.overlap(region, annotation.getRegion());

                    if (overlap != null) {
                      enhancers.add(annotation.getName());
                    }
                  }
                }
              }
            } else {
              int minD = Integer.MAX_VALUE;

              for (GappedSearchFeatures<Annotation> features : results) {
                for (Entry<GenomicRegion, List<Annotation>> r : features) {
                  for (Annotation annotation : r.getValue()) {
                    int d = GenomicRegion.midDist(region, annotation.getRegion());

                    if (Math.abs(d) < minD) {
                      minD = Math.abs(d);
                    }
                  }
                }
              }

              for (GappedSearchFeatures<Annotation> features : results) {
                for (Entry<GenomicRegion, List<Annotation>> r : features) {
                  for (Annotation annotation : r.getValue()) {
                    int d = GenomicRegion.midDist(region, annotation.getRegion());

                    if (Math.abs(d) == minD) {
                      enhancers.add(annotation.getName());
                    }
                  }
                }
              }
            }
          }

          // Write out the enhancers or put n/a if none were
          // found
          if (enhancers.size() > 0) {
            matrix.set(i, c + j, TextUtils.scJoin(CollectionUtils.sort(enhancers)));
          } else {
            matrix.set(i, c + j, TextUtils.NA);
          }
        }
      }

      return matrix;
    }
  }

  /**
   * Overlap segments.
   * 
   * @author Antony Holmes
   *
   */
  /*
   * private class OverlapTask extends SwingWorker<Void, Void> {
   * 
   * private GappedSearch<Annotation> mGappedSearch; private XlsxTableModel
   * mNewModel; private MessageDialogTaskGlassPane mSearchScreen; private Path
   * mFile2; private boolean mAddBeginning;
   * 
   * public OverlapTask(GappedSearch<Annotation> gappedSearch, Path file2, boolean
   * addBeginning) { mGappedSearch = gappedSearch; mFile2 = file2; mAddBeginning =
   * addBeginning; }
   * 
   * @Override public Void doInBackground() { mSearchScreen =
   * mWindow.createTaskDialog("Overlapping...");
   * 
   * try { mNewModel = overlap(); } catch (Exception e) { e.printStackTrace(); }
   * 
   * return null; }
   * 
   * @Override public void done() { if (mNewModel != null) {
   * mWindow.history().addToHistory("Overlap with " + PathUtils.getName(mFile2),
   * mNewModel); }
   * 
   * mSearchScreen.close(); }
   * 
   * private XlsxTableModel overlap() throws Exception { DataFrame model =
   * getCurrentModel();
   * 
   * XSSFWorkbook workbook = new XSSFWorkbook();
   * 
   * Sheet sheet = workbook.createSheet();
   * 
   * // Keep track of how many rows we have created. int r = 0;
   * 
   * XSSFRow row = (XSSFRow)sheet.createRow(r); XSSFCell cell;
   * 
   * int c = 0;
   * 
   * if (mAddBeginning) { cell = row.createCell(c++);
   * cell.setCellValue("Overlap Region"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap %"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap Feature"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap Feature Location"); }
   * 
   * // The header for (int i = 0; i < model.getColumnCount(); ++i) { cell =
   * row.createCell(c++); cell.setCellValue(model.getColumnHeader().getHeader(i));
   * }
   * 
   * if (!mAddBeginning) { cell = row.createCell(c++);
   * cell.setCellValue("Overlap Region"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap %"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap Feature"); cell = row.createCell(c++);
   * cell.setCellValue("Overlap Feature Location"); }
   * 
   * ++r;
   * 
   * for (int i = 0; i < model.getRowCount(); ++i) { c = 0;
   * 
   * GenomicRegion region = null;
   * 
   * if (Io.isEmptyLine(model.getText(i, 0))) { region = null; } else if
   * (model.getText(i, 0).contains(TextUtils.NA)) { region = null; } else if
   * (GenomicRegion.isRegion(model.getText(i, 0))) { region =
   * GenomicRegion.parse(model.getText(i, 0)); } else { // three column format
   * 
   * region = new GenomicRegion(GenomeService.getInstance().parse(model.getText(i,
   * 0)), TextUtils.parseInt(model.getText(i, 1)),
   * TextUtils.parseInt(model.getText(i, 2))); }
   * 
   * row = (XSSFRow)sheet.createRow(r);
   * 
   * List<GappedSearchFeatures<Annotation>> results =
   * mGappedSearch.getFeatures(region);
   * 
   * int maxOverlapWidth = -1; GenomicRegion maxOverlap = null; Annotation
   * maxAnnotation = null;
   * 
   * if (results != null) { for (GappedSearchFeatures<Annotation> features:
   * results) { for (Annotation annotation : features) {
   * //System.err.println("Comp " + region + " " + annotation.getRegion());
   * 
   * GenomicRegion overlap = GenomicRegion.overlap(region,
   * annotation.getRegion());
   * 
   * if (overlap != null) { if (overlap.getLength() > maxOverlapWidth) {
   * maxOverlapWidth = overlap.getLength(); maxOverlap = overlap; maxAnnotation =
   * annotation; } } } } }
   * 
   * if (mAddBeginning) { if (maxOverlapWidth > 0) { double p = 100.0 *
   * Math.min(1.0, maxOverlapWidth / (double)region.getLength());
   * 
   * 
   * cell = row.createCell(c++); cell.setCellValue(maxOverlap.toString()); cell =
   * row.createCell(c++); cell.setCellValue(Mathematics.round(p, 2)); cell =
   * row.createCell(c++); cell.setCellValue(maxAnnotation.getName()); cell =
   * row.createCell(c++); cell.setCellValue(maxAnnotation.getRegion().toString());
   * } else { cell = row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); } }
   * 
   * for (int j = 0; j < model.getColumnCount(); ++j) { cell =
   * row.createCell(c++);
   * 
   * setCellValue(cell, model.getText(i, j)); }
   * 
   * if (!mAddBeginning) { if (maxOverlapWidth > 0) { double p = 100.0 *
   * Math.min(1.0, maxOverlapWidth / (double)region.getLength());
   * 
   * cell = row.createCell(c++); cell.setCellValue(maxOverlap.toString()); cell =
   * row.createCell(c++); cell.setCellValue(Mathematics.round(p, 2)); cell =
   * row.createCell(c++); cell.setCellValue(maxAnnotation.getName()); cell =
   * row.createCell(c++); cell.setCellValue(maxAnnotation.getRegion().toString());
   * } else { cell = row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); cell =
   * row.createCell(c++); cell.setCellValue(TextUtils.NA); } }
   * 
   * ++r; }
   * 
   * XlsxTableModel newModel = new XlsxTableModel(workbook, true);
   * 
   * return newModel; } }
   */

  /*
   * private class OverlapTask extends SwingWorker<Void, Void> {
   * 
   * private FixedGapSearch<Annotation> mGappedSearch1; private DataFrame
   * mNewModel; private MessageDialogTaskGlassPane mSearchScreen; private Path
   * mFile; private boolean mAddBeginning;
   * 
   * public OverlapTask(FixedGapSearch<Annotation> gappedSearch1, Path file,
   * boolean addBeginning) { mGappedSearch1 = gappedSearch1; mFile = file;
   * mAddBeginning = addBeginning; }
   * 
   * @Override public Void doInBackground() { mSearchScreen =
   * mWindow.createTaskDialog("Overlapping...");
   * 
   * try { mNewModel = overlap(); } catch (Exception e) { e.printStackTrace(); }
   * 
   * return null; }
   * 
   * @Override public void done() { if (mNewModel != null) {
   * mWindow.history().addToHistory("Overlap with " + PathUtils.getName(mFile),
   * mNewModel); }
   * 
   * mSearchScreen.close(); }
   * 
   * private DataFrame overlap() throws Exception { DataFrame model =
   * getCurrentModel();
   * 
   * DataFrame matrix = AnnotatableMatrix.createMatrix(model.getRowCount(),
   * model.getColumnCount() + 4);
   * 
   * int c = 0;
   * 
   * if (mAddBeginning) { c = 0; } else { c = matrix.getColumnCount() - 4; }
   * 
   * String name = PathUtils.getNameNoExt(mFile);
   * 
   * matrix.setColumnName(c + 0, name + " Overlap Region"); matrix.setColumnName(c
   * + 1, name + " Overlap %"); matrix.setColumnName(c + 2, name +
   * " Overlap Feature"); matrix.setColumnName(c + 3, name + " Overlap Location");
   * 
   * if (mAddBeginning) { c = 4; } else { c = 0; }
   * 
   * for (int i = 0; i < model.getColumnCount(); ++i) { matrix.copyColumn(model,
   * i, c + i); }
   * 
   * if (mAddBeginning) { c = 0; } else { c = matrix.getColumnCount() - 4; }
   * 
   * for (int i = 0; i < model.getRowCount(); ++i) { GenomicRegion region = null;
   * 
   * if (Io.isEmptyLine(model.getText(i, 0))) { region = null; } else if
   * (model.getText(i, 0).contains(TextUtils.NA)) { region = null; } else if
   * (GenomicRegion.isRegion(model.getText(i, 0))) { region =
   * GenomicRegion.parse(model.getText(i, 0)); } else { // three column format
   * 
   * region = new GenomicRegion(GenomeService.getInstance().parse(model.getText(i,
   * 0)), TextUtils.parseInt(model.getText(i, 1)),
   * TextUtils.parseInt(model.getText(i, 2))); }
   * 
   * // Find the closest feature List<GappedSearchFeatures<Annotation>> results =
   * mGappedSearch1.getFeatures(region);
   * 
   * int maxOverlapWidth = -1; GenomicRegion maxOverlap = null; Annotation
   * maxAnnotation = null;
   * 
   * if (results != null) { for (GappedSearchFeatures<Annotation> features:
   * results) { for (Annotation annotation : features) {
   * //System.err.println("Comp " + region + " " + annotation.getRegion());
   * 
   * GenomicRegion overlap = GenomicRegion.overlap(region,
   * annotation.getRegion());
   * 
   * if (overlap != null) { if (overlap.getLength() > maxOverlapWidth) {
   * maxOverlapWidth = overlap.getLength(); maxOverlap = overlap; maxAnnotation =
   * annotation; } } } } }
   * 
   * if (maxOverlapWidth > 0) { double p = 100.0 * Math.min(1.0, maxOverlapWidth /
   * (double)region.getLength());
   * 
   * matrix.setText(i, c + 0, maxOverlap.toString()); matrix.setValue(i, c + 1,
   * Mathematics.round(p, 2)); matrix.setText(i, c + 2, maxAnnotation.getName());
   * matrix.setText(i, c + 3, maxAnnotation.getRegion().getLocation()); } else {
   * matrix.setText(i, c + 0, TextUtils.NA); matrix.setText(i, c + 1,
   * TextUtils.NA); matrix.setText(i, c + 2, TextUtils.NA); matrix.setText(i, c +
   * 3, TextUtils.NA); } }
   * 
   * return matrix; } }
   */

  /**
   * Overlap segments.
   * 
   * @author Antony Holmes
   */
  private class TwoWayOverlapTask extends SwingWorker<Void, Void> {

    private DataFrame mNewModel;
    private MessageDialogTaskGlassPane mSearchScreen;
    private Path mFile2;
    private Path mFile1;
    private FixedGapSearch<Annotation> mGappedSearch;
    private Map<GenomicRegion, Set<Path>> mFileMap = new HashMap<GenomicRegion, Set<Path>>();
    private boolean mVenn;

    public TwoWayOverlapTask(Path file1, int header1, Path file2, int header2, boolean venn)
        throws InvalidFormatException, IOException, ParseException {
      mFile1 = file1;
      mFile2 = file2;
      mVenn = venn;

      mGappedSearch = Annotation.parseFixed(file1, header1);

      for (Chromosome chr : mGappedSearch) {
        for (Annotation annotation : mGappedSearch.getFeatures(chr)) {
          GenomicRegion region = annotation.getRegion();

          if (!mFileMap.containsKey(region)) {
            mFileMap.put(region, new HashSet<Path>());
          }

          mFileMap.get(region).add(mFile1);
        }
      }

      FixedGapSearch<Annotation> gap2 = Annotation.parseFixed(file2, header2);

      for (Chromosome chr : gap2) {
        for (Annotation annotation : gap2.getFeatures(chr)) {
          GenomicRegion region = annotation.getRegion();

          if (!mFileMap.containsKey(region)) {
            mFileMap.put(region, new HashSet<Path>());
          }

          mFileMap.get(region).add(mFile2);
        }
      }

      mGappedSearch.add(gap2);
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Overlapping...");

      try {
        mNewModel = overlap();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Overlap with " + PathUtils.getName(mFile2), mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame overlap() throws Exception {
      Set<GenomicRegion> allocated = new HashSet<GenomicRegion>();

      Map<Chromosome, Map<GenomicRegion, Map<Path, GenomicRegion>>> overlapMap = new TreeMap<Chromosome, Map<GenomicRegion, Map<Path, GenomicRegion>>>();

      for (Chromosome chr1 : mGappedSearch) {
        for (Annotation annotation1 : mGappedSearch.getFeatures(chr1)) {
          GenomicRegion region1 = annotation1.getRegion();

          // Genome genome = region1.mGenome;

          List<Annotation> testRegions = mGappedSearch.getValues(region1);

          boolean exhausted = false;

          Set<GenomicRegion> used = new HashSet<GenomicRegion>();

          while (!exhausted) {
            Set<GenomicRegion> groupedRegions = CollectionUtils.toSet(region1);

            int start1 = region1.getStart();
            int end1 = region1.getEnd();

            for (Annotation annotation2 : testRegions) {
              GenomicRegion region2 = annotation2.getRegion();

              if (region2.equals(region1)) {
                continue;
              }

              if (used.contains(region2)) {
                continue;
              }

              int start2 = region2.getStart();
              int end2 = region2.getEnd();

              int minStart;
              int minEnd;
              int maxStart;
              int maxEnd;

              if (start1 <= start2) {
                minStart = start1;
                minEnd = end1;
                maxStart = start2;
                maxEnd = end2;
              } else {
                minStart = start2;
                minEnd = end2;
                maxStart = start1;
                maxEnd = end1;
              }

              int overlap = -1;
              int overlapStart = -1;

              if (maxStart >= minStart && maxEnd <= minEnd) {
                overlap = maxEnd - maxStart + 1;
                overlapStart = maxStart;
              } else if (minStart < maxStart && minEnd > maxStart) {
                overlap = minEnd - maxStart + 1;
                overlapStart = maxStart;
              } else {
                // do nothing
              }

              if (overlap == -1) {
                continue;
              }

              start1 = overlapStart;
              end1 = start1 + overlap - 1;

              groupedRegions.add(region2);
            }

            if (groupedRegions.size() > 1 || !allocated.contains(region1)) {
              GenomicRegion overlapRegion = new GenomicRegion(chr1, start1, end1);

              for (GenomicRegion region : groupedRegions) {
                Set<Path> files = mFileMap.get(region);

                if (!overlapMap.containsKey(region.getChr())) {
                  overlapMap.put(overlapRegion.getChr(), new TreeMap<GenomicRegion, Map<Path, GenomicRegion>>());
                }

                if (!overlapMap.get(overlapRegion.getChr()).containsKey(overlapRegion)) {
                  overlapMap.get(overlapRegion.getChr()).put(overlapRegion, new TreeMap<Path, GenomicRegion>());
                }

                // In cases where the same peak occurs in both
                // files, make sure both files are added to
                // the overlap map.
                for (Path file : files) {
                  overlapMap.get(overlapRegion.getChr()).get(overlapRegion).put(file, region);
                }

                used.add(region);
                allocated.add(region);
              }
            }

            // We cannot group this region with anything else
            if (groupedRegions.size() == 1) {
              exhausted = true;
            }
          }
        }
      }

      int r = 0;

      for (Chromosome chr : overlapMap.keySet()) {
        for (GenomicRegion o : overlapMap.get(chr).keySet()) {
          ++r;
        }
      }

      DataFrame matrix = DataFrame.createDataFrame(r, 9);

      matrix.setColumnName(0, "Overlap Region");
      matrix.setColumnName(1, "Overlap Width");
      matrix.setColumnName(2, "Number Of Overlapping Regions");
      matrix.setColumnName(3, PathUtils.getNameNoExt(mFile1) + " Regions");
      matrix.setColumnName(4, PathUtils.getNameNoExt(mFile1) + " Region Widths");
      matrix.setColumnName(5, PathUtils.getNameNoExt(mFile1) + " % Overlap");
      matrix.setColumnName(6, PathUtils.getNameNoExt(mFile2) + " Regions");
      matrix.setColumnName(7, PathUtils.getNameNoExt(mFile2) + " Region Widths");
      matrix.setColumnName(8, PathUtils.getNameNoExt(mFile2) + " % Overlap");

      final Group group1 = new Group(PathUtils.getNameNoExt(mFile1), Color.RED);
      final Group group2 = new Group(PathUtils.getNameNoExt(mFile2), Color.BLUE);

      r = 0;

      for (Chromosome chr : overlapMap.keySet()) {
        for (GenomicRegion overlapRegion : overlapMap.get(chr).keySet()) {
          matrix.set(r, 0, overlapRegion.getLocation());
          matrix.set(r, 1, overlapRegion.getLength());
          matrix.set(r, 2, overlapMap.get(chr).get(overlapRegion).size());

          if ((overlapMap.get(chr).get(overlapRegion).containsKey(mFile1))) {
            group1.add(overlapRegion.getLocation());

            matrix.set(r, 3, overlapMap.get(chr).get(overlapRegion).get(mFile1).getLocation());
            matrix.set(r, 4, overlapMap.get(chr).get(overlapRegion).get(mFile1).getLength());

            GenomicRegion region = overlapMap.get(chr).get(overlapRegion).get(mFile1);

            double p = 100.0 * Math.min(1.0, GenomicRegion.p(overlapRegion, region));

            matrix.set(r, 5, Mathematics.round(p, 2));
          } else {
            matrix.set(r, 3, TextUtils.NA);
            matrix.set(r, 4, TextUtils.NA);
            matrix.set(r, 5, TextUtils.NA);
          }

          if ((overlapMap.get(chr).get(overlapRegion).containsKey(mFile2))) {
            group2.add(overlapRegion.getLocation());

            matrix.set(r, 6, overlapMap.get(chr).get(overlapRegion).get(mFile2).getLocation());

            matrix.set(r, 7, overlapMap.get(chr).get(overlapRegion).get(mFile2).getLength());

            GenomicRegion region = overlapMap.get(chr).get(overlapRegion).get(mFile2);

            double p = 100.0 * Math.min(1.0, GenomicRegion.p(overlapRegion, region));

            matrix.set(r, 8, Mathematics.round(p, 2));
          } else {
            matrix.set(r, 6, TextUtils.NA);
            matrix.set(r, 7, TextUtils.NA);
            matrix.set(r, 8, TextUtils.NA);
          }

          ++r;
        }
      }

      if (mVenn) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MainVennWindow plotWindow = new MainVennWindow(group1, group2, CircleStyle.PROPORTIONAL);

            plotWindow.setVisible(true);
          }
        });
      }

      return matrix;
    }
  }

  /**
   * Annotate
   * 
   * @author Antony Holmes
   *
   */

  private class ConservationTask extends SwingWorker<Void, Void> {

    private DataFrame mNewModel;
    private MessageDialogTaskGlassPane mSearchScreen;
    private boolean mShowScores;
    private boolean mShowMedian;
    private boolean mShowMean;
    private Genome mGenome;

    public ConservationTask(Genome genome, boolean showMean, boolean showMedian, boolean showScores) {
      mGenome = genome;
      mShowMean = showMean;
      mShowMedian = showMedian;
      mShowScores = showScores;
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Annotating...");

      try {
        mNewModel = conservation();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Add Conservation", mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame conservation() throws Exception {
      DataFrame model = getCurrentModel();

      int extra = 0;

      if (mShowMean) {
        ++extra;
      }

      if (mShowMedian) {
        ++extra;
      }

      if (mShowScores) {
        ++extra;
      }

      DataFrame matrix = DataFrame.createDataFrame(model.getRows(), model.getCols() + extra);

      DataFrame.copyColumnHeaders(model, matrix);

      int c = model.getCols();

      if (mShowMean) {
        matrix.setColumnName(c++, "Mean Conservation %");
      }

      if (mShowMedian) {
        matrix.setColumnName(c++, "Median Conservation %");
      }

      if (mShowScores) {
        matrix.setColumnName(c++, "Conservation Scores");
      }

      for (int i = 0; i < model.getRows(); ++i) {
        matrix.copyRow(model, i, i);

        GenomicRegion region;

        if (Io.isEmptyLine(model.getText(i, 0))) {
          continue;
        } else if (model.getText(i, 0).contains(TextUtils.NA)) {
          continue;
        } else if (GenomicRegion.isGenomicRegion(model.getText(i, 0))) {
          region = GenomicRegion.parse(mGenome, model.getText(i, 0));
        } else {
          // three column format

          region = new GenomicRegion(ChromosomeService.getInstance().chr(mGenome, model.getText(i, 0)),
              TextUtils.parseInt(model.getText(i, 1)), TextUtils.parseInt(model.getText(i, 2)));
        }

        c = model.getCols();

        List<Double> scores = mConservationAssembly.getScores(region);

        if (mShowMean) {
          matrix.set(i, c++, Statistics.mean(scores));
        }

        if (mShowMedian) {
          matrix.set(i, c++, Statistics.median(scores));
        }

        if (mShowScores) {
          matrix.set(i, c++, TextUtils.scJoin(scores));
        }
      }

      return matrix;
    }
  }

  private class ConservationMouseOnlyTask extends SwingWorker<Void, Void> {

    private DataFrame mNewModel;
    private MessageDialogTaskGlassPane mSearchScreen;
    private boolean mShowScores;
    private boolean mShowConservation;
    private Genome mGenome;

    public ConservationMouseOnlyTask(Genome genome, boolean showConservation, boolean showScores) {
      mGenome = genome;
      mShowConservation = showConservation;
      mShowScores = showScores;
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Annotating..");

      try {
        mNewModel = conservation();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Add Mouse Conservation", mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame conservation() throws Exception {
      DataFrame model = getCurrentModel();

      int extra = 0;

      if (mShowConservation) {
        ++extra;
      }

      if (mShowScores) {
        ++extra;
      }

      DataFrame matrix = DataFrame.createDataFrame(model.getRows(), model.getCols() + extra);

      DataFrame.copyColumnHeaders(model, matrix);

      int c = model.getCols();

      if (mShowConservation) {
        matrix.setColumnName(c++, "Mouse Conservation %");
      }

      if (mShowScores) {
        matrix.setColumnName(c++, "Mouse Conservation Scores");
      }

      for (int i = 0; i < model.getRows(); ++i) {
        matrix.copyRow(model, i, i);

        GenomicRegion region;

        if (Io.isEmptyLine(model.getText(i, 0))) {
          continue;
        } else if (model.getText(i, 0).contains(TextUtils.NA)) {
          continue;
        } else if (GenomicRegion.isGenomicRegion(model.getText(i, 0))) {
          region = GenomicRegion.parse(mGenome, model.getText(i, 0));
        } else {
          // three column format

          region = new GenomicRegion(ChromosomeService.getInstance().chr(mGenome, model.getText(i, 0)),
              TextUtils.parseInt(model.getText(i, 1)), TextUtils.parseInt(model.getText(i, 2)));
        }

        c = model.getCols();

        List<Double> scores = mMouseConservationAssembly.getScores(region);

        if (mShowConservation) {
          matrix.set(i, c++, Statistics.pNonZero(scores) * 100);
        }

        if (mShowScores) {
          matrix.set(i, c++, TextUtils.scJoin(scores));
        }
      }

      return matrix;
    }
  }

  private class StitchTask extends SwingWorker<Void, Void> {
    private DataFrame mNewModel;
    private MessageDialogTaskGlassPane mSearchScreen;
    private int mDistance;
    private boolean mTssExclusion;
    private int mExt5p;
    private int mExt3p;
    private GenesDB mTssSearch;
    private Genome mGenome;

    public StitchTask(Genome genome, GenesDB tssSearch, int distance, boolean tssExclusion, int ext5p, int ext3p) {
      mGenome = genome;
      mTssSearch = tssSearch;
      mDistance = distance;
      mTssExclusion = tssExclusion;
      mExt5p = ext5p;
      mExt3p = ext3p;
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Stitching...");

      try {
        mNewModel = stitch();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Stitch", mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame stitch() throws IOException, ParseException {
      DataFrame model = getCurrentModel();

      // Keep track of all peaks closest to a given reference start
      // peak
      Map<Chromosome, Map<Integer, List<GenomicRegion>>> stitchMap = new TreeMap<Chromosome, Map<Integer, List<GenomicRegion>>>();

      // sort regions by start
      Map<Chromosome, List<GenomicRegion>> sortedRegions = GenomicRegion.sortByStart(getRegions(mGenome, model));

      int stitchCount = 0;

      Genome genome = Genome.NO_GENOME;

      for (Chromosome chr : CollectionUtils.sort(sortedRegions.keySet())) {
        // from each earliest start, build a cluster

        stitchMap.put(chr, new TreeMap<Integer, List<GenomicRegion>>());

        // Get the regions
        List<GenomicRegion> regions = sortedRegions.get(chr);

        int i = 0;

        while (i < regions.size()) {
          GenomicRegion region1 = regions.get(i);

          // genome = region1.mGenome;

          ++stitchCount;

          ++i;

          int start1 = region1.getStart();
          int end1 = region1.getEnd();

          // System.err.println("start " + start1 + " " + end1);

          // Use the first closest peak as an an anchor and
          // attempt to add peaks to it
          stitchMap.get(chr).put(start1, new ArrayList<GenomicRegion>());
          stitchMap.get(chr).get(start1).add(region1);

          GenomicElement region1UpstreamGene = upstreamOfTss(region1);

          // If we are in the exclusion zone then stop
          if (mTssExclusion && inTssExZone(region1)) {
            System.err.println("region 1 exclusion " + region1UpstreamGene);

            continue;
          }
          // region 1 cannot be wholly inside a tss
          // exclusion zone if it is to be stitched to
          // something

          int s2 = i;

          for (int j = s2; j < regions.size(); ++j) {
            GenomicRegion region2 = regions.get(j);

            // Clearly if we are checking the exclusion and
            // are deemed removable, we have reached a TSS
            // zone and should stop
            if (mTssExclusion && inTssExZone(region2)) {
              System.err.println("region 2 inTssExZone " + region2);
              break;
            }

            boolean region2TssDownstream = downstreamOfTss(region2, region1UpstreamGene);

            // System.err.println("r2 " + region2 + " " + region2TssDownstream);

            // Region 1 cannnot be before the tss and region 2
            // cannot be after as this means they are spanning
            // a tss, so they cannot be stiched
            if (mTssExclusion && region1UpstreamGene != null && region2TssDownstream) {
              break;
            }

            int start2 = region2.getStart();
            int end2 = region2.getEnd();

            // System.err.println("s2 " + start2 + " " + (end1 - start2));

            // Stop if the gap between the regions is too large
            if (Math.abs(end1 - start2) > mDistance) {
              break;
            }

            // Finally after all checks we can stitch this
            // region to the chain
            stitchMap.get(chr).get(start1).add(region2);

            end1 = end2;

            ++i;
          }
        }
      }

      DataFrame matrix = DataFrame.createTextMatrix(stitchCount, 1);

      matrix.setColumnName(0, "Stitched Region");

      // XSSFWorkbook workbook = new XSSFWorkbook();

      // Sheet sheet = workbook.createSheet();

      // Keep track of how many rows we have created.
      int r = 0;

      // XSSFRow row = (XSSFRow)sheet.createRow(r);
      // XSSFCell cell;

      // int c = 0;

      // cell = row.createCell(c++);
      // cell.setCellValue("Stitched Region");

      // ++r;

      for (Chromosome chr : stitchMap.keySet()) {
        for (int start : stitchMap.get(chr).keySet()) {
          List<GenomicRegion> stitchList = stitchMap.get(chr).get(start);

          int min = stitchList.get(0).getStart();
          int max = stitchList.get(stitchList.size() - 1).getEnd();

          GenomicRegion newRegion = new GenomicRegion(chr, min, max);

          // row = (XSSFRow)sheet.createRow(r);

          // cell = row.createCell(0);
          // cell.setCellValue(newRegion.toString());

          matrix.set(r, 0, newRegion.toString());

          ++r;
        }
      }

      // XlsxTableModel newModel =
      // new XlsxTableModel(workbook, true);

      // return newModel;

      return matrix;
    }

    /**
     * Returns true if region is wholly within tss exclusion zone.
     * 
     * @param region
     * @return
     * @throws IOException
     */
    private boolean inTssExZone(GenomicRegion region) throws IOException {

      List<GenomicElement> closestFeatures = mTssSearch.closest(mGenome, region, GenomicType.TRANSCRIPT, 1);

      for (GenomicElement gene : closestFeatures) {
        GenomicRegion tss = gene.getTss();

        // Extend around the tss
        GenomicRegion extTss = GenomicRegion.extend(tss, mExt5p, mExt3p);

        // see if the region is wholly within the tss

        // System.out.println(tss + " " + extTss + " " + gene + " " + region + " " +
        // GenomicRegion.within(region, extTss));

        if (GenomicRegion.within(region, extTss)) {
          return true;
        }
      }

      return false;
    }

    private boolean overlappingTssExZone(GenomicRegion region) throws IOException {

      List<GenomicElement> closestFeatures = mTssSearch.closest(mGenome, region, GenomicType.TRANSCRIPT, 1);

      for (GenomicElement gene : closestFeatures) {
        GenomicRegion tss = gene.getTss();

        // Extend around the tss
        GenomicRegion extTss = GenomicRegion.extend(tss, mExt5p, mExt3p);

        // see if the region is wholly within the tss

        if (GenomicRegion.overlap(region, extTss) != null) {
          return true;
        }
      }

      return false;
    }

    private GenomicElement upstreamOfTss(GenomicRegion region) throws IOException {

      List<GenomicElement> closestFeatures = mTssSearch.closest(mGenome, region, GenomicType.TRANSCRIPT, 1);

      for (GenomicElement gene : closestFeatures) {
        GenomicRegion tss = gene.getTss();

        // Extend around the tss
        int mid = GenomicRegion.midDist(region, tss);

        if (mid < 0) {
          return gene;
        }
      }

      return null;
    }

    private boolean downstreamOfTss(GenomicRegion region, GenomicElement gene) {
      if (gene == null) {
        return false;
      }

      GenomicRegion tss = gene.getTss();

      // Extend around the tss
      int mid = GenomicRegion.midDist(region, tss);

      return mid >= 0;
    }
  }

  private class ExtendTask extends SwingWorker<Void, Void> {
    private DataFrame mNewModel;
    private MessageDialogTaskGlassPane mSearchScreen;
    private int mExt3p;
    private int mExt5p;
    private Genome mGenome;

    public ExtendTask(Genome genome, int ext5p, int ext3p) {
      mGenome = genome;
      mExt5p = ext5p;
      mExt3p = ext3p;
    }

    @Override
    public Void doInBackground() {
      mSearchScreen = mWindow.createTaskDialog("Extending...");

      try {
        mNewModel = extend();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      if (mNewModel != null) {
        mWindow.history().addToHistory("Extend", mNewModel);
      }

      mSearchScreen.close();
    }

    private DataFrame extend() throws IOException, ParseException {
      DataFrame model = getCurrentModel();

      List<GenomicRegion> regions = getRegions(mGenome, model);

      List<GenomicRegion> extendedRegions = GenomicRegion.extend(regions, mExt5p, mExt3p);

      DataFrame matrix = DataFrame.createTextMatrix(extendedRegions.size(), 1);

      matrix.setColumnName(0, "Extended Region");

      for (int i = 0; i < extendedRegions.size(); ++i) {
        matrix.set(i, 0, extendedRegions.get(i).toString());
      }

      return matrix;

      /*
       * XSSFWorkbook workbook = new XSSFWorkbook();
       * 
       * Sheet sheet = workbook.createSheet();
       * 
       * // Keep track of how many rows we have created. int r = 0;
       * 
       * XSSFRow row = (XSSFRow)sheet.createRow(r); XSSFCell cell;
       * 
       * int c = 0;
       * 
       * cell = row.createCell(c++); cell.setCellValue("Extended Region");
       * 
       * ++r;
       * 
       * for (GenomicRegion region : extendedRegions) { row =
       * (XSSFRow)sheet.createRow(r);
       * 
       * cell = row.createCell(0); cell.setCellValue(region.toString());
       * 
       * ++r; }
       * 
       * XlsxTableModel newModel = new XlsxTableModel(workbook, true);
       * 
       * return newModel;
       */
    }
  }

  public RegionsModule() {
    try {
      mConservationAssembly = new ConservationAssemblyWeb(
          new URL(SettingsService.getInstance().getString("regions.conservation.remote-url")));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      mMouseConservationAssembly = new ConservationAssemblyWeb(
          new URL(SettingsService.getInstance().getString("regions.mouse.conservation.remote-url")));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "Regions";
  }

  @Override
  public GuiAppInfo getModuleInfo() {
    return new RegionsInfo();
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    Ribbon ribbon = window.getRibbon();

    ModernClickWidget button;

    button = new RibbonLargeButton("Overlap", AssetService.getInstance().loadIcon("common_regions", 24));
    button.setToolTip(new ModernToolTip("Overlap", "Find common regions between two sets of coordinates."));
    button.addClickListener(this);
    ribbon.getToolbar("Genomic").getSection("Regions").add(button);

    /*
     * button = new RibbonLargeButton("N-way Overlap",
     * UIService.getInstance().loadIcon("common_regions", 24));
     * button.setToolTip(new ModernToolTip("N-way Overlap",
     * "Find common regions between multiple sets of coordinates."),
     * ribbon.getToolTipModel()); button.addClickListener(this);
     * ribbon.getToolbar("Genomic").getSection("Regions").add(button);
     */

    button = new RibbonLargeButton("Closest", AssetService.getInstance().loadIcon("closest", 24));
    button.setToolTip(new ModernToolTip("Closest", "Find the closest feature in another list of features."));
    // Ui.setSize(button, new Dimension(60, getRibbon().LARGE_BUTTON_HEIGHT));
    button.addClickListener(this);
    ribbon.getToolbar("Genomic").getSection("Regions").add(button);

    button = new RibbonLargeButton("Stitch", AssetService.getInstance().loadIcon("stitch", 24));
    button.setToolTip(new ModernToolTip("Stitch", "Stitch regions together."));
    // Ui.setSize(button, new Dimension(60, getRibbon().LARGE_BUTTON_HEIGHT));
    button.addClickListener(this);
    ribbon.getToolbar("Genomic").getSection("Regions").add(button);

    button = new RibbonLargeButton("Extend", AssetService.getInstance().loadIcon("extend", 24));
    button.setToolTip(new ModernToolTip("Extend", "Extend regions."));
    // Ui.setSize(button, new Dimension(60, getRibbon().LARGE_BUTTON_HEIGHT));
    button.addClickListener(this);
    ribbon.getToolbar("Genomic").getSection("Regions").add(button);

    ribbon.getToolbar("Genomic").getSection("Regions").addSeparator();

    button = new RibbonLargeButton("Super", "Enhancers", AssetService.getInstance().loadIcon("enhancers", 24));
    button.setToolTip(new ModernToolTip("Super Enhancers", "Annotate peaks with enhancers."));
    // Ui.setSize(button, new Dimension(60, getRibbon().LARGE_BUTTON_HEIGHT));
    button.addClickListener(this);
    ribbon.getToolbar("Genomic").getSection("Regions").add(button);

    //
    // Tss plots
    //

    ModernPopupMenu2 popup;

    /*
     * ModernPopupMenu popup = new ModernPopupMenu();
     * 
     * popup.addMenuItem(new ModernTwoLineMenuItem("46-way Conservation",
     * "Add 46-way conservation scores.",
     * UIService.getInstance().loadIcon("conservation", 24)));
     * 
     * popup.addMenuItem(new ModernTwoLineMenuItem("Mouse Conservation",
     * "Add conservation in mouse.",
     * UIService.getInstance().loadIcon("conservation", 24)));
     * 
     * button = new RibbonLargeDropDownButton("Conservation",
     * UIService.getInstance().loadIcon("conservation", 24), popup);
     * button.setToolTip("Conservation", "Add conservation scores to regions.");
     * mWindow.getRibbon().getToolbar("Genomic").getSection("Regions").add( button);
     * button.addClickListener(this);
     */

    /*
     * button = new RibbonLargeButton("46-way Cons",
     * UIService.getInstance().loadIcon("conservation", 24)); button.setToolTip(new
     * ModernToolTip("46-way Conservation", "Add 46-way conservation."));
     * //Ui.setSize(button, new Dimension(80, getRibbon().LARGE_BUTTON_HEIGHT));
     * button.addClickListener(this);
     * ribbon.getToolbar("Genomic").getSection("Conservation").add(button);
     * 
     * button = new RibbonLargeButton("Mouse Cons",
     * UIService.getInstance().loadIcon("conservation", 24)); button.setToolTip(new
     * ModernToolTip("Mouse Only Conservation", "Add conservation in mouse."));
     * //Ui.setSize(button, new Dimension(80, getRibbon().LARGE_BUTTON_HEIGHT));
     * button.addClickListener(this);
     * ribbon.getToolbar("Genomic").getSection("Conservation").add(button);
     */

    //
    // Tss plots
    //

    ribbon.getToolbar("Genomic").getSection("Regions").addSeparator();

    popup = new ModernPopupMenu2();

    popup.addMenuItem(new ModernTwoLineMenuItem("TSS Plot", "Plot the distance between regions and gene TSS.",
        AssetService.getInstance().loadIcon("graph", 24)));

    popup.addMenuItem(new ModernTwoLineMenuItem("Distance Plot", "Plot distances between two sets of regions.",
        AssetService.getInstance().loadIcon("graph", 24)));

    // popup.addMenuItem(new ModernMenuSeparator());

    // popup.addMenuItem(new ModernMenuHelpItem("Help with splitting a
    // matrix...",
    // "matcalc.split.help.url").setTextOffset(48));

    button = new RibbonLargeDropDownButton2(AssetService.getInstance().loadIcon("graph", 24), popup);
    button.setToolTip("Distance Plot", "Plot the distance between regions.");

    mWindow.getRibbon().getToolbar("Genomic").getSection("Regions").add(button);

    button.addClickListener(this);

    /*
     * button = new RibbonLargeButton("Export BED",
     * UIService.getInstance().loadIcon("save", 24)); button.setToolTip(new
     * ModernToolTip("Export BED Path", "Export Table As BED Path."),
     * ribbon.getToolTipModel()); button.addClickListener(this);
     * ribbon.getToolbar("Genomic").getSection("Plot").add(button);
     */
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals("Super Enhancers")) {
      try {
        enhancers(Genome.HG19);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Overlap") || e.getMessage().equals("N-way Overlap")) {
      try {
        nWayOverlap(Genome.HG19);
      } catch (IOException | InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Closest")) {
      try {
        closest(Genome.HG19);
      } catch (IOException | InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Stitch")) {
      try {
        stitch();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Extend")) {
      extend();
    } else if (e.getMessage().equals("46-way Conservation")) {
      try {
        conservation();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Mouse Conservation")) {
      try {
        conservationMouseOnly();
      } catch (IOException | InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("TSS Plot")) {
      try {
        tssPlot();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Distance Plot")) {
      try {
        distancePlot(Genome.HG19);
      } catch (IOException | InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Export BED")) {
      try {
        exportBed(Genome.HG19);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else {
      // Do nothing
    }
  }

  private void exportBed(Genome genome) throws IOException {
    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      return;
    }

    List<GenomicRegion> regions = getRegions(genome, m);

    Path file = BioInfDialog.saveBedFile(mWindow, RecentFilesService.getInstance().getPwd());

    Bed.writeBed(regions, file);

    RecentFilesService.getInstance().add(file);

    ModernMessageDialog.createFileSavedDialog(mWindow, file);
  }

  private void enhancers(Genome genome) throws IOException {
    EnhancerDialog dialog = new EnhancerDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    Map<String, BinaryGapSearch<Annotation>> gappedSearch = dialog.getGappedSearch(genome);

    EnhancerTask task = new EnhancerTask(gappedSearch, dialog.getOverlapMode());

    task.execute();
  }

  /*
   * private void overlap() throws IOException, InvalidFormatException,
   * ParseException { if (mWindow.getInputFile() == null) { return; }
   * 
   * Path file = openOverlapFileDialog(mWindow,
   * RecentFilesService.getInstance().getPwd());
   * 
   * if (file == null) { return; }
   * 
   * OverlapDialog dialog = new OverlapDialog(mWindow, mWindow.getInputFile(),
   * file);
   * 
   * dialog.setVisible(true);
   * 
   * if (dialog.getStatus() == ModernDialogStatus.CANCEL) { return; }
   * 
   * FixedGapSearch<Annotation> gappedSearch1 = Annotation.parse(file);
   * 
   * if (dialog.getStrict()) { OverlapTask task = new OverlapTask(gappedSearch1,
   * file, dialog.getAddBeginning());
   * 
   * task.execute(); } else { TwoWayOverlapTask task = new
   * TwoWayOverlapTask(mWindow.getInputFile(), dialog.getHeader1(), file,
   * dialog.getHeader2(), dialog.getDrawVenn());
   * 
   * task.execute(); } }
   */

  private void nWayOverlap(Genome genome) throws InvalidFormatException, IOException {
    NWayOverlapDialog dialog = new NWayOverlapDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    List<Path> files = dialog.getFiles();

    if (files.size() == 0) {
      return;
    }

    if (dialog.getOneWay()) {
      OneWayOverlapTask task = new OneWayOverlapTask(mWindow, genome, files, dialog.getAtBeginning(), true);

      task.doInBackground();
    } else {
      NWayOverlapTask task = new NWayOverlapTask(mWindow, files, dialog.getDrawVenn());

      task.doInBackground();
    }
  }

  private void closest(Genome genome) throws IOException, InvalidFormatException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    Path file = openOverlapFileDialog(mWindow, RecentFilesService.getInstance().getPwd());

    if (file == null) {
      return;
    }

    BinaryGapSearch<Annotation> gappedSearch = null;

    if (PathUtils.getFileExt(file).equals("bed")) {
      gappedSearch = Annotation.parseBed(file);
    } else if (PathUtils.getFileExt(file).equals("bedgraph")) {
      gappedSearch = Annotation.parseBed(file);
    } else {
      ModernDataModel model = Bioinformatics.getModel(file, 1, TextUtils.emptyList(), 0, TextUtils.TAB_DELIMITER);

      gappedSearch = Annotation.parsePeaks(genome, model);
    }

    // SpeciesDialog dialog = new SpeciesDialog(mWindow, "Closest");

    // dialog.setVisible(true);

    // if (dialog.isCancelled()) {
    // return;
    // }

    ClosestTask task = new ClosestTask(mWindow, genome, gappedSearch, file);

    // task.execute();

    task.doInBackground();
    task.done();
  }

  private void stitch() throws IOException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    StitchDialog dialog = new StitchDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    Genome genome = dialog.getGenome();

    GenesDB tssSearch = AnnotationService.getInstance().getSearch(genome); // getBinarySearch(genome);

    StitchTask task = new StitchTask(genome, tssSearch, dialog.getDistance(), dialog.getTssExclusion(),
        dialog.getTss5p(), dialog.getTss3p());

    task.execute();

  }

  private void extend() {
    if (mWindow.getInputFile() == null) {
      return;
    }

    ExtendDialog dialog = new ExtendDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ExtendTask task = new ExtendTask(dialog.getGenome(), dialog.getExt5p(), dialog.getExt3p());

    task.execute();
  }

  private void conservation() throws IOException, InvalidFormatException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    ConservationDialog dialog = new ConservationDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ConservationTask task = new ConservationTask(dialog.getGenome(), dialog.getShowMean(), dialog.getShowMedian(),
        dialog.getShowScores());

    task.execute();
  }

  private void conservationMouseOnly() throws IOException, InvalidFormatException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    MouseConservationDialog dialog = new MouseConservationDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ConservationMouseOnlyTask task = new ConservationMouseOnlyTask(dialog.getGenome(), dialog.getShowConservation(),
        dialog.getShowScores());

    task.execute();
  }

  private void tssPlot() throws IOException, ParseException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    TssPlotDialog dialog = new TssPlotDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    Genome genome = dialog.getGenome();

    if (genome == null) {
      return;
    }

    GenesDB tssSearch = AnnotationService.getInstance().getSearch(genome);

    TssPlotTask task = new TssPlotTask(mWindow, genome, tssSearch, dialog.getStart(), dialog.getEnd(),
        dialog.getUnits(), dialog.getBinSize(), dialog.getBinUnits());

    task.plot();
  }

  private void distancePlot(Genome genome) throws IOException, InvalidFormatException {
    if (mWindow.getInputFile() == null) {
      return;
    }

    Path file = openOverlapFileDialog(mWindow, RecentFilesService.getInstance().getPwd());

    if (file == null) {
      return;
    }

    PlotDialog plotDialog = new PlotDialog(mWindow);

    plotDialog.setVisible(true);

    if (plotDialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    BinaryGapSearch<Annotation> gappedSearch = null;

    if (PathUtils.getFileExt(file).equals("bed")) {
      gappedSearch = Annotation.parseBed(file);
    } else if (PathUtils.getFileExt(file).equals("bedgraph")) {
      gappedSearch = Annotation.parseBed(file);
    } else {
      ModernDataModel model = Bioinformatics.getModel(file, 1, TextUtils.emptyList(), 0, TextUtils.TAB_DELIMITER);

      gappedSearch = Annotation.parsePeaks(plotDialog.getGenome(), model);
    }

    DistancePlotTask task = new DistancePlotTask(mWindow, genome, gappedSearch, plotDialog.getStart(),
        plotDialog.getEnd(), plotDialog.getUnits(), plotDialog.getBinSize(), plotDialog.getBinUnits());

    task.plot();
  }

  private DataFrame getCurrentModel() {
    return mWindow.getCurrentMatrix();
  }

  public static Path openOverlapFileDialog(Frame parent, Path pwd) throws IOException {
    return FileDialog.openFile(parent, pwd, new AllRegionGuiFileFilter(), new XlsxGuiFileFilter(),
        new TxtGuiFileFilter(), new BedGuiFileFilter(), new BedGraphGuiFileFilter());
  }

  private static List<GenomicRegion> getRegions(Genome genome, DataFrame model) {

    List<GenomicRegion> regions = new ArrayList<GenomicRegion>();

    for (int i = 0; i < model.getRows(); ++i) {
      GenomicRegion region = null;

      if (Io.isEmptyLine(model.getText(i, 0))) {
        continue;
      } else if (model.getText(i, 0).contains(TextUtils.NA)) {
        continue;
      } else if (GenomicRegion.isGenomicRegion(model.getText(i, 0))) {
        region = GenomicRegion.parse(genome, model.getText(i, 0));
      } else {
        // three column format

        region = new GenomicRegion(ChromosomeService.getInstance().chr(genome, model.getText(i, 0)),
            Integer.parseInt(model.getText(i, 1)), Integer.parseInt(model.getText(i, 2)));
      }

      regions.add(region);
    }

    return regions;
  }

}
