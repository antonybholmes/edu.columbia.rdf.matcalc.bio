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
package edu.columbia.rdf.matcalc.bio.toolbox.deseq2;

import java.util.List;

import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.modern.UI;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

import edu.columbia.rdf.matcalc.GroupPanel;

/**
 * The class PatternDiscoveryDialog.
 */
public class Deseq2Dialog extends ModernDialogHelpWindow
    implements ModernClickListener {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private List<XYSeries> mGroups;

  private GroupPanel mGroupPanel;

  /**
   * Instantiates a new pattern discovery dialog.
   *
   * @param parent the parent
   * @param labels
   * @param matrix the matrix
   * @param groups the groups
   */
  public Deseq2Dialog(ModernWindow parent, List<XYSeries> groups) {
    super(parent, "deseq2.help.url");

    mGroups = groups;

    setTitle("DESeq2");

    setup();

    createUi();

  }

  /**
   * Setup.
   */
  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setSize(480, 320);

    UI.centerWindowToScreen(this);
  }

  /**
   * Creates the ui.
   */
  private final void createUi() {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    mGroupPanel = new GroupPanel(mGroups);

    setCard(mGroupPanel);
  }

  /**
   * Gets the group1.
   *
   * @return the group1
   */
  public XYSeries getGroup1() {
    return mGroupPanel.getGroup1();
  }

  /**
   * Gets the group2.
   *
   * @return the group2
   */
  public XYSeries getGroup2() {
    return mGroupPanel.getGroup2();
  }
}
