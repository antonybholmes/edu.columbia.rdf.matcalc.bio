package edu.columbia.rdf.matcalc.bio.toolbox.conversion;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;

import org.jebtk.core.text.TextUtils;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.panel.VBox;

/**
 * List available annotations organized by genome that a user can select from.
 * 
 * @author Antony Holmes
 *
 */
public class DatabaseSidePanel extends ModernComponent {
  private static final long serialVersionUID = 1L;

  //private CheckBox mSelectAllButton = new ModernCheckSwitch("Select All");

  private Map<ModernRadioButton, String> mCheckMap = 
      new HashMap<ModernRadioButton, String>();

  public DatabaseSidePanel() {

    Box box = VBox.create();

    ModernDialogTaskWindow.sectionHeader("Version", box);

    ModernButtonGroup group = new ModernButtonGroup();
    
    ModernRadioButton latest = null;
    
    // If two services provide the same genome, use the later.
    try {
      for (String v : GenesService.getInstance().versions()) {

        ModernRadioButton button = new ModernRadioButton(v);
        //button.setBorder(LEFT_BORDER);
        mCheckMap.put(button, v);
        box.add(button);
        group.add(button);
        latest = button;
        
        box.add(UI.createVGap(5));
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    latest.doClick();

    // box.setBorder(BORDER);

    setBody(box); //new ModernScrollPane(box).setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER));

    setBorder(DOUBLE_BORDER);
  }

  public String getVersion() {
    for (ModernRadioButton button : mCheckMap.keySet()) {
      if (button.isSelected()) {
        return mCheckMap.get(button);
      }
    }
    
    return TextUtils.EMPTY_STRING;
  }
}
