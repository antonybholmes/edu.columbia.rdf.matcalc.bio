package edu.columbia.rdf.matcalc.bio.toolbox.motifs;
import java.awt.Graphics2D;
import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.core.Props;
import org.jebtk.core.geom.IntRect;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.animation.WidgetAnimation;
import org.jebtk.modern.theme.DrawUIService;

public class BaseButtonHighlightAnimation extends WidgetAnimation {
  private final BaseButton mButton;

  public BaseButtonHighlightAnimation(BaseButton button) {
    super(button);

    mButton = button;
  }
  
  @Override
  public void draw(ModernWidget c, Graphics2D g2, Props props) {
    if (mWidget.isEnabled()) {

      int x = (mButton.getWidth() - BaseButton.SIZE) / 2; // PADDING;
      int y = (mButton.getHeight() - BaseButton.SIZE) / 2;
      int w = BaseButton.SIZE; // - 1;

      // UIDrawService.getInstance().get("circle-fill").draw(g2, x, y, w,
      // w, SequenceService.getInstance().getBaseColor(mButton.getBase()));

      DrawUIService.getInstance().getRenderer("circle-fill").draw(g2, new IntRect(x, y, w, w), 
              SequenceService.getInstance().getBaseColor(mButton.getBase()));
    }
  }
}
