/**
 * Copyright 2018 Antony Holmes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.columbia.rdf.matcalc.bio.toolbox.conversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.EntryCreator;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.Splitter;
import org.jebtk.core.text.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class GenesMap.
 */
public class GenesMap {

  /** The Constant SYMBOL_TYPE. */
  public static final String SYMBOL_TYPE = "symbol";

  /** The Constant ENTREZ_TYPE. */
  public static final String ENTREZ_TYPE = "entrez";

  /** The Constant REFSEQ_TYPE. */
  public static final String REFSEQ_TYPE = "refseq";

  /** The Constant ENSEMBL_GENE_TYPE. */
  public static final String ENSEMBL_GENE_TYPE = "ensembl-gene";

  /** The Constant ENSEMBL_TRANSCRIPT_TYPE. */
  public static final String ENSEMBL_TRANSCRIPT_TYPE = "ensembl-transcript";

  /** The Constant CHR_TYPE. */
  public static final String CHR_TYPE = "chr";

  /** The Constant STRAND_TYPE. */
  public static final String STRAND_TYPE = "strand";

  /** The m id to symbol map. */
  private Map<String, String> mIdToEntrezMap = new HashMap<String, String>();

  /** The m alt id to symbol map. */
  private Map<String, String> mAltIdToEntrezMap = new HashMap<String, String>();

  /** The m old id to symbol map. */
  private Map<String, String> mOldIdToEntrezMap = new HashMap<String, String>();

  /** The m offical id map. */
  private Map<String, GeneMapping> mOfficalIdMap;
  
  private Map<String, GeneMapping> mAltIdMap;

  private int mTaxId;

  private String mName;

  private String mSpecies;

  /**
   * Instantiates a new genes map.
   * @param taxId 
   * @param species 
   *
   * @param species the species
   */
  public GenesMap(int taxId, String name, String species) {
    mTaxId = taxId;
    mName = name;
    mSpecies = species;
    
    mOfficalIdMap = DefaultHashMap.create(new EntryCreator<GeneMapping>() {
      @Override
      public GeneMapping newEntry() {
        return new GeneMapping();
      }
    });
    
    mAltIdMap = DefaultHashMap.create(new EntryCreator<GeneMapping>() {
      @Override
      public GeneMapping newEntry() {
        return new GeneMapping();
      }
    });
  }

  /**
   * Adds the mapping.
   *
   * @param entrez the symbol
   * @param name the name
   * @param type the type
   */
  public void map(String entrez, String name, String type) {
    entrez = santize(entrez);

    mIdToEntrezMap.put(entrez, entrez);

    mIdToEntrezMap.put(santize(name), entrez);

    mOfficalIdMap.get(entrez).getIds(type).add(name);
    //mOfficalIdMap.get(ls).getIds(SYMBOL_TYPE).add(symbol);
  }

  /**
   * Adds the old mapping.
   *
   * @param entrez the symbol
   * @param name the name
   */
  public void mapOld(String entrez, String name) {
    mOldIdToEntrezMap.put(santize(name), santize(entrez));
  }

  /**
   * Add an unofficial name mapping. These are considered still valid and not
   * retired.
   *
   * @param entrez the symbol
   * @param name the name
   */
  public void mapAlt(String entrez, String name, String type) {
    entrez = santize(entrez);
    
    // We can map from an old id to an entrez
    mAltIdToEntrezMap.put(santize(name), entrez);
    
    mAltIdMap.get(entrez).getIds(type).add(name);
  }

  /**
   * Updates a symbol/id to what is the current up to date symbol.
   *
   * @param c the c
   * @param split the split
   * @param symbols the symbols
   */
  public void convert(Conversion c, boolean split, Collection<Conversion> symbols) {
    convert(c, split, null, symbols);
  }

