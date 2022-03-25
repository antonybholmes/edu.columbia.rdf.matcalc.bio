package edu.columbia.rdf.matcalc.bio.toolbox.probes;

import java.nio.file.Path;

import javax.swing.Box;

import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class ProbesDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private ArrayCombo mArrayCombo = new ArrayCombo();

  public ProbesDialog(ModernWindow parent) {
    super(parent, "probes.help.url");

    setTitle("Probes");

    setup();

    createUi();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(480, 200);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    Box box = VBox.create();

    box.add(new HExpandBox("Array", mArrayCombo));

    UI.setSize(mArrayCombo, ModernWidget.VERY_LARGE_SIZE);

    setContent(box);
  }

  public Path getFile() {
    return mArrayCombo.getFile();
  }
}
