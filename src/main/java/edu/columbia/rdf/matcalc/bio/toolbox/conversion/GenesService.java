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
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The class MotifsDBService.
 */
public class GenesService {

  /**
   * The Class GenesServiceLoader.
   */
  private static class GenesServiceLoader {

    /** The Constant INSTANCE. */
    private static final GenesService INSTANCE = new GenesService();
  }

  /**
   * Gets the single instance of SettingsService.
   *
   * @return single instance of SettingsService
   */
  public static GenesService getInstance() {
    return GenesServiceLoader.INSTANCE;
  }

  /** The Constant HUMAN_TAX_ID. */
  public static final int HUMAN_TAX_ID = 9606;

  /** The Constant MOUSE_TAX_ID. */
  public static final int MOUSE_TAX_ID = 10090;

  /**
   * The member dbs. We only load each db type once
   */
  private SpeciesGenesMap mSpeciesMap = new SpeciesGenesMap();

  /** The m homology map. */
  private SpeciesHomologyMap mHomologyMap = new SpeciesHomologyMap();

  /** The m auto load. */
  private boolean mAutoLoad = true;

  /** The m auto load homology. */
  private boolean mAutoLoadHomology = true;

  private Path mDir;

  /** The Constant GENES_DIR. */
  private static final Path GENES_DIR = 
      PathUtils.getPath("res/modules/conversion");

  /**
   * Instantiates a new motifs db service.
   */
  private GenesService() {
    // do nothing
  }
  
  public void setVersion(String version) {
    mDir = GENES_DIR.resolve(version);
    
    mAutoLoad = true;
    mAutoLoadHomology = true;
  }

  /**
   * Gets the map.
   *
   * @return the map
   */
  public SpeciesGenesMap getMap() {
    return mSpeciesMap;
  }

  /**
   * Gets the homology map.
   *
   * @return the homology map
   */
  public SpeciesHomologyMap getHomologyMap() {
    try {
      autoLoadHomology();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }

    return mHomologyMap;
  }

  /**
   * Adds the back end.
   *
   * @param taxId the tax id
   * @return the map
   */
  public GenesMap getMap(int taxId) {
    try {
      autoLoad();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }

    return mSpeciesMap.getMap(taxId);
  }

  /**
   * Gets the human map.
   *
   * @return the human map
   */
  public GenesMap getHumanMap() {
    return getMap(HUMAN_TAX_ID);
  }

  /**
   * Gets the mouse map.
   *
   * @return the mouse map
   */
  public GenesMap getMouseMap() {
    return getMap(MOUSE_TAX_ID);
  }

  /**
   * Load xml.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  public void loadXml(Path file)
      throws IOException, ParserConfigurationException, SAXException {
    SpeciesGenesMap.parseGenesXmlGz(file, mSpeciesMap);
  }

  /**
   * Auto load.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  private void autoLoad()
      throws IOException, ParserConfigurationException, SAXException {
    if (mAutoLoad) {
      // autoLoadEnsembl();

      autoLoadXml();

      mAutoLoad = false;
    }
  }

  /**
   * Auto load xml.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  private void autoLoadXml()
      throws IOException, ParserConfigurationException, SAXException {
    List<Path> files = FileUtils.match(mDir, true, "human", "xml.gz");

    if (files.size() > 0) {
      Collections.sort(files);
      Collections.reverse(files);

      // Pick last file by name
      loadXml(files.get(0));
    }
    
    files = FileUtils.match(mDir, true, "mouse", "xml.gz");

    if (files.size() > 0) {
      Collections.sort(files);
      Collections.reverse(files);

      // Pick last file by name
      loadXml(files.get(0));
    }
    
    //for (Path file : files) {
    //  loadXml(file);
    //}
  }

  /**
   * Auto load homology.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  private void autoLoadHomology()
      throws IOException, ParserConfigurationException, SAXException {
    if (mAutoLoadHomology) {
      loadHomologyXml();

      mAutoLoadHomology = false;
    }
  }

  /**
   * Load homology xml.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  private void loadHomologyXml()
      throws IOException, ParserConfigurationException, SAXException {
    List<Path> files = FileUtils.match(mDir, true, "homology", "xml.gz");

    if (files.size() > 0) {
      Collections.sort(files);
      Collections.reverse(files);

      // Pick last file by name
      loadHomologyXml(files.get(0));
    }

    //for (Path file : files) {
    // loadHomologyXml(file);
    //}
  }

  /**
   * Load homology xml.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   */
  public void loadHomologyXml(Path file)
      throws IOException, ParserConfigurationException, SAXException {
    SpeciesHomologyMap.parseGenesXmlGz(file, mHomologyMap);
  }
  
  public List<String> versions() throws IOException {
    return PathUtils.names(CollectionUtils.sort(FileUtils.lsdir(GENES_DIR)));
  }

  /*
   * public void autoLoadEnsembl() throws IOException {
   * mSpeciesMap.getMap(HUMAN_MAP).loadEnsembl(GenesModule.ENSEMBL_HUMAN_FILE);
   * mSpeciesMap.getMap(MOUSE_MAP).loadEnsembl(GenesModule.ENSEMBL_MOUSE_FILE);
   * }
   */
}
