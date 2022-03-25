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
package edu.columbia.rdf.matcalc.bio.toolbox.pathway;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.jebtk.bioinformatics.pathway.GeneSetCollection;
import org.jebtk.core.io.Io;
import org.jebtk.core.text.TextUtils;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.dialog.ModernDialogContentPanel;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.text.ModernDialogHeadingLabel;
import org.jebtk.modern.text.ModernTextArea;
import org.jebtk.modern.window.ModernWindow;

/**
 * The class GenesPanel.
 */
public class GenesPanel extends ModernWidget implements ModernClickListener {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The m genes field.
   */
  private ModernTextArea mGenesField = new ModernTextArea();

  /**
   * The collection map.
   */
  private Map<ModernCheckBox, GeneSetCollection> collectionMap = new HashMap<ModernCheckBox, GeneSetCollection>();

  /**
   * The m model.
   */
  private Set<GeneSetCollection> mModel;

  /**
   * Instantiates a new genes panel.
   *
   * @param parent the parent
   * @param model the model
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public GenesPanel(ModernWindow parent, Set<GeneSetCollection> model)
      throws IOException {
    mModel = model;

    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    Box box = Box.createVerticalBox();
    box.setAlignmentY(TOP_ALIGNMENT);

    // box.add(new ModernDialogHeadingLabel("Gene Symbols"));
    // box.add(ModernTheme.createVerticalGap());

    ModernScrollPane scrollPane = new ModernScrollPane(mGenesField);
    scrollPane.setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER);
    // Ui.setSize(scrollPane, new Dimension(150, 400));
    box.add(new ModernDialogContentPanel(scrollPane));
    box.setMinimumSize(new Dimension(150, 0));

    add(box);

    add(ModernWidget.createHGap());

    box = Box.createVerticalBox();
    box.setAlignmentY(TOP_ALIGNMENT);

    box.add(new ModernDialogHeadingLabel("Gene Sets"));
    box.add(ModernWidget.createVGap());

    for (GeneSetCollection collection : model) {
      ModernCheckBox collCheck = new ModernCheckBox(collection.getName());
      collCheck.addClickListener(this);
      UI.setSize(collCheck, new Dimension(200, 24));

      box.add(collCheck);
      box.add(ModernWidget.createVGap());

      collectionMap.put(collCheck, collection);
    }

    add(box);
  }

  /**
   * Open file.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void openFile(File file) throws IOException {
    if (file == null) {
      return;
    }

    BufferedReader reader = new BufferedReader(new FileReader(file));

    String line;

    try {
      while ((line = reader.readLine()) != null) {
        if (Io.isEmptyLine(line)) {
          continue;
        }

        if (mGenesField.getText().length() > 0) {
          mGenesField.append(TextUtils.NEW_LINE);
        }

        mGenesField.append(line);
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Sets the collections.
   */
  private void setCollections() {
    List<GeneSetCollection> collections = new ArrayList<GeneSetCollection>();

    for (ModernCheckBox check : collectionMap.keySet()) {
      if (check.isSelected()) {
        collections.add(collectionMap.get(check));
      }
    }

    Collections.sort(collections);

    mModel.addAll(collections);
  }

  /**
   * Gets the symbols.
   *
   * @return the symbols
   */
  public List<String> getSymbols() {
    return TextUtils.newLineSplit(mGenesField.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.abh.lib.ui.modern.event.ModernClickListener#clicked(org.abh.lib.ui.
   * modern .event.ModernClickEvent)
   */
  @Override
  public void clicked(ModernClickEvent e) {
    setCollections();
  }

}
