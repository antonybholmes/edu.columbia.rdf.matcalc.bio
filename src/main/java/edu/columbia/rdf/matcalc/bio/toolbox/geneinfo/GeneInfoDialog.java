package edu.columbia.rdf.matcalc.bio.toolbox.geneinfo;

import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.combobox.ModernComboBox;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.panel.MatrixPanel;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;

import edu.columbia.rdf.matcalc.toolbox.ColumnsCombo;

public class GeneInfoDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private DataFrame mMatrix;

  private ModernComboBox mColumnsCombo;

  public GeneInfoDialog(ModernWindow parent, DataFrame matrix) {
    super(parent);

    setTitle("Gene Info");

    mMatrix = matrix;

    setup();

    createUi();
  }

  private void setup() {
    setSize(600, 200);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    int[] rows = { ModernWidget.WIDGET_HEIGHT };
    int[] cols = { 100, 400 };

    MatrixPanel panel = new MatrixPanel(rows, cols, ModernWidget.PADDING,
        ModernWidget.PADDING);

    panel.add(new ModernAutoSizeLabel("Column"));

    mColumnsCombo = new ColumnsCombo(mMatrix);
    panel.add(mColumnsCombo);

    setCard(panel);

  }

  public int getColumnIndex() {
    return mColumnsCombo.getSelectedIndex();
  }
}
