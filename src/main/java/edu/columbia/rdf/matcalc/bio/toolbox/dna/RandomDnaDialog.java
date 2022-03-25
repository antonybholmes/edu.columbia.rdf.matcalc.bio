package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.util.List;

import javax.swing.Box;

import org.jebtk.bioinformatics.Bio;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class RandomDnaDialog extends ModernDialogTaskWindow {

  private static final long serialVersionUID = 1L;

  private ModernCompactSpinner mLength = new ModernCompactSpinner(0, 10000, 200,
      false);

  private ModernCompactSpinner mN = new ModernCompactSpinner(0, 10000, 100,
      false);

  private ModernRadioButton mCheckUpper = new ModernRadioButton(
      UI.ASSET_UPPERCASE);

  private ModernRadioButton mCheckLower = new ModernRadioButton(
      UI.ASSET_LOWERCASE);

  private MaskCombo mMaskCombo = new MaskCombo();

  private GenomeSidePanel mDnaPanel;

  public RandomDnaDialog(ModernWindow parent, Genome genome) {
    super(parent);

    setTitle("Random DNA");

    mDnaPanel = new GenomeSidePanel(genome);
    
    createUi();

    setup();
  }

  private void setup() {
    new ModernButtonGroup(mCheckUpper, mCheckLower);

    mCheckUpper.doClick();

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(600, 380);

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

    // sectionHeader("Extension", box);

    Box box2 = HBox.create();
    box2.add(new ModernAutoSizeLabel("Sequence length", 200));
    box2.add(mLength);
    box.add(box2);
    box.add(UI.createVGap(5));
    box2 = HBox.create();
    box2.add(new ModernAutoSizeLabel("Number of sequences", 200));
    box2.add(mN);
    box.add(box2);

    midSectionHeader(UI.ASSET_OUTPUT, box);

    box.add(mCheckUpper);
    // box.add(UI.createVGap(5));
    box.add(mCheckLower);
    box.add(UI.createVGap(5));
    // box2 = HBox.create();

    box.add(new HExpandBox(Bio.ASSET_MASK, mMaskCombo));

    setCard(box);

    getTabsPane().tabs().left().add(Bio.ASSET_GENOMES, mDnaPanel, 150, 100, 300);
  }

  public RepeatMaskType getRepeatMaskType() {
    return mMaskCombo.getRepeatMaskType();
  }

  public boolean getDisplayUpper() {
    return mCheckUpper.isSelected();
  }

  public List<String> getGenomes() {
    return mDnaPanel.getGenomes();
  }

  public SequenceReader getAssembly() {
    return mDnaPanel.getAssembly();
  }

  public int getN() {
    return mN.getIntValue();
  }

  public int getLength() {
    return mLength.getIntValue();
  }
  
  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {
      if (getGenomes().size() == 0) {
        ModernMessageDialog.createWarningDialog(mParent, "You must select at least one genome.");
        
        return;
      }
    }
    
    super.clicked(e);
  }
}
