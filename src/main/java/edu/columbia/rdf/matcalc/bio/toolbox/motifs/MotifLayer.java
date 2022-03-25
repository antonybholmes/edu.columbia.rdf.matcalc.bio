package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jebtk.core.ColorUtils;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.PlotLayer;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.graphics.DrawingContext;

public class MotifLayer extends PlotLayer {

  private static final long serialVersionUID = 1L;
  private String mSearchId;

  public MotifLayer(String searchId) {
    super("Bar Box");

    mSearchId = searchId;
  }

  @Override
  public void plotLayer(Graphics2D g2, DrawingContext context, Figure figure, SubFigure subFigure, Axes axes, Plot plot,
      DataFrame m) {

    // int dnaCol = DataFrame.findColumn(m, "DNA Sequence");
    int mutationCol = DataFrame.findColumn(m, "Mutation");

    // String dna = m.getText(0, dnaCol);

    // int l = dna.length();

    int y1 = axes.toPlotY1(0.6);
    int y2 = axes.toPlotY1(0.4);

    // int nameCol = DataFrame.findColumn(m, "Motif Name");
    int motifCol = DataFrame.findColumn(m, "5 Sequence");
    int offsetCol = DataFrame.findColumn(m, "Offset From Reference");
    // int strandCol = DataFrame.findColumn(m, "Strand");

    for (int i = 0; i < m.getRows(); ++i) {
      String id = m.getText(i, mutationCol);

      if (!id.equals(mSearchId)) {
        continue;
      }

      int offset = (int) m.getValue(i, offsetCol);
      String motif = m.getText(i, motifCol);

      // String strand = m.getText(i, strandCol);

      // if (!strand.equals("+")) {
      // continue;
      // }

      int x1 = axes.toPlotX1(offset);
      int x2 = axes.toPlotX1(offset + motif.length());

      // int h =
      // SettingsService.getInstance().getInt("mutplot.plot.protein.height");

      // int y = PLOT_OFFSET.y + internalPlotSize.height - FEATURE_BLOCK_HEIGHT
      // +
      // (FEATURE_BLOCK_HEIGHT - h) / 2;

      g2.setColor(ColorUtils.tint(Color.RED, 0.5)); // ColorUtils.decodeHtmlColor(mProperty.getChildByPath("background/color").getValue()));

      // g2.fillRect(PLOT_OFFSET.x, y, internalPlotSize.width, h);
      g2.fillRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);

      g2.setColor(Color.RED);

      // g2.drawRect(PLOT_OFFSET.x, y, internalPlotSize.width, h);
      g2.drawRect(x1, y1, x2 - x1, y2 - y1);
    }
  }
}
