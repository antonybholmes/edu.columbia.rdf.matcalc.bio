/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.MessageDialogType;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.tooltip.ModernToolTip;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.Module;

// TODO: Auto-generated Javadoc
/**
 * Merges designated segments together using the merge column. Consecutive rows
 * with the same merge id will be merged together. Coordinates and copy number
 * will be adjusted but genes, cytobands etc are not.
 *
 * @author Antony Holmes
 *
 */
public class ConversionModule extends Module 
implements ModernClickListener {

  // private static final Path MOUSE_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.mouse-human-conversion");

  // private static final Path REFSEQ_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human-refseq");
  // //"res/ucsc_refseq_hg19_20160627.txt.gz";

  // public static final Path ENSEMBL_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human.ensembl");
  // //"res/ucsc_ensembl_hg19_20160627.txt.gz";

  // private static final Path NCBI_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human-ncbi");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path NCBI_GENE_INFO_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human.ncbi-gene-info");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path NCBI_GENE_HISTORY_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human.ncbi-gene-history");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path NCBI_GENE_REFSEQ_HUMAN_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human.ncbi-gene-refseq");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path HUGO_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.human.hugo");
  // //"res/hugo_symbol_entrez_20160608.txt.gz";

  // private static final Path NCBI_GENE_INFO_MOUSE_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.mouse.ncbi-gene-info");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path NCBI_GENE_HISTORY_MOUSE_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.mouse.ncbi-gene-history");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  // private static final Path NCBI_GENE_REFSEQ_MOUSE_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.mouse.ncbi-gene-refseq");
  // //"res/ncbi_grch37_hg19_p10_entrez_refseq_symbol_20160609.txt.gz";

  /** The Constant ENSEMBL_MOUSE_FILE. */
  public static final Path ENSEMBL_MOUSE_FILE = SettingsService.getInstance()
      .getFile("org.matcalc.toolbox.bio.genes.files.mouse.ensembl"); // "res/ucsc_ensembl_mm10_20160627.txt.gz";

  /** The Constant ARROW. */
  private static final String ARROW = " > ";

  // private static final Path REFSEQ_MOUSE_FILE =
  // SettingsService.getInstance().getFile("org.matcalc.toolbox.bio.genes.files.mouse-refseq");
  // //"res/ucsc_refseq_mm10_20160627.txt.gz";

  /**
   * The member convert button.
   */
  private RibbonLargeButton mConvertButton = new RibbonLargeButton("Convert",
      AssetService.getInstance().loadIcon(RunVectorIcon.class, 24));

  /**
   * The member window.
   */
  private MainMatCalcWindow mWindow;

  /*
   * (non-Javadoc)
   * 
   * @see org.abh.lib.NameProperty#getName()
   */
  @Override
  public String getName() {
    return "Genes";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.columbia.rdf.apps.matcalc.modules.Module#init(edu.columbia.rdf.apps.
   * matcalc.MainMatCalcWindow)
   */
  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    // home
    mConvertButton.setToolTip(
        new ModernToolTip("Convert", "Append gene conversions."));
    mConvertButton.setClickMessage("Append");
    mWindow.getRibbon().getToolbar("Genomic").getSection("Annotation")
    .add(mConvertButton);

    mConvertButton.addClickListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.abh.lib.ui.modern.event.ModernClickListener#clicked(org.abh.lib.ui.
   * modern .event.ModernClickEvent)
   */
  @Override
  public final void clicked(ModernClickEvent e) {
    if (e.getSource().equals(mConvertButton)) {
      try {
        convert();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }

  /**
   * Convert.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void convert() throws IOException {
    int col = mWindow.getSelectedColumn();

    DataFrame m = mWindow.getCurrentMatrix();

    if (col == Integer.MIN_VALUE) {
      // Try and find a column called gene
      col = DataFrame.findColumn(m, "Gene");
    }

    if (col == -1) { //Integer.MIN_VALUE) {
      ModernMessageDialog.createDialog(mWindow,
          "You must select a column of gene ids or symbols.",
          MessageDialogType.WARNING);

      return;
    }

    int chrCol = DataFrame.findColumn(m, "location", "chr");

    // map using lowercase to make matching easier
    String[] ids = m.columnToText(col);

    GenesDialog dialog = new GenesDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }
    
    GenesService.getInstance().setVersion(dialog.getVersion());

    GenesMap humanMap = GenesService.getInstance().getHumanMap();
    GenesMap mouseMap = GenesService.getInstance().getMouseMap();

    int c = m.getCols();

    if (dialog.getOutputSymbols()) {
      ++c;
    }

    if (dialog.getOutputEntrez()) {
      ++c;
    }

    if (dialog.getOutputRefSeq()) {
      ++c;
    }

    if (dialog.getOutputEnsemblGene()) {
      ++c;
    }

    if (dialog.getOutputEnsemblTranscript()) {
      ++c;
    }

    if (dialog.getOutputChr()) {
      ++c;
    }

    if (dialog.getOutputStrand()) {
      ++c;
    }

    if (dialog.getOutputConversions()) {
      ++c;
    }
    
    if (dialog.getOutputParams()) {
      ++c;
    }
    
    if (dialog.getShowAlt()) {
      ++c;
    }

    DataFrame ret = DataFrame.createDataFrame(m.getRows(), c);

    DataFrame.copy(m, ret);

    c = m.getCols();

    String prefix;

    if (dialog.getConvToMouse()) {
      prefix = "Mouse";
    } else {
      prefix = "Human";
    }
    
    System.err.println("huh " + prefix);

    if (dialog.getOutputSymbols()) {
      ret.setColumnName(c++, prefix + " Gene Symbol");
    }
    
    if (dialog.getShowAlt()) {
      ret.setColumnName(c++, prefix + " Alt");
    }

    if (dialog.getOutputEntrez()) {
      ret.setColumnName(c++, prefix + " Entrez");
    }

    if (dialog.getOutputRefSeq()) {
      ret.setColumnName(c++, prefix + " RefSeq");
    }

    if (dialog.getOutputEnsemblGene()) {
      ret.setColumnName(c++, prefix + " Ensembl Gene");
    }

    if (dialog.getOutputEnsemblTranscript()) {
      ret.setColumnName(c++, prefix + " Ensembl Transcript");
    }

    if (dialog.getOutputChr()) {
      ret.setColumnName(c++, prefix + " Chromosome");
    }

    if (dialog.getOutputStrand()) {
      ret.setColumnName(c++, prefix + " Strand");
    }

    if (dialog.getOutputConversions()) {
      ret.setColumnName(c++, prefix + " Conversions");
    }
    
    if (dialog.getOutputParams()) {
      ret.setColumnName(c++, "Conversion Parameters");
    }

    for (int i = 0; i < ret.getRows(); ++i) {
      String id = ids[i];
      String chr = null;

      if (chrCol != -1) {
        Matcher matcher = Chromosome.CHR_REGEX.matcher(m.getText(i, chrCol));

        if (matcher.find()) {
          chr = matcher.group(1);
        }
      }

      Conversion fromId = new Conversion(id, chr, "in:" + id);

      Set<Conversion> toEntrezes = new TreeSet<Conversion>();

      if (dialog.getConvFromHuman() == dialog.getConvToHuman()) {
        // We are not converting between species

        if (dialog.getConvFromHuman()) {
          // Human

          /*
           * convert(id, dialog.getSplit(), idToSymbolHumanMap,
           * oldIdToSymbolHumanMap, symbolChrHumanMap, symbols);
           */

          GenesService.getInstance().getHumanMap()
          .convert(fromId, dialog.getSplit(), toEntrezes);

        } else {
          // Mouse
          /*
           * convert(id, dialog.getSplit(), idToSymbolMouseMap, null,
           * symbolChrMouseMap, symbols);
           */

          GenesService.getInstance().getMouseMap()
          .convert(fromId, dialog.getSplit(), toEntrezes);
        }
      } else {
        // Converting between mouse and human

        if (dialog.getConvFromHuman()) {
          // To mouse
          /*
           * convertBetweenSpecies(id, dialog.getSplit(), humanMouseMap,
           * idToSymbolHumanMap, oldIdToSymbolHumanMap, symbolChrHumanMap,
           * idToSymbolMouseMap, oldIdToSymbolMouseMap, symbolChrMouseMap,
           * symbols);
           */

          convertBetweenSpecies(fromId,
              dialog.getSplit(),
              GenesService.getInstance().getHumanMap(),
              GenesService.getInstance().getMouseMap(),
              GenesService.getInstance().getHomologyMap().humanToMouse(),
              toEntrezes);
        } else {
          // To human
          /*
           * convertBetweenSpecies(id, dialog.getSplit(), mouseHumanMap,
           * idToSymbolMouseMap, oldIdToSymbolMouseMap, symbolChrMouseMap,
           * idToSymbolHumanMap, oldIdToSymbolHumanMap, symbolChrHumanMap,
           * symbols);
           */

          convertBetweenSpecies(fromId,
              dialog.getSplit(),
              GenesService.getInstance().getMouseMap(),
              GenesService.getInstance().getHumanMap(),
              GenesService.getInstance().getHomologyMap().mouseToHuman(),
              toEntrezes);
        }
      }

      c = m.getCols();

      if (dialog.getOutputSymbols()) {
        String o = TextUtils.NA;

        // If updating species, keep old name if we can't find a new name
        if (dialog.getConvFromHuman() == dialog.getConvToHuman()) {
          if (dialog.getKeepOld()) {
            o = id;
          }
        }

        if (toEntrezes.size() > 0) {
          o = mapping(humanMap,
              mouseMap,
              toEntrezes,
              GenesMap.SYMBOL_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, o);
      }
      
      if (dialog.getShowAlt()) {
        String alt = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          alt = altMapping(humanMap,
              mouseMap,
              toEntrezes,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, alt);
      }

      if (dialog.getOutputEntrez()) {
        String o = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion entrez = toEntrezes.iterator().next();

          o = mapping(humanMap,
              mouseMap,
              entrez,
              GenesMap.ENTREZ_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, o);
      }

      if (dialog.getOutputRefSeq()) {
        String refSeq = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion symbol = toEntrezes.iterator().next();

          refSeq = mapping(humanMap,
              mouseMap,
              symbol,
              GenesMap.REFSEQ_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, refSeq);
      }

      if (dialog.getOutputEnsemblGene()) {
        String ensembl = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion symbol = toEntrezes.iterator().next();

          ensembl = mapping(humanMap,
              mouseMap,
              symbol,
              GenesMap.ENSEMBL_GENE_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, ensembl);
      }

      if (dialog.getOutputEnsemblTranscript()) {
        String ensembl = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion symbol = toEntrezes.iterator().next();

          ensembl = mapping(humanMap,
              mouseMap,
              symbol,
              GenesMap.ENSEMBL_TRANSCRIPT_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, ensembl);
      }

      if (dialog.getOutputChr()) {
        chr = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion symbol = toEntrezes.iterator().next();

          chr = mapping(humanMap,
              mouseMap,
              symbol,
              GenesMap.CHR_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, chr);
      }

      if (dialog.getOutputStrand()) {
        String strand = TextUtils.NA;

        if (toEntrezes.size() > 0) {
          Conversion symbol = toEntrezes.iterator().next();

          strand = mapping(humanMap,
              mouseMap,
              symbol,
              GenesMap.STRAND_TYPE,
              dialog.getConvToHuman());
        }

        ret.set(i, c++, strand);
      }

      if (dialog.getOutputConversions()) {
        if (toEntrezes.size() > 0) {
          ret.set(i, c++, conversions(toEntrezes)); // convert(symbols,
          // officialSymbolHumanMap));
        } else {
          ret.set(i, c++, TextUtils.NA);
        }
      }
      
      if (dialog.getOutputParams()) {
        ret.set(i, c++, props(humanMap, mouseMap,  dialog.getConvFromHuman(), dialog.getConvToHuman()));
      }
      
    }

    mWindow.history().addToHistory("Gene Conversion", ret);
  }

  /**
   * Convert.
   *
   * @param humanMap the human map
   * @param mouseMap the mouse map
   * @param symbol the symbol
   * @param type the type
   * @param convToHuman the conv to human
   * @return the string
   */
  private static String mapping(GenesMap humanMap,
      GenesMap mouseMap,
      Conversion symbol,
      String type,
      boolean convToHuman) {
    String ret;

    if (convToHuman) {
      ret = format(humanMap.getMappings(symbol, type)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptHumanMap);
    } else {
      ret = format(mouseMap.getMappings(symbol, type)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptMouseMap);
    }

    return ret;
  }

  /**
   * Convert.
   *
   * @param humanMap the human map
   * @param mouseMap the mouse map
   * @param symbols the symbols
   * @param type the type
   * @param convToHuman the conv to human
   * @return the string
   */
  private static String mapping(GenesMap humanMap,
      GenesMap mouseMap,
      Set<Conversion> symbols,
      String type,
      boolean convToHuman) {
    String ret;

    if (convToHuman) {
      ret = format(humanMap.getMappings(symbols, type)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptHumanMap);
    } else {
      ret = format(mouseMap.getMappings(symbols, type)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptMouseMap);
    }

    return ret;
  }
  
  private static String altMapping(GenesMap humanMap,
      GenesMap mouseMap,
      Set<Conversion> symbols,
      boolean convToHuman) {
    String ret;

    if (convToHuman) {
      ret = format(humanMap.getAltMappings(symbols)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptHumanMap);
    } else {
      ret = format(mouseMap.getAltMappings(symbols)); // formatSymbolToIds(symbol,
      // symbolEnsemblTranscriptMouseMap);
    }

    return ret;
  }

  /**
   * Conversions.
   *
   * @param symbols the symbols
   * @return the string
   */
  private static String conversions(Set<Conversion> symbols) {
    List<String> ret = new ArrayList<String>();

    for (Conversion c : symbols) {
      Conversion parent = c;

      List<String> types = new ArrayList<String>();

      while (parent != null) {
        types.add(parent.getType());

        parent = parent.getParent();
      }

      Collections.reverse(types);

      ret.add(Join.on(ARROW).values(types).toString());
    }

    return Join.onSemiColon().values(ret).toString();
  }
  
  private String props(GenesMap humanMap,
      GenesMap mouseMap,
      boolean fromHuman, 
      boolean toHuman) {
    List<String> ret = new ArrayList<String>();

    if (fromHuman == toHuman) {
      if (fromHuman) {
        ret.add("human:" + humanMap.getName());
      } else {
        ret.add("mouse:" + mouseMap.getName());
      }
    } else {
      ret.add("human:" + humanMap.getName());
      ret.add("mouse:" + mouseMap.getName());
    }

    return Join.onSemiColon().values(ret).toString();
  }

  /**
   * Loads a HUGO file cope with new symbols and mappings between old and new
   * symbols.
   *
   * @param symbols the symbols
   * @return the string
   */
  /*
   * private static void loadHugoMap(final Path file, Map<String, String>
   * idToSymbolMap, Map<String, String> oldIdToSymbolMap, Map<String, String>
   * officalMap) throws IOException { String line; List<String> tokens;
   * 
   * String symbol; String ls;
   * 
   * BufferedReader reader = Resources.getGzipReader(file);
   * 
   * Splitter splitter = Splitter.onTab();
   * 
   * Splitter splitter2 = Splitter.on(";");
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * symbol = tokens.get(1); ls = symbol.toLowerCase();
   * 
   * idToSymbolMap.put(ls, ls);
   * 
   * officalMap.put(ls, symbol);
   * 
   * for (String entrez : splitter2.text(tokens.get(5))) { if
   * (!entrez.equals(TextUtils.NA)) { idToSymbolMap.put(entrez, ls); } }
   * 
   * for (String refseq : splitter2.text(tokens.get(6))) { if
   * (!refseq.equals(TextUtils.NA)) { idToSymbolMap.put(refseq, ls); } }
   * 
   * // // Deal with older mappings //
   * 
   * for (String previous :
   * TextUtils.toLowerCase(splitter2.text(tokens.get(3)))) { if
   * (!previous.equals(TextUtils.NA)) { oldIdToSymbolMap.put(previous, ls); } }
   * 
   * for (String synonym : TextUtils.toLowerCase(splitter2.text(tokens.get(4))))
   * { if (!synonym.equals(TextUtils.NA)) { oldIdToSymbolMap.put(synonym, ls); }
   * } } } finally { reader.close(); } }
   */

  /**
   * Loads a HUGO file cope with new symbols and mappings between old and new
   * symbols.
   * 
   * @param geneInfoFile
   * @param idToSymbolMap
   * @param oldIdToSymbolMap Will contain old symbol names mapping to their
   *          newest.
   * @param officalMap
   * @throws IOException
   */
  /*
   * private static void loadNcbiMap(final Path geneInfoFile, final Path
   * geneHistoryFile, final Path geneRefseqFile, Map<String, String>
   * idToSymbolMap, Map<String, String> oldIdToSymbolMap, Map<String, String>
   * officalMap, Map<String, Collection<String>> symbolRefSeqMap, Map<String,
   * Collection<String>> symbolEntrezMap) throws IOException { String line;
   * List<String> tokens;
   * 
   * String entrez; String symbol; String ls;
   * 
   * Splitter splitter = Splitter.onTab();
   * 
   * Splitter splitter2 = Splitter.on(";");
   * 
   * Map<String, String> entrezMap = new HashMap<String, String>();
   * 
   * 
   * BufferedReader reader = Resources.getGzipReader(geneInfoFile);
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * entrez = tokens.get(1); symbol = tokens.get(2); ls = symbol.toLowerCase();
   * 
   * entrezMap.put(entrez, ls);
   * 
   * idToSymbolMap.put(ls, ls); officalMap.put(ls, symbol);
   * 
   * symbolEntrezMap.get(ls).add(entrez);
   * 
   * // // Deal with older mappings //
   * 
   * for (String previous : splitter2.text(tokens.get(3))) { if
   * (!previous.equals(TextUtils.NA) &&
   * !previous.equals(TextUtils.DASH_DELIMITER)) {
   * 
   * if (previous.contains("1110007C09Rik")) { System.err.println("dsfsdf " +
   * previous + " " + ls); }
   * 
   * oldIdToSymbolMap.put(previous.toLowerCase(), ls); } } } } finally {
   * reader.close(); }
   * 
   * reader = Resources.getGzipReader(geneHistoryFile);
   * 
   * String oldEntrez;
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * entrez = tokens.get(1); oldEntrez = tokens.get(2);
   * 
   * // The old symbol symbol = tokens.get(3); ls = symbol.toLowerCase();
   * 
   * if (entrezMap.containsKey(entrez)) { oldIdToSymbolMap.put(oldEntrez,
   * entrezMap.get(entrez)); oldIdToSymbolMap.put(ls, entrezMap.get(entrez)); }
   * } } finally { reader.close(); }
   * 
   * reader = Resources.getGzipReader(geneRefseqFile);
   * 
   * String refseq;
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * entrez = tokens.get(1);
   * 
   * ls = entrezMap.get(entrez);
   * 
   * refseq = tokens.get(3);
   * 
   * symbolRefSeqMap.get(ls).add(refseq); } } finally { reader.close(); } }
   */

  /*
   * private static void loadNcbiMap(Path file, Map<String, String>
   * idToSymbolMap, Map<String, String> officialMap, Map<String,
   * Collection<String>> symbolRefSeqMap, Map<String, Collection<String>>
   * symbolEntrezMap) throws IOException { BufferedReader reader =
   * Resources.getGzipReader(file); String line; List<String> tokens;
   * 
   * try { // Skip header reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens =
   * Splitter.onTab().text(line);
   * 
   * // symbol String ls = tokens.get(2).toLowerCase();
   * 
   * idToSymbolMap.put(ls, ls);
   * 
   * officialMap.put(ls, tokens.get(2));
   * 
   * symbolEntrezMap.get(ls).add(tokens.get(0));
   * symbolRefSeqMap.get(ls).add(tokens.get(1));
   * 
   * // entrez idToSymbolMap.put(tokens.get(0), ls);
   * 
   * // refseq idToSymbolMap.put(tokens.get(1).toLowerCase(), ls); } } finally {
   * reader.close(); } }
   */

  /*
   * private static Motifs parseGenesXmlGz(Path file) throws IOException,
   * ParserConfigurationException, SAXException { InputStream stream =
   * Resources.getGzipInputStream(file);
   * 
   * Motifs motifs = null;
   * 
   * try { motifs = parseGenesXml(stream); } finally { stream.close(); }
   * 
   * return motifs; }
   * 
   * private static Motifs parseGenesXml(InputStream is) throws
   * ParserConfigurationException, SAXException, IOException { if (is == null) {
   * return null; }
   * 
   * SAXParserFactory factory = SAXParserFactory.newInstance(); SAXParser
   * saxParser = factory.newSAXParser();
   * 
   * MotifXmlHandler handler = new MotifXmlHandler();
   * 
   * saxParser.parse(is, handler);
   * 
   * return handler.getMotifs(); }
   */

  /**
   * Converts primary symbol ids back to a string of other ids, for example
   * symbol to Entrez ids. Returns Ids as a semi-colon separated string of
   * sorted terms.
   * 
   * @param symbol
   * @param symbolToIdMap
   * @return
   */
  /*
   * private static String formatSymbolToIds(final String symbol, final
   * Map<String, Collection<String>> symbolToIdMap) { if
   * (symbolToIdMap.containsKey(symbol)) { return
   * Join.onSemiColon().values(CollectionUtils.sort(symbolToIdMap.get(symbol))).
   * toString(); } else { return TextUtils.NA; } }
   */

  /**
   * Populates the symbols set with lowercase gene symbols that an id maps to.
   * 
   * @param id
   * @param idToSymbolMap
   * @param symbols
   */
  /*
   * private void convert(String id, boolean split, final Map<String, String>
   * idToSymbolMap, final Map<String, String> oldIdToSymbolMap, Map<String,
   * String> symbolChrMap, Set<String> symbols) {
   * 
   * if (idToSymbolMap.containsKey(id)) { symbols.add(idToSymbolMap.get(id)); }
   * 
   * if (symbols.size() == 0) { // OK, might be an old symbol name
   * 
   * // As a backup, check the old symbols if (oldIdToSymbolMap != null) { if
   * (oldIdToSymbolMap.containsKey(id)) { symbols.add(oldIdToSymbolMap.get(id));
   * } } }
   * 
   * if (symbols.size() == 0) { // See if its a loc problem
   * 
   * if (id.startsWith("loc")) { id = id.substring(3); }
   * 
   * if (idToSymbolMap.containsKey(id)) { symbols.add(idToSymbolMap.get(id)); }
   * else { if (oldIdToSymbolMap != null) { if
   * (oldIdToSymbolMap.containsKey(id)) { symbols.add(oldIdToSymbolMap.get(id));
   * } } } }
   * 
   * if (symbols.size() == 0) { if (split) { // Consider splitting the id to see
   * if it is made up // of a two gene symbols separated by a dash
   * 
   * List<String> terms = Splitter.onDash().text(id);
   * 
   * // We ignore anti-sense cases for (String term : terms) { if
   * (term.endsWith("as1") || term.endsWith("as2")) { return; } }
   * 
   * // Each term must be its own mappable term for (String term : terms) { if
   * (!idToSymbolMap.containsKey(term)) { return; } }
   * 
   * // all terms must be on the same chr String chr = null;
   * 
   * for (String term : terms) { String s = idToSymbolMap.get(term);
   * 
   * if (chr == null) { chr = symbolChrMap.get(s); }
   * 
   * if (!symbolChrMap.get(s).equals(chr)) { return; } }
   * 
   * for (String term : terms) { if (idToSymbolMap.containsKey(term)) {
   * symbols.add(idToSymbolMap.get(term)); } } } } }
   */

  private static String format(final Collection<String> symbols) {

    List<String> nonEmpty = TextUtils.nonEmpty(CollectionUtils.sort(symbols));

    if (nonEmpty.size() > 0) {
      return Join.onSemiColon().values(nonEmpty).toString();
    } else {
      return TextUtils.NA;
    }
  }

  /**
   * Converts an id between species and then populates the symbol set with lower
   * case gene symbols reflecting the genes that the id maps to. If the id
   * cannot be converted, null will be returned. Note that species conversion
   * requires that the id be either a gene symbol
   *
   * @param id the id
   * @param split the split
   * @param fromMap the from map
   * @param toMap the to map
   * @param homologyMap the homology map
   * @param symbols the symbols
   */
  private void convertBetweenSpecies(final Conversion id,
      boolean split,
      GenesMap fromMap,
      GenesMap toMap,
      final HomologyMap homologyMap,
      Set<Conversion> symbols) {

    // First convert the id to a lower case gene symbol in its own
    // species to make sure names are up to date etc.
    Set<Conversion> fromEntrezes = new HashSet<Conversion>();
    fromMap.convert(id, split, fromEntrezes);

    // System.err.println(conversionMap.printKeys());

    // Now convert between species
    for (Conversion entrez : fromEntrezes) {
      if (homologyMap.contains(entrez)) {
        List<Conversion> homologyEntrezes = homologyMap.homology(entrez);

        for (Conversion homologyEntrez : homologyEntrezes) {
          // System.err.println("conv " + symbol.getId() + " " + c.getId() + " "
          // +
          // c.getType());

          // Use the translated symbol for the conversion
          toMap.convert(homologyEntrez, split, symbols);
        }
      } else {
        // Conversion converted = new Conversion(symbol, "no_homology");

        // Fall back and see if the symbol exists without conversion
        toMap.convert(entrez, split, homologyMap, symbols);
      }
    }
  }

  /*
   * private void convertBetweenSpecies(final String id, boolean split, final
   * Map<String, String> humanMouseMap, final Map<String, String>
   * idToSymbolFromMap, final Map<String, String> oldIdToSymbolFromMap, final
   * Map<String, String> symbolChrFromMap, final Map<String, String>
   * idToSymbolToMap, final Map<String, String> oldIdToSymbolToMap, final
   * Map<String, String> symbolChrToMap, Set<String> symbols) {
   * 
   * // First convert the id to a lower case gene symbol in its own // species
   * to make sure names are uptodate etc.
   * 
   * Set<String> fromSymbols = new HashSet<String>();
   * 
   * convert(id, split, idToSymbolFromMap, oldIdToSymbolFromMap,
   * symbolChrFromMap, fromSymbols);
   * 
   * if (id.contains("1110007c09rik")) { System.err.println("conv " + id + " " +
   * oldIdToSymbolFromMap.containsKey(id)); }
   * 
   * // Now convert between species for (String symbol : fromSymbols) { if
   * (humanMouseMap.containsKey(symbol)) {
   * 
   * String convertedId = humanMouseMap.get(symbol);
   * 
   * // Use the translated symbol for the conversion convert(convertedId, split,
   * idToSymbolToMap, oldIdToSymbolToMap, symbolChrToMap, symbols); } } }
   */

  /*
   * private static void loadConversion(final Path file, Map<String, String>
   * humanMouseMap, Map<String, String> mouseHumanMap) throws IOException {
   * BufferedReader reader = Resources.getGzipReader(file); String line;
   * List<String> tokens;
   * 
   * String humanEntrez = null; String humanSymbol = null; String humanRefseq =
   * null; String mouseEntrez = null; String mouseSymbol = null; String
   * mouseRefseq = null; String hid = null;
   * 
   * try { // Skip header reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens =
   * Splitter.onTab().text(line);
   * 
   * if (hid == null || !tokens.get(0).equals(hid)) { // Each time we encounter
   * a new species group, reset // the mapping humanEntrez = null; humanSymbol =
   * null; humanRefseq = null; mouseEntrez = null; mouseSymbol = null;
   * mouseRefseq = null;
   * 
   * hid = tokens.get(0); }
   * 
   * // Human if (tokens.get(1).equals("9606")) { humanEntrez = tokens.get(2);
   * humanSymbol = tokens.get(3).toLowerCase(); humanRefseq =
   * tokens.get(5).toLowerCase().replaceFirst("\\..+", TextUtils.EMPTY_STRING);
   * }
   * 
   * // Mouse if (tokens.get(1).equals("10090")) { mouseEntrez = tokens.get(2);
   * mouseSymbol = tokens.get(3).toLowerCase(); mouseRefseq =
   * tokens.get(5).toLowerCase().replaceFirst("\\..+", TextUtils.EMPTY_STRING);
   * }
   * 
   * 
   * if (humanSymbol != null && mouseSymbol != null) { // If both the human and
   * mouse appear within a group, // create a mapping between them
   * humanMouseMap.put(humanSymbol, mouseSymbol); humanMouseMap.put(humanEntrez,
   * mouseSymbol); humanMouseMap.put(humanRefseq, mouseSymbol);
   * mouseHumanMap.put(mouseSymbol, humanSymbol); mouseHumanMap.put(mouseEntrez,
   * humanSymbol); mouseHumanMap.put(mouseRefseq, humanSymbol); } } } finally {
   * reader.close(); } }
   */

  /*
   * private static void loadMaps(final Path file, Map<String, String>
   * idToSymbolMap, Map<String, String> officalMap, Map<String, String>
   * symbolChrMap, Map<String, String> symbolStrandMap, Map<String,
   * Collection<String>> symbolIdMap1, Map<String, Collection<String>>
   * symbolIdMap2) throws IOException { String line; List<String> tokens;
   * 
   * String id1; String id2; String symbol; String ls; String chr; String
   * strand;
   * 
   * BufferedReader reader = Resources.getGzipReader(file);
   * 
   * Splitter splitter = Splitter.onTab();
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * id1 = tokens.get(0); id2 = tokens.get(1); symbol = tokens.get(2); ls =
   * symbol.toLowerCase(); chr = tokens.get(3); strand = tokens.get(4);
   * 
   * officalMap.put(ls, symbol); idToSymbolMap.put(ls, ls);
   * idToSymbolMap.put(id1, ls); idToSymbolMap.put(id2, ls);
   * 
   * symbolChrMap.put(ls, chr); symbolStrandMap.put(ls, strand);
   * 
   * symbolIdMap1.get(ls).add(id1); symbolIdMap2.get(ls).add(id2); } } finally {
   * reader.close(); } }
   */
}
