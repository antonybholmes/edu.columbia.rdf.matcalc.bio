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

import java.util.Map;

import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.DefaultTreeMapCreator;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.TreeMapCreator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// TODO: Auto-generated Javadoc
/**
 * The class KeyXmlHandler.
 */
public class HomologyXmlHandler extends DefaultHandler {

  /** The m symbol. */
  private String mEntrez;

  /** The m species map. */
  private SpeciesHomologyMap mSpeciesMap;

  /** The m id map. */
  private Map<Integer, IterMap<String, IterMap<String, String>>> mIdMap;

  /** The m tax id. */
  private int mTaxId;

  /**
   * Instantiates a new homology xml handler.
   *
   * @param speciesMap the species map
   */
  public HomologyXmlHandler(SpeciesHomologyMap speciesMap) {
    mSpeciesMap = speciesMap;

    mIdMap = new DefaultTreeMap<Integer, IterMap<String, IterMap<String, String>>>(
        new DefaultTreeMapCreator<String, IterMap<String, String>>(
            new TreeMapCreator<String, String>()));

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   * java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri,
      String localName,
      String qName,
      Attributes attributes) throws SAXException {
    if (qName.equals("group")) {
      mIdMap.clear();
    } else if (qName.equals("tax")) {
      if (attributes.getValue("id") != null) {
        mTaxId = Integer.parseInt(attributes.getValue("id"));
      }
    } else if (qName.equals("gene")) {
      if (attributes.getValue("entrez") != null) {
        mEntrez = attributes.getValue("entrez");
      } else {
        mEntrez = attributes.getValue("symbol");
      }

      if (attributes.getValue("tax_id") != null) {
        mTaxId = Integer.parseInt(attributes.getValue("tax_id"));
      }

      mIdMap.get(mTaxId).get(mEntrez).put("symbol", mEntrez);
    } else if (qName.equals("id")) {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type").toLowerCase();

      mIdMap.get(mTaxId).get(mEntrez).put(type, name);
    } else {
      // Do nothing
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (qName.equals("group")) {

      for (int taxId1 : mIdMap.keySet()) {
        for (String entrez1 : mIdMap.get(taxId1).keySet()) {
          for (String type1 : mIdMap.get(taxId1).get(entrez1).keySet()) {
            String id1 = mIdMap.get(taxId1).get(entrez1).get(type1)
                .toLowerCase();

            for (int taxId2 : mIdMap.keySet()) {
              for (String entrez2 : mIdMap.get(taxId2).keySet()) {
                for (String type2 : mIdMap.get(taxId2).get(entrez2).keySet()) {
                  //String id2 = entrez2.toLowerCase(); // mIdMap.get(taxId2).get(symbol2).get(type2).toLowerCase();

                  String id2 = mIdMap.get(taxId2).get(entrez2).get(type2)
                      .toLowerCase();

                  //System.err.println("hom " + taxId1 + " " + taxId2 + " " + id1 + " " + id2);

                  mSpeciesMap.getMap(taxId1, taxId2).map(id1, id2);
                  // mSpeciesMap.getMap(taxId2, taxId1).addMapping(id2, id1);
                }
              }
            }
          }
        }
      }

      /*
       * mSpeciesMap.getMap(mTaxId, mTaxId2).addMapping(mSymbol, mSymbol2);
       * 
       * for (String type : mIdMap.keySet()) { mSpeciesMap.getMap(mTaxId,
       * mTaxId2).addMapping(mIdMap.get(type), mSymbol2); }
       * 
       * mSpeciesMap.getMap(mTaxId2, mTaxId).addMapping(mSymbol2, mSymbol);
       * 
       * for (String type : mIdMap2.keySet()) { mSpeciesMap.getMap(mTaxId2,
       * mTaxId).addMapping(mIdMap2.get(type), mSymbol); }
       */
    }
  }
}
