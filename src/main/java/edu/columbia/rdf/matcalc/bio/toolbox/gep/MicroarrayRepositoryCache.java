/**
 * Copyright 2016 Antony Holmes
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
package edu.columbia.rdf.matcalc.bio.toolbox.gep;

import java.io.UnsupportedEncodingException;

import org.jebtk.core.path.Path;

import edu.columbia.rdf.edb.EDBWLogin;
import edu.columbia.rdf.edb.ui.RestrictedRepositoryCache;

/**
 * The class ChipSeqRepositoryCache.
 */
public class MicroarrayRepositoryCache extends RestrictedRepositoryCache {

  private static final Path ALL_PATH = new Path("/Microarray/All");

  /**
   * Instantiates a new chip seq repository cache.
   *
   * @param login the login
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public MicroarrayRepositoryCache(EDBWLogin login)
      throws UnsupportedEncodingException {
    super(login, ALL_PATH);
  }
}
