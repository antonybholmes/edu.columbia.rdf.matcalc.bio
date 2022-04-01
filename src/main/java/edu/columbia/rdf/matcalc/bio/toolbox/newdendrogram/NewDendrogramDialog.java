package edu.columbia.rdf.matcalc.bio.toolbox.newdendrogram;

import java.text.ParseException;

import javax.swing.Box;

import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.cluster.AverageLinkage;
import org.jebtk.math.cluster.CompleteLinkage;
import org.jebtk.math.cluster.DistanceMetric;
import org.jebtk.math.cluster.EuclideanDistanceMetric;
import org.jebtk.math.cluster.ManhattanDistanceMetric;
import org.jebtk.math.cluster.MaximumDistanceMetric;
import org.jebtk.math.cluster.PearsonDistanceMetric;
import org.jebtk.math.cluster.SingleLinkage;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.combobox.ModernComboBox;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

import edu.columbia.rdf.matcalc.figure.PlotConstants;
import org.jebtk.math.cluster.ILinkage;

public class NewDendrogramDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  private ModernRadioButton mCheckLog2 = new ModernRadioButton("Log 2", true);

  private ModernRadioButton mCheckLog10 = new ModernRadioButton("Log 10");

  private ModernRadioButton mCheckTransNone = new ModernRadioButton("None");

  private CheckBox mCheckMinExp = new ModernCheckSwitch("Minimum expression");

  private ModernComboBox mDistanceCombo = new ModernComboBox();

  private ModernComboBox mLinkageCombo = new ModernComboBox();

  private CheckBox mClusterColumnsCheck = new ModernCheckSwitch(
      "Cluster columns", true);

  private CheckBox mClusterRowsCheck = new ModernCheckSwitch("Cluster rows");

  private CheckBox mCheckOptimalLeafOrder = new ModernCheckSwitch(
      "Optimal leaf ordering", true);

  private ModernCompactSpinner mExpressionField = new ModernCompactSpinner(0,
      10000, 10);

  private ModernCompactSpinner mStdField = new ModernCompactSpinner(0, 10000,
      1.5, 0.1);

  private CheckBox mCheckPlot = new ModernCheckSwitch(PlotConstants.MENU_PLOT,
      true);

  private CheckBox mCheckReset = new ModernCheckSwitch(
      PlotConstants.MENU_RESET_HISTORY, false);

  public NewDendrogramDialog(ModernWindow parent) {
    super(parent, "newdendrogram.help.url");

    setTitle(NewDendrogramModule.NAME);

    setup();

    createUi();
  }

  private void setup() {
    new ModernButtonGroup(mCheckTransNone, mCheckLog2, mCheckLog10);

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    mLinkageCombo.addMenuItem("Average");
    mLinkageCombo.addMenuItem("Complete");
    mLinkageCombo.addMenuItem("Single");
    mLinkageCombo.setSelectedIndex(0);

    mDistanceCombo.addMenuItem("Euclidean");
    mDistanceCombo.addMenuItem("Manhattan");
    mDistanceCombo.addMenuItem("Maximum");
    mDistanceCombo.addMenuItem("Pearson");
    mDistanceCombo.setSelectedIndex(3);

    switch (SettingsService.getInstance().getInt("newdendrogram.transform")) {
    case 1:
      mCheckLog2.doClick();
      break;
    case 2:
      mCheckLog10.doClick();
      break;
    default:
      mCheckTransNone.doClick();
      break;
    }

    mCheckMinExp.setSelected(
        SettingsService.getInstance().getBool("newdendrogram.min-exp-mode"));

    mExpressionField.setValue(SettingsService.getInstance()
        .getDouble("newdendrogram.min-expression"));
    mStdField.setValue(
        SettingsService.getInstance().getDouble("newdendrogram.min-stdev"));

    setSize(640, 640);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {

    Box box = VBox.create();

    sectionHeader("Transform", box);

    box.add(mCheckTransNone);
    // box.add(UI.createVGap(5));
    box.add(mCheckLog2);
    // box.add(UI.createVGap(5));
    box.add(mCheckLog10);

    midSectionHeader("Filtering", box);

    box.add(new HExpandBox(mCheckMinExp, mExpressionField));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("Minimum standard deviation", mStdField));
    box.setBorder(ModernWidget.BORDER);

    midSectionHeader("Clustering", box);

    // UI.setSize(mDistanceCombo, 120);
    // UI.setSize(mLinkageCombo, 120);

    box.add(new HExpandBox("Distance", mDistanceCombo));
    box.add(UI.createVGap(5));
    box.add(new HExpandBox("Linkage", mLinkageCombo));
    box.add(UI.createVGap(5));
    box.add(mClusterRowsCheck);
    box.add(UI.createVGap(5));
    box.add(mClusterColumnsCheck);
    box.add(UI.createVGap(5));
    box.add(mCheckOptimalLeafOrder);

    box.add(UI.createVGap(20));
    box.add(mCheckPlot);

    setCard(box);
  }

  @Override
  public final void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {
      SettingsService.getInstance().update("newdendrogram.min-expression",
          mExpressionField.getValue());

      SettingsService.getInstance().update("newdendrogram.min-stdev",
          mStdField.getValue());

      SettingsService.getInstance().update("newdendrogram.transform",
          getIsLogTransformed());

      SettingsService.getInstance().update("newdendrogram.min-exp-mode",
          mCheckMinExp.isSelected());
    }

    super.clicked(e);
  }

  public DistanceMetric getDistanceMetric() {
    DistanceMetric distanceMetric;

    switch (mDistanceCombo.getSelectedIndex()) {
    case 0:
      distanceMetric = new EuclideanDistanceMetric();
      break;
    case 1:
      distanceMetric = new ManhattanDistanceMetric();
      break;
    case 2:
      distanceMetric = new MaximumDistanceMetric();
      break;
    default:
      distanceMetric = new PearsonDistanceMetric();
      break;
    }

    return distanceMetric;
  }

  public ILinkage getLinkage() {

    ILinkage linkage;

    switch (mLinkageCombo.getSelectedIndex()) {
    case 1:
      linkage = new CompleteLinkage();
      break;
    case 2:
      linkage = new SingleLinkage();
      break;
    default:
      linkage = new AverageLinkage();
      break;
    }

    return linkage;
  }

  public boolean clusterRows() {
    return mClusterRowsCheck.isSelected();
  }

  public boolean clusterColumns() {
    return mClusterColumnsCheck.isSelected();
  }

  public int getIsLogTransformed() {
    if (mCheckLog2.isSelected()) {
      return 1;
    } else if (mCheckLog10.isSelected()) {
      return 2;
    } else {
      return 0;
    }
  }

  public boolean getUseMinExp() {
    return mCheckMinExp.isSelected();
  }

  public double getMinExp() throws ParseException {
    return TextUtils.parseDouble(mExpressionField.getText());
  }

  public double getMinStd() throws ParseException {
    return TextUtils.parseDouble(mStdField.getText());
  }

  public boolean getCreatePlot() {
    return mCheckPlot.isSelected();
  }

  public boolean getReset() {
    return mCheckReset.isSelected();
  }

  public boolean optimalLeafOrder() {
    return mCheckOptimalLeafOrder.isSelected();
  }
}
