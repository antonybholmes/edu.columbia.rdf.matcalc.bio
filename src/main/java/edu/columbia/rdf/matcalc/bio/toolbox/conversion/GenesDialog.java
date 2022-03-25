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
package edu.columbia.rdf.matcalc.bio.toolbox.conversion;

import javax.swing.Box;

import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.HBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

// TODO: Auto-generated Javadoc
/**
 * The class PatternDiscoveryDialog.
 */
public class GenesDialog extends ModernDialogHelpWindow
    implements ModernClickListener {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The check reset.
   */
  private CheckBox mCheckSplit = new ModernCheckSwitch("Split hyphens");

  /** The m radio conv from human. */
  private ModernTwoStateWidget mRadioConvFromHuman = new ModernRadioButton(
      "Human");

  /** The m radio conv from mouse. */
  private ModernTwoStateWidget mRadioConvFromMouse = new ModernRadioButton(
      "Mouse");

  /** The m radio conv to human. */
  private ModernTwoStateWidget mRadioConvToHuman = new ModernRadioButton("Human");

  /** The m radio conv to mouse. */
  private ModernTwoStateWidget mRadioConvToMouse = new ModernRadioButton("Mouse");

  /** The m check symbol. */
  private CheckBox mCheckSymbol = new ModernCheckSwitch("Symbol", true);

  /** The m check entrez. */
  private CheckBox mCheckEntrez = new ModernCheckSwitch("Entrez ID");

  /** The m check ref seq. */
  private CheckBox mCheckRefSeq = new ModernCheckSwitch("RefSeq ID");

  /** The m check ensembl transcript. */
  private CheckBox mCheckEnsemblTranscript = new ModernCheckSwitch(
      "Ensembl transcript");

  /** The m check ensembl gene. */
  private CheckBox mCheckEnsemblGene = new ModernCheckSwitch("Ensembl gene");

  /** The m check chr. */
  private CheckBox mCheckChr = new ModernCheckSwitch("Chromosome");

  /** The m check strand. */
  private CheckBox mCheckStrand = new ModernCheckSwitch("Strand");

  /** The m check conversions. */
  private CheckBox mCheckConversions = new ModernCheckSwitch("Conversions",
      true);
  
  private CheckBox mCheckParams = new ModernCheckSwitch("Parameters",
      true);
  
  private CheckBox mCheckKeepOld = new ModernCheckSwitch("Keep old ID", true);
  
  private CheckBox mCheckShowAlt = new ModernCheckSwitch("Alt names");
  
  private DatabaseSidePanel mDbPanel = new DatabaseSidePanel();

  /**
   * Instantiates a new pattern discovery dialog.
   *
   * @param parent the parent
   */
  public GenesDialog(ModernWindow parent) {
    super(parent, "genes.help.url");

    setTitle("Genes");

    setup();

    createUi();
  }

  /**
   * Setup.
   */
  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    new ModernButtonGroup(mRadioConvFromHuman, mRadioConvFromMouse);
    new ModernButtonGroup(mRadioConvToHuman, mRadioConvToMouse);

    mRadioConvFromHuman.doClick();
    mRadioConvToHuman.doClick();

    setSize(600, 500);

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

    sectionHeader("Input", box);

    Box box2 = HBox.create();
    box2.add(mRadioConvFromHuman);
    box2.add(mRadioConvFromMouse);
    box.add(box2);
    box.add(UI.createVGap(5));
    box.add(mCheckSplit);

    midSectionHeader("Output", box);

    box2 = HBox.create();
    box2.add(mRadioConvToHuman);
    box2.add(mRadioConvToMouse);
    box.add(box2);
    box.add(UI.createVGap(20));
    
    box2 = HBox.create();
    box2.setAlignmentY(TOP_ALIGNMENT);
    VBox box3 = VBox.create();
    box3.setAlignmentY(TOP_ALIGNMENT);
    VBox box4 = VBox.create();
    box4.setAlignmentY(TOP_ALIGNMENT);
    box2.add(box3);
    box2.add(UI.createHGap(20));
    box2.add(box4);
    box.add(box2);
    
    
    box3.add(mCheckSymbol);
    // box2.add(UI.createVGap(5));
    box3.add(mCheckEntrez);
    // box2.add(UI.createVGap(5));
    box3.add(mCheckRefSeq);
    // box2.add(UI.createVGap(5));
    box3.add(mCheckEnsemblTranscript);
    // box2.add(UI.createVGap(5));
    box3.add(mCheckEnsemblGene);
    // box2.add(UI.createVGap(5));
    box3.add(mCheckChr);
    // box2.add(UI.createVGap(5));
    box4.add(mCheckStrand);
    // box2.add(UI.createVGap(5));
    box4.add(mCheckKeepOld);
    box4.add(mCheckShowAlt);
    box4.add(mCheckConversions);
    box4.add(mCheckParams);
    
    setCard(box);
    
    getTabsPane().tabs().left().add("Genomes", mDbPanel, 120, 100, 400);
  }

  /**
   * Gets the output symbols.
   *
   * @return the output symbols
   */
  public boolean getOutputSymbols() {
    return mCheckSymbol.isSelected();
  }

  /**
   * Gets the conv from human.
   *
   * @return the conv from human
   */
  public boolean getConvFromHuman() {
    return mRadioConvFromHuman.isSelected();
  }

  /**
   * Gets the conv from mouse.
   *
   * @return the conv from mouse
   */
  public boolean getConvFromMouse() {
    return mRadioConvFromMouse.isSelected();
  }

  /**
   * Gets the conv to human.
   *
   * @return the conv to human
   */
  public boolean getConvToHuman() {
    return mRadioConvToHuman.isSelected();
  }

  /**
   * Gets the conv to mouse.
   *
   * @return the conv to mouse
   */
  public boolean getConvToMouse() {
    return mRadioConvToMouse.isSelected();
  }

  /**
   * Gets the output chr.
   *
   * @return the output chr
   */
  public boolean getOutputChr() {
    return mCheckChr.isSelected();
  }

  /**
   * Gets the output strand.
   *
   * @return the output strand
   */
  public boolean getOutputStrand() {
    return mCheckStrand.isSelected();
  }

  /**
   * Gets the output entrez.
   *
   * @return the output entrez
   */
  public boolean getOutputEntrez() {
    return mCheckEntrez.isSelected();
  }

  /**
   * Gets the output ref seq.
   *
   * @return the output ref seq
   */
  public boolean getOutputRefSeq() {
    return mCheckRefSeq.isSelected();
  }

  /**
   * Gets the output ensembl gene.
   *
   * @return the output ensembl gene
   */
  public boolean getOutputEnsemblGene() {
    return mCheckEnsemblGene.isSelected();
  }

  /**
   * Gets the output ensembl transcript.
   *
   * @return the output ensembl transcript
   */
  public boolean getOutputEnsemblTranscript() {
    return mCheckEnsemblTranscript.isSelected();
  }

  /**
   * Gets the split.
   *
   * @return the split
   */
  public boolean getSplit() {
    return mCheckSplit.isSelected();
  }

  /**
   * Gets the output conversions.
   *
   * @return the output conversions
   */
  public boolean getOutputConversions() {
    return mCheckConversions.isSelected();
  }

  /**
   * Returns true if old symbols should be kept if a replacement cannot be
   * found.
   * 
   * @return
   */
  public boolean getKeepOld() {
    return mCheckKeepOld.isSelected();
  }
  
  /**
   * Returns true if alternative ids for a gene should be displayed.
   * 
   * @return
   */
  public boolean getShowAlt() {
    return mCheckShowAlt.isSelected();
  }

  public boolean getOutputParams() {
    return mCheckParams.isSelected();
  }
  
  public String getVersion() {
    return mDbPanel.getVersion();
  }
}
