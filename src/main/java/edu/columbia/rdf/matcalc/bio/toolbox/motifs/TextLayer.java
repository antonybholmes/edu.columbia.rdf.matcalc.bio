package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.PlotLayer;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.graphics.DrawingContext;
import org.jebtk.modern.graphics.ImageUtils;

public class TextLayer extends PlotLayer {
  private static final long serialVersionUID = 1L;

  private double ROTATION = Math.toRadians(-90);

  private String mSearchId;

  public TextLayer(String searchId) {
    super("Text");

    mSearchId = searchId;
  }

  @Override
  public void plotLayer(Graphics2D g2, DrawingContext context, Figure figure, SubFigure subFigure, Axes axes, Plot plot,
      DataFrame m) {

    int nameCol = DataFrame.findColumn(m, "Motif Name");
    int motifCol = DataFrame.findColumn(m, "5 Sequence");
    int offsetCol = DataFrame.findColumn(m, "Offset From Reference");
    // int strandCol = DataFrame.findColumn(m, "Strand");
    int mutationCol = DataFrame.findColumn(m, "Mutation");

    int lineHeight = g2.getFontMetrics().getAscent() / 2; // ModernWidget.getStringHeight(g2)
                                                          // / 2;

    int y = axes.toPlotY1(0.7);

    g2.setColor(Color.BLACK);

    for (int i = 0; i < m.getRows(); ++i) {
      String id = m.getText(i, mutationCol);

      if (!id.equals(mSearchId)) {
        continue;
      }

      String name = m.getText(i, nameCol);
      String motif = m.getText(i, motifCol);

      int offset = (int) m.getValue(i, offsetCol);

      int x1 = axes.toPlotX1(offset);
      int x2 = axes.toPlotX1(offset + motif.length());

      int x = (x1 + x2) / 2;

      // String strand = m.getText(i, strandCol);

      // if (!strand.equals("+")) {
      // continue;
      // }

      Graphics2D g2Text = ImageUtils.createAATextGraphics(g2);

      try {
        g2Text.translate(x, y);
        g2Text.rotate(ROTATION);

        g2Text.drawString(name, 0, lineHeight);

      } finally {
        g2Text.dispose();
      }
    }
  }
}