package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButton;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.color.ColorSwatchButton;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.ribbon.RibbonButton;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * Options for motif searching.
 * 
 * @author Antony Holmes
 *
 */
public class BaseDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private ColorSwatchButton mColorAButton;

  private ColorSwatchButton mColorCButton;

  private ColorSwatchButton mColorGButton;

  private ColorSwatchButton mColorTButton;

  private ColorSwatchButton mColorNButton;

  public BaseDialog(ModernWindow parent) {
    super(parent, "org.matcalc.toolbox.bio.motifs.seqlogo.dna-bases.help.url");

    setTitle("DNA Bases");

    setup();

    createUi();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(400, 380);

    UI.centerWindowToScreen(this);
  }

  private void createUi() {
    mColorAButton = new ColorSwatchButton(mParent, SequenceService.getInstance().getBaseAColor());

    mColorCButton = new ColorSwatchButton(mParent, SequenceService.getInstance().getBaseCColor());

    mColorGButton = new ColorSwatchButton(mParent, SequenceService.getInstance().getBaseGColor());

    mColorTButton = new ColorSwatchButton(mParent, SequenceService.getInstance().getBaseTColor());

    mColorNButton = new ColorSwatchButton(mParent, SequenceService.getInstance().getBaseNColor());

    VBox box = VBox.create();

    // sectionHeader("Base Color", box);

    box.add(new HExpandBox("A", mColorAButton));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("C", mColorCButton));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("G", mColorGButton));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("T", mColorTButton));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("N", mColorNButton));
    box.add(UI.createVGap(40));

    ModernButton button = new RibbonButton("Defaults");
    button.addClickListener((ModernClickEvent e) -> {
      resetToDefaults();
    });

    box.add(button);

    setCard(box);
  }

  /**
   * Reset the colors to their defaults.
   */
  private void resetToDefaults() {
    ModernDialogStatus status = ModernMessageDialog.createOkCancelWarningDialog(mParent,
        "The base colors will be reset to their default values.");

    if (status == ModernDialogStatus.OK) {
      SequenceService.getInstance().reset();

      setColors();
    }
  }

  /**
   * Change the button color.
   */
  private void setColors() {
    mColorAButton.setSelectedColor(SequenceService.getInstance().getBaseAColor());
    mColorCButton.setSelectedColor(SequenceService.getInstance().getBaseCColor());
    mColorGButton.setSelectedColor(SequenceService.getInstance().getBaseGColor());
    mColorTButton.setSelectedColor(SequenceService.getInstance().getBaseTColor());
    mColorNButton.setSelectedColor(SequenceService.getInstance().getBaseNColor());
  }

  @Override
  public final void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {
      SequenceService.getInstance().setBaseAColor(mColorAButton.getSelectedColor());
      SequenceService.getInstance().setBaseCColor(mColorCButton.getSelectedColor());
      SequenceService.getInstance().setBaseGColor(mColorGButton.getSelectedColor());
      SequenceService.getInstance().setBaseTColor(mColorTButton.getSelectedColor());
      SequenceService.getInstance().setBaseNColor(mColorNButton.getSelectedColor());
    }

    super.clicked(e);
  }
}