  /**
   * Convert symbol also checking that if the symbol is an old symbol that is
   * converted to a new symbol, the new symbol does not have a homology since
   * homology is used as the gold standard. Thus is there is a homology between
   * species, the conversion must be by homology only and not simple name
   * similarity mapping.
   *
   * @param c the c
   * @param split the split
   * @param homologyMap the conversion map
   * @param symbols the symbols
   */
  public void convert(Conversion c,
      boolean split,
      HomologyMap homologyMap,
      Collection<Conversion> symbols) {

    String id = santize(c.getId());
    String chr = c.getChr();

    if (mIdToEntrezMap.containsKey(id)) {
      String entrez = mIdToEntrezMap.get(id);

      if (chrCheck(entrez, chr)) {
        symbols.add(endMapping(new Conversion(c, entrez, "map:" + entrez)));

        return;
      }
    }

    // OK, might be an unofficial alternative symbol name

    // As a backup, check the alternative symbols
    if (mAltIdToEntrezMap.containsKey(id)) {
      String entrez = mAltIdToEntrezMap.get(id);

      if (chrCheck(entrez, chr)) {
        if (homologyMap == null || !homologyMap.contains(entrez)) {
          symbols.add(endMapping(new Conversion(c, entrez, "alt:" + entrez)));
        } else {
          symbols.add(new Conversion(c, TextUtils.NA, "hom-err"));
        }

        /*
         * if (conversionMap == null || !conversionMap.contains(newId)) {
         * symbols.add(c); } else { symbols.add(new Conversion(TextUtils.NA, c,
         * "hom-err")); }
         */

        return;
      }
    }

    // OK, might be an old symbol name

    // As a backup, check the old symbols
    if (mOldIdToEntrezMap.containsKey(id)) {
      String entrez = mOldIdToEntrezMap.get(id);

      if (chrCheck(entrez, chr)) {
        if (homologyMap == null || !homologyMap.contains(entrez)) {
          symbols.add(endMapping(new Conversion(c, entrez, "old:" + entrez)));
        } else {
          symbols.add(new Conversion(c, TextUtils.NA, "hom-err"));
        }

        return;
      }
    }

    // See if its a LOC naming problem

    if (id.startsWith("loc")) {
      //Remove loc as sometimes the id is the numerical part of a LOC.... name.
      id = id.substring(3);

      c = new Conversion(c, id, "loc:" + id);
      
      // Recursively try again
      convert(c, split, homologyMap, symbols);
      
      /*
      if (mIdToSymbolMap.containsKey(id)) {
        String newId = mIdToSymbolMap.get(id);

        symbols.add(new Conversion(c, newId, "map:" + newId));
        return;
      } else {
        if (mOldIdToSymbolMap.containsKey(id)) {
          String newId = mOldIdToSymbolMap.get(id);

          c = new Conversion(c, newId, "old:" + newId);

          if (conversionMap == null || !conversionMap.contains(newId)) {
            symbols.add(c);
          } else {
            symbols.add(new Conversion(c, TextUtils.NA, "hom-err"));
          }

          return;
        }
      }
      */
    }

    if (split) {
      // Consider splitting the id to see if it is made up
      // of a two gene symbols separated by a dash

      List<String> terms = Splitter.onDash().text(id);

      // We ignore anti-sense cases
      for (String term : terms) {
        if (term.endsWith("as1") || term.endsWith("as2")) {
          return;
        }
      }

      // Each term must be its own mappable term
      for (String term : terms) {
        if (!mIdToEntrezMap.containsKey(term)) {
          return;
        }
      }

      // all terms must be on the same chr
      /*
       * String chr = null;
       * 
       * for (String term : terms) { String s = mIdToSymbolMap.get(term);
       * 
       * if (chr == null) { chr = symbolChrMap.get(s); }
       * 
       * if (!symbolChrMap.get(s).equals(chr)) { return; } }
       */

      for (String term : terms) {
        if (mIdToEntrezMap.containsKey(term)) {
          String newId = mIdToEntrezMap.get(term);

          //symbols.add(new Conversion(newId, c, "split:" + newId));

          // recursively update
          convert(new Conversion(c, newId, "split:" + newId),
              split,
              homologyMap,
              symbols);
        }
      }
    }
  }

  private Conversion endMapping(Conversion c) {
    return new Conversion(c, c.getId(), "out:" + mSpecies);
  }

  /**
   * If chr data available, check we are on the right chr.
   * 
   * @param id
   * @param chr
   * @return
   */
  private boolean chrCheck(String id, String chr) {
    if (chr == null) {
      return true;
    }

    String c = getChr(id);

    if (c == null) {
      return true;
    }

    return c.equals(chr);
  }

  /**
   * Gets the symbols.
   *
   * @param c the c
   * @return the symbols
   */
  public Collection<String> getSymbols(Conversion c) {
    return getSymbols(c.getId());
  }

  /**
   * Gets the symbols.
   *
   * @param id the id
   * @return the symbols
   */
  public Collection<String> getSymbols(String id) {
    return getMappings(id, SYMBOL_TYPE);
  }

  /**
   * Gets the symbols.
   *
   * @param ids the ids
   * @return the symbols
   */
  public Collection<String> getSymbols(final Collection<Conversion> ids) {
    return getMappings(ids, SYMBOL_TYPE);
  }

  /**
   * Gets the entrez.
   *
   * @param c the c
   * @return the entrez
   */
  public Collection<String> getEntrez(Conversion c) {
    return getEntrez(c.getId());
  }

