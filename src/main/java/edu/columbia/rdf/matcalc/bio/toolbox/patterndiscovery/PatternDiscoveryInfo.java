package edu.columbia.rdf.matcalc.bio.toolbox.patterndiscovery;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class PatternDiscoveryInfo extends GuiAppInfo {

  public PatternDiscoveryInfo() {
    super("Pattern Discovery", new AppVersion(1),
        "Copyright (C) 2016-${year} Antony Holmes",
        AssetService.getInstance().loadIcon(PatternDiscoveryIcon.class, 128));
  }

}
