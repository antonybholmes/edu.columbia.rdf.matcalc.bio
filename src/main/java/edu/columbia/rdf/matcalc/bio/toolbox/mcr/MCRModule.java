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
package edu.columbia.rdf.matcalc.bio.toolbox.mcr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.search.Feature;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.UniqueArrayListCreator;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.Matrix;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

/**
 * Map probes to genes.
 *
 * @author Antony Holmes
 *
 */
public class MCRModule extends Module implements ModernClickListener {

  private static final String[] COL_NAMES = { "Genomic Location", "Number of Samples", "Segment Ids" };

  /**
   * The member convert button.
   */
  private RibbonLargeButton mConvertButton = new RibbonLargeButton("MCR",
      AssetService.getInstance().loadIcon(MCRIcon.class, 24));

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
    return "Minimal Common Regions";
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.columbia.rdf.apps.matcalc.modules.Module#init(edu.columbia.rdf.apps.
   * matcalc.MainMatCalcWindow)
   */
  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    // home
    mWindow.getRibbon().getToolbar("Genomic").getSection("Regions").add(mConvertButton);

    mConvertButton.addClickListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.ui.modern.event.ModernClickListener#clicked(org.abh.lib.ui.
   * modern .event.ModernClickEvent)
   */
  @Override
  public final void clicked(ModernClickEvent e) {
    run();
  }

  public final void run() {
    DataFrame ret = run(mWindow.getCurrentMatrix());

    mWindow.history().addToHistory("MCR", ret);
  }

  public final DataFrame run(Matrix m) {
    IterMap<Chromosome, List<ConsensusRegion>> consensusRegions = mcr(m);

    int rows = 0;

    for (Entry<Chromosome, List<ConsensusRegion>> chr : consensusRegions.entrySet()) {
      rows += chr.getValue().size();
    }

    DataFrame ret = DataFrame.createMixedMatrix(rows, 3);

    ret.setColumnNames(COL_NAMES);

    int row = 0;

    for (Entry<Chromosome, List<ConsensusRegion>> c : consensusRegions.entrySet()) {

      // get positions in order
      Collections.sort(c.getValue());

      for (ConsensusRegion cr : c.getValue()) {
        ret.set(row, 0, cr.getLocation());
        ret.set(row, 1, cr.getIds().size());
        ret.set(row, 2, TextUtils.join(cr.getIds(), TextUtils.SEMI_COLON_DELIMITER));

        ++row;

      }
    }

    return ret;
  }

  /**
   * Mcr.
   *
   * @param gain             the gain
   * @param loss             the loss
   * @param gainMode         the gain mode
   * @param consensusRegions the consensus regions
   * @return
   * @throws InvalidFormatException the invalid format exception
   * @throws IOException            Signals that an I/O exception has occurred.
   */
  private static final IterMap<Chromosome, List<ConsensusRegion>> mcr(Matrix m) {
    // List<String> tokens;

    IterMap<Chromosome, List<ConsensusRegion>> ret = DefaultHashMap
        .create(new UniqueArrayListCreator<ConsensusRegion>());

    IterMap<Chromosome, List<Feature>> features = DefaultHashMap.create(new UniqueArrayListCreator<Feature>());

    for (int r = 0; r < m.getRows(); ++r) {

      Feature feature = new Feature(m.getText(r, 0), Chromosome.newChr(m.getText(r, 1)), m.getInt(r, 2),
          m.getInt(r, 3));

      features.get(feature.getChr()).add(feature);

      // add non duplicate starts and ends
    }

    for (Entry<Chromosome, List<Feature>> cf : features.entrySet()) {
      // sort the positions
      Collections.sort(cf.getValue());
    }

    GenomicRegion mcr;
    GenomicRegion overlap;
    List<String> ids = new ArrayList<String>();
    ;
    // Determine the mcrs

    for (Entry<Chromosome, List<Feature>> cf : features.entrySet()) {
      for (Feature f1 : cf.getValue()) {

        // Start an mcr from every feature
        mcr = f1;

        ids.clear();
        ids.add(f1.getName());
        mcr = f1;

        for (Feature f2 : cf.getValue()) {
          if (f1.equals(f2)) {
            continue;
          }

          overlap = GenomicRegion.overlap(mcr, f1);

          if (overlap != null) {
            ids.add(f1.getName());
            mcr = overlap;
          }
        }

        // If the mcr is a duplicate, it will be ignored.
        ret.get(cf.getKey()).add(new ConsensusRegion(mcr).addIds(ids));
      }
    }

    return ret;
  }
}
