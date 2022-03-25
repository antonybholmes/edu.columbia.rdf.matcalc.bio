package edu.columbia.rdf.matcalc.bio.toolbox.probes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.modern.combobox.ModernComboBox;
import org.jebtk.modern.io.GzGuiFileFilter;

public class ArrayCombo extends ModernComboBox {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private List<Path> mFiles;

  public ArrayCombo() {
    try {
      mFiles = FileUtils.ls(ProbesModule.DIR, new GzGuiFileFilter());

      for (Path file : mFiles) {
        addMenuItem(PathUtils.getName(file).substring(0,
            PathUtils.getName(file).length() - 8));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Path getFile() {
    return mFiles.get(getSelectedIndex());
  }

}
