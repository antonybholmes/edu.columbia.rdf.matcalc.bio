package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jebtk.bioinformatics.ext.ucsc.BedElement;
import org.jebtk.bioinformatics.gapsearch.BinaryGapSearch;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.GenomicType;
import org.jebtk.core.tree.TreeNode;
import org.jebtk.core.tree.TreeRootNode;
import org.jebtk.database.DatabaseResultsTable;
import org.jebtk.database.SqliteJDBCConnection;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.search.ModernSearchPanel;
import org.jebtk.modern.tree.ModernTree;

import edu.columbia.rdf.matcalc.bio.Annotation;

public class EnhancerTreePanel extends ModernWidget {
  private static final long serialVersionUID = 1L;

  private static final List<Integer> NO_ENHANCERS = Collections.unmodifiableList(new ArrayList<Integer>());

  private ModernTree<Integer> mTree = new ModernTree<Integer>();

  // private ModernButton mSearchButton =
  // new ModernButton(UIResources.getInstance().loadIcon("search", 16));

  private ModernSearchPanel mSearchPanel = new ModernSearchPanel();

  public class RefreshEvents implements ModernClickListener {

    @Override
    public void clicked(ModernClickEvent e) {
      try {
        refresh();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

  }

  public class CollapseEvents implements ModernClickListener {

    @Override
    public void clicked(ModernClickEvent e) {
      setState(false);
    }
  }

  public class ExpandEvents implements ModernClickListener {

    @Override
    public void clicked(ModernClickEvent e) {
      setState(true);
    }
  }

  public EnhancerTreePanel() {
    createUi();

    mSearchPanel.addClickListener(new RefreshEvents());

    try {
      refresh();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void createUi() {
    setHeader(mSearchPanel);

    ModernScrollPane scrollPane = new ModernScrollPane(mTree);
    scrollPane.setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER);

    setBody(new ModernComponent(scrollPane, ModernWidget.BORDER));
  }

  /**
   * Generate a tree view of a sample folder and its sub folders.
   * 
   * @param sampleFolder
   * @param tree
   * @param ascending
   * @throws IOException
   */
  public void refresh() throws IOException {
    String ls = mSearchPanel.getText().toLowerCase();

    TreeRootNode<Integer> root = new TreeRootNode<Integer>();

    try {
      SqliteJDBCConnection connection = new SqliteJDBCConnection(EnhancerDialog.SUPER_ENHANCER_DB_FILE);

      try {
        DatabaseResultsTable table = connection
            .getTable("SELECT sources.id, sources.name FROM sources ORDER BY sources.name");

        PreparedStatement statement = connection.prepare(
            "SELECT DISTINCT tissue.id, tissue.name FROM tissue, super_enhancers WHERE super_enhancers.tissue_id = tissue.id AND super_enhancers.source_id = ? ORDER BY tissue.name");

        for (int i = 0; i < table.getRowCount(); ++i) {
          int sid = table.getInt(i, 0);
          String s = table.getString(i, 1);

          boolean foundInSource = s.toLowerCase().contains(ls);

          TreeNode<Integer> sourceNode = new TreeNode<Integer>(s);

          statement.setInt(1, sid);

          DatabaseResultsTable table2 = SqliteJDBCConnection.getTable(statement);

          for (int j = 0; j < table2.getRowCount(); ++j) {
            int id = table2.getInt(j, 0);
            String name = table2.getString(j, 1);

            if (!foundInSource && !name.toLowerCase().contains(ls)) {
              continue;
            }

            TreeNode<Integer> tissueNode = new TreeNode<Integer>(name, id);

            sourceNode.addChild(tissueNode);
          }

          root.addChild(sourceNode);
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    mTree.setRoot(root);

    // setState();
  }

  // private void setState() {
  // setState(mState);
  // }

  private void setState(boolean state) {
    // mState = state;

    mTree.getRoot().setChildrenAreExpanded(state);
  }

  public void setSelected(int i) {
    mTree.selectNode(i);
  }

  public Map<String, BinaryGapSearch<Annotation>> getGappedSearch(Genome genome) throws IOException {
    /*
     * List<File> files = new ArrayList<File>();
     * 
     * for (ModernCheckBox checkBox : mFileMap.keySet()) { if
     * (checkBox.isSelected()) { files.add(mFileMap.get(checkBox)); } }
     */

    Map<String, BinaryGapSearch<Annotation>> map = new TreeMap<String, BinaryGapSearch<Annotation>>();

    try {
      SqliteJDBCConnection connection = new SqliteJDBCConnection(EnhancerDialog.SUPER_ENHANCER_DB_FILE);

      PreparedStatement statement = connection.prepare(
          "SELECT super_enhancers.id, super_enhancers.name, super_enhancers.chr, super_enhancers.start, super_enhancers.end, super_enhancers.tissue_id FROM super_enhancers WHERE super_enhancers.tissue_id = ? ORDER BY super_enhancers.chr, super_enhancers.start");

      PreparedStatement statement2 = connection
          .prepare("SELECT tissue.id, tissue.name FROM tissue WHERE tissue.id = ?");

      try {
        for (int id : getSelectedEnhancers()) {
          statement.setInt(1, id);

          DatabaseResultsTable table = SqliteJDBCConnection.getTable(statement);

          for (int i = 0; i < table.getRowCount(); ++i) {
            String name = table.getString(i, 1);
            Chromosome chr = ChromosomeService.getInstance().chr(genome, table.getString(i, 2));
            int start = table.getInt(i, 3);
            int end = table.getInt(i, 4);
            int tid = table.getInt(i, 5);

            BedElement region = new BedElement(GenomicType.SUPER_ENHANCER, name, new GenomicRegion(chr, start, end));

            Annotation annotation = new Annotation(region.getName(), region);

            statement2.setInt(1, tid);

            DatabaseResultsTable table2 = SqliteJDBCConnection.getTable(statement2);

            String tissue = table2.getString(0, 1);

            if (!map.containsKey(tissue)) {
              map.put(tissue, new BinaryGapSearch<Annotation>());
            }

            map.get(tissue).add(region, annotation);
          }
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return map; // Annotation.parseBedEnhancers(files);
  }

  public List<Integer> getSelectedEnhancers() {
    if (mTree.getSelectedNodes().size() == 0) {
      return NO_ENHANCERS; // new ArrayList<ExperimentSearchResult>();
    }

    List<Integer> enhancers = new ArrayList<Integer>();

    for (TreeNode<Integer> node : mTree.getSelectedNodes()) {
      selectedEnhancers(node, enhancers);
    }

    return enhancers;
  }

  /**
   * Recursively examine a node and its children to find those with experiments.
   * 
   * @param node
   * @param experiments
   * @throws IOException
   */
  private void selectedEnhancers(TreeNode<Integer> node, List<Integer> motifs) {
    if (node.getValue() != null) {
      motifs.add(node.getValue());
    }

    for (TreeNode<Integer> child : node) {
      selectedEnhancers(child, motifs);
    }
  }
}
