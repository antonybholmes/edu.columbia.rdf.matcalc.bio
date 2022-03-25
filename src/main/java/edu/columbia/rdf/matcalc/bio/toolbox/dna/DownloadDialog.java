package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;

import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.IterTreeMap;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class DownloadDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private IterMap<String, CheckBox> mCheckMap = new IterTreeMap<String, CheckBox>();

  public DownloadDialog(ModernWindow parent) {
    super(parent);

    setTitle("Download");

    try {
      createUi();
    } catch (IOException e) {
      e.printStackTrace();
    }

    setup();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(480, 300);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() throws IOException {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    ModernComponent c = new ModernComponent();

    // sectionHeader("Genomes", box);

    // UI.setSize(mDnaPanel, 500, 150);
    // box.add(mDnaPanel);

    c.setHeader(new ModernComponent(new ModernAutoSizeLabel(
        "Select the genomes you would like to download:")).bottomBorder(20));

    Box box = VBox.create();

    for (Entry<String, GenomeDownload> name : GenomeDownloadService.getInstance()) {
      CheckBox checkBox = new ModernCheckSwitch(name.getKey());

      box.add(checkBox);

      mCheckMap.put(name.getKey(), checkBox);
    }

    c.setBody(new ModernScrollPane(box)
        .setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER));

    setCard(c);
  }

  public List<GenomeDownload> getDownloads() {
    List<GenomeDownload> ret = new ArrayList<GenomeDownload>(mCheckMap.size());

    for (Entry<String, CheckBox> item : mCheckMap) {
      if (item.getValue().isSelected()) {
        ret.add(GenomeDownloadService.getInstance().get(item.getKey()));
      }
    }

    return ret;
  }
}
