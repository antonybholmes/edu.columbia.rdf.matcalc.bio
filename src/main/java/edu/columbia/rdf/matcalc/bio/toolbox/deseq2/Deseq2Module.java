package edu.columbia.rdf.matcalc.bio.toolbox.deseq2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import org.jebtk.core.collections.IterMap;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.io.TmpService;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.MixedMatrixParser;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.MessageDialogType;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

public class Deseq2Module extends Module implements ModernClickListener {

  // private static final int DEFAULT_POINTS =
  // SettingsService.getInstance().getInt("pattern-discovery.cdf.points");

  // private static final List<Double> EVAL_POINTS =
  // Linspace.generate(0, 1, DEFAULT_POINTS);

  private static String R = SettingsService.getInstance()
      .getString("deseq2.r.interpreter");

  private static Path SCRIPT = SettingsService.getInstance()
      .getFile("deseq2.r.script.path"); // PathUtils.getPath("res/scripts/python/violin.py");

  private MainMatCalcWindow mWindow;

  private static final Logger LOG = LoggerFactory.getLogger(Deseq2Module.class);

  @Override
  public String getName() {
    return "DESeq2";
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    RibbonLargeButton button = new RibbonLargeButton("DESeq2",
        AssetService.getInstance().loadIcon(Deseq2Icon.class, 24), "DESeq2",
        "Run DESeq2.");
    button.addClickListener(this);
    mWindow.getRibbon().getToolbar("Classification").getSection("Classifier")
    .add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    try {
      deseq2();
    } catch (IOException | InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Pattern discovery.
   *
   * @param properties the properties
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException
   */
  private void deseq2() throws IOException, InterruptedException {
    List<XYSeries> groups = mWindow.getGroups();

    if (groups.size() == 0) {
      MainMatCalcWindow.createGroupWarningDialog(mWindow);

      return;
    }

    Deseq2Dialog dialog = new Deseq2Dialog(mWindow, groups);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    XYSeries g1 = dialog.getGroup1();
    XYSeries g2 = dialog.getGroup2();

    if (g1 == null || g2 == null) {
      return;
    }

    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      return;
    }

    ModernMessageDialog.createDialog(mWindow,
        "DESeq2 may take several minutes to run.",
        MessageDialogType.INFORMATION);

    BufferedWriter writer;

    Path tmp = TmpService.getInstance().getTmpDir();

    // write groups

    Path groupsFile = tmp.resolve("groups.txt");

    writer = FileUtils.newBufferedWriter(groupsFile);

    try {
      writer.write("group1\tgroup2");
      writer.newLine();

      writer.write(g1.getName());
      writer.write(TextUtils.TAB_DELIMITER);
      writer.write(g2.getName());
      writer.newLine();
    } finally {
      writer.close();
    }

    /*
     * Path groupsFile =
     * TmpService.getInstance().getTmpDir().resolve("groups.txt");
     * 
     * writer = FileUtils.newBufferedWriter(groupsFile);
     * 
     * try { writer.write("group1\tgroup2"); writer.newLine();
     * 
     * for (int i = 0; i < groups.size(); ++i) { for (int j = i + 1; j <
     * groups.size(); ++j) { writer.write(groups.get(i).getName());
     * writer.write(TextUtils.TAB_DELIMITER);
     * writer.write(groups.get(j).getName()); writer.newLine(); } } } finally {
     * writer.close(); }
     */

    // Write phenotypes. Need to maintain the same column order as in the
    // matrix, otherwise DESeq2 will complain.

    Path phenFile = tmp.resolve("phenotypes.txt");

    writer = FileUtils.newBufferedWriter(phenFile);

    try {
      writer.write("sample\tphenotype");
      writer.newLine();
      
      System.err.println(groups);

      IterMap<Integer, List<XYSeries>> indices = XYSeries.indexGroupMap(m, groups);
      
      for (int i = 0; i < m.getCols(); ++i) {
        writer.write(m.getColumnName(i));
        writer.write(TextUtils.TAB_DELIMITER);
        
        if (indices.containsKey(i)) {
          List<XYSeries> item = indices.get(i);
          
          System.err.println(i + " " + m.getColumnName(i) + " " + item);
          
          writer.write(item.get(0).getName());
        } else {
          // Not assigned to a group, so use 'n/a'
          writer.write(TextUtils.NA);
        }
        
        writer.newLine();
      }
    } finally {
      writer.close();
    }

    // Save the matrix to file
    Path countsFile = tmp.resolve("counts.txt");
    mWindow.write(countsFile);
    
    // Call external R process

    String[] args = { R, PathUtils.toString(SCRIPT) };

    ProcessBuilder builder = new ProcessBuilder(args);

    builder.redirectErrorStream(true);

    Process process = null;

    try {
      process = builder.start();

      // Runtime runtime = Runtime.getRuntime();
      // Process process = runtime.exec(args);

      BufferedReader br = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
      String line;

      while ((line = br.readLine()) != null) {
        LOG.info(line);
      }

      process.waitFor();
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    Path normFile = tmp.resolve("counts_deseq2_normalized.txt");
    m = new MixedMatrixParser(1, 0, TextUtils.TAB_DELIMITER).parse(normFile);
    mWindow.history().addToHistory("DESeq2 Normalized", m);

    Path testFile = tmp.resolve(
        TextUtils.cat("deseq2_", g1.getName(), "_vs_", g2.getName(), ".txt"));
    m = new MixedMatrixParser(1, 0, TextUtils.TAB_DELIMITER).parse(testFile);
    mWindow.history().addToHistory(
        TextUtils.cat("DESeq2 ", g1.getName(), " vs ", g2.getName()),
        m);
  }
}
