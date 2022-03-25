/**
 * Copyright 2016 Antony Holmes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.violin;

import java.util.List;

import javax.swing.Box;

import org.jebtk.core.settings.SettingsService;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.HExBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.text.ModernTextBorderPanel;
import org.jebtk.modern.text.ModernTextField;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * The class PatternDiscoveryDialog.
 */
public class ViolinDialog extends ModernDialogHelpWindow
    implements ModernClickListener {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private ModernTwoStateWidget mCheckViolin = new ModernCheckSwitch(
      "Show violin", true);

  /** The m check con min sup only. */
  private ModernTwoStateWidget mCheckBox = new ModernCheckSwitch(
      "Show box and whiskers");
  
  private ModernTwoStateWidget mCheckSwarm = new ModernCheckSwitch(
      "Show swarm");

  private ModernTextField mTextX = new ModernTextField(SettingsService.getInstance().getString("violin.plot.xlabel"));
  private ModernTextField mTextY = new ModernTextField(SettingsService.getInstance().getString("violin.plot.ylabel"));
  
  /**
   * Instantiates a new pattern discovery dialog.
   *
   * @param parent the parent
   * @param labels 
   * @param matrix the matrix
   * @param groups the groups
   */
  public ViolinDialog(ModernWindow parent, List<String> labels) {
    super(parent, "violin.help.url");

    setTitle("Violin Plot");

    setup();
    
    createUi();

  }

  /**
   * Setup.
   */
  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(500, 400);

    UI.centerWindowToScreen(this);
  }

  /**
   * Creates the ui.
   */
  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    Box box = VBox.create();

    // box.add(new ModernDialogSectionSeparator("Filter options"));

    // matrixPanel = new MatrixPanel(rows, cols, ModernWidget.PADDING,
    // ModernWidget.PADDING);
    // matrixPanel.add(new ModernLabel("Signal/noise ratio"));
    // matrixPanel.add(new ModernTextBorderPanel(snrField));
    // matrixPanel.add(new ModernLabel("Fold change tests"));
    // matrixPanel.add(new ModernTextBorderPanel(nField));
    // matrixPanel.add(new ModernLabel("Fold percentile"));
    // matrixPanel.add(new ModernTextBorderPanel(percentileField));
    // matrixPanel.setBorder(ModernPanel.LARGE_BORDER);

    // box.add(matrixPanel);

    // box.add(new ModernDialogSectionSeparator("Group options"));

    sectionHeader("Labels", box);
    
    box.add(new HExBox("X axis", new ModernTextBorderPanel(mTextX, 150)));

    box.add(UI.createVGap(5));
    
    box.add(new HExBox("Y axis", new ModernTextBorderPanel(mTextY, 150)));
    
    midSectionHeader("Options", box);

    box.add(mCheckViolin);

    box.add(UI.createVGap(5));
    
    box.add(mCheckBox);
    
    box.add(UI.createVGap(5));
    
    box.add(mCheckSwarm);

    setCard(box);
  }
  
  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getSource().equals(mOkButton)) {
      SettingsService.getInstance().set("violin.plot.xlabel", mTextX.getText());
      SettingsService.getInstance().set("violin.plot.ylabel", mTextY.getText());
    }
    
    super.clicked(e);
  }

  public boolean getShowViolin() {
    return mCheckViolin.isSelected();
  }

  public boolean getShowBox() {
    return mCheckBox.isSelected();
  }
  
  public boolean getShowSwarm() {
    return mCheckSwarm.isSelected();
  }

  public String getXLabel() {
    return mTextX.getText();
  }
  
  public String getYLabel() {
    return mTextY.getText();
  }
}
