package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.util.List;

import javax.swing.Box;

import org.jebtk.bioinformatics.motifs.Motif;
import org.jebtk.bioinformatics.ui.groups.GroupsModel;
import org.jebtk.bioinformatics.ui.motifs.MotifModel;
import org.jebtk.bioinformatics.ui.motifs.MotifsTreePanel;
import org.jebtk.bioinformatics.ui.motifs.SeqLogoCanvas;
import org.jebtk.core.event.ChangeEvent;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.FigurePanel;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.Validation;
import org.jebtk.modern.ValidationException;
import org.jebtk.modern.button.ModernButton;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernSelectionListener;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.spinner.ModernCompactSpinner;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;


public class MotifSearchDialog extends ModernDialogHelpWindow {
  private static final long serialVersionUID = 1L;

  // private ModernCheckBox mCheckAll = new ModernCheckBox("Select All");

  // private ModernTextField mExt5pField =
  // new
  // ModernNumericalTextField(SettingsService.getInstance().getInt("motifs.search.5p-extension"));

  // private ModernTextField mExt3pField =
  // new
  // ModernNumericalTextField(SettingsService.getInstance().getInt("motifs.search.3p-extension"));

  private final ModernCompactSpinner mThresholdField = new ModernCompactSpinner(0, 1,
      SettingsService.getInstance().getDouble("motifs.motif-threshold"), 0.1);

  private final ModernButton mSettingsButton = 
          new ModernButton(AssetService.getInstance().loadIcon("settings", 16));

  // private ModernCheckBox mCheckPeakWidth =
  // new ModernCheckBox("Peak widths only");

  // private ModernCheckBox mCheckMainVariants =
  // new ModernCheckBox("Main gene variants");

  private MotifsTreePanel mMotifsPanel;

  private final MotifModel mModel = new MotifModel();

  private SeqLogoCanvas mSubFigure;

  // private GroupComboBox mForegroundCombo;

  // private TabsModel mTabsModel = new TabsModel();

  // private Map<ModernCheckBox, Motif> mMotifMap =
  // new HashMap<ModernCheckBox, Motif>();

  // private List<Motif> mMotifs;

  /*
   * private class CheckAllEvents implements ModernClickListener {
   * 
   * @Override public void clicked(ModernClickEvent e) { for (ModernCheckBox
   * checkBox : mMotifMap.keySet()) {
   * checkBox.setSelected(mCheckAll.isSelected()); } }
   * 
   * }
   */

  private class MotifEvents implements ModernSelectionListener {

    @Override
    public void selectionAdded(ChangeEvent e) {
      mSubFigure.setMotif(mModel.getSelected());
    }

    @Override
    public void selectionRemoved(ChangeEvent e) {
      // TODO Auto-generated method stub

    }

  }

  public MotifSearchDialog(ModernWindow parent, GroupsModel regionGroupsModel) {
    super(parent, "org.matcalc.toolbox.bio.motifs.search.help.url");

    setTitle("Motif Search");

    createUi();

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    // mCheckAll.addClickListener(new CheckAllEvents());
    mModel.addSelectionListener(new MotifEvents());

    setResizable(true);

    setSize(800, 680);

    UI.centerWindowToScreen(this);

    mSettingsButton.addClickListener((ModernClickEvent e) -> {
      alterBases();
    });
  }

  private void alterBases() {
    BaseDialog dialog = new BaseDialog(mParent);

    dialog.setVisible(true);

    mSubFigure.fireChanged();
  }

