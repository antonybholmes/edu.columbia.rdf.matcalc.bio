package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.help.GuiAppInfo;

public class MotifsInfo extends GuiAppInfo {

  public MotifsInfo() {
    super("Motifs", new AppVersion(3), "Copyright (C) 2014-${year} Antony Holmes", new MotifsIcon(),
        "Search for motifs.");
  }
}
