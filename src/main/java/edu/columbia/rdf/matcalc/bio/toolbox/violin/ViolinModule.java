package edu.columbia.rdf.matcalc.bio.toolbox.violin;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.jebtk.core.ColorUtils;
import org.jebtk.core.collections.UniqueArrayList;
import org.jebtk.core.http.Http;
import org.jebtk.core.http.URLUtils;
import org.jebtk.core.http.URLPath;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.io.TmpService;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.sys.ExternalProcess;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.graphplot.figure.series.XYSeriesGroup;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

public class ViolinModule extends Module implements ModernClickListener {

  // private static final int DEFAULT_POINTS =
  // SettingsService.getInstance().getInt("pattern-discovery.cdf.points");

  // private static final List<Double> EVAL_POINTS =
  // Linspace.generate(0, 1, DEFAULT_POINTS);

  private static Path SCRIPT = SettingsService.getInstance()
      .getFile("violin.script.path"); // PathUtils.getPath("res/scripts/python/violin.py");

  private MainMatCalcWindow mWindow;

  private static final Logger LOG = LoggerFactory.getLogger(ViolinModule.class);

  private static final String PYTHON = SettingsService.getInstance()
      .getString("violin.python.interpreter");

  private static final URL PDF_URL = SettingsService.getInstance()
      .getUrl("violin.plot.pdf.url");

  private static boolean WEB_MODE = SettingsService.getInstance()
      .getString("violin.plot.mode").equals("web");

  @Override
  public String getName() {
    return "Violin";
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    RibbonLargeButton button = new RibbonLargeButton("Violin",
        AssetService.getInstance().loadIcon(ViolinIcon.class, 24),
        "Violin Plot", "Create violin plots.");
    button.addClickListener(this);
    mWindow.getRibbon().getToolbar("Plot").getSection("Plot").add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    try {
      violin();
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
  private void violin() throws IOException, InterruptedException {
    XYSeriesGroup groups = mWindow.getRowGroups();

    if (groups.getCount() == 0) {
      MainMatCalcWindow.createRowGroupWarningDialog(mWindow);

      return;
    }

    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      return;
    }

    List<String> labels = new UniqueArrayList<String>();

    for (int i = 0; i < m.getRows(); ++i) {
      labels.add(m.getRowName(i));
    }

    ViolinDialog dialog = new ViolinDialog(mWindow, labels);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    // Write data file in specific form, two columns: Label and Value

    Path dataFile = TmpService.getInstance().newTmpFile("txt");

    BufferedWriter writer = FileUtils.newBufferedWriter(dataFile);

    try {
      writer.write("Label\tValue");
      writer.newLine();

      // Write samples in the order of the groups, thus allowing user to
      // adjust ordering if necessary without recreating file.
      for (XYSeries group : groups) {
        List<Integer> indices = XYSeries.findRowIndices(m, group);

        for (int i : indices) {
          writer.write(m.getRowName(i) + "\t" + m.getText(i, 0));
          writer.newLine();
        }
      }
    } finally {
      writer.close();
    }

    // Write out the group colors in the order given

    Path colorFile = TmpService.getInstance().newTmpFile("txt");

    writer = FileUtils.newBufferedWriter(colorFile);

    try {
      writer.write("Color");
      writer.newLine();
      for (XYSeries group : groups) {
        // Use hex colors e.g. #FF0000
        writer.write(ColorUtils.toHtml(group.getColor()));
        writer.newLine();
      }
    } finally {
      writer.close();
    }

    // Create a random file
    Path out = TmpService.getInstance().newTmpFile("violin", "pdf");

    if (WEB_MODE) {
      webPdf(dataFile,
          colorFile,
          dialog.getShowViolin(),
          dialog.getShowBox(),
          dialog.getShowSwarm(),
          dialog.getXLabel(),
          dialog.getYLabel(),
          out);
    } else {
      localPdf(dataFile,
          colorFile,
          dialog.getShowViolin(),
          dialog.getShowBox(),
          dialog.getShowSwarm(),
          dialog.getXLabel(),
          dialog.getYLabel(),
          out);
    }

    // Use default app to open PDF
    Desktop.getDesktop().open(out.toFile());
  }

  private static void localPdf(Path tableFile,
      Path colorFile,
      boolean violin,
      boolean box,
      boolean swarm,
      String xlabel,
      String ylabel,
      Path out) throws IOException, InterruptedException {
    ExternalProcess p = new ExternalProcess(
        TmpService.getInstance().getTmpDir());
    p.addArg(PYTHON,
        PathUtils.toString(SCRIPT),
        PathUtils.toString(tableFile),
        PathUtils.toString(colorFile),
        PathUtils.toString(out));
    p.addParam("xlabel", xlabel);
    p.addParam("ylabel", ylabel);
    p.addParam("violinplot", violin);
    p.addParam("boxplot", box);
    p.addParam("swarmplot", swarm);

    System.err.println(p.toString());

    p.run();
  }

  private static void webPdf(Path tableFile,
      Path colorFile,
      boolean violin,
      boolean box,
      boolean swarm,
      String xlabel,
      String ylabel,
      Path out) throws IOException {
    URLPath url = URLPath.fromUrl(PDF_URL).param("violin", violin)
        .param("box", box).param("swarm", swarm).param("x", xlabel)
        .param("y", ylabel);

    InputStream inputStream = Http.open(url)
        .post()
        .multipart()
        .addFile("data_file", tableFile)
        .addFile("color_file", colorFile)
        .execute()
        .getInputStream();

    try {
      // Stream the output into the tmp PDF file
      URLUtils.downloadFile(inputStream, out);
    } finally {
      inputStream.close();
    }
  }
}
