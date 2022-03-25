package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import javax.swing.Box;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.modern.UI;
import org.jebtk.modern.Validation;
import org.jebtk.modern.ValidationException;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.ModernPanel;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes
 *
 */
public class ExtendDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private ModernCompactSpinner mExt5pText = new ModernCompactSpinner(0, 100000, 500, 1000, false);

  private ModernCompactSpinner mExt3pText = new ModernCompactSpinner(0, 100000, 500, 1000, false);

  public ExtendDialog(ModernWindow parent) {
    super(parent);

    setTitle("Extend");

    setup();

    createUi();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(360, 200);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    Box box = Box.createVerticalBox();

    box.add(new HExpandBox("5' extension", mExt5pText, ModernPanel.createHGap(), new ModernAutoSizeLabel("bp")));

    box.add(UI.createVGap(10));

    box.add(new HExpandBox("3' extension", mExt3pText, ModernPanel.createHGap(), new ModernAutoSizeLabel("bp")));

    setCard(box);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {

      // validate user input.

      try {
        Validation.validateAsInt("5' extension", mExt5pText.getText());
        Validation.validateAsInt("3' extension", mExt3pText.getText());
      } catch (ValidationException ex) {
        ex.printStackTrace();

        Validation.showValidationError(mParent, ex);

        return;
      }
    }

    super.clicked(e);
  }

  public int getExt5p() {
    return mExt5pText.getIntValue();
  }

  public int getExt3p() {
    return mExt3pText.getIntValue();
  }

  public Genome getGenome() {
    return Genome.HG19;
  }
}
