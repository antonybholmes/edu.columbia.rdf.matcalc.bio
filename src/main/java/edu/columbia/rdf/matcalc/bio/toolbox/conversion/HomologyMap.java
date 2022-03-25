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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.TreeSetCreator;

// TODO: Auto-generated Javadoc
/**
 * The Class HomologyMap.
 */
public class HomologyMap {
  
  /** The m id map. */
  private Map<String, Set<String>> mIdMap = DefaultTreeMap
      .create(new TreeSetCreator<String>());

  /**
   * Adds a mapping between two ids.
   *
   * @param id1 the id 1
   * @param id2 the id 2
   */
  public void map(String id1, String id2) {
    id1 = GenesMap.santize(id1);
    id2 = GenesMap.santize(id2);
    
    mIdMap.get(id1).add(id2);
    
    // May not be necessary
    mIdMap.get(id2).add(id1);
  }

  /**
   * Homology.
   *
   * @param c the c
   * @return the list
   */
  public List<Conversion> homology(Conversion c) {
    List<Conversion> ret = new ArrayList<Conversion>();

    String id = c.getId();

    for (String newId : mIdMap.get(id)) {
      ret.add(new Conversion(c, newId, "hom:" + newId));
    }

    return ret; // new Conversion(ret, c, "homology");
  }

  /**
   * Contains.
   *
   * @param id the id
   * @return true, if successful
   */
  public boolean contains(String id) {
    return mIdMap.containsKey(id.toLowerCase());
  }

  /**
   * Contains.
   *
   * @param c the c
   * @return true, if successful
   */
  public boolean contains(Conversion c) {
    return contains(c.getId());
  }

  /**
   * Prints the keys.
   *
   * @return the string
   */
  public String printKeys() {
    return mIdMap.keySet().toString();
  }
}
