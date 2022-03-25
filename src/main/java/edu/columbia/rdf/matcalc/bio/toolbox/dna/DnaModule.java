package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.jebtk.bioinformatics.Bio;
import org.jebtk.bioinformatics.dna.DirZipSequenceReader;
import org.jebtk.bioinformatics.dna.WebSequenceReader;
import org.jebtk.bioinformatics.ext.ucsc.Bed;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomeService;
import org.jebtk.bioinformatics.genomic.GenomicElement;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.GenomicType;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.bioinformatics.genomic.Sequence;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.bioinformatics.genomic.SequenceRegion;
import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.core.Range;
import org.jebtk.core.cli.ArgParser;
import org.jebtk.core.cli.Args;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.http.URLUtils;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.UI;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.graphics.icons.DownloadVectorIcon;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.help.GuiAppInfo;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.status.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.OpenMode;
import edu.columbia.rdf.matcalc.bio.FastaReaderModule;
import edu.columbia.rdf.matcalc.bio.FastaWriterModule;
import edu.columbia.rdf.matcalc.toolbox.Module;

public class DnaModule extends Module {
  public static final Logger LOG = LoggerFactory.getLogger(DnaModule.class);

  private MainMatCalcWindow mWindow;

  private static final Args ARGS = new Args();
  

