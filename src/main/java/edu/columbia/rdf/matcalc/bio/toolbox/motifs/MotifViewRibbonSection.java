package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import org.jebtk.bioinformatics.motifs.MotifView;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeRadioButton;
import org.jebtk.modern.ribbon.RibbonSection;

/**
 * Allows user to select the resolution to view sequences
 *
 * @author Antony Holmes
 *
 */
public class MotifViewRibbonSection extends RibbonSection implements ModernClickListener {
  private static final long serialVersionUID = 1L;

  private RibbonLargeRadioButton mPButton = new RibbonLargeRadioButton("Probability");

  private RibbonLargeRadioButton mBitsButton = new RibbonLargeRadioButton("Bits");

  private ModernTwoStateWidget mButtonRevComp = new ModernCheckSwitch("Reverse Complement");

  private MotifViewModel mModel;

  public MotifViewRibbonSection(Ribbon ribbon, MotifViewModel model) {
    super(ribbon, "Style");

    mModel = model;

    mPButton.setToolTip("Probabilities", "Show sequence logo scores as probabilities.");
    add(mPButton);

    mBitsButton.setToolTip("Bits", "Show sequence logo scores in bits.");
    add(mBitsButton);

    add(UI.createHGap(10));

    add(mButtonRevComp);

    ModernButtonGroup group = new ModernButtonGroup();

    group.add(mPButton);
    group.add(mBitsButton);

    mPButton.addClickListener(this);
    mBitsButton.addClickListener(this);
    mButtonRevComp.addClickListener(this);

    switch (mModel.get()) {
    case BITS:
      mBitsButton.setSelected(true);
      break;
    default:
      mPButton.setSelected(true);
      break;
    }

    mButtonRevComp.setSelected(mModel.getRevComp());
  }

  private void change(ModernClickEvent e) {
    if (mPButton.isSelected()) {
      mModel.update(MotifView.P);
    } else if (mBitsButton.isSelected()) {
      mModel.update(MotifView.BITS);
    } else {

    }

    mModel.setRevComp(mButtonRevComp.isSelected());
  }

  @Override
  public void clicked(ModernClickEvent e) {
    change(e);
  }
}
