package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jebtk.core.tree.TreeNode;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.theme.ThemeService;
import org.jebtk.modern.tree.ModernTreeNodeRenderer;
import org.jebtk.modern.tree.Tree;
import org.jebtk.modern.tree.TreeIconNodeCountRenderer;

/**
 * Displays a sample as a two line node with title and some basic info. Headers
 * are displayed in bold
 *
 * @author Antony Holmes
 *
 */
public class EnhancerTreeNodeRenderer extends ModernTreeNodeRenderer {
  private static final long serialVersionUID = 1L;

  private static final int HEADER_HEIGHT = 32;
  private static final int HEIGHT = 50;
  // private static final int LINE_HEIGHT = Resources.ICON_SIZE_24;

  private static final Color FILL_COLOR = ThemeService.getInstance().getColors().getGray(2);

  private String text1 = null;
  private String text2 = null;
  private String text3 = null;

  @Override
  public final void drawBackground(Graphics2D g2) {
    Rectangle r = new Rectangle(mRect.getX(), mRect.getY(), mRect.getW(), mRect.getH());

    if (mNode.isParent()) {
      fill(g2, FILL_COLOR, r);
    } else if (mNodeIsSelected) {
      paintThemeSelected(g2, getRect());
    } else if (mNodeIsHighlighted) {
      paintThemeHighlighted(g2, getRect());
    } else {
      // do nothing
    }
  }

  @Override
  public void drawForegroundAA(Graphics2D g2) {

    int y;

    int x = 0;

    // Draw the dividing line only if the node is not highlighted, otherwise
    // it interferes with the highlighting rectangle

    if (mNode.isParent()) {
      y = (HEADER_HEIGHT - AssetService.ICON_SIZE_16) / 2;

      if (mNode.isExpanded()) {
        TreeIconNodeCountRenderer.BRANCH_CLOSED_ICON.drawIcon(g2, x, y, 16);
      } else {
        TreeIconNodeCountRenderer.BRANCH_OPEN_ICON.drawIcon(g2, x, y, 16);
      }

      x += TreeIconNodeCountRenderer.BRANCH_OPEN_ICON.getWidth(); // +
                                                                  // ModernTheme.getInstance().getClass("widget").getInt("padding");

      y = (HEADER_HEIGHT + g2.getFontMetrics().getAscent()) / 2;

      // g2.clipRect(0, 0, getWidth(), getHeight());

      g2.setFont(ModernWidget.BOLD_FONT);
      g2.setColor(TEXT_COLOR);
      g2.drawString(getTruncatedText(g2, text1, x, mRect.getW()), x, y);

      g2.setColor(ModernWidget.LINE_COLOR);

      // g2.drawLine(0, 0, mRect.getW() - 1, 0);

      y = mRect.getH() - 1;
      g2.drawLine(0, y, mRect.getW() - 1, y);
    } else {
      x += TreeIconNodeCountRenderer.BRANCH_OPEN_ICON.getWidth(); // +
                                                                  // ModernTheme.getInstance().getClass("widget").getInt("padding");

      g2.setFont(ThemeService.loadFont("widget.sub-heading"));
      g2.setColor(TEXT_COLOR);
      y = (mRect.getH() / 2 + g2.getFontMetrics().getAscent()) / 2;
      g2.drawString(getTruncatedText(g2, text1, x, mRect.getW()), x, y);

      g2.setColor(mNodeIsSelected ? TEXT_COLOR : ALT_TEXT_COLOR);
      g2.setFont(ModernWidget.FONT);

      y = mRect.getH() / 2 + (mRect.getH() / 2 + g2.getFontMetrics().getAscent()) / 2;
      g2.drawString(getTruncatedText(g2, text2, x, mRect.getW()), x, y);

      y = (mRect.getH() + g2.getFontMetrics().getAscent()) / 2;
      x = mRect.getW() - g2.getFontMetrics().stringWidth(text3) - ModernWidget.DOUBLE_PADDING;
      g2.drawString(getTruncatedText(g2, text3, x, mRect.getW()), x, y);

      if (!mNodeIsHighlighted) {
        g2.setColor(ModernWidget.LINE_COLOR);
        g2.drawLine(0, mRect.getH() - 1, mRect.getW() - 1, mRect.getH() - 1);
      }
    }
  }

  @Override
  public ModernTreeNodeRenderer getRenderer(Tree<?> tree, TreeNode<?> node, boolean nodeIsHighlighted,
      boolean nodeIsSelected, boolean hasFocus, boolean isDragToNode, int depth, int row) {
    super.getRenderer(tree, node, nodeIsHighlighted, nodeIsSelected, hasFocus, isDragToNode, depth, row);

    if (node.isParent()) {
      text1 = node.getName();
    } else {
      Enhancer sample = (Enhancer) node.getValue();

      if (sample != null) {
        text1 = sample.getName(); // + " (" +
                                  // sample.getOrganism().getScientificName() +
                                  // ")";

        // text2 = sample.getPerson().getName();

        // text3 = sample.getDate().toString();
      }
    }

    if (node.isParent()) {
      setSize(tree.getWidth(), HEADER_HEIGHT);
    } else {
      setSize(tree.getWidth(), HEIGHT);
    }

    return this;
  }
}