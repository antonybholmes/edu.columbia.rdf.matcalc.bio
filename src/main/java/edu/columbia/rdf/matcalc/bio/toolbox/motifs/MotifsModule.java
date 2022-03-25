package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;

import org.jebtk.bioinformatics.dna.WebSequenceReader;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.bioinformatics.motifs.MotifsDataSourceService;
import org.jebtk.bioinformatics.motifs.MotifsFs;
import org.jebtk.bioinformatics.motifs.MotifsPwtFs;
import org.jebtk.bioinformatics.motifs.MotifsXmlFs;
import org.jebtk.bioinformatics.ui.external.ucsc.BedGuiFileFilter;
import org.jebtk.bioinformatics.ui.filters.MotifGuiFileFilter;
import org.jebtk.bioinformatics.ui.filters.MotifPwm2GuiFileFilter;
import org.jebtk.bioinformatics.ui.filters.MotifPwmGuiFileFilter;
import org.jebtk.bioinformatics.ui.groups.GroupsModel;
import org.jebtk.bioinformatics.ui.groups.GroupsPanel;
import org.jebtk.core.AppService;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.collections.UniqueArrayList;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.Join;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.GridLocation;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernClickWidget;
import org.jebtk.modern.contentpane.CloseableHTab;
import org.jebtk.modern.dialog.MessageDialogType;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.graphics.icons.SaveVectorIcon;
import org.jebtk.modern.graphics.icons.SearchVectorIcon;
import org.jebtk.modern.help.GuiAppInfo;
import org.jebtk.modern.io.FileDialog;
import org.jebtk.modern.io.GuiFileExtFilter;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.ribbon.RibbonToolbar;
import org.jebtk.modern.tabs.SizableTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.FileType;
import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.figure.graph2d.Graph2dWindow;
import edu.columbia.rdf.matcalc.toolbox.core.io.IOModule;

public class MotifsModule extends IOModule implements ModernClickListener {
  public static final Logger LOG = LoggerFactory.getLogger(MotifsModule.class);

  private static final Path MOD_DIR = AppService.MOD_DIR.resolve("motifs");

  private static final Path DATABASE_DIR = MOD_DIR.resolve("database");

  private static final Set<GuiFileExtFilter> FILE_TYPES_SET = new TreeSet<GuiFileExtFilter>();

  private static final int MAX_MUTATIONS_PLOT = 50;

  private MainMatCalcWindow mWindow;

  private final GroupsModel mRegionGroupsModel = new GroupsModel();
  private GroupsPanel mRegionGroupsPanel;

  // private GenomeAssemblyWeb mAssembly;

