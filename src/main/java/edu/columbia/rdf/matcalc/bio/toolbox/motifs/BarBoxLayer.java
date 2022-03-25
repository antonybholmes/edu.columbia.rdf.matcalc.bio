package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.Graphics2D;

import org.jebtk.core.settings.SettingsService;
import org.jebtk.graphplot.figure.Axes;
import org.jebtk.graphplot.figure.Figure;
import org.jebtk.graphplot.figure.Plot;
import org.jebtk.graphplot.figure.PlotLayer;
import org.jebtk.graphplot.figure.SubFigure;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.graphics.DrawingContext;

public class BarBoxLayer extends PlotLayer {

  private static final long serialVersionUID = 1L;

  public BarBoxLayer() {
    super("Bar Box");
  }

  @Override
  public void plotLayer(Graphics2D g2, DrawingContext context, Figure figure, SubFigure subFigure, Axes axes, Plot plot,
      DataFrame m) {

    int dnaCol = DataFrame.findColumn(m, "DNA Sequence");

    String dna = m.getText(0, dnaCol);

    int l = dna.length();

    int x1 = axes.toPlotX1(0);
    int x2 = axes.toPlotX1(l);

    int y1 = axes.toPlotY1(0.55);
    int y2 = axes.toPlotY1(0.45);

    // int h =
    // SettingsService.getInstance().getInt("mutplot.plot.protein.height");

    // int y = PLOT_OFFSET.y + internalPlotSize.height - FEATURE_BLOCK_HEIGHT +
    // (FEATURE_BLOCK_HEIGHT - h) / 2;

    g2.setColor(SettingsService.getInstance().getColor("motifs.plot.bar.background.color")); // ColorUtils.decodeHtmlColor(mProperty.getChildByPath("background/color").getValue()));

    // g2.fillRect(PLOT_OFFSET.x, y, internalPlotSize.width, h);
    g2.fillRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);

    g2.setColor(SettingsService.getInstance().getColor("motifs.plot.bar.border.color"));

    // g2.drawRect(PLOT_OFFSET.x, y, internalPlotSize.width, h);
    g2.drawRect(x1, y1, x2 - x1, y2 - y1);
  }
}
