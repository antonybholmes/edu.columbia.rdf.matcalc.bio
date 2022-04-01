package edu.columbia.rdf.matcalc.bio.toolbox.newdendrogram;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.jebtk.core.Indexed;
import org.jebtk.core.IndexedInt;
import org.jebtk.math.MathUtils;
import org.jebtk.math.cluster.DistanceMetric;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.utils.MatrixOperation;
import org.jebtk.math.matrix.utils.MatrixOperations;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;
import edu.columbia.rdf.matcalc.toolbox.plot.heatmap.HeatMapProps;
import edu.columbia.rdf.matcalc.toolbox.plot.heatmap.cluster.legacy.LegacyClusterModule;
import org.jebtk.math.cluster.ILinkage;

public class NewDendrogramModule extends Module
    implements ModernClickListener {
  private MainMatCalcWindow mWindow;

  public static final String NAME = "NewDendrogram";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    RibbonLargeButton button = new RibbonLargeButton(NAME,
        AssetService.getInstance().loadIcon(NewDendrogramIcon.class, 24), NAME,
        "Cluster rows and columns using the New Dendrogam method.");
    button.addClickListener(this);
    mWindow.getRibbon().getToolbar("Classification").getSection("Classifier")
        .add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    try {
      newDendrogram();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ParseException e1) {
      e1.printStackTrace();
    }
  }

  private void newDendrogram() throws IOException, ParseException {
    if (mWindow.getHistoryPanel().getItemCount() == 0) {
      return;
    }

    NewDendrogramDialog dialog = new NewDendrogramDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    DistanceMetric distanceMetric = dialog.getDistanceMetric();

    ILinkage linkage = dialog.getLinkage();

    double minStd = dialog.getMinStd();

    double minExp = dialog.getMinExp();

    if (dialog.getReset()) {
      mWindow.resetHistory();
    }

    DataFrame m = mWindow.getCurrentMatrix();

    DataFrame minM;

    if (dialog.getUseMinExp()) {
      minM = mWindow.history().addToHistory("Minimum expression",
          Double.toString(minExp),
          MatrixOperations.min(m, minExp)); // new
                                            // MinThresholdMatrixView(m,
                                            // minExp));
    } else {
      minM = m;
    }

    DataFrame log2M;

    /*
     * switch (dialog.getIsLogTransformed()) { case 1: log2M =
     * mWindow.history().addToHistory("Log 2 transform",
     * MatrixOperation.transform().min(1).log2().to(minM)); break; case 2: log2M
     * = mWindow.history().addToHistory("Log 10 transform",
     * MatrixOperation.transform().min(1).log10().to(minM)); default: log2M =
     * minM; break; }
     */

    switch (dialog.getIsLogTransformed()) {
    case 1:
      log2M = mWindow.history().addToHistory("Log 2 transform",
          MatrixOperation.transform().log2().to(minM));
      break;
    case 2:
      log2M = mWindow.history().addToHistory("Log 10 transform",
          MatrixOperation.transform().log10().to(minM));
      break;
    default:
      log2M = minM;
      break;
    }

    List<Double> sd = MatrixOperations.rowStdev(log2M);

    // Index the sd
    List<Indexed<Integer, Double>> sdIndexed = IndexedInt.index(sd);

    // Sort by stdev
    // Collections.sort(sdIndexed);

    // Sort the maxtrix rows
    DataFrame stdM = DataFrame.copyInnerRowsIndexed(log2M, sdIndexed);

    sd = Indexed.values(sdIndexed);

    // Reindex
    // sdIndexed = IndexedValueInt.index(sd);

    // Add the stdev as annotation
    stdM.getIndex().setAnnotation("STDEV", sd.toArray());

    mWindow.history().addToHistory("STDEV", stdM);

    // Filter by min exp
    sdIndexed = MathUtils.min(sdIndexed, minStd);

    if (sdIndexed.size() > 0) {

      DataFrame stdevFilterM = mWindow.history().addToHistory("Keep STDEV >= " + minStd,
          DataFrame.copyInnerRowsIndexed(stdM, sdIndexed)); // new
                                                            // StdDevFilterMatrixView(mlog2,
                                                            // minStd));

      DataFrame rowZTransM = mWindow.history().addToHistory("Row z-score transform",
          MatrixOperations.rowZscore(stdevFilterM)); // new
                                                     // RowZTransformMatrixView(mstdevfilter));

      boolean plot = dialog.getCreatePlot();

      if (plot) {
        LegacyClusterModule.cluster(mWindow,
            rowZTransM,
            distanceMetric,
            linkage,
            dialog.clusterRows(),
            dialog.clusterColumns(),
            dialog.optimalLeafOrder(),
            new HeatMapProps());
      }

      mWindow.history().addToHistory("Results", stdevFilterM);
    } else {
      ModernMessageDialog.createWarningDialog(mWindow,
          "The matrix is empty after filtering.");
    }
  }
}
