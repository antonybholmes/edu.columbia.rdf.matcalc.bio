package edu.columbia.rdf.matcalc.bio.toolbox.violin;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.help.GuiAppInfo;

public class ViolinInfo extends GuiAppInfo {

  public ViolinInfo() {
    super("Violin", new AppVersion(1),
        "Copyright (C) ${year} Antony Holmes",
        new ViolinIcon());
  }

}
