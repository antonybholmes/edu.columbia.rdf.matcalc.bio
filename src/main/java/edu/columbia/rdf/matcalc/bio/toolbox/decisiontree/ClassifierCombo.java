package edu.columbia.rdf.matcalc.bio.toolbox.decisiontree;

import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.combobox.ModernComboBox;

public class ClassifierCombo extends ModernComboBox {
  private static final long serialVersionUID = 1L;

  public ClassifierCombo() {
    for (String c : ClassifierService.getInstance()) {
      addScrollMenuItem(c);
    }

    UI.setSize(this, 200, ModernWidget.WIDGET_HEIGHT);
  }

  public void refresh() {
    clear();

    for (String c : ClassifierService.getInstance()) {
      addScrollMenuItem(c);
    }

    setSelectedIndex(0);
  }
}
