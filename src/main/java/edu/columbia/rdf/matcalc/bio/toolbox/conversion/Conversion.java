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

// TODO: Auto-generated Javadoc
/**
 * The Class Conversion keeps a record of each step in the conversion pipeline
 * so that a record of modifications can be created to track how and why
 * a gene was converted to an newer name.
 */
public class Conversion implements Comparable<Conversion> {

  /** The m id. */
  private String mId;
  
  /** The m type. */
  private String mType;
  
  /** The m parent. */
  private Conversion mParent;

  private String mChr;

  /**
   * Instantiates a new conversion.
   *
   * @param id the id
   * @param type the type
   */
  public Conversion(String id, String type) {
    this(id, null, type);
  }
  
  public Conversion(String id, String chr, String type) {
    this(null, id, chr, type);
  }

  /**
   * Instantiates a new conversion.
   *
   * @param id the id
   * @param parent the parent
   * @param type the type
   */
  public Conversion(Conversion parent, String id, String type) {
   this(parent, id, null, type);
  }
  
  public Conversion(Conversion parent, String id, String chr, String type) {
    mParent = parent;
    mId = GenesMap.santize(id);
    mChr = chr;
    mType = type;
  }

  /**
   * Instantiates a new conversion.
   *
   * @param c the c
   * @param type the type
   */
  public Conversion(Conversion c, String type) {
    this(c, c.getId(), type);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Conversion c) {
    return mId.compareTo(c.mId);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Conversion) {
      return compareTo((Conversion) o) == 0;
    } else {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return mId.hashCode();
  }

  /**
   * Gets the parent.
   *
   * @return the parent
   */
  public Conversion getParent() {
    return mParent;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return mId;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return mType;
  }

  public String getChr() {
    return mChr;
  }
}
