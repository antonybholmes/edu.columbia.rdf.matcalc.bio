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

import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.pathway.GeneSet;
import org.jebtk.bioinformatics.pathway.IdToSymbol;
import org.jebtk.bioinformatics.pathway.Pathway;
import org.jebtk.core.Resources;
import org.jebtk.core.collections.UniqueArrayList;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.io.TmpService;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.MixedWorksheetParser;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.xml.sax.SAXException;

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
public class PathwayModule extends Module implements ModernClickListener {

  /**
   * The constant HUMAN_REFSEQ_Path.
   */
  private static final Path HUMAN_REFSEQ_FILE = PathUtils
      .getPath("res/modules/pathway/ucsc_refseq_genes_hg19_20160331.txt.gz");

  private static final Path HUMAN_ENSEMBL_FILE = PathUtils
      .getPath("res/modules/pathway/ucsc_ensembl_genes_hg19_20160711.txt.gz");

  /**
   * The member convert button.
   */
  private RibbonLargeButton mConvertButton = new RibbonLargeButton("Pathway",
      AssetService.getInstance().loadIcon(PathwayIcon.class, 24));

  public static final Path GENE_SETS_DIR = PathUtils
      .getPath("res/modules/pathway/gene_sets");

  /**
   * The member window.
   */
  private MainMatCalcWindow mWindow;

  private IdToSymbol mHumanRefSeqConversion;

  private IdToSymbol mHumanEnsemblConversion;

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.NameProperty#getName()
   */
  @Override
  public String getName() {
    return "Pathway";
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

    // home
    mWindow.getRibbon().getHomeToolbar().getSection("Tools")
        .add(mConvertButton);

    mConvertButton.addClickListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.abh.lib.ui.modern.event.ModernClickListener#clicked(org.abh.lib.ui.
   * modern .event.ModernClickEvent)
   */
  @Override
  public final void clicked(ModernClickEvent e) {
    try {
      analysis();
    } catch (IOException e1) {
      e1.printStackTrace();
    } catch (InvalidFormatException e1) {
      e1.printStackTrace();
    } catch (SAXException e1) {
      e1.printStackTrace();
    } catch (ParserConfigurationException e1) {
      e1.printStackTrace();
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    } catch (InstantiationException e1) {
      e1.printStackTrace();
    } catch (IllegalAccessException e1) {
      e1.printStackTrace();
    } catch (FontFormatException e1) {
      e1.printStackTrace();
    } catch (UnsupportedLookAndFeelException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Analysis.
   * 
   * @throws IOException
   * @throws ParseException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws InvalidFormatException
   * @throws UnsupportedLookAndFeelException
   * @throws FontFormatException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   * @throws Exception
   */
  private void analysis() throws IOException, InvalidFormatException,
      SAXException, ParserConfigurationException, ClassNotFoundException,
      InstantiationException, IllegalAccessException, FontFormatException,
      UnsupportedLookAndFeelException {
    List<Integer> columns = mWindow.getSelectedColumns();

    if (columns.size() == 0) {
      ModernMessageDialog.createWarningDialog(mWindow,
          "You must select a column of gene ids/symbols.");

      return;
    }

    int c = columns.get(0);

    PathwayDialog dialog = new PathwayDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    // Load the gene mapping if necessary

    if (mHumanRefSeqConversion == null) {
      mHumanRefSeqConversion = new IdToSymbol(
          Resources.getGzipReader(HUMAN_REFSEQ_FILE));
    }

    if (mHumanEnsemblConversion == null) {
      mHumanEnsemblConversion = new IdToSymbol(
          Resources.getGzipReader(HUMAN_ENSEMBL_FILE));
    }

    DataFrame m = mWindow.getCurrentMatrix();

    List<String> ids = new UniqueArrayList<String>(Arrays.asList(m.columnToText(c)));

    Path tempPath = TmpService.getInstance().newTmpFile("txt");
    Path tableTempPath = TmpService.getInstance().newTmpFile("txt");

    Set<GeneSet> collections = dialog.getCollections();

    double maxFdr = dialog.getFdr();

    Pathway.analysis(ids,
        collections,
        mHumanRefSeqConversion,
        mHumanEnsemblConversion,
        maxFdr,
        tempPath,
        tableTempPath);

    // addTablePane(mTempPath);
    
    m = new MixedWorksheetParser(0, 0, TextUtils.TAB_DELIMITER).parse(tempPath);
    
    mWindow.history().addToHistory("Pathway", m);

    //new OpenFile(mWindow, tempPath).headers(0).rowAnnotations(0)
    //    .openMode(OpenMode.CURRENT_WINDOW).open();
    
    // MainMatCalc.openPath(mTablePath, true, 2, "Heat Map", "plot");
  }
}
