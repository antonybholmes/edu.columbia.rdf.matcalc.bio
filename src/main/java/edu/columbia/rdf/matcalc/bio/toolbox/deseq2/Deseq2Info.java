package edu.columbia.rdf.matcalc.bio.toolbox.deseq2;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class Deseq2Info extends GuiAppInfo {

  public Deseq2Info() {
    super("DESeq2", new AppVersion(1), "Copyright (C) ${year} Antony Holmes",
        AssetService.getInstance().loadIcon(Deseq2Icon.class, 128));
  }

}
