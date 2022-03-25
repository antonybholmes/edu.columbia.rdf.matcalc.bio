package edu.columbia.rdf.matcalc.bio.toolbox.regions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jebtk.core.tree.TreeNode;
import org.jebtk.core.tree.TreeRootNode;
import org.jebtk.modern.search.FilterModel;
import org.jebtk.modern.tree.ModernTree;

public class SortSamplesByName extends EnhancerSorter {
  public void arrange(Collection<Enhancer> enhancers, ModernTree<Enhancer> tree, boolean ascending,
      FilterModel filterModel) {
    List<Enhancer> sortedSamples = sortByName(enhancers, ascending);

    tree.clear();

    TreeRootNode<Enhancer> root = new TreeRootNode<Enhancer>();

    for (Enhancer enhancer : sortedSamples) {
      if (!filterModel.keep(enhancer.getName())) {
        continue;
      }

      TreeNode<Enhancer> node = new TreeNode<Enhancer>(enhancer.getName(), enhancer);

      root.addChild(node);
    }

    tree.setRoot(root);
  }

  @Override
  public void filter(Collection<Enhancer> samples, FilterModel filterModel) {
    super.filter(samples, filterModel);

    Set<String> names = new TreeSet<String>();

    for (Enhancer sample : samples) {
      names.add(sample.getName());
    }

    addFilterNames(names, filterModel);
  }

  public final String getName() {
    return "Name";
  }
}
