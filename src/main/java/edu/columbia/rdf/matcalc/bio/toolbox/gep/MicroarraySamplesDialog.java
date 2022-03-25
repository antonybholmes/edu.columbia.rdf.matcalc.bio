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
package edu.columbia.rdf.matcalc.bio.toolbox.gep;

import java.awt.Component;

import javax.swing.Box;

import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernCheckBox;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.search.SearchModel;
import org.jebtk.modern.window.ModernWindow;

import edu.columbia.rdf.edb.ui.RepositoryService;
import edu.columbia.rdf.edb.ui.SamplesDialog;
import edu.columbia.rdf.matcalc.bio.toolbox.gep.GepSortModel;

public class MicroarraySamplesDialog extends SamplesDialog {
  private static final long serialVersionUID = 1L;

  private ModernCheckBox mCheckMas5;
  private ModernCheckBox mCheckRMA;

  public MicroarraySamplesDialog(ModernWindow parent) {
    super(parent, "Microarray Samples", "microarray.help.url",
        RepositoryService.DEFAULT_REP, new GepSortModel(), new SearchModel());
  }

  @Override
  public Component footer() {
    mCheckMas5 = new ModernCheckBox("MAS5", true);
    mCheckRMA = new ModernCheckBox("RMA");

    Box box = VBox.create();

    box.add(UI.createVGap(20));
    box.add(mCheckMas5);
    box.add(mCheckRMA);

    return box;
  }

  /**
   * Returns true if mas5 data should be displayed.
   * 
   * @return
   */
  public boolean getShowMas5() {
    return mCheckMas5.isSelected();
  }

  /**
   * Returns true if RMA data should be displayed.
   * 
   * @return
   */
  public boolean getShowRMA() {
    return mCheckRMA.isSelected();
  }
}
