package edu.columbia.rdf.matcalc.bio.toolbox.gep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jebtk.core.NetworkFileException;
import org.jebtk.core.collections.ArrayListMultiMap;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.Io;
import org.jebtk.core.io.TmpService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.edb.Sample;
import edu.columbia.rdf.edb.VfsFile;
import edu.columbia.rdf.edb.ui.FileDownloader;
import edu.columbia.rdf.edb.ui.Repository;
import edu.columbia.rdf.edb.ui.RepositoryService;
import edu.columbia.rdf.matcalc.MainMatCalcWindow;

public class RnaSeqData {
  private static final Logger LOG = LoggerFactory.getLogger(RnaSeqData.class);

  private static final int ANNOTATION_COLUMNS = 3;

  /**
   * Shows the expression data.
   * 
   * @param parent
   * @param samples
   * @param type
   * @param columns
   * @param checkIfFileExists
   * @param statusModel
   * @throws IOException
   * @throws NetworkFileException
   * @throws ParseException
   */
  public void showTables(MainMatCalcWindow parent,
      Collection<Sample> samples,
      boolean checkIfFileExists) throws NetworkFileException, IOException {

    Map<String, Set<Sample>> samplesGrouped = Sample.sortByOrganism(samples);

    boolean areDifferent = samplesGrouped.size() > 1;

    if (areDifferent) {
      String types = TextUtils
          .listAsSentence(CollectionUtils.sort(samplesGrouped.keySet()));

      ModernMessageDialog.createWarningDialog(parent,
          "You have selected samples from " + types + ".",
          "You cannot group these.");

      return;
    }

    /*
     * for threading ExpressionDataTask expressionTask = new
     * ExpressionDataTask(parent, experiments, samplesGroupedByArray, type,
     * columns, columnAnnotations, checkIfFileExists, statusModel);
     * 
     * expressionTask.execute();
     */

    download(parent, samples, checkIfFileExists);
  }

