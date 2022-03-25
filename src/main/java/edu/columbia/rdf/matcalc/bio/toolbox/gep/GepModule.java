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
package edu.columbia.rdf.matcalc.bio.toolbox.gep;

import java.io.IOException;
import java.util.List;

import org.jebtk.core.NetworkFileException;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.button.ModernButton;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.tooltip.ModernToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.edb.EDBWLogin;
import edu.columbia.rdf.edb.Sample;
import edu.columbia.rdf.edb.ui.Repository;
import edu.columbia.rdf.edb.ui.RepositoryService;
import edu.columbia.rdf.edb.ui.microarray.Mas5Dialog;
import edu.columbia.rdf.edb.ui.microarray.MicroarrayNormalizationType;
import edu.columbia.rdf.edb.ui.microarray.RMADialog;
import edu.columbia.rdf.edb.ui.network.ServerException;
import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

/**
 * Merges designated segments together using the merge column. Consecutive rows
 * with the same merge id will be merged together. Coordinates and copy number
 * will be adjusted but genes, cytobands etc are not.
 *
 * @author Antony Holmes
 *
 */
public class GepModule extends Module {
  private static final Logger LOG = LoggerFactory.getLogger(GepModule.class);

  /**
   * The member window.
   */
  private MainMatCalcWindow mWindow;

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.NameProperty#getName()
   */
  @Override
  public String getName() {
    return "GEP";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.columbia.rdf.apps.matcalc.modules.Module#init(edu.columbia.rdf.apps.
   * matcalc.MainMatCalcWindow)
   */
  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    ModernButton button = new RibbonLargeButton("Microarray",
        AssetService.getInstance().loadIcon("microarray", 32),
        AssetService.getInstance().loadIcon("microarray", 24));

    button.setToolTip(
        new ModernToolTip("Microarray", "Download microarray data."));
    button.setClickMessage("Microarray");
    mWindow.getRibbon().getToolbar("Expression").getSection("Expression")
        .add(button);

    button.addClickListener(new ModernClickListener() {
      @Override
      public void clicked(ModernClickEvent e) {
        try {
          microarray();
        } catch (ServerException | ClassNotFoundException | IOException
            | NetworkFileException e1) {
          e1.printStackTrace();
        }
      }
    });

    button = new RibbonLargeButton("RNA-seq",
        AssetService.getInstance().loadIcon("rna", 32),
        AssetService.getInstance().loadIcon("rna", 24));

    button.setToolTip(new ModernToolTip("RNA-seq", "Download RNA-seq data."));
    button.setClickMessage("RNA-seq");
    mWindow.getRibbon().getToolbar("Expression").getSection("Expression")
        .add(button);

    button.addClickListener(new ModernClickListener() {
      @Override
      public void clicked(ModernClickEvent e) {
        try {
          rna();
        } catch (ServerException | ClassNotFoundException | IOException
            | NetworkFileException e1) {
          e1.printStackTrace();
        }
      }
    });
  }

  private void microarray() throws ServerException, ClassNotFoundException,
      IOException, NetworkFileException {
    // if (RepositoryService.getInstance().getRepository() == null) {
    // Load the default login
    EDBWLogin login = EDBWLogin.loadFromSettings();

    GepLoginDialog loginDialog = new GepLoginDialog(mWindow, login);

    loginDialog.setVisible(true);

    if (loginDialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    login = loginDialog.getLogin();

    MicroarrayRepositoryCache session = new MicroarrayRepositoryCache(login);
    Repository repository = session.restore();
    RepositoryService.getInstance().setRepository(RepositoryService.DEFAULT_REP,
        repository);
    // }

    MicroarraySamplesDialog dialog = new MicroarraySamplesDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    showMicroarrayData(dialog.getSelectedSamples(),
        dialog.getShowMas5(),
        dialog.getShowRMA());
  }

  private void showMicroarrayData(List<Sample> samples,
      boolean mas5,
      boolean rma) throws NetworkFileException, IOException {
    if (samples.size() == 0) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must select at least one sample.");

      return;
    }

    boolean correctType = true;

    for (Sample sample : samples) {
      if (!sample.getDataType().getName().equals("Microarray")) {
        correctType = false;
        break;
      }
    }

    if (!correctType) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "Some of the samples you have selected do not contain expression data.");

      return;
    }

    MicroarrayExpressionData expressionData = new MicroarrayExpressionData();

    if (mas5) {
      Mas5Dialog dialog = new Mas5Dialog(mWindow);

      dialog.setVisible(true);

      if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
        return;
      }

      expressionData.showTables(mWindow,
          samples,
          MicroarrayNormalizationType.MAS5,
          dialog.getColumns(),
          dialog.getAnnotations(),
          true);
    }

    if (rma) {
      RMADialog dialog = new RMADialog(mWindow);

      dialog.setVisible(true);

      if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
        return;
      }

      expressionData.showTables(mWindow,
          samples,
          MicroarrayNormalizationType.RMA,
          CollectionUtils.asList(true),
          dialog.getAnnotations(),
          true);
    }
  }

  private void rna() throws ServerException, ClassNotFoundException,
      IOException, NetworkFileException {
    // if (RepositoryService.getInstance().getRepository() == null) {
    // Load the default login
    EDBWLogin login = EDBWLogin.loadFromSettings();

    GepLoginDialog loginDialog = new GepLoginDialog(mWindow, login);

    loginDialog.setVisible(true);

    if (loginDialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    login = loginDialog.getLogin();

    RnaRepositoryCache session = new RnaRepositoryCache(login);
    Repository repository = session.restore();
    RepositoryService.getInstance().setRepository("rnaseq", repository);
    // }

    RnaSamplesDialog dialog = new RnaSamplesDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    showRnaData(dialog.getSelectedSamples());
  }

  private void showRnaData(List<Sample> samples)
      throws NetworkFileException, IOException {
    if (samples.size() == 0) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must select at least one sample.");

      return;
    }

    RnaSeqData rnaData = new RnaSeqData();

    rnaData.showTables(mWindow, samples, true);
  }
}
