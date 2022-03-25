package edu.columbia.rdf.matcalc.bio.toolbox.motifs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;

import org.jebtk.graphplot.figure.Axes;
import org.jebtk.modern.ModernWidget;
import org.jebtk.modern.collapsepane.ModernCollapsePane;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.tabs.TabsModel;
import org.jebtk.modern.tabs.TabsViewPanel;
import org.jebtk.modern.tabs.TextTabs;
import org.jebtk.modern.window.ModernWindow;

import edu.columbia.rdf.matcalc.figure.AxisControl;

public class FormatPanel extends ModernWidget {
  private static final long serialVersionUID = 1L;

  public FormatPanel(ModernWindow parent, Axes gp) {

    TabsModel groupTabsModel = new TabsModel();
    TextTabs groupTabs = new TextTabs(groupTabsModel);

    ModernCollapsePane rightPanel;

    Box box;

    Component element;

    //
    // Heat map
    //

    rightPanel = new ModernCollapsePane();

    box = Box.createVerticalBox();

    element = new AxisControl(parent, gp.getX1Axis(), true);

    box.add(element);

    box.add(createVGap());

    rightPanel.addTab("X Axis", box, true);

    box = Box.createVerticalBox();

    element = new AxisControl(parent, gp.getY1Axis(), true);

    box.add(element);

    box.add(createVGap());

    rightPanel.addTab("Y Axis", box, true);

    groupTabsModel.addTab("Axes", new ModernScrollPane(rightPanel));

    add(groupTabs, BorderLayout.PAGE_START);

    TabsViewPanel viewPanel = new TabsViewPanel(groupTabsModel);
    viewPanel.setBorder(BORDER);
    add(viewPanel, BorderLayout.CENTER);

    groupTabsModel.changeTab(0);
  }
}