  private void createUi() {
    // SideTabs sideTabs = new SideTabs(mTabsModel);

    // ModernPanel panel = sideTabs; //new ModernLineBorderPanel(sideTabs);

    // Ui.setSize(sideTabs, new Dimension(200, Short.MAX_VALUE));

    // ModernComponent content = new ModernComponent();

    // content.add(sideTabs, BorderLayout.LINE_START);

    // TabsViewPanel viewPanel = new TabsViewPanel(mTabsModel);
    // viewPanel.setBorder(ModernComponent.LEFT_BORDER);

    // content.setBody(viewPanel);


    mMotifsPanel = new MotifsTreePanel(mParent, mModel);

    UI.setSize(mMotifsPanel, 800, 240);

    addBlock(mMotifsPanel);

    addBlock(UI.createVGap(10));

    mSubFigure = new SeqLogoCanvas();

    ModernScrollPane scrollPane = new ModernScrollPane(
        new FigurePanel(new Figure("Seq Logo Figure").addSubFigure(mSubFigure)));
    scrollPane.setVerticalScrollBarPolicy(ScrollBarPolicy.NEVER);

    UI.setSize(scrollPane, 800, 180);

    ModernComponent panel = new ModernComponent();

    panel.setBody(scrollPane);

    Box box = VBox.create();

    mSettingsButton.setToolTip("DNA Base Settings", "Adjust the way DNA bases are represented.");
    box.add(mSettingsButton);

    panel.setRight(box);

    addCard(panel);

    addBlock(UI.createVGap(20));

    box = HBox.create();

    // mForegroundCombo = new GroupComboBox(mRegionsModel);
    box.add(new ModernAutoSizeLabel("Motif threshold", 100));
    box.add(mThresholdField);

    addBlock(box);

    // panel.add(new ModernDialogHelpButton("Group", "The group of regions in
    // which
    // to search for motifs."));
    // panel.add(new ModernLabel("5' extension"));
    // panel.add(new ModernTextBorderPanel(mExt5pField));
    // panel.add(new ModernDialogHelpButton("5' Extension", "How much to extend
    // the
    // 5' search region relative to the feature orientation."));
    // panel.add(new ModernLabel("3' extension"));
    // panel.add(new ModernTextBorderPanel(mExt3pField));
    // panel.add(new ModernDialogHelpButton("3' Extension", "How much to extend
    // the
    // 3' search region relative to the feature orientation."));
    // panel.add(new ModernLabel("Motif threshold"));
    // panel.add(new ModernTextBorderPanel(mThresholdField));
    // panel.add(new ModernDialogHelpButton("Motif Threshold", "Motifs must
    // score at
    // least p * maxScore()."));
    // box.add(panel);
    // box.add(ModernPanel.createVGap());

    // Box box2 = HBox.create();

    // box2.add(mCheckMainVariants);
    // box2.add(Ui.createHGap(20));
    // box2.add(mCheckPeakWidth);
    // box.add(box2);

    // mTabsModel.addTab("Search Options", box);

    // addCard(c);
  }

  @Override
  public final void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.BUTTON_OK)) {
      try {
        // Validation.validateAsInt("5' extension", mExt5pField.getText());
        // Validation.validateAsInt("3' extension", mExt3pField.getText());
        Validation.validateAsDouble("Motif threshold", mThresholdField.getText(), 0, 1);
        Validation.validateSelection("motifs", getMotifs());

        // SettingsService.getInstance().update("motifs.search.5p-extension",
        // mExt5pField.getText());
        // SettingsService.getInstance().update("motifs.search.3p-extension",
        // mExt5pField.getText());
      } catch (ValidationException ex) {
        ex.printStackTrace();

        Validation.showValidationError(mParent, ex);

        return;
      }

      setStatus(ModernDialogStatus.OK);
    }

    close();
  }

  public List<Motif> getMotifs() {
    return mMotifsPanel.getSelectedMotifs();

    /*
     * List<Motif> ret = new ArrayList<Motif>();
     * 
     * for (ModernCheckBox checkBox : mMotifMap.keySet()) { if
     * (checkBox.isSelected()) { ret.add(mMotifMap.get(checkBox)); } }
     * 
     * return ret;
     */
  }

  // public Group getForegroundGroup() {
  // return mRegionsModel.get(mForegroundCombo.getSelectedIndex());
  // }

  // public int get5pExt() {
  // return Integer.parseInt(mExt5pField.getText());
  // }

  // public int get3pExt() {
  // return Integer.parseInt(mExt3pField.getText());
  // }

  public double getThreshold() {
    return mThresholdField.getValue();
  }

  // public boolean getUseMainVariants() {
  //// return mCheckMainVariants.isSelected();
  // }

  // public boolean getUsePeakWidths() {
  // return mCheckPeakWidth.isSelected();
  // }
}
