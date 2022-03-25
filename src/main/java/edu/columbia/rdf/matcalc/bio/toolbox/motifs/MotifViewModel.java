package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import org.jebtk.bioinformatics.motifs.MotifView;
import org.jebtk.core.model.ItemModel;
import org.jebtk.core.settings.SettingsService;

/**
 * Centrally keep track of selected experiments in the order they were selected.
 * 
 * @author Antony Holmes
 *
 */
public class MotifViewModel extends ItemModel<MotifView> {

  private static final long serialVersionUID = 1L;
  private boolean mRevComp;

  public MotifViewModel() {
    update(MotifView.parse(SettingsService.getInstance().getString("org.matcalc.toolbox.bio.seqlogo.motif-view")));
    setRevComp(SettingsService.getInstance().getBool("org.matcalc.toolbox.bio.seqlogo.rev-comp"));
  }

  @Override
  public void update(MotifView view) {
    super.update(view);

    // Store the setting
    SettingsService.getInstance().update("org.matcalc.toolbox.bio.seqlogo.motif-view", view.toString().toLowerCase());
  }

  public void setRevComp(boolean selected) {
    updateRevComp(selected);

    fireChanged();
  }

  public void updateRevComp(boolean revComp) {
    mRevComp = revComp;

    SettingsService.getInstance().update("org.matcalc.toolbox.bio.seqlogo.rev-comp", revComp);
  }

  public boolean getRevComp() {
    return mRevComp;
  }
}
