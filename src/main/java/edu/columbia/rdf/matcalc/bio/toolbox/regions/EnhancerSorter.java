package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.tree.TreeNode;
import org.jebtk.core.tree.TreeRootNode;
import org.jebtk.modern.search.Sorter;
import org.jebtk.modern.tree.ModernTree;

/**
 * The experiment tree can be sorted in multiple ways. Given a list of
 * experiments and a tree, generate a custom sorted tree of experiments.
 *
 * @author Antony Holmes
 *
 */
public abstract class EnhancerSorter extends Sorter<Enhancer> {
  @Override
  public String getType() {
    return "Common Properties";
  }

  protected static <T extends Comparable<? super T>> void arrange(ModernTree<Enhancer> tree, Map<T, Set<Enhancer>> map,
      boolean ascending) {
    List<T> sortedNames = CollectionUtils.sortKeys(map, ascending);

    tree.clear();

    TreeRootNode<Enhancer> root = new TreeRootNode<Enhancer>();

    for (T array : sortedNames) {
      TreeNode<Enhancer> node = new TreeNode<Enhancer>(array.toString());

      List<Enhancer> sortedChipSeqSamples = sortByName(map.get(array), ascending);

      for (Enhancer ChipSeqSample : sortedChipSeqSamples) {
        node.addChild(new TreeNode<Enhancer>(ChipSeqSample.getName(), ChipSeqSample));
      }

      root.addChild(node);
    }

    tree.setRoot(root);

    // tree.getSelectionModel().setSelection(1);
  }

}
