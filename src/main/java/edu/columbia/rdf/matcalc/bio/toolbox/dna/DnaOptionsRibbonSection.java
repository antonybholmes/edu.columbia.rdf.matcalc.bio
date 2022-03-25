package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import javax.swing.Box;

import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.combobox.ModernComboBox2;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.ribbon.RibbonLargeCheckButton;
import org.jebtk.modern.ribbon.RibbonLargeRadioButton;
import org.jebtk.modern.ribbon.RibbonSection;
import org.jebtk.modern.ribbon.RibbonStripContainer;
import org.jebtk.modern.ribbon.RibbonSubSectionSeparator;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;

/**
 * Allows user to select a color map.
 *
 * @author Antony Holmes
 *
 */
public class DnaOptionsRibbonSection extends RibbonSection
    implements ModernClickListener {
  private static final long serialVersionUID = 1L;

  private RibbonLargeCheckButton mCheckRevComp = new RibbonLargeCheckButton(
      "Reverse Complement",
      AssetService.getInstance().loadIcon("reverse_complement", 24),
      "Reverse Complement", "Reverse complement DNA.");

  private ModernCompactSpinner mStartSpinner = new ModernCompactSpinner(0, 1000,
      0);

  private ModernCompactSpinner mEndSpinner = new ModernCompactSpinner(0, 1000,
      0);

  private ModernCheckBox mCheckMutations = new ModernCheckBox("Mutations");

  private RibbonLargeRadioButton mCheckUppercase = new RibbonLargeRadioButton(
      "Uppercase", AssetService.getInstance().loadIcon("uppercase", 24), true,
      "Uppercase", "Display sequence in uppercase letters.");

  private RibbonLargeRadioButton mCheckLowercase = new RibbonLargeRadioButton(
      "Lowercase", AssetService.getInstance().loadIcon("lowercase", 24),
      "Lowercase", "Display sequence in lowercase letters.");

  private ModernComboBox2 mList = new MaskCombo();

  public DnaOptionsRibbonSection(Ribbon ribbon) {
    super(ribbon, "DNA");

    RibbonLargeButton mButton = new RibbonLargeButton("DNA",
        AssetService.getInstance().loadIcon(RunVectorIcon.class, 24), "DNA",
        "Extract the DNA for regions.");
    mButton.addClickListener(this);

    add(mButton);
    add(new RibbonSubSectionSeparator());

    Box box = new RibbonStripContainer();

    box.add(new ModernAutoSizeLabel("5' extension"));
    box.add(createHGap());
    box.add(mStartSpinner);
    box.add(createHGap());
    box.add(new ModernAutoSizeLabel("3' extension"));
    box.add(createHGap());
    box.add(mEndSpinner);
    box.add(createHGap());
    box.add(mCheckMutations);

    add(box);

    add(new RibbonSubSectionSeparator());

    add(mCheckRevComp);

    add(new RibbonSubSectionSeparator());

    add(mCheckUppercase);
    add(mCheckLowercase);

    box = new RibbonStripContainer();
    box.add(mList);
    add(box);

    ModernButtonGroup group = new ModernButtonGroup();

    group.add(mCheckUppercase);
    group.add(mCheckLowercase);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    fireClicked(e);
  }

  public boolean getRevComp() {
    return mCheckRevComp.isSelected();
  }

  public int getOffset5p() {
    return mStartSpinner.getIntValue();
  }

  public int getOffset3p() {
    return mEndSpinner.getIntValue();
  }

  public boolean getDisplayUpper() {
    return mCheckUppercase.isSelected();
  }

  public boolean getIndelMode() {
    return mCheckMutations.isSelected();
  }

  public RepeatMaskType getRepeatMaskType() {
    switch (mList.getSelectedIndex()) {
    case 0:
      return RepeatMaskType.UPPERCASE;
    case 2:
      return RepeatMaskType.N;
    default:
      return RepeatMaskType.LOWERCASE;
    }
  }
}