  static {
    // We only want to load the assemblies once so that each invocation
    // of the module does not trigger them to be loaded repeatedly.

    if (SettingsService.getInstance()
        .getBool("org.matcalc.toolbox.bio.dna.web.enabled")) {
      try {
        SequenceService.getInstance().add(new WebSequenceReader(SettingsService.getInstance().getUrl("dna.remote-url")));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    ARGS.add('s', "switch-tab")
    .add('f', "file", true)
    .add('g', "genome", true)
    .add('m', "mode", true)
    .add('n', "n", true)
    .add('l', "length", true)
    .add('d', "genome-dir", true)
    .add('u', "ui")
    .add('z', "zip-dir", true);

    // GenomeAssemblyService.getInstance().add(new
    // GenomeAssemblyFS(Genome.GENOME_HOME));

    // Prefer local over web
    //SequenceReaderService.getInstance().add(new DirZipSequenceReader());
  }
  
  @Override
  public Args getArgs() {
    return ARGS;
  }

  @Override
  public String getName() {
    return Bio.ASSET_DNA;
  }

  @Override
  public GuiAppInfo getModuleInfo() {
    return new DnaInfo();
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    Ribbon ribbon = window.getRibbon();

    RibbonLargeButton button = new RibbonLargeButton(Bio.ASSET_DNA,
        AssetService.getInstance().loadIcon(RunVectorIcon.class, 24),
        Bio.ASSET_DNA, "Extract the DNA for regions.");

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).add(button);
    // ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));
    // ribbon.getToolbar("DNA").getSection("DNA").add(new
    // RibbonStripContainer(mCheckIndels));

    // mDnaSection.addClickListener(this);
    button.addClickListener((ModernClickEvent e) -> {
      try {
        dna(Genome.HG19);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });

    // ribbon.getToolbar("DNA").getSection("DNA").addSeparator();
    // ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));

    button = new RibbonLargeButton(
        AssetService.getInstance().loadIcon("rev_comp", 24),
        "Reverse Complement DNA", "Reverse Complement DNA.");

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).add(button);

    button.addClickListener((ModernClickEvent e) -> {
      revComp(Genome.HG19);
    });

    button = new RibbonLargeButton(
        AssetService.getInstance().loadIcon("random", 24), "Random DNA",
        "Create random DNA sequences.");

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).add(button);

    button.addClickListener((ModernClickEvent e) -> {
      try {
        randomDna();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).addSeparator();

    button = new RibbonLargeButton(AssetService.getInstance().loadIcon("zip", 24),
        "Encode DNA", "Encode DNA.");

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).add(button);

    button.addClickListener((ModernClickEvent e) -> {
      try {
        encode();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });

    button = new RibbonLargeButton(
        AssetService.getInstance().loadIcon(DownloadVectorIcon.class, 24),
        "Download", "Download prebuilt genomes.");

    ribbon.getToolbar(Bio.ASSET_DNA).getSection(Bio.ASSET_DNA).add(button);

    button.addClickListener((ModernClickEvent e) -> {
      try {
        download();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });
  }

  @Override
  public void run(ArgParser ap) {
    Genome genome = Genome.GRCH38;

    Path zipDir = PathUtils.getPwd();
    Path genomeDir = PathUtils.getPwd();
    Path file = null;

    int n = 200;
    int l = 200;

    boolean uiMode = false;

    String mode = "random";

    for (Entry<String, List<String>> cmdArg : ap) {
      switch (cmdArg.getKey()) {
      case "file":
        file = PathUtils.getPath(cmdArg.getValue().get(0));
        break;
      case "mode":
        mode = cmdArg.getValue().get(0);
        break;
      case "n":
        n = Integer.parseInt(cmdArg.getValue().get(0));
        break;
      case "l":
        l = Integer.parseInt(cmdArg.getValue().get(0));
        break;
      case "g":
        genome = GenomeService.getInstance().guessGenome(cmdArg.getValue().get(0));
        break;
      case "d":
        genomeDir = PathUtils.getPath(cmdArg.getValue().get(0));
        break;
      case "u":
        uiMode = true;
      case "z":
        zipDir = PathUtils.getPath(cmdArg.getValue().get(0));
        break;
      }
    }

    LOG.info("dna {}: {} {} {} {}", mode, genome, l, n, zipDir);

    ChromosomeService.getInstance().addDir(genomeDir);

    SequenceReader assembly = null;

    if (zipDir != null) {
      assembly = new DirZipSequenceReader(zipDir);
    }

    if (uiMode) {
      if (mode.startsWith("seq")) {
        try {
          dna(genome);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      // command line

      if (mode.startsWith("seq")) {
        try {
          cmdBed(genome, file, assembly);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (mode.startsWith("rand")) {
        cmdRand(genome, l, n, assembly);
      } else if (mode.startsWith("encode")) {
        try {
          Path outDir = genomeDir;
          encode(genome, genomeDir, outDir);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void download() throws IOException {
    DownloadDialog dialog = new DownloadDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ModernDialogStatus status = ModernMessageDialog.createOkCancelInfoDialog(
        mWindow,
        "Downloading may take a few minutes depending on your connection speed.");

    if (status == ModernDialogStatus.CANCEL) {
      return;
    }

    List<GenomeDownload> downloads = dialog.getDownloads();

    for (GenomeDownload download : downloads) {
      Path dir = Genome.GENOME_DIR.resolve(download.getName());

      FileUtils.mkdir(dir);

      Path file = dir.resolve(download.getName() + ".chrs.gz");
      URLUtils.downloadFile(download.getChrURL(), file);

      file = dir.resolve(download.getName() + ".dna.zip");
      URLUtils.downloadFile(download.getDNAURL(), file);
    }

    String message = TextUtils.format("Finished downloading files to {}",
        TextUtils.truncateCenter(PathUtils.toString(Genome.GENOME_DIR), 70));

    LOG.info(message);

    ModernMessageDialog.createInformationDialog(mWindow,
        "Finished downloading files to",
        TextUtils.truncateCenter(PathUtils.toString(Genome.GENOME_DIR), 70));
  }

  private void encode() throws IOException {
    EncodeDialog dialog = new EncodeDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    ModernDialogStatus status = ModernMessageDialog.createOkCancelInfoDialog(
        mWindow,
        "Encoding may take several minutes.");

    if (status == ModernDialogStatus.CANCEL) {
      return;
    }

    Genome genome = GenomeService.getInstance().guessGenome(dialog.getGenome());

    Path outDir = Genome.GENOME_DIR.resolve(genome.getAssembly());

    Path out = encode(genome, dialog.getDir(), outDir);

    // Once encoded, invalidate the caches so that it can be discovered.
    SequenceService.getInstance().cache();
    ChromosomeService.getInstance().cache();

    ModernMessageDialog.createInformationDialog(mWindow,
        TextUtils.format("Genome {} was saved in", genome),
        TextUtils.truncateCenter(PathUtils.toString(out), 70));
  }

  private static Path encode(Genome genome, Path dir, Path outDir)
      throws IOException {
    return EncodeExt2Bit.encodeGenome(genome, dir, outDir);
  }

  private static void cmdBed(Genome genome, Path file, SequenceReader assembly)
      throws IOException {
    if (file == null) {
      return;
    }

    Bed bed = Bed.parseBedGraph(GenomicType.REGION, file);

    List<GenomicElement> regions = CollectionUtils.sort(bed.getElements());

    cmdOutputSeqs(genome, regions, assembly);
  }

  private static void cmdRand(Genome genome,
      int l,
      int n,
      SequenceReader assembly) {

    List<GenomicRegion> regions = new ArrayList<>(n);

    try {

      for (int i : Range.create(n)) {
        regions.add(GenomicRegion.randomRegion(genome, l));
      }

      // Sort to speed up retrival
      Collections.sort(regions);

      cmdOutputSeqs(genome, regions, assembly);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void cmdOutputSeqs(Genome genome,
      List<? extends GenomicRegion> regions,
      SequenceReader assembly) throws IOException {
    for (GenomicRegion region : regions) {
      SequenceRegion seq = assembly.getSequence(genome, region);

      System.out.println(seq.toFasta());
    }
  }

  /**
   * Create a new matrix and add columns containing the DNA for each row. The
   * first column of the matrix being annotated should contain genomic
   * coordinates.
   * 
   * @throws IOException
   * @throws ParseException
   */
  private void dna(Genome genome) throws IOException {
    /*
     * List<Integer> columns = mWindow.getSelectedColumns();
     * 
     * if (columns.size() == 0) {
     * ModernMessageDialog.createWarningDialog(mWindow,
     * "You must select a location column.");
     * 
     * return; }
     * 
     * int c = columns.get(0);
     */

    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      showLoadMatrixError(mWindow);
      return;
    }

    int locCol = TextUtils.findFirst(m.getColumnNames(), UI.ASSET_LOCATION);

    System.err.println(m.getColumnNames());

    int chrCol = -1;
    int startCol = -1;
    int endCol = -1;

    if (locCol == -1) {
      chrCol = TextUtils.findFirst(m.getColumnNames(), "Chr");
      startCol = TextUtils.findFirst(m.getColumnNames(), "Start", "Position");

      endCol = TextUtils.findFirst(m.getColumnNames(), "End");
    }

    if (locCol == -1 && chrCol == -1) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must create a location column or chr, start, and end columns.");

      return;
    }

    List<GenomicRegion> regions = new ArrayList<>(m.getRows());

    for (int i = 0; i < m.getRows(); ++i) {
      if (locCol != -1) {
        regions.add(GenomicRegion.parse(genome, m.getText(i, locCol)));
      } else {
        if (endCol != -1) {
          regions.add(GenomicRegion.parse(genome,
              m.getText(i, chrCol),
              m.getInt(i, startCol), //Integer.parseInt(m.getText(i, startCol)),
              m.getInt(i, endCol))); //Integer.parseInt(m.getText(i, endCol))));
        } else {
          // Same start and end
          int s = Integer.parseInt(m.getText(i, startCol));
          
          regions.add(GenomicRegion.parse(genome, m.getText(i, chrCol), s, s));

          System.err.println(regions.get(regions.size() - 1).getChr() + " "
              + regions.get(regions.size() - 1).getStart());
        }
      }
    }

    DnaDialog dialog = new DnaDialog(mWindow, genome);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    genome = GenomeService.getInstance().guessGenome(dialog.getGenome());
    SequenceReader assembly = dialog.getAssembly();

    StatusService.getInstance().setStatus("Extending regions...");
    LOG.info("Extending regions...");

    if (dialog.getFromCenter()) {
      // Center sequences

      regions = GenomicRegion.center(regions);
    }

    int offset5p = dialog.getOffset5p(); // mDnaSection.getOffset5p();
    int offset3p = dialog.getOffset3p(); // mDnaSection.getOffset3p();

    // Extend if necessary
    List<GenomicRegion> extendedRegions = GenomicRegion
        .extend(regions, offset5p, offset3p);

    StatusService.getInstance().setStatus("Extracting DNA sequences...");

    LOG.info("Extracting DNA sequences using {}...", assembly.getName());

    RepeatMaskType repeatMaskType = dialog.getRepeatMaskType(); // mDnaSection.getRepeatMaskType();

    boolean uppercase = dialog.getDisplayUpper();

    List<SequenceRegion> sequences = assembly
        .getSequences(genome, extendedRegions, uppercase, repeatMaskType);

    //
    // Cope with insertions and deletions
    //

    List<SequenceRegion> indelSequences = null;

    // There were no indels so the sequences remain unchanged.
    if (indelSequences == null) {
      indelSequences = sequences;
    }

    List<SequenceRegion> revCompSeqs = null;

    if (dialog.getRevComp()) {
      revCompSeqs = SequenceRegion.reverseComplementRegion(indelSequences);
    } else {
      revCompSeqs = indelSequences;
    }

    StatusService.getInstance().setStatus("Creating matrix...");
    LOG.info("Creating matrix...");

    int n = m.getCols();

    DataFrame ret = DataFrame.createDataFrame(m.getRows(), n + 5);

    DataFrame.copyColumns(m, ret, 0);

    ret.setColumnName(n, Bio.ASSET_GENOME);
    ret.setColumnName(n + 1, Bio.ASSET_DNA_LOCATION);
    ret.setColumnName(n + 2, Bio.ASSET_DNA_SEQUENCE);
    ret.setColumnName(n + 3, Bio.ASSET_LEN_BP);
    ret.setColumnName(n + 4, UI.ASSET_OPTIONS);

    List<String> options = new ArrayList<>(4);

    options.add("strand=" + (dialog.getRevComp() ? "-" : "+"));
    options.add("repeat-mask=" + repeatMaskType.toString().toLowerCase());
    options.add("5'-ext=" + offset5p);
    options.add("3'-ext=" + offset3p);

    String opts = Join.onSemiColon().values(options).toString();

    for (int i = 0; i < m.getRows(); ++i) {
      String seq = revCompSeqs.get(i).getSequence().toString();

      ret.set(i, n, genome);
      ret.set(i, n + 1, revCompSeqs.get(i).getLocation());
      ret.set(i, n + 2, seq);
      ret.set(i, n + 3, seq.length());
      ret.set(i, n + 4, opts);
    }

    mWindow.history().addToHistory("Extract DNA", ret);

    StatusService.getInstance().setReady();
  }

  private void randomDna() throws IOException {
    RandomDnaDialog dialog = new RandomDnaDialog(mWindow, Genome.GRCH38);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    List<String> genomes = dialog.getGenomes();
    SequenceReader assembly = dialog.getAssembly();

    RepeatMaskType repeatMaskType = dialog.getRepeatMaskType(); // mDnaSection.getRepeatMaskType();

    boolean uppercase = dialog.getDisplayUpper();

    int n = dialog.getN();
    int length = dialog.getLength();
    int t = n * genomes.size();

    Genome genome = GenomeService.getInstance().guessGenome(genomes.get(0));
    
    List<SequenceRegion> seqs = new ArrayList<SequenceRegion>(t);

    seqs.addAll(randomDna(genome,
          assembly,
          length,
          n,
          uppercase,
          repeatMaskType));

    Collections.sort(seqs);

    DataFrame ret = DataFrame.createDataFrame(t, 5);

    ret.setColumnName(0, Bio.ASSET_GENOME);
    ret.setColumnName(0, Bio.ASSET_GENOMIC_LOCATION);
    ret.setColumnName(1, Bio.ASSET_LEN_BP);
    ret.setColumnName(2, UI.ASSET_OPTIONS);
    ret.setColumnName(3, Bio.ASSET_DNA_SEQUENCE);

    List<String> options = new ArrayList<String>(4);

    options.add("repeat-mask=" + repeatMaskType.toString().toLowerCase());

    String opts = Join.onSemiColon().values(options).toString();

    for (int i = 0; i < seqs.size(); ++i) {
      SequenceRegion seq = seqs.get(i);

      ret.set(i, 0, genome);
      ret.set(i, 1, seq.getLocation());
      ret.set(i, 2, seq.getSequence().getLength());
      ret.set(i, 3, opts);
      ret.set(i, 4, seq.getSequence().toString());
    }

    ret.setName("Random DNA");

    mWindow.openMatrices().openMode(OpenMode.NEW_WINDOW).open(ret);
  }

  private static List<SequenceRegion> randomDna(Genome genome,
      SequenceReader assembly,
      int length,
      int n) throws IOException {
    return randomDna(genome,
        assembly,
        length,
        n,
        true,
        RepeatMaskType.LOWERCASE);
  }

  private static List<SequenceRegion> randomDna(Genome genome,
      SequenceReader assembly,
      int length,
      int n,
      boolean displayUpper,
      RepeatMaskType repeatMaskType) throws IOException {
    // genome

    List<GenomicRegion> regions = new ArrayList<>(n);

    for (int i : Range.create(n)) {
      regions.add(GenomicRegion.randomRegion(genome, length));
    }

    // Sort to speed up retrival
    Collections.sort(regions);

    List<SequenceRegion> ret = new ArrayList<>(n);

    for (GenomicRegion region : regions) {
      ret.add(
          assembly.getSequence(genome, region, displayUpper, repeatMaskType));
    }

    return ret;
  }

  private static SequenceRegion randomDna(Genome genome,
      SequenceReader assembly,
      int length) throws IOException {
    return randomDna(genome, assembly, length, true, RepeatMaskType.LOWERCASE);
  }

  private static SequenceRegion randomDna(Genome genome,
      SequenceReader assembly,
      int length,
      boolean displayUpper,
      RepeatMaskType repeatMaskType) throws IOException {
    // genome

    GenomicRegion region = GenomicRegion.randomRegion(genome, length);

    return assembly.getSequence(genome, region, displayUpper, repeatMaskType);
  }

  private void revComp(Genome genome) {
    DataFrame m = mWindow.getCurrentMatrix();

    List<Sequence> sequences = FastaWriterModule.toSequences(mWindow, m);

    List<Sequence> revComps = Sequence.reverseComplement(sequences);

    List<GenomicRegion> regions = toRegions(mWindow, genome, m);
    
    System.err.println("rev " + regions);

    DataFrame ret = FastaReaderModule.toMatrix(genome, regions, revComps);

    ret.setName(Bio.ASSET_REV_COMP);

    mWindow.openMatrices().openMode(OpenMode.NEW_WINDOW).open(ret);
  }

  public static List<GenomicRegion> toRegions(final MainMatCalcWindow window,
      Genome genome,
      final DataFrame m) {

    int c1 = DataFrame.findColumn(m, Bio.ASSET_DNA_LOCATION);

    if (c1 == -1) {
      c1 = DataFrame.findColumn(m, UI.ASSET_LOCATION);
    }

    List<GenomicRegion> sequences = new ArrayList<>(m.getRows());

    for (int i = 0; i < m.getRows(); ++i) {
      sequences.add(GenomicRegion.parse(genome, m.getText(i, c1)));
    }

    return sequences;
  }
}
