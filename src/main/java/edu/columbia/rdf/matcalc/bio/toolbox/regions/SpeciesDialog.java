package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import javax.swing.Box;

import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.text.ModernDialogHeadingLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes
 *
 */
public class SpeciesDialog extends ModernDialogTaskWindow implements ModernClickListener {
  private static final long serialVersionUID = 1L;

  private ModernRadioButton mCheckHuman = new ModernRadioButton("Human");

  private ModernRadioButton mCheckMouse = new ModernRadioButton("Mouse");

  public SpeciesDialog(ModernWindow parent, String name) {
    super(parent);

    setTitle(name);

    createUi();

    setup();
  }

  private void setup() {
    new ModernButtonGroup(mCheckHuman, mCheckMouse);

    mCheckHuman.doClick();

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(360, 220);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    Box box = VBox.create();

    box.add(new ModernDialogHeadingLabel("Species"));
    box.add(UI.createVGap(10));
    box.add(mCheckHuman);
    box.add(UI.createVGap(5));
    box.add(mCheckMouse);

    setCard(box);
  }

  public boolean getHuman() {
    return mCheckHuman.isSelected();
  }
}
