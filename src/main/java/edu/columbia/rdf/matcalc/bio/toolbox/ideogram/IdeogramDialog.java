/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.ideogram;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;

import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.color.ColorSwatchButton;
import org.jebtk.modern.panel.HExpandBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * User can select how many annotations there are.
 *
 * @author Antony Holmes
 */
public class IdeogramDialog extends ModernDialogHelpWindow
    implements ModernClickListener {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private Map<String, ModernRadioButton> mGenomeMap = new TreeMap<String, ModernRadioButton>();

  private ColorSwatchButton mGainsButton;

  private ColorSwatchButton mLossesButton;

  /**
   * Instantiates a new row annotation dialog.
   *
   * @param parent the parent
   * @param rowAnnotations the row annotations
   */
  public IdeogramDialog(ModernWindow parent, Color gainColor, Color lossColor) {
    super(parent, "ideogram.help.url");

    setTitle("Ideogram");

    createUi(gainColor, lossColor);

    setup();
  }

  /**
   * Setup.
   */
  private void setup() {
    mGenomeMap.get(mGenomeMap.keySet().iterator().next()).setSelected(true);

    setSize(480, 360);

    addWindowFocusListener(new WindowWidgetFocusEvents(mOkButton));

    UI.centerWindowToScreen(this);
  }

  /**
   * Creates the ui.
   */
  private final void createUi(Color gainColor, Color lossColor) {
    // this.getWindowContentPanel().add(new JLabel("Change " +
    // getProductDetails().getProductName() + " settings", JLabel.LEFT),
    // BorderLayout.PAGE_START);

    Box box = VBox.create();

    sectionHeader("Reference Genome", box);

    ModernButtonGroup group = new ModernButtonGroup();

    // create a list of the genomes

    File[] files = IdeogramModule.RES_DIR.listFiles();

    List<String> genomes = new ArrayList<String>();

    for (File file : files) {
      if (!file.isDirectory()) {
        continue;
      }

      genomes.add(file.getName());
    }

    Collections.sort(genomes);

    for (String genome : genomes) {

      ModernRadioButton button = new ModernRadioButton(genome);

      box.add(button);
      box.add(UI.createVGap(5));

      mGenomeMap.put(genome, button);

      group.add(button);
    }

    midSectionHeader("Colors", box);

    mGainsButton = new ColorSwatchButton(getParentWindow(), gainColor);
    box.add(new HExpandBox("Gains", mGainsButton));

    box.add(UI.createVGap(5));

    mLossesButton = new ColorSwatchButton(getParentWindow(), lossColor);
    box.add(new HExpandBox("Losses", mLossesButton));

    setCard(box);
  }

  public final String getGenome() {
    for (String genome : mGenomeMap.keySet()) {
      if (mGenomeMap.get(genome).isSelected()) {
        return genome;
      }
    }

    return null;
  }

  public Color getGainColor() {
    return mGainsButton.getSelectedColor();
  }

  public Color getLossColor() {
    return mLossesButton.getSelectedColor();
  }
}