  static {
    if (SettingsService.getInstance().getBool("org.matcalc.toolbox.bio.dna.web.enabled")) {
      try {
        SequenceService.getInstance()
            .add(new WebSequenceReader(SettingsService.getInstance().getUrl("dna.remote-url")));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // GenomeAssemblyService.getInstance().add(new GenomeAssemblyFS(RES_DIR));

    FILE_TYPES_SET.add(new MotifGuiFileFilter());
    FILE_TYPES_SET.add(new MotifPwmGuiFileFilter());
    FILE_TYPES_SET.add(new MotifPwm2GuiFileFilter());
  }

  public MotifsModule() throws MalformedURLException, IOException {
    // GenomeAssemblyService.getInstance().add(new GenomeAssemblyWeb(new
    // URL(SettingsService.getInstance().getString("dna.remote-url"))));
    // GenomeAssemblyService.getInstance().add(new
    // GenomeAssemblyFile4Bit(FILE_PATH));

    // mAssembly = new
    // GenomeAssemblyWeb(SettingsService.getInstance().getUrl("dna.remote-url"));

    // MotifsDBService.getInstance().addBackEnd(new MotifsWeb());

    MotifsDataSourceService.getInstance().addDataSource(new MotifsFs(DATABASE_DIR));
    MotifsDataSourceService.getInstance().addDataSource(new MotifsXmlFs(DATABASE_DIR));
    MotifsDataSourceService.getInstance().addDataSource(new MotifsPwtFs(DATABASE_DIR));
  }

  @Override
  public String getName() {
    return "Motifs";
  }

  @Override
  public GuiAppInfo getModuleInfo() {
    return new MotifsInfo();
  }

  @Override
  public Set<GuiFileExtFilter> getOpenFileFilters() {
    return FILE_TYPES_SET;
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    mRegionGroupsPanel = new GroupsPanel(mWindow, mRegionGroupsModel);

    Ribbon ribbon = window.getRibbon();

    ModernClickWidget button;

    /*
     * button = new RibbonLargeButton2("Motif Groups",
     * UIResources.getInstance().loadIcon("sidebar", 24), "Motif Groups",
     * "Show the motifs group"); button.addClickListener(this);
     * ribbon.getToolbar("DNA").getSection("Motifs").add(button);
     */

    RibbonToolbar toolbar = ribbon.getToolbar("Motifs");

    button = new RibbonLargeButton("Search", AssetService.getInstance().loadIcon(SearchVectorIcon.class, 24), "Search",
        "Search for motifs.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);

    toolbar.getSection("Motifs").addSeparator();

    button = new RibbonLargeButton("GC Background", AssetService.getInstance().loadIcon("enrichment", 24),
        "GC Background", "Generate background.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);

    button = new RibbonLargeButton("Enrichment", AssetService.getInstance().loadIcon("enrichment", 24), "Enrichment",
        "Look for enriched motifs.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);

    toolbar.getSection("Motifs").addSeparator();

    button = new RibbonLargeButton("Export BED", AssetService.getInstance().loadIcon(SaveVectorIcon.class, 24),
        "Export BED", "Export Results as BED.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);

    toolbar.getSection("Motifs").addSeparator();

    button = new RibbonLargeButton("SeqLogo", AssetService.getInstance().loadIcon(SeqLogoIcon.class, 24), "SeqLogo",
        "Browse sequence logos.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);

    button = new RibbonLargeButton("Plot", AssetService.getInstance().loadIcon(RunVectorIcon.class, 24), "Plot",
        "Plot motifs.");
    button.addClickListener(this);
    toolbar.getSection("Motifs").add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals("Export BED")) {
      try {
        exportBed();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Motif Groups")) {
      addRegionGroupsPanel();
    } else if (e.getMessage().equals("Search")) {
      try {
        motifSearch();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Enrichment")) {
      try {
        motifEnrichment();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("GC Background")) {
      try {
        gcBackground();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("SeqLogo")) {
      try {
        seqLogo();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Plot")) {
      plot();
    } else {
      // do nothing
    }
  }

  @Override
  public DataFrame read(final MainMatCalcWindow window, final Path file, FileType type, int headers, int rowAnnotations,
      String delimiter, Collection<String> skipLines) throws IOException {
    seqLogo(file);

    return null;
  }

  private void exportBed() throws IOException {
    Path file = FileDialog.save(mWindow).filter(new BedGuiFileFilter())
        .getFile(RecentFilesService.getInstance().getPwd());

    if (file == null) {
      return;
    }

    if (FileUtils.exists(file)) {
      if (ModernMessageDialog.createFileReplaceDialog(mWindow, file) == ModernDialogStatus.CANCEL) {
        return;
      }
    }

    DataFrame m = mWindow.getCurrentMatrix();

    int locCol = DataFrame.findColumn(m, "Motif Location");
    int scoreCol = DataFrame.findColumn(m, "Score");
    int strandCol = DataFrame.findColumn(m, "Strand");
    int nameCol = DataFrame.findColumn(m, "Motif Name");
    int idCol = DataFrame.findColumn(m, "Motif ID");

    try (BufferedWriter writer = FileUtils.newBufferedWriter(file)) {
      writer.write("track name=\"Motifs\" description=\"Motifs\"");
      writer.newLine();

      for (int i = 0; i < m.getRows(); ++i) {
        GenomicRegion r = GenomicRegion.parse(Genome.HG19, m.getText(i, locCol));
        double score = m.getValue(i, scoreCol);
        char strand = m.getText(i, strandCol).charAt(0);

        String name = m.getText(i, nameCol).replaceAll(" +", "_").replaceAll(",", "") + "_" + m.getText(i, idCol);

        writer.write(Join.onTab().values(r.getChr(), r.getStart(), r.getEnd(), name, score, strand).toString());
        writer.newLine();
      }
    }

    ModernMessageDialog.createFileSavedDialog(mWindow, file);
  }

  private void seqLogo() throws IOException {
    // Lets determine if we have a motif table loaded, and if so can
    // we extract some motif names and default to showing those first.

    int c = -1;

    DataFrame m = mWindow.getCurrentMatrix();

    if (m != null) {
      c = DataFrame.findColumn(m, "Motif ID");
    }

    JFrame window;

    if (c != -1) {
      List<String> names = CollectionUtils.uniquePreserveOrder(Arrays.asList(m.columnToText(c)));

      window = new MainSeqLogoWindow(names);

    } else {
      window = new MainSeqLogoWindow();
    }

    UI.centerWindowToScreen(window);

    window.setVisible(true);
  }

  private void seqLogo(Path file) throws IOException {
    JFrame window = new MainSeqLogoWindow(file);

    UI.centerWindowToScreen(window);

    window.setVisible(true);
  }

  private void addRegionGroupsPanel() {
    if (mWindow.tabsPane().tabs().left().contains("Motif Groups")) {
      return;
    }

    SizableTab sizePane = new SizableTab("Motif Groups",
        new CloseableHTab("Motif Groups", mRegionGroupsPanel, mWindow.tabsPane()), 250, 100, 500);

    mWindow.tabsPane().tabs().left().add(sizePane);
  }

  private void motifSearch() throws IOException {
    MotifSearchDialog dialog = new MotifSearchDialog(mWindow, mRegionGroupsModel);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    MotifSearchTask task = new MotifSearchTask(mWindow, Genome.HG19, dialog.getMotifs(), dialog.getThreshold());

    task.doInBackground();
    task.done();
  }

  private void motifEnrichment() throws IOException {
    MotifEnrichmentDialog dialog = new MotifEnrichmentDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ModernDialogStatus status = ModernMessageDialog.createDialog(mWindow,
        "Searching for motifs may take several minutes.", MessageDialogType.INFORMATION_OK_CANCEL);

    if (status == ModernDialogStatus.CANCEL) {
      return;
    }

    List<Motif> searchMotifs = dialog.getMotifs();

    double threshold = dialog.getThreshold();
    double sensitivity = dialog.getMinSensitivity();
    double specificity = dialog.getMinSpecificity();
    MainMatCalcWindow foregroundGroup = mWindow; // dialog.getForegroundGroup();
    MainMatCalcWindow backgroundGroup = dialog.getBackgroundGroup();

    // Load some chromosome sizes for hg19

    if (dialog.useForeVsBackMode()) {
      MotifEnrichmentTask task = new MotifEnrichmentTask(mWindow, Genome.HG19, searchMotifs, foregroundGroup,
          backgroundGroup, threshold, sensitivity, specificity);

      task.doInBackground(); // execute();
    } else {
      MotifEnrichmentGCHistTask task = new MotifEnrichmentGCHistTask(mWindow, Genome.HG19,
          SequenceService.getInstance(), searchMotifs, foregroundGroup, threshold, sensitivity, specificity);

      task.doInBackground();
    }
  }

  private void gcBackground() throws IOException {
    ModernDialogStatus status = ModernMessageDialog.createDialog(mWindow,
        "Generating a GC matched set of sequences may take several minutes.", MessageDialogType.INFORMATION_OK_CANCEL);

    if (status == ModernDialogStatus.CANCEL) {
      return;
    }

    GCBackgroundTask task = new GCBackgroundTask(mWindow, Genome.HG19, SequenceService.getInstance());

    task.doInBackground();
  }

  private void plot() {
    DataFrame m = mWindow.getCurrentMatrix();

    int mutationCol = DataFrame.findColumn(m, "Mutation");

    if (mutationCol == -1) {
      ModernMessageDialog.createWarningDialog(mWindow, "You need column called 'Mutation'.");

      return;
    }

    List<String> mutations = new UniqueArrayList<>();

    for (int i = 0; i < m.getRows(); ++i) {
      mutations.add(m.getText(i, mutationCol));
    }

    int dnaCol = DataFrame.findColumn(m, "DNA Sequence");

    String dna = m.getText(0, dnaCol);

    int l = dna.length();

    PlotDialog dialog = new PlotDialog(mWindow, mutations);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    // We just plot the mutations of interest
    mutations = dialog.getMutations();

    Figure figure = Figure.createRowFigure();

    for (int i = Math.max(0, mutations.size() - MAX_MUTATIONS_PLOT - 1); i < mutations.size(); ++i) {
      String mutation = mutations.get(i);

      SubFigure subFigure = figure.newSubFigure();

      Axes axes = subFigure.currentAxes();
      axes.setInternalSize(2000, 200);
      axes.setMargins(150, 100, 100, 200);

      Plot plot = axes.currentPlot();
      plot.setMatrix(m);

      plot.addChild(new BarBoxLayer());
      plot.addChild(new MotifLayer(mutation));
      plot.addChild(new TextLayer(mutation));

      axes.getX1Axis().setLimitsAuto(0, l);
      axes.getY1Axis().setLimitsAuto(0, 1);
      axes.getY1Axis().setVisible(false);
      axes.getTitle().setText(mutation);
      axes.getTitle().setPosition(GridLocation.E);
    }

    Graph2dWindow window = new Graph2dWindow(mWindow, figure);
    window.setVisible(true);
  }

}
