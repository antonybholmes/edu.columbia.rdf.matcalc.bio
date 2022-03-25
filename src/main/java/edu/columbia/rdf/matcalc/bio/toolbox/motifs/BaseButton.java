package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.core.event.ChangeEvent;
import org.jebtk.modern.UI;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeColorSwatchButton2;
import org.jebtk.modern.window.ModernWindow;

public class BaseButton extends RibbonLargeColorSwatchButton2 {
  private static final long serialVersionUID = 1L;

  public static final int SIZE = 24; // Ribbon.COMPACT_ICON_SIZE;

  private char mBase;

  /**
   * Instantiates a new color swatch button.
   *
   * @param parent the parent
   * @param color  the color
   */
  public BaseButton(ModernWindow parent, char base) {
    super(parent, SequenceService.getInstance().getBaseColor(base));

    mBase = base;

    SequenceService.getInstance().addChangeListener(base, (ChangeEvent e) -> {
      repaint();
    });

    addClickListener((ModernClickEvent e) -> {
      SequenceService.getInstance().setBaseColor(mBase, getSelectedColor());
    });

    setAnimations(new BaseButtonHighlightAnimation(this));
  }

  @Override
  public void setCompactSize() {
    UI.setSize(this, Ribbon.COMPACT_BUTTON_HEIGHT, Ribbon.COMPACT_BUTTON_HEIGHT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.abh.lib.ui.modern.button.ModernDropDownButton#drawBackground(java.awt.
   * Graphics2D)
   */
  @Override
  public void drawForegroundAA(Graphics2D g2) {
    Color color = SequenceService.getInstance().getBaseColor(mBase); // ((ColorPopupMenu2)
    // mMenu).getSelectedColor();
    if (color.equals(Color.WHITE)) {
      g2.setColor(Color.BLACK);
    } else {
      g2.setColor(Color.WHITE);
    }

    String s = Character.toString(mBase);

    g2.setFont(BOLD_FONT);

    int x = getTextXPosCenter(g2, s, getWidth());
    int y = getTextYPosCenter(g2, getHeight());

    g2.drawString(s, x, y);

    // TRIANGLE_ICON.drawIcon(g2, getWidth() - 16, (getHeight() - 16) / 2, 16);
  }

  public char getBase() {
    return mBase;
  }
}
