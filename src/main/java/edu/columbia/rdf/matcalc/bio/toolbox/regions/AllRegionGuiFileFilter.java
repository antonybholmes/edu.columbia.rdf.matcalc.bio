package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import org.jebtk.modern.io.GuiFileExtFilter;

public class AllRegionGuiFileFilter extends GuiFileExtFilter {
  public AllRegionGuiFileFilter() {
    super("csv", "txt", "xls", "xlsx", "bed", "bedgraph");
  }

  @Override
  public final String getDescription() {
    return "All Region Files (*.bed;*.bedgraph;*.csv;*.txt;*.xls;*.xlsx)";
  }
}
