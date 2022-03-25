package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;

import org.jebtk.bioinformatics.ui.genome.RegionsTextArea;
import org.jebtk.bioinformatics.ui.groups.GroupComboBox;
import org.jebtk.bioinformatics.ui.groups.GroupsModel;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.BorderService;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonWidget;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.color.ColorSwatchButton;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.ModernPanel;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.ribbon.RibbonPanelButton;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.text.ModernAutoSizeLabel;
import org.jebtk.modern.text.ModernClipboardTextField;
import org.jebtk.modern.text.ModernTextBorderPanel;
import org.jebtk.modern.text.ModernTextField;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class BackgroundDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  private static final Dimension NAME_FIELD_SIZE = new Dimension(300, 24);

  ModernButtonWidget mCreateButton = new RibbonPanelButton("Create",
      AssetService.getInstance().loadIcon(RunVectorIcon.class, 32));

  private GroupComboBox mForegroundCombo;

  private final GroupsModel mRegionsModel;

  private ColorSwatchButton mColorButton;

  private final ModernTextField mNameField = new ModernClipboardTextField(UI.ASSET_NAME);

  private final RegionsTextArea mTextArea = new RegionsTextArea();

  private class CreateEvents implements ModernClickListener {

    @Override
    public void clicked(ModernClickEvent e) {
      createBackground();
    }

  }

  public BackgroundDialog(ModernWindow parent, GroupsModel regionGroupsModel) {
    super(parent);

    mRegionsModel = regionGroupsModel;

    setTitle("Motif Search");

    setup();

    createUi();
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    mCreateButton.addClickListener(new CreateEvents());

    setSize(800, 700);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    mColorButton = new ColorSwatchButton(mParent, Color.RED);
    mForegroundCombo = new GroupComboBox(mRegionsModel);

    ModernPanel box = new ModernPanel();

    Box box2 = VBox.create();

    Box box3 = HBox.create();
    box3.add(new ModernAutoSizeLabel(UI.ASSET_GROUP, 100));
    box3.add(mForegroundCombo);
    box2.add(box3);
    box2.add(ModernPanel.createVGap());

    box3 = HBox.create();
    box3.add(new ModernAutoSizeLabel(UI.ASSET_NAME, 100));
    box3.add(new ModernTextBorderPanel(mNameField, NAME_FIELD_SIZE));
    box2.add(box3);
    box2.add(ModernPanel.createVGap());

    box3 = HBox.create();
    box3.add(new ModernAutoSizeLabel(UI.ASSET_COLOR, 100));
    box3.add(mColorButton);
    box2.add(box3);

    box2.setBorder(ModernPanel.RIGHT_BORDER);

    ModernPanel panel = new ModernPanel();

    panel.add(box2, BorderLayout.CENTER);
    panel.add(mCreateButton, BorderLayout.LINE_END);
    panel.setBorder(BorderService.getInstance().createBottomBorder(10));
    box.add(panel, BorderLayout.PAGE_START);

    ModernScrollPane scrollPane = new ModernScrollPane(mTextArea);
    scrollPane.setVerticalScrollBarPolicy(ScrollBarPolicy.ALWAYS);

    box.add(scrollPane, BorderLayout.CENTER);

    setCard(box);
  }

  private void createBackground() {

  }

  public String getName() {
    return this.mNameField.getText();
  }

  public Color getColor() {
    return mColorButton.getSelectedColor();
  }

  public List<String> getRegions() {
    return this.mTextArea.getLines();
  }
}
