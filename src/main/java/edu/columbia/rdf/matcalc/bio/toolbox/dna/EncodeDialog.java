package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.nio.file.Path;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.io.ChooseFilePanel;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBoxAutoWidth;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.text.ModernTextBorderPanel;
import org.jebtk.modern.text.ModernTextField;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class EncodeDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private ChooseFilePanel mChooseFilePanel;

  private ModernTextField mNameField = new ModernTextField(Genome.GRCH38.getAssembly());

  public EncodeDialog(ModernWindow parent) {
    super(parent, "dna.encode.help.url");

    setTitle("Encode DNA");

    setup();

    createUi();

  }

  private void setup() {
    mChooseFilePanel = new ChooseFilePanel(getParentWindow(), true);
    mChooseFilePanel.setFile(RecentFilesService.getInstance().getPwd());

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(600, 300);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    ModernComponent box = VBoxAutoWidth.create();

    UI.setSize(mNameField, 200);

    box.add(new HExpandBox(UI.ASSET_NAME, new ModernTextBorderPanel(mNameField)));
    box.add(UI.createVGap(40));
    box.add(new ModernAutoSizeLabel(
        "Choose the directory containing FASTA files (*.fa.gz)"));
    box.add(ModernWidget.createVGap());
    box.add(mChooseFilePanel);

    setCard(box);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {

    }

    super.clicked(e);
  }

  public String getGenome() {
    return mNameField.getText(); // Genome.GRCH38;
  }

  public Path getDir() {
    return mChooseFilePanel.getFile();
  }
}
