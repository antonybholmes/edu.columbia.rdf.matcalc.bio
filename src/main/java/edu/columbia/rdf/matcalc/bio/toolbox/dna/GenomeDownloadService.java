package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.IterTreeMap;
import org.jebtk.core.http.URLPath;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.json.Json;
import org.jebtk.core.json.JsonParser;
import org.jebtk.core.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GenomeDownloadService implements Iterable<Entry<String, GenomeDownload>> {
  private static class DownloadServiceLoader {
    private static final GenomeDownloadService INSTANCE = new GenomeDownloadService();
  }

  /**
   * Gets the single instance of GenomeAssemblyService.
   *
   * @return single instance of GenomeAssemblyService
   */
  public static GenomeDownloadService getInstance() {
    return DownloadServiceLoader.INSTANCE;
  }

  private static final Path DOWNLOAD_JSON_FILE = PathUtils
      .getPath("res/genomes/genomes.download.json");

  private static final Path DOWNLOAD_XML_FILE = PathUtils
      .getPath("res/genomes/genomes.download.xml");

  private boolean mAutoLoad = true;

  private IterMap<String, GenomeDownload> mDownloadMap = new IterTreeMap<String, GenomeDownload>();

  private static final Logger LOG = LoggerFactory
      .getLogger(GenomeDownloadService.class);

  private class GenomeDownloadXmlHandler extends DefaultHandler {

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

      if (qName.equals("genome")) {
        String name = attributes.getValue("name");
        URLPath url = URLPath.fromUrl(attributes.getValue("url"));

        try {
          mDownloadMap.put(name,
              new GenomeDownload(name,
                  url.join(TextUtils.format("{}.chrs.gz", name)).toURL(),
                  url.join(TextUtils.format("{}.dna.zip", name)).toURL()));
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void autoLoad() throws IOException {

    if (mAutoLoad) {
      // loadJson();

      try {
        loadXml();
      } catch (SAXException | ParserConfigurationException e) {
        e.printStackTrace();
      }

      mAutoLoad = false;
    }
  }

  private void loadJson() throws IOException {
    Json json = new JsonParser().parse(DOWNLOAD_JSON_FILE);

    for (int i = 0; i < json.size(); ++i) {
      Json dnaJson = json.get(i);

      mDownloadMap.put(dnaJson.getString("name"),
          new GenomeDownload(dnaJson.getString("name"),
              new URL(dnaJson.getString("chrs")),
              new URL(dnaJson.getString("dna"))));
    }
  }

  private void loadXml()
      throws SAXException, IOException, ParserConfigurationException {
    LOG.info("Loading genomes from {}...", DOWNLOAD_XML_FILE);

    InputStream stream = FileUtils.newBufferedInputStream(DOWNLOAD_XML_FILE);

    try {
      loadXml(stream);
    } finally {
      stream.close();
    }
  }

  /**
   * Load xml.
   *
   * @param is the is
   * @param update the update
   * @return true, if successful
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   */
  protected synchronized boolean loadXml(InputStream is)
      throws SAXException, IOException, ParserConfigurationException {
    if (is == null) {
      return false;
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    GenomeDownloadXmlHandler handler = new GenomeDownloadXmlHandler();

    saxParser.parse(is, handler);

    return true;
  }

  @Override
  public Iterator<Entry<String, GenomeDownload>> iterator() {
    try {
      autoLoad();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return mDownloadMap.iterator();
  }

  public GenomeDownload get(String name) {
    try {
      autoLoad();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return mDownloadMap.get(name);
  }
}
