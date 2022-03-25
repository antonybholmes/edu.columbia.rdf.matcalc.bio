package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;

import org.jebtk.bioinformatics.Bio;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.bioinformatics.genomic.SequenceService;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.IterTreeMap;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernTwoStateWidget;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.text.ModernLabelBold;
import org.jebtk.modern.text.ModernSubHeadingLabel;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes
 *
 */
public class GenomeSidePanel extends ModernComponent {
  private static final long serialVersionUID = 1L;

  private Map<CheckBox, SequenceReader> mCheckMap = new HashMap<CheckBox, SequenceReader>();

  public GenomeSidePanel(Genome genome) {
    setHeader(
        new ModernSubHeadingLabel(Bio.ASSET_GENOMES).bottomBorder(10));

    IterMap<Genome, SequenceReader> assemblyMap = 
        new IterTreeMap<Genome, SequenceReader>();

    /*
     * for (GenomeAssembly a : SequenceService.getInstance()) { try { for (String
     * genome : a.getGenomes()) { assemblyMap.put(genome, a); } } catch
     * (IOException e) { e.printStackTrace(); } }
     */

    for (Entry<Genome, SequenceReader> item : SequenceService.getInstance()) {
      assemblyMap.put(item.getKey(), item.getValue());
    }

    Box box = VBox.create();

    //ModernButtonGroup bg = new ModernButtonGroup();

    //boolean first = true;
    
    System.err.println(genome);
    
    IterMap<String, IterMap<String, Genome>> sorted = Genome.sortByAssembly(assemblyMap.keySet());

    // If two services provide the same genome, use the later.
    
    for (Entry<String, IterMap<String, Genome>> sortedEntry : sorted) {
      box.add(new ModernLabelBold(sortedEntry.getKey()));
      
      for (Entry<String, Genome> genomeEntry : sortedEntry.getValue()) {
        CheckBox checkBox = new ModernCheckSwitch(genomeEntry.getKey(), 
            genomeEntry.getValue().equals(genome));
        
        System.err.println(genome + " " + genomeEntry.getValue());
        
        mCheckMap.put(checkBox, assemblyMap.get(genomeEntry.getValue()));
        //bg.add(checkBox);
        box.add(checkBox);
      }
      
      box.add(UI.createVGap(20));
  }

    setBody(new ModernScrollPane(box)
        .setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER));
    setBorder(DOUBLE_BORDER);
  }

  

  public List<SequenceReader> getAssemblies() {
    List<SequenceReader> ret = new ArrayList<SequenceReader>(mCheckMap.size());

    for (ModernTwoStateWidget button : mCheckMap.keySet()) {
      if (button.isSelected()) {
        ret.add(mCheckMap.get(button));
      }
    }

    return ret;
  }

  public SequenceReader getAssembly() {
    List<SequenceReader> assemblies = getAssemblies();

    if (assemblies.size() > 0) {
      return assemblies.get(0);
    } else {
      return null;
    }
  }

  public List<String> getGenomes() {
    List<String> ret = new ArrayList<String>(mCheckMap.size());

    for (CheckBox button : mCheckMap.keySet()) {
      if (button.isSelected()) {
        ret.add(button.getText());
      }
    }

    return ret;
  }

  /**
   * Returns the first genome selected or null if there is no selection.
   * 
   * @return
   */
  public String getGenome() {
    List<String> genomes = getGenomes();

    if (genomes.size() > 0) {
      return genomes.get(0);
    } else {
      return null;
    }
  }
}
