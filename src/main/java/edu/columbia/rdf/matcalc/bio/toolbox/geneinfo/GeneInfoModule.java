package edu.columbia.rdf.matcalc.bio.toolbox.geneinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jebtk.bioinformatics.genomic.Strand;
import org.jebtk.core.Resources;
import org.jebtk.core.collections.ArrayListCreator;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.TreeSetCreator;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.Parser;
import org.jebtk.core.text.Splitter;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

public class GeneInfoModule extends Module implements ModernClickListener {

  public static final Path RES_DIR = PathUtils.getPath("res/modules/geneinfo/");

  private static final Path HUMAN_GENE_FILE = RES_DIR
      .resolve("ucsc_refseq_hg19.txt.gz");

  private static final Path MOUSE_GENE_FILE = RES_DIR
      .resolve("ucsc_refseq_mm10.txt.gz");

  private MainMatCalcWindow mWindow;

  @Override
  public String getName() {
    return "Gene Info";
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    RibbonLargeButton button = new RibbonLargeButton("Gene Info",
        AssetService.getInstance().loadIcon("gene_info", 24));

    button.addClickListener(this);
    mWindow.getRibbon().getToolbar("Genomic").getSection("Annotation")
        .add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    try {
      addInfo();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ParseException e1) {
      e1.printStackTrace();
    }
  }

  private void addInfo() throws IOException, ParseException {
    DataFrame m = mWindow.getCurrentMatrix();

    GeneInfoDialog dialog = new GeneInfoDialog(mWindow, m);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    int column = dialog.getColumnIndex();

    Map<String, Set<String>> chrMap = DefaultHashMap
        .create(new TreeSetCreator<String>());

    Map<String, Set<Strand>> strandMap = DefaultHashMap
        .create(new TreeSetCreator<Strand>());

    Map<String, List<Integer>> startMap = DefaultHashMap
        .create(new ArrayListCreator<Integer>());

    Map<String, List<Integer>> endMap = DefaultHashMap
        .create(new ArrayListCreator<Integer>());

    // Load Both files
    loadFile(HUMAN_GENE_FILE, chrMap, strandMap, startMap, endMap);

    loadFile(MOUSE_GENE_FILE, chrMap, strandMap, startMap, endMap);

    DataFrame ml = DataFrame.createDataFrame(m.getRows(), m.getCols() + 4);

    DataFrame.copy(m, ml);

    ml.setColumnName(m.getCols(), "Chr");
    ml.setColumnName(m.getCols() + 1, "Strand");
    ml.setColumnName(m.getCols() + 2, "Start");
    ml.setColumnName(m.getCols() + 3, "End");

    List<String> names = m.getIndex().getNames();

    for (int i = 0; i < m.getRows(); ++i) {
      String id;

      if (column < names.size()) {
        id = m.getIndex().getText(names.get(column), i);
      } else {
        id = m.getText(i, column - names.size());
      }

      if (chrMap.containsKey(id)) {
        ml.set(i, m.getCols(), Join.onSemiColon().values(chrMap.get(id)));
        ml.set(i,
            m.getCols() + 1,
            Join.onSemiColon().values(Strand.toChar(strandMap.get(id))));
        ml.set(i, m.getCols() + 2, Join.onSemiColon().values(startMap.get(id)));
        ml.set(i, m.getCols() + 3, Join.onSemiColon().values(endMap.get(id)));
      } else {
        ml.set(i, m.getCols(), TextUtils.NA);
        ml.set(i, m.getCols() + 1, TextUtils.NA);
        ml.set(i, m.getCols() + 2, TextUtils.NA);
        ml.set(i, m.getCols() + 3, TextUtils.NA);
      }
    }

    mWindow.history().addToHistory("Gene Info", ml);
  }

  private static void loadFile(Path file,
      Map<String, Set<String>> chrMap,
      Map<String, Set<Strand>> strandMap,
      Map<String, List<Integer>> startMap,
      Map<String, List<Integer>> endMap) throws IOException, ParseException {
    String line;
    List<String> tokens;

    BufferedReader reader = Resources.getGzipReader(file);

    try {
      reader.readLine();

      while ((line = reader.readLine()) != null) {
        tokens = Splitter.onTab().text(line);

        String chr = tokens.get(3);
        Strand strand = Strand.parse(tokens.get(4));
        int start = Parser.toInt(tokens.get(5));
        int end = Parser.toInt(tokens.get(6));

        // refseq
        addEntry(tokens.get(
            0), chr, strand, start, end, chrMap, strandMap, startMap, endMap);

        // entrez
        addEntry(tokens.get(
            1), chr, strand, start, end, chrMap, strandMap, startMap, endMap);

        // symbol
        addEntry(tokens.get(
            2), chr, strand, start, end, chrMap, strandMap, startMap, endMap);
      }
    } finally {
      reader.close();
    }
  }

  private static void addEntry(String name,
      String chr,
      Strand strand,
      int start,
      int end,
      Map<String, Set<String>> chrMap,
      Map<String, Set<Strand>> strandMap,
      Map<String, List<Integer>> startMap,
      Map<String, List<Integer>> endMap) {
    if (name == null || name.equals(TextUtils.NA) || name.equals("---")) {
      return;
    }

    chrMap.get(name).add(chr);
    strandMap.get(name).add(strand);
    startMap.get(name).add(start);
    endMap.get(name).add(end);
  }
}
