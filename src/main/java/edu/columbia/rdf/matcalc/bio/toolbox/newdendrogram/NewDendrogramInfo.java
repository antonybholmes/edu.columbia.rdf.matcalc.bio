package edu.columbia.rdf.matcalc.bio.toolbox.newdendrogram;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class NewDendrogramInfo extends GuiAppInfo {

  public NewDendrogramInfo() {
    super("NewDendrogram", new AppVersion(2),
        "Copyright (C) 2016-${year} Antony Holmes",
        AssetService.getInstance().loadIcon(NewDendrogramIcon.class, 128));
  }

  // UIService.getInstance().loadIcon("newdendrogram", 32)

}
