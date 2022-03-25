package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;

import org.jebtk.core.collections.IterHashMap;
import org.jebtk.core.collections.IterMap;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.dialog.ModernDialogTaskWindow;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

public class PlotDialog extends ModernDialogTaskWindow {
  private static final long serialVersionUID = 1L;

  // private ModernCheckBox mCheckPeakWidth =
  // new ModernCheckBox("Peak widths only");

  private CheckBox mCheckAll = new ModernCheckSwitch("All", true);

  private IterMap<String, ModernTwoStateWidget> mSelectedMap = new IterHashMap<String, ModernTwoStateWidget>();

  private final List<String> mMutations;

  public PlotDialog(ModernWindow parent, List<String> mutations) {
    super(parent);

    mMutations = mutations;

    setTitle("Select Motifs");

    createUi();

    addWindowListener(new WindowWidgetFocusEvents(mOkButton));

    setResizable(true);

    setSize(480, 360);

    UI.centerWindowToScreen(this);

    mCheckAll.addClickListener((ModernClickEvent e) -> {
      for (Entry<String, ModernTwoStateWidget> item : mSelectedMap) {
        item.getValue().setSelected(mCheckAll.isSelected());
      }
    });

  }

  public void createUi() {

    Box box = new VBox();

    box.add(mCheckAll);

    for (String mutation : mMutations) {
      CheckBox check = new ModernCheckSwitch(mutation, true);

      box.add(check);

      mSelectedMap.put(mutation, check);
    }

    ModernScrollPane scrollPane = new ModernScrollPane(box).setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER);

    setCard(scrollPane);
  }

  public List<String> getMutations() {
    List<String> ret = new ArrayList<>(mSelectedMap.size());

    for (String m : mMutations) {
      if (mSelectedMap.get(m).isSelected()) {
        ret.add(m);
      }
    }

    return ret;
  }

}
