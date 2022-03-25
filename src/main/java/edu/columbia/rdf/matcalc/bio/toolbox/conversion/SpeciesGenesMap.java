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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jebtk.core.Resources;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class SpeciesGenesMap.
 */
public class SpeciesGenesMap {
  
  /** The m species map. */
  private Map<Integer, GenesMap> mSpeciesMap = new HashMap<Integer, GenesMap>();

  /**
   * Gets the map.
   *
   * @param taxId the tax id
   * @return the map
   */
  public GenesMap getMap(int taxId) {
    /*
    if (!mSpeciesMap.containsKey(taxId)) {
      switch (taxId) {
      case GenesService.MOUSE_TAX_ID:
        mSpeciesMap.put(taxId, new GenesMap(taxId, "mouse"));
        break;
      default:
        mSpeciesMap.put(taxId, new GenesMap(taxId, "human"));
        break;
      }
    }
    */

    return mSpeciesMap.get(taxId);
  }
  
  public void addMap(GenesMap map) {
    mSpeciesMap.put(map.getTaxId(), map);
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
  public static void parseGenesXmlGz(Path file, SpeciesGenesMap speciesMap)
      throws IOException, ParserConfigurationException, SAXException {
    InputStream stream = Resources.getGzipInputStream(file);

    try {
      parseGenesXml(stream, speciesMap);
    } finally {
      stream.close();
    }
  }

  /**
   * Parses the genes xml.
   *
   * @param is the is
   * @param speciesMap the species map
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void parseGenesXml(InputStream is, SpeciesGenesMap speciesMap)
      throws ParserConfigurationException, SAXException, IOException {
    if (is == null) {
      return;
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    GenesXmlHandler handler = new GenesXmlHandler(speciesMap);

    saxParser.parse(is, handler);
  }
}
