package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;

import org.jebtk.bioinformatics.gapsearch.BinaryGapSearch;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.core.io.PathUtils;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

import edu.columbia.rdf.matcalc.bio.Annotation;

public class EnhancerDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private ModernCheckBox mCheckAll = new ModernCheckBox("Select All");

  private Map<ModernCheckBox, Integer> mFileMap = new HashMap<ModernCheckBox, Integer>();

  private static final Path ENHANCER_DIR = PathUtils.getPath("res/super_enhancers/human");

  public static final java.nio.file.Path SUPER_ENHANCER_DB_FILE = PathUtils.getPath("res/super_enhancers.db");

  private ModernRadioButton mSearchTypeOverlap = new ModernRadioButton("Overlapping", true);

  private ModernRadioButton mSearchTypeNearest = new ModernRadioButton("Nearest");

  // private ModernSearchPanel mSearchPanel = new ModernSearchPanel();

  private EnhancerTreePanel mEnhancersPanel = new EnhancerTreePanel(); // new
                                                                       // ModernPanel();

  private class CheckAllEvents implements ModernClickListener {
    @Override
    public void clicked(ModernClickEvent e) {
      for (ModernCheckBox checkBox : mFileMap.keySet()) {
        checkBox.setSelected(mCheckAll.isSelected());
      }
    }

  }

  private class SearchEvents implements ModernClickListener {

    @Override
    public void clicked(ModernClickEvent e) {
      search();
    }

  }

  public EnhancerDialog(ModernWindow parent) {
    super(parent);

    setTitle("Super Enhancers");

    createUi();

    setup();
  }

  private void setup() {
    ModernButtonGroup group = new ModernButtonGroup();

    group.add(mSearchTypeOverlap);
    group.add(mSearchTypeNearest);

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    mCheckAll.addClickListener(new CheckAllEvents());

    setSize(600, 500);

    UI.centerWindowToScreen(this);

    search();
  }

  private final void createUi() {
    ModernComponent mPanel = new ModernComponent();

    // mPanel.add(mSearchPanel, BorderLayout.PAGE_START);

    mPanel.setBody(mEnhancersPanel);

    Box box2 = VBox.create();

    box2.add(UI.createVGap(10));
    box2.add(mSearchTypeOverlap);
    // box2.add(UI.createVGap(5));
    box2.add(mSearchTypeNearest);

    mPanel.setFooter(box2);

    setCard(mPanel);
  }

  private void search() {
    // String ls = mSearchPanel.getText().toLowerCase();

    // Box box2 = Box.createVerticalBox();

    // box2.add(mCheckAll);

    /*
     * File[] bedFiles = ENHANCER_DIR.listFiles(new BedGuiFileFilter());
     * 
     * Arrays.sort(bedFiles);
     * 
     * mFileMap.clear();
     * 
     * for (File file : bedFiles) { String id = file.getName().substring(0,
     * file.getName().length() - 4);
     * 
     * if (!id.toLowerCase().contains(ls)) { continue; }
     * 
     * ModernCheckBox checkBox = new ModernCheckBox(id);
     * 
     * box2.add(checkBox);
     * 
     * mFileMap.put(checkBox, file); }
     */

    /*
     * try { SqliteJDBCConnection connection = new
     * SqliteJDBCConnection(SUPER_ENHANCER_DB_FILE);
     * 
     * try { DatabaseResultsTable table = connection.
     * getTable("SELECT tissue.id, tissue.name FROM tissue ORDER BY tissue.name" );
     * 
     * for (int i = 0; i < table.getRowCount(); ++i) { int id =
     * table.getDataAsInt(i, 0); String name = table.getData(i, 1);
     * 
     * if (!name.toLowerCase().contains(ls)) { continue; }
     * 
     * System.err.println(table.getData(i, 1));
     * 
     * ModernCheckBox checkBox = new ModernCheckBox(name);
     * 
     * box2.add(checkBox);
     * 
     * mFileMap.put(checkBox, id); } } finally { connection.close(); } } catch
     * (Exception e) { e.printStackTrace(); }
     * 
     * 
     * ModernScrollPane2 scrollPane = new ModernScrollPane2(box2);
     * scrollPane.setBorder(ModernPanel.TOP_BORDER);
     * 
     * 
     * mEnhancersPanel.removeAll(); mEnhancersPanel.add(scrollPane,
     * BorderLayout.CENTER); mEnhancersPanel.revalidate();
     */
  }

  public Map<String, BinaryGapSearch<Annotation>> getGappedSearch(Genome genome) throws IOException {
    return mEnhancersPanel.getGappedSearch(genome);

    /*
     * List<File> files = new ArrayList<File>();
     * 
     * for (ModernCheckBox checkBox : mFileMap.keySet()) { if
     * (checkBox.isSelected()) { files.add(mFileMap.get(checkBox)); } }
     */

    /*
     * Map<String, GappedSearch<Annotation>> map = new TreeMap<String,
     * GappedSearch<Annotation>>();
     * 
     * try { SqliteJDBCConnection connection = new
     * SqliteJDBCConnection(SUPER_ENHANCER_DB_FILE);
     * 
     * PreparedStatement statement = connection.
     * prepare("SELECT super_enhancers.id, super_enhancers.name, super_enhancers.chr, super_enhancers.start, super_enhancers.end FROM super_enhancers WHERE super_enhancers.tissue_id = ? ORDER BY super_enhancers.chr, super_enhancers.start"
     * );
     * 
     * try { for (ModernCheckBox checkBox : mFileMap.keySet()) { if
     * (!checkBox.isSelected()) { continue; }
     * 
     * GappedSearch<Annotation> search = new GappedSearch<Annotation>();
     * 
     * map.put(checkBox.getText(), search);
     * 
     * int id = mFileMap.get(checkBox);
     * 
     * statement.setInt(1, id);
     * 
     * DatabaseResultsTable table = SqliteJDBCConnection.getTable(statement);
     * 
     * for (int i = 0; i < table.getRowCount(); ++i) { String name =
     * table.getData(i, 1); Chromosome chr =
     * GenomeService.getInstance().parse(table.getData(i, 2)); int start =
     * table.getDataAsInt(i, 3); int end = table.getDataAsInt(i, 4);
     * 
     * BedRegion region = new BedRegion(chr, start, end, name);
     * 
     * Annotation annotation = new Annotation(region.getName(), region);
     * 
     * search.addFeature(region, annotation); } } } finally { connection.close(); }
     * } catch (Exception e) { e.printStackTrace(); }
     * 
     * 
     * return map; //Annotation.parseBedEnhancers(files);
     */
  }

  public boolean getOverlapMode() {
    return mSearchTypeOverlap.isSelected();
  }
}