  /**
   * Download and merge samples.
   * 
   * @param parent
   * @param experiments
   * @param samplesGroupedByArray
   * @param type
   * @param checkIfFileExists
   * @param statusModel
   * @throws IOException
   * @throws NetworkFileException
   * @throws ParseException
   */
  public void download(MainMatCalcWindow window,
      Collection<Sample> samples,
      boolean checkIfFileExists) throws IOException, NetworkFileException {

    //
    // Download the experiments so we can extract data from them
    //

    Map<Sample, Path> sampleFiles = new TreeMap<Sample, Path>();

    for (Sample sample : samples) {
      Path localFile = downloadExpressionData(sample, checkIfFileExists);

      sampleFiles.put(sample, localFile);
    }

    //
    // Now lets merge all of the experiment data together
    //

    Path allSamplesFile = pasteFiles(sampleFiles);

    // If the first column is set to true and the others are false, this
    // means only the signal is required. In that case we can load the
    // data directly into plot

    /*
     * boolean signal = columns.get(0);
     * 
     * boolean other = false;
     * 
     * for (int i = 1; i < columns.size(); ++i) { other = columns.get(i);
     * 
     * if (other) { break; } }
     */

    LOG.info("Opening {}...", allSamplesFile);

    try {
      window.openFiles().indexes(3).open(allSamplesFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // SwingUtilities.invokeLater(new PlotSamples(allSamplesFile,
    // samplesGroupedByArray.keySet().iterator().next()));

    /*
     * if (signal && !other) { // A numerical matrix can be sent straight to the
     * plot tool SwingUtilities.invokeLater(new PlotSamples(allSamplesFile,
     * samplesGroupedByArray.keySet().iterator().next())); } else {
     * SwingUtilities.invokeLater(new ShowSamples(allSamplesFile,
     * samplesGroupedByArray.keySet().iterator().next())); }
     */
  }

  /**
   * Takes a list of files and pastes them together
   * 
   * @param sampleFileMap
   * @param columns
   * @param columnAnnotations
   * @param statusModel
   * @return
   * @throws IOException
   */
  private Path pasteFiles(Map<Sample, Path> sampleFileMap) throws IOException {
    LOG.info("Merging expression data...");

    Path tempFile1 = TmpService.getInstance().newTmpFile("txt");

    String line;

    BufferedReader reader;

    //
    // Create a list of file handlers for each file
    //

    List<Sample> samples = CollectionUtils.sort(sampleFileMap.keySet());

    List<BufferedReader> readers = new ArrayList<BufferedReader>();

    for (Entry<Sample, Path> entry : sampleFileMap.entrySet()) {
      readers.add(FileUtils.newBufferedReader(entry.getValue()));
    }

    //
    // First make a list of the common probes
    //

    LOG.info("Finding common probes...");

    List<String> probes = new ArrayList<String>();
    Map<String, Integer> probeMap = new HashMap<String, Integer>();

    for (BufferedReader r : readers) {
      try {

        // Skip header
        r.readLine();

        while ((line = r.readLine()) != null) {
          if (Io.isEmptyLine(line)) {
            continue;
          }

          List<String> tokens = TextUtils.tabSplit(line);

          String probe = tokens.get(0);

          if (probe.equals("")) {
            continue;
          }

          if (probeMap.containsKey(probe)) {
            probeMap.put(probe, probeMap.get(probe) + 1);
          } else {
            // initially set
            probeMap.put(probe, 1);
            probes.add(probe);
          }
        }
      } finally {
        r.close();
      }
    }

    LOG.info("Sorting common probes...");

    List<String> commonProbesList = new ArrayList<String>();
    Set<String> commonProbesSet = new HashSet<String>();

    for (String probe : probes) {
      if (probeMap.get(probe) < sampleFileMap.size()) {
        continue;
      }

      commonProbesList.add(probe);
      commonProbesSet.add(probe);
    }

    // Allow memory to be freed if necessary
    probes.clear();
    probeMap.clear();

    LOG.info("Building common annotations...");

    // We allow for repeating probe ids where the gene annotation is different
    Map<String, List<String>> annotationMap = ArrayListMultiMap.create();

    // Since all the files have the same annotation, use the first
    reader = FileUtils.newBufferedReader(sampleFileMap.get(samples.get(0)));

    try {
      // Skip header
      reader.readLine();

      while ((line = reader.readLine()) != null) {
        if (Io.isEmptyLine(line)) {
          continue;
        }

        List<String> tokens = TextUtils.tabSplit(line);

        String probe = tokens.get(0);

        if (TextUtils.isNullOrEmpty(probe)) {
          continue;
        }

        if (!commonProbesSet.contains(probe)) {
          continue;
        }

        // The first three columns are annotation (probe, entrez, gene symbol)
        // so
        // we store them with the probe since this doesn't change regardless
        // of the number of samples

        // Since there can be multiple gene annotations for a given probe,
        // we store all of them
        annotationMap.get(probe).add(TextUtils
            .tabJoin(CollectionUtils.head(tokens, ANNOTATION_COLUMNS)));
      }
    } finally {
      reader.close();
    }

    //
    // Do some writing
    //

    LOG.info("Writing common probes...");

    BufferedWriter writer = FileUtils.newBufferedWriter(tempFile1);

    readers = new ArrayList<BufferedReader>();

    for (Sample sample : samples) {
      readers.add(FileUtils.newBufferedReader(sampleFileMap.get(sample)));
    }

    // Create a global header
    List<String> tokens;

    try {
      writer.write("Gene Symbol");
      writer.write(TextUtils.TAB_DELIMITER);
      writer.write("Entrez ID");
      writer.write(TextUtils.TAB_DELIMITER);
      writer.write("Locus");

      for (int i = 0; i < samples.size(); ++i) {
        // Sample sample = samples.get(i);

        reader = readers.get(i);

        line = reader.readLine();

        tokens = TextUtils.tabSplit(line);

        // don't need the first two columns as these are duplicated in
        // every expression file
        tokens = CollectionUtils.subList(tokens, ANNOTATION_COLUMNS);

        //
        // Now we deal with each of the columns in the annotation
        // file, notably to replace the names with the real sample
        // name to account for changes in the file and the database

        for (int c = 0; c < tokens.size(); ++c) {
          String header = tokens.get(c);
          writer.write(TextUtils.TAB_DELIMITER);
          writer.write(header);
        }
      }

      writer.newLine();

      // write the probes

      for (String probe : commonProbesList) {
        // For each different gene annotation of a probe, write out
        // the expression
        for (String annotation : annotationMap.get(probe)) {
          writer.write(annotation);

          tokens = null;

          // For each sample, skip lines until we find the probe of interest
          for (int i = 0; i < samples.size(); ++i) {
            reader = readers.get(i);

            boolean skip = false;

            while (true) {
              line = reader.readLine();

              if (Io.isEmptyLine(line)) {
                skip = true;
                break;
              }

              tokens = TextUtils.tabSplit(line);

              // System.err.println(tokens.get(0) + " " + probe + " " +
              // annotation + " " +
              // samples.get(0).getName() + " " + samples.get(1).getName() + " "
              // +
              // commonProbesList.size() + " " +
              // annotationMap.get(probe).size());

              if (tokens.get(0).equals(probe)) {
                break;
              }
            }

            if (skip) {
              continue;
            }

            // Skip the annotation columns in each file
            tokens = CollectionUtils.subList(tokens, ANNOTATION_COLUMNS);

            // Write out the annotation for this probe for this sample
            for (int c = 0; c < tokens.size(); ++c) {
              writer.write(TextUtils.TAB_DELIMITER);
              writer.write(tokens.get(c));
            }
          }

          // Finally write a new line since we have written the annotations
          // for each sample
          writer.newLine();
        }
      }
    } finally {
      for (BufferedReader r : readers) {
        r.close();
      }

      writer.close();
    }

    LOG.info("Finished pasting {}", tempFile1);

    return tempFile1;
  }

  /**
   * Formats a sample name to match a file name to remove inconsistencies in the
   * way files are named and the way samples are named.
   *
   * @param name
   * @return
   */
  public final String convertSampleNameToFilename(String name) {
    String formattedName = name;

    formattedName = name.replaceAll("\\s", "_");
    formattedName = name.replaceAll("\\(", "_");
    formattedName = name.replaceAll("\\)", "_");
    formattedName = name.replaceAll("_$", "");

    return formattedName;
  }

  /**
   * Download a file associated with a given sample.
   * 
   * @param sample
   * @param type
   * @param checkExists
   * @param statusModel
   * @return
   * @throws NetworkFileException
   * @throws IOException
   * @throws ParseException
   */
  private final Path downloadExpressionData(Sample sample, boolean checkExists)
      throws NetworkFileException, IOException {
    LOG.info("Downloading expression data for sample {} ...", sample.getName());

    FileDownloader downloader = RepositoryService.getInstance()
        .getRepository("rnaseq").getFileDownloader();

    VfsFile arrayFile = getRemoteExpressionFile(sample);

    Path localFile = TmpService.getInstance().newTmpFile();

    downloader.downloadFile(arrayFile, localFile);

    return localFile;
  }

  /**
   * For a given sample, returns the file accessor for its expression file in
   * either MAS5 or RMA form.
   * 
   * @param sample
   * @param normalisationType
   * @return
   * @throws ParseException
   * @throws IOException
   */
  private final VfsFile getRemoteExpressionFile(Sample sample)
      throws IOException {
    Repository repository = RepositoryService.getInstance()
        .getRepository("rnaseq");

    for (VfsFile f : repository.getSampleFiles(sample)) {
      if (f.getName().contains("fpkm.txt")) {
        return f;
      }
    }

    return null;
  }
}
