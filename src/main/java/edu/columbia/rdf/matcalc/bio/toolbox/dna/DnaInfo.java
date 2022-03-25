package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class DnaInfo extends GuiAppInfo {

  public DnaInfo() {
    super("DNA", new AppVersion(3), "Copyright (C) 2014-2016 Antony Holmes",
        AssetService.getInstance().loadIcon(DnaIcon.class, 128),
        "Extract DNA sequences.");
  }

}
