package edu.columbia.rdf.matcalc.toolbox.motifs;

public class Triplet implements Comparable<Triplet> {
  public int triplet;
  public int offset;

  @Override
  public int compareTo(Triplet t) {
    if (triplet > t.triplet) {
      return 1;
    } else if (triplet < t.triplet) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public int hashCode() {
    return triplet + offset;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Triplet) {
      Triplet t = (Triplet) o;
      return compareTo(t) == 0 && offset == t.offset;
    } else {
      return false;
    }
  }
}
