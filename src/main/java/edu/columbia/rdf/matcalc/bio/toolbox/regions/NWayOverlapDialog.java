package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.nio.file.Path;
import java.util.List;

import javax.swing.Box;

import org.jebtk.bioinformatics.ui.external.ucsc.BedGraphGuiFileFilter;
import org.jebtk.bioinformatics.ui.external.ucsc.BedGuiFileFilter;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.math.ui.external.microsoft.ExcelGuiFileFilter;
import org.jebtk.modern.BorderService;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.io.ChooseFilesPanel;
import org.jebtk.modern.io.CsvGuiFileFilter;
import org.jebtk.modern.io.TsvGuiFileFilter;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes
 *
 */
public class NWayOverlapDialog extends ModernDialogHelpWindow implements ModernClickListener {
  private static final long serialVersionUID = 1L;

  private ModernRadioButton mCheckOneWay = new ModernRadioButton("One way", true);

  private ModernRadioButton mCheckTwoWay = new ModernRadioButton("Two way");

  private ModernTwoStateWidget mCheckVenn = new ModernCheckSwitch("Show Venn diagram");

  private ModernTwoStateWidget mCheckBeginning = new ModernCheckSwitch("At beginning", true);

  private ChooseFilesPanel mChooseFilesPanel;

  public NWayOverlapDialog(MainMatCalcWindow parent) {
    super(parent, "org.matcalc.toolbox.bio.regions.nway.help.url");

    setTitle("Overlap");

    mChooseFilesPanel = new ChooseFilesPanel(parent, new AllRegionGuiFileFilter(), BedGuiFileFilter.INSTANCE,
        BedGraphGuiFileFilter.INSTANCE, CsvGuiFileFilter.INSTANCE, TsvGuiFileFilter.INSTANCE,
        ExcelGuiFileFilter.INSTANCE);

    setup();

    createUi();

    // Add the existing file from the current window as a convenience
    if (parent.getInputFile() != null) {
      mChooseFilesPanel.addFile(parent.getInputFile());
    }
  }

  private void setup() {
    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    new ModernButtonGroup(mCheckOneWay, mCheckTwoWay);

    if (SettingsService.getInstance().getBool("org.matcalc.toolbox.bio.regions.nway.one-way", true)) {
      mCheckOneWay.doClick();
    } else {
      mCheckTwoWay.doClick();
    }

    setSize(520, 520);

    UI.centerWindowToScreen(this);
  }

  private final void createUi() {
    ModernComponent content = new ModernComponent();

    Box box = VBox.create();
    // midSectionHeader("Options", box);
    box.add(mCheckOneWay);
    // box.add(UI.createVGap(5));
    box.add(mCheckTwoWay);
    mCheckVenn.setBorder(BorderService.getInstance().createLeftBorder(10));
    box.add(mCheckVenn);
    box.add(mCheckBeginning);

    content.setFooter(box);

    content.setBody(mChooseFilesPanel);

    setCard(content);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    if (e.getSource().equals(mOkButton)) {
      if (getFiles().size() == 0) {
        ModernMessageDialog.createWarningDialog(mParent, "You must choose at least one file.");

        return;
      }

      SettingsService.getInstance().update("org.matcalc.toolbox.bio.regions.nway.one-way", mCheckOneWay.isSelected());
    }

    super.clicked(e);
  }

  public List<Path> getFiles() {
    return mChooseFilesPanel.getFiles();
  }

  /**
   * Returns true if one way overlap is selected.
   * 
   * @return
   */
  public boolean getOneWay() {
    return mCheckOneWay.isSelected();
  }

  public boolean getDrawVenn() {
    return mCheckVenn.isSelected();
  }

  public boolean getAtBeginning() {
    return mCheckBeginning.isSelected();
  }
}
