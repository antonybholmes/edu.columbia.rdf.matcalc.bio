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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.TreeSetCreator;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneMapping.
 */
public class GeneMapping {
  
  /** The m offical id map. */
  private IterMap<String, Set<String>> mOfficalIdMap = DefaultHashMap
      .create(new TreeSetCreator<String>());

  /**
   * Gets the all the ids regardless of type.
   *
   * @param mType the type
   * @return the ids
   */
  public Collection<String> getIds() {
    Set<String> ret = new TreeSet<String>();
    
    for (Entry<String, Set<String>> item : mOfficalIdMap) {
      ret.addAll(item.getValue());
    }
    
    return ret;
  }
  
  public Collection<String> getIds(String type) {
    return mOfficalIdMap.get(type);
  }
}
