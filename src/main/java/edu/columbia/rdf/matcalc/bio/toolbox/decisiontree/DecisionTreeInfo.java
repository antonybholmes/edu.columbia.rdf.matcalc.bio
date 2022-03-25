package edu.columbia.rdf.matcalc.bio.toolbox.decisiontree;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class DecisionTreeInfo extends GuiAppInfo {

  public DecisionTreeInfo() {
    super("Decision Tree", new AppVersion(1),
        "Copyright (C) ${year} Antony Holmes",
        AssetService.getInstance().loadIcon(DecisionTreeIcon.class, 128));
  }

}
