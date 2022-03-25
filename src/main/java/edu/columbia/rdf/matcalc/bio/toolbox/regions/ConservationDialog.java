package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import javax.swing.Box;

import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes
 *
 */
public class ConservationDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private ModernCheckBox mCheckMean = new ModernCheckBox("Mean Conservation", true);
  private ModernCheckBox mCheckMedian = new ModernCheckBox("Median Conservation");
  private ModernCheckBox mCheckScores = new ModernCheckBox("Scores");

  public ConservationDialog(ModernWindow parent) {
    super(parent);

    setTitle("Conservation");

    setup();

    createUi();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(360, 240);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    Box box = Box.createVerticalBox();

    box.add(mCheckMean);

    box.add(UI.createVGap(10));

    box.add(mCheckMedian);

    box.add(UI.createVGap(10));

    box.add(mCheckScores);

    setCard(box);
  }

  public boolean getShowMean() {
    return mCheckMean.isSelected();
  }

  public boolean getShowMedian() {
    return mCheckMedian.isSelected();
  }

  public boolean getShowScores() {
    return mCheckScores.isSelected();
  }

  public Genome getGenome() {
    return Genome.HG19;
  }
}
