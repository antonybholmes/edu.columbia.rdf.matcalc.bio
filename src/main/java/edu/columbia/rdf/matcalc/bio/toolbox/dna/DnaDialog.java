package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import javax.swing.Box;

import org.jebtk.bioinformatics.Bio;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class DnaDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private ModernCheckSwitch mCheckRevComp = new ModernCheckSwitch(
      "Reverse complement");

  private ModernCompactSpinner mExt5pSpinner = new ModernCompactSpinner(0,
      10000, 0);

  private ModernCompactSpinner mExt3pSpinner = new ModernCompactSpinner(0,
      10000, 0);

  private ModernRadioButton mCheckUpper = new ModernRadioButton(
      UI.ASSET_UPPERCASE);

  private ModernRadioButton mCheckLower = new ModernRadioButton(
      UI.ASSET_LOWERCASE);

  private ModernCheckSwitch mCheckFromCenter = new ModernCheckSwitch(
      "From center");

  private MaskCombo mMaskCombo = new MaskCombo();

  private GenomeSidePanel mDnaPanel;

  public DnaDialog(ModernWindow parent, Genome genome) {
    super(parent, "dna.help.url");

    setTitle("DNA");

    mDnaPanel = new GenomeSidePanel(genome);
    
    createUi();

    setup();
  }

  private void setup() {
    mExt5pSpinner.setValue(SettingsService.getInstance()
        .getInt("org.matcalc.toolbox.bio.dna.ext-5p"));
    mExt3pSpinner.setValue(SettingsService.getInstance()
        .getInt("org.matcalc.toolbox.bio.dna.ext-3p"));
    mCheckFromCenter.setSelected(SettingsService.getInstance()
        .getBool("org.matcalc.toolbox.bio.dna.from-center"));

    new ModernButtonGroup(mCheckUpper, mCheckLower);

    mCheckUpper.doClick();

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(640, 480);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    Box box = VBox.create();

    // sectionHeader("Genomes", box);

    // UI.setSize(mDnaPanel, 500, 150);
    // box.add(mDnaPanel);

    sectionHeader("Extension", box);

    Box box2 = HBox.create();
    box2.add(new ModernAutoSizeLabel(Bio.ASSET_5P_OFFSET, 100));
    box2.add(mExt5pSpinner);
    box.add(box2);
    box.add(UI.createVGap(5));
    box2 = HBox.create();
    box2.add(new ModernAutoSizeLabel(Bio.ASSET_3P_OFFSET, 100));
    box2.add(mExt3pSpinner);
    box.add(box2);
    box.add(UI.createVGap(10));
    box.add(mCheckFromCenter);

    midSectionHeader("Output", box);

    box.add(mCheckUpper);
    // box.add(UI.createVGap(5));
    box.add(mCheckLower);
    box.add(UI.createVGap(5));
    // box2 = HBox.create();
    box.add(
        new HExpandBox(new ModernAutoSizeLabel(Bio.ASSET_MASK), mMaskCombo));
    box.add(UI.createVGap(5));
    box.add(mCheckRevComp);

    setCard(box);

    getTabsPane().tabs().left().add(Bio.ASSET_GENOMES, mDnaPanel, 150, 100, 300);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {

      // Save some settings
      SettingsService.getInstance().update("org.matcalc.toolbox.bio.dna.ext-5p",
          mExt5pSpinner.getIntValue());

      SettingsService.getInstance().update("org.matcalc.toolbox.bio.dna.ext-3p",
          mExt3pSpinner.getIntValue());

      SettingsService.getInstance().update(
          "org.matcalc.toolbox.bio.dna.from-center",
          mCheckFromCenter.isSelected());
    }

    super.clicked(e);
  }

  public int getOffset5p() {
    return mExt5pSpinner.getIntValue();
  }

  public int getOffset3p() {
    return mExt3pSpinner.getIntValue();
  }

  public boolean getRevComp() {
    return mCheckRevComp.isSelected();
  }

  public boolean getFromCenter() {
    return mCheckFromCenter.isSelected();
  }

  public RepeatMaskType getRepeatMaskType() {
    return mMaskCombo.getRepeatMaskType();
  }

  public boolean getDisplayUpper() {
    return mCheckUpper.isSelected();
  }

  public String getGenome() {
    return mDnaPanel.getGenome();
  }

  public SequenceReader getAssembly() {
    return mDnaPanel.getAssembly();
  }
}
