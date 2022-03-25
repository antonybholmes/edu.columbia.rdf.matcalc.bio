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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// TODO: Auto-generated Javadoc
/**
 * The class KeyXmlHandler.
 */
public class GenesXmlHandler extends DefaultHandler {

  /** The m symbol. */
  private String mEntrez;
  
  /** The m species map. */
  private SpeciesGenesMap mSpeciesMap;
  
  /** The m taxt id. */
  private int mTaxId;

  /**
   * Instantiates a new genes xml handler.
   *
   * @param speciesMap the species map
   */
  public GenesXmlHandler(SpeciesGenesMap speciesMap) {
    mSpeciesMap = speciesMap;
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
    if (qName.equals("genes")) {
      mTaxId = Integer.parseInt(attributes.getValue("tax_id"));
      
      String id = attributes.getValue("id");
      
      String species = attributes.getValue("species");
      
      mSpeciesMap.addMap(new GenesMap(mTaxId, id, species));
    } else if (qName.equals("gene")) {
      if (attributes.getValue("entrez") != null) {
        mEntrez = attributes.getValue("entrez");
      } else {
        mEntrez = attributes.getValue("symbol");
      }
      
      //mChr = attributes.getValue("chr");
    } else if (qName.equals("mapping")) {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type");

      mSpeciesMap.getMap(mTaxId).map(mEntrez, name, type);
    } else if (qName.equals("alt-mapping")) {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type");
      
      mSpeciesMap.getMap(mTaxId).mapAlt(mEntrez, name, type);
    } else if (qName.equals("old-mapping")) {
      String name = attributes.getValue("name");
      mSpeciesMap.getMap(mTaxId).mapOld(mEntrez, name);
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

  }
}
