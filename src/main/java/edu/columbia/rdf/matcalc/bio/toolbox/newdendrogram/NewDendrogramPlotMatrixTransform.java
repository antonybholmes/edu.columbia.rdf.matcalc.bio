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
package edu.columbia.rdf.matcalc.bio.toolbox.newdendrogram;

import java.util.List;

import org.jebtk.core.Props;
import org.jebtk.graphplot.figure.heatmap.legacy.CountGroups;
import org.jebtk.graphplot.figure.series.XYSeriesModel;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.window.ModernRibbonWindow;

import edu.columbia.rdf.matcalc.toolbox.plot.heatmap.legacy.HeatMapPlotMatrixTransform;

/**
 * Transform the rows of a matrix.
 * 
 * @author Antony Holmes
 *
 */
public class NewDendrogramPlotMatrixTransform
    extends HeatMapPlotMatrixTransform {

  /**
   * Instantiates a new pattern discovery plot matrix transform.
   *
   * @param parent the parent
   * @param inputMatrix the input matrix
   * @param comparisonGroups the comparison groups
   * @param groups the groups
   * @param history the history
   * @param properties the properties
   */
  public NewDendrogramPlotMatrixTransform(ModernRibbonWindow parent,
      DataFrame inputMatrix, XYSeriesModel groups, XYSeriesModel rowGroups,
      CountGroups countGroups, List<String> history, Props properties) {
    super(parent, "Create New Dendrogram Plot", inputMatrix, groups, rowGroups,
        countGroups, history, properties);
  }

  /**
   * @Override public ModernWindow createWindow() { return
   *           newDendrogramPlotWindow(mParent, mInputMatrix, mGroups,
   *           mRowGroups, mCountGroups, mHistory, mProperties); }
   */
}
