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
package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.UI;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.ribbon.RibbonSection;
import org.jebtk.modern.window.ModernRibbonWindow;
import org.jebtk.modern.window.ModernWindow;

/**
 * Allows user to select a color map.
 *
 * @author Antony Holmes
 *
 */
public class BaseColorRibbonSection extends RibbonSection {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private BaseButton mColorAButton;

  private BaseButton mColorCButton;

  private BaseButton mColorGButton;

  private BaseButton mColorTButton;

  private BaseButton mColorNButton;

  private RibbonLargeButton mDefaultsButton;

  private ModernWindow mParent;

  /**
   * Instantiates a new genomic region ribbon section.
   *
   * @param model       the model
   * @param genomeModel the genome model
   * @param sizes       the sizes
   * @param genes       the genes
   */
  public BaseColorRibbonSection(ModernRibbonWindow parent) {
    super(parent.getRibbon(), "Base Color");

    mParent = parent;

    mColorAButton = new BaseButton(parent, 'A');

    mColorCButton = new BaseButton(parent, 'C');

    mColorGButton = new BaseButton(parent, 'G');

    mColorTButton = new BaseButton(parent, 'T');

    mColorNButton = new BaseButton(parent, 'N');

    mDefaultsButton = new RibbonLargeButton("Defaults", AssetService.getInstance().loadIcon("reset", 24), "Defaults",
        "Reset colors to their defaults.");

    // Box box = new RibbonStripContainer();

    // box.add(new ModernAutoSizeLabel("A"));
    // box.add(UI.createHGap(5));
    add(mColorAButton);

    // add(UI.createHGap(10));
    // box.add(new ModernAutoSizeLabel("C"));
    // box.add(UI.createHGap(5));
    add(mColorCButton);

    // add(UI.createHGap(10));
    // box.add(new ModernAutoSizeLabel("G"));
    // box.add(UI.createHGap(5));
    add(mColorGButton);

    // add(UI.createHGap(10));
    // box.add(new ModernAutoSizeLabel("T"));
    // box.add(UI.createHGap(5));
    add(mColorTButton);

    // add(UI.createHGap(10));
    // box.add(new ModernAutoSizeLabel("N"));
    // box.add(UI.createHGap(5));
    add(mColorNButton);

    add(UI.createHGap(10));
    add(mDefaultsButton);

    // add(box);

    mDefaultsButton.addClickListener(new ModernClickListener() {
      @Override
      public void clicked(ModernClickEvent e) {
        resetToDefaults();
      }
    });
  }

  private void resetToDefaults() {
    ModernDialogStatus status = ModernMessageDialog.createOkCancelWarningDialog(mParent,
        "The base colors will be reset to their default values.");

    if (status == ModernDialogStatus.OK) {
      SequenceService.getInstance().reset();
      fireClicked();
    }
  }
}
