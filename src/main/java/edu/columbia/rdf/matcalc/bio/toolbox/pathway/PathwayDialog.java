package edu.columbia.rdf.matcalc.bio.toolbox.pathway;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box;

import org.jebtk.bioinformatics.pathway.GeneSet;
import org.jebtk.bioinformatics.pathway.GeneSetCollection;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class PathwayDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private Map<String, Path> mPathwayPathMap = new TreeMap<String, Path>();

  private Map<String, ModernCheckBox> mPathwayMap = new TreeMap<String, ModernCheckBox>();

  /**
   * The member field fdr.
   */
  private ModernCompactSpinner mFieldFdr = new ModernCompactSpinner(0, 1, 0.05,
      0.01);

  public PathwayDialog(ModernWindow parent) {
    super(parent, "pathway.help.url");

    setTitle("Pathway");

    setup();

    createUi();
  }

  private void setup() {
    try {
      load();
    } catch (IOException e) {
      e.printStackTrace();
    }

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(500, 400);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    ModernComponent content = new ModernComponent();

    Box box = VBox.create();

    sectionHeader("Gene Sets", box);

    content.setHeader(box);

    box = VBox.create();

    for (String name : mPathwayPathMap.keySet()) {
      ModernCheckBox c = new ModernCheckBox(name);

      box.add(c);
      box.add(UI.createVGap(5));

      mPathwayMap.put(name, c);
    }

    box.setBorder(ModernWidget.BORDER);

    ModernScrollPane scrollPane = new ModernScrollPane(box);
    scrollPane.setVerticalScrollBarPolicy(ScrollBarPolicy.ALWAYS);
    // UI.setSize(scrollPane, 400, 200);

    content.setBody(scrollPane);

    box = VBox.create();

    box.add(UI.createVGap(20));

    Box box2 = HBox.create();
    box2.add(new ModernAutoSizeLabel("Maximum FDR", 100));
    box2.add(ModernWidget.createHGap());
    box2.add(mFieldFdr);
    box.add(box2);
    content.setFooter(box);

    setCard(content);
  }

  private void load() throws IOException {
    if (FileUtils.exists(PathwayModule.GENE_SETS_DIR)) {
      for (Path file : FileUtils.ls(PathwayModule.GENE_SETS_DIR)) {
        String name = PathUtils.getName(file);
        
        if (name.contains("gmt")) {
          String gs = GeneSetCollection.getGeneSetName(file);

          mPathwayPathMap.put(gs, file);

          System.err.println("pathway " + gs);
        }
      }
    }
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {
      if (getCollections().size() == 0) {
        ModernMessageDialog.createWarningDialog(mParent,
            "You must select at least one gene collection.");

        return;
      }
    }

    super.clicked(e);
  }

  public Set<GeneSet> getCollections() {
    Set<GeneSet> collections = new TreeSet<GeneSet>();

    for (String name : mPathwayMap.keySet()) {
      if (mPathwayMap.get(name).isSelected()) {
        try {
          GeneSetCollection.parse(mPathwayPathMap.get(name), collections);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return collections;
  }

  public double getFdr() {
    return mFieldFdr.getValue();
  }
}
