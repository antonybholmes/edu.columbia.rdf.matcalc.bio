package edu.columbia.rdf.matcalc.bio.toolbox.gep;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class GepInfo extends GuiAppInfo {

  public GepInfo() {
    super("GEP", new AppVersion(1), "Copyright (C) ${year} Antony Holmes",
        AssetService.getInstance().loadIcon(GepIcon.class, 128),
        "Download GEP expression from experiments database.");
  }

}