  /**
   * Gets the entrez.
   *
   * @param id the id
   * @return the entrez
   */
  public Collection<String> getEntrez(String id) {
    return getMappings(id, ENTREZ_TYPE);
  }

  /**
   * Gets the refseq.
   *
   * @param c the c
   * @return the refseq
   */
  public Collection<String> getRefseq(Conversion c) {
    return getRefseq(c.getId());
  }

  /**
   * Gets the refseq.
   *
   * @param id the id
   * @return the refseq
   */
  public Collection<String> getRefseq(String id) {
    return getMappings(id, REFSEQ_TYPE);
  }

  public String getChr(Conversion c) {
    return getChr(c.getId());
  }

  /**
   * Gets the refseq.
   *
   * @param id the id
   * @return the refseq
   */
  public String getChr(String id) {
    return getMapping(id, CHR_TYPE);
  }

  /**
   * Returns the first mapping available for a gene or an empty string if
   * there isn't a mapping type associated with the gene.
   * 
   * @param id
   * @param type
   * @return
   */
  public String getMapping(String id, String type) {
    Collection<String> mappings = getMappings(id, type);

    if (mappings.size() > 0) {
      return mappings.iterator().next();
    } else {
      return TextUtils.EMPTY_STRING;
    }
  }

  /**
   * For a given id, return any mappings of a given type. type will be of the
   * form "entrez", "refseq", "ensembl-gene" etc depending on the data sources
   * available.
   *
   * @param id the id
   * @param type the type
   * @return the mappings
   */
  public Collection<String> getMappings(String id, String type) {
    return mOfficalIdMap.get(santize(id)).getIds(type);
  }
  
  public Collection<String> getAltMappings(String id) {
    return mAltIdMap.get(santize(id)).getIds();
  }
  
  public Collection<String> getAltMappings(String id, String type) {
    return mAltIdMap.get(santize(id)).getIds(type);
  }
  
  public Collection<String> getAltMappings(Conversion c, String type) {
    return getAltMappings(c.getId(), type);
  }
  
  public Collection<String> getAltMappings(Conversion c) {
    return getAltMappings(c.getId());
  }

  /**
   * Gets the mappings.
   *
   * @param c the c
   * @param type the type
   * @return the mappings
   */
  public Collection<String> getMappings(Conversion c, String type) {
    return getMappings(c.getId(), type);
  }

  /**
   * Convert ids to real ids.
   *
   * @param ids the ids
   * @param type the type
   * @return the mappings
   */
  public Set<String> getMappings(final Collection<Conversion> ids, String type) {
    Set<String> symbols = new TreeSet<String>();

    for (Conversion c : ids) {
      symbols.addAll(getMappings(c, type));
    }

    return symbols;
  }
  
  public Set<String> getAltMappings(final Collection<Conversion> ids, String type) {
    Set<String> symbols = new TreeSet<String>();

    for (Conversion c : ids) {
      symbols.addAll(getAltMappings(c, type));
    }

    return symbols;
  }
  
  public Set<String> getAltMappings(final Collection<Conversion> ids) {
    Set<String> symbols = new TreeSet<String>();

    for (Conversion c : ids) {
      symbols.addAll(getAltMappings(c));
    }

    return symbols;
  }

  /**
   * To string.
   *
   * @param symbols the symbols
   * @param type the type
   * @return the string
   */
  public String toString(final Set<Conversion> symbols, String type) {
    return Join.onSemiColon()
        .values(CollectionUtils.sort(getMappings(symbols, type))).toString();
  }

  public static final String santize(String v) {
    return v.toLowerCase();
  }

  public String getName() {
    return mName;
  }

  /**
   * Return the taxonomy id of the map.
   * 
   * @return
   */
  public int getTaxId() {
    return mTaxId;
  }

  /*
   * public void loadEnsembl(final Path file) throws IOException {
   * loadEnsembl(file, this); }
   * 
   * public static void loadEnsembl(final Path file, GenesMap genesMap) throws
   * IOException { String line; List<String> tokens;
   * 
   * String id1; String id2; String symbol;
   * 
   * BufferedReader reader = Resources.getGzipReader(file);
   * 
   * Splitter splitter = Splitter.onTab();
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * id1 = tokens.get(0); id2 = tokens.get(1); symbol = tokens.get(2); //ls =
   * symbol.toLowerCase(); //chr = tokens.get(3); //strand = tokens.get(4);
   * 
   * genesMap.addMapping(symbol, id1, "ensembl_transcript");
   * genesMap.addMapping(symbol, id2, "ensembl_gene");
   * 
   * } } finally { reader.close(); } }
   */
}
