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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jebtk.core.Resources;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.DefaultHashMapCreator;
import org.jebtk.core.collections.EntryCreator;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.text.Splitter;
import org.jebtk.core.text.TextUtils;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class SpeciesHomologyMap.
 */
public class SpeciesHomologyMap {

  /** The m conversion map. */
  private Map<Integer, IterMap<Integer, HomologyMap>> mConversionMap;

  /**
   * Instantiates a new species homology map.
   */
  public SpeciesHomologyMap() {
    mConversionMap = DefaultHashMap
        .create(new DefaultHashMapCreator<Integer, HomologyMap>(
            new EntryCreator<HomologyMap>() {
              @Override
              public HomologyMap newEntry() {
                return new HomologyMap();
              }
            }));
  }

  /**
   * Human to mouse.
   *
   * @return the homology map
   */
  public HomologyMap humanToMouse() {
    return mConversionMap.get(GenesService.HUMAN_TAX_ID)
        .get(GenesService.MOUSE_TAX_ID);
  }

  /**
   * Mouse to human.
   *
   * @return the homology map
   */
  public HomologyMap mouseToHuman() {
    return mConversionMap.get(GenesService.MOUSE_TAX_ID)
        .get(GenesService.HUMAN_TAX_ID);
  }

  /**
   * Gets the map.
   *
   * @param fromTaxId the from tax id
   * @param toTaxId the to tax id
   * @return the map
   */
  public HomologyMap getMap(int fromTaxId, int toTaxId) {
    return mConversionMap.get(fromTaxId).get(toTaxId);
  }

  /**
   * Load ncbi.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void loadNcbi(final Path file) throws IOException {
    BufferedReader reader = Resources.getGzipReader(file);
    String line;
    List<String> tokens;

    String humanEntrez = null;
    String humanSymbol = null;
    String humanRefseq = null;
    String mouseEntrez = null;
    String mouseSymbol = null;
    String mouseRefseq = null;
    String hid = null;

    try {
      // Skip header
      reader.readLine();

      while ((line = reader.readLine()) != null) {
        tokens = Splitter.onTab().text(line);

        // taxId = Parser.toInt(tokens.get(1));

        if (hid == null || !tokens.get(0).equals(hid)) {
          // Each time we encounter a new species group, reset
          // the mapping
          humanEntrez = null;
          humanSymbol = null;
          humanRefseq = null;
          mouseEntrez = null;
          mouseSymbol = null;
          mouseRefseq = null;

          hid = tokens.get(0);
        }

        // Human
        if (tokens.get(1).equals("9606")) {
          humanEntrez = tokens.get(2);
          humanSymbol = tokens.get(3).toLowerCase();
          humanRefseq = tokens.get(5).toLowerCase().replaceFirst("\\..+",
              TextUtils.EMPTY_STRING);
        }

        // Mouse
        if (tokens.get(1).equals("10090")) {
          mouseEntrez = tokens.get(2);
          mouseSymbol = tokens.get(3).toLowerCase();
          mouseRefseq = tokens.get(5).toLowerCase().replaceFirst("\\..+",
              TextUtils.EMPTY_STRING);
        }

        if (humanSymbol != null && mouseSymbol != null) {
          // If both the human and mouse appear within a group,
          // create a mapping between them
          getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID)
              .map(humanSymbol, mouseSymbol);
          getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID)
              .map(humanEntrez, mouseSymbol);
          getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID)
              .map(humanRefseq, mouseSymbol);
          getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID)
              .map(mouseSymbol, humanSymbol);
          getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID)
              .map(mouseEntrez, humanSymbol);
          getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID)
              .map(mouseRefseq, humanSymbol);
        }
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Parses the genes xml gz.
   *
   * @param file the file
   * @param speciesMap the species map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  public static void parseGenesXmlGz(Path file, SpeciesHomologyMap speciesMap)
      throws IOException, ParserConfigurationException, SAXException {
    InputStream stream = Resources.getGzipInputStream(file);

    try {
      parseHomologyXml(stream, speciesMap);
    } finally {
      stream.close();
    }
  }

  /**
   * Parses the homology xml.
   *
   * @param is the is
   * @param speciesMap the species map
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void parseHomologyXml(InputStream is,
      SpeciesHomologyMap speciesMap)
      throws ParserConfigurationException, SAXException, IOException {
    if (is == null) {
      return;
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    HomologyXmlHandler handler = new HomologyXmlHandler(speciesMap);

    saxParser.parse(is, handler);
  }
}
