package edu.columbia.rdf.matcalc.bio.toolbox.dna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jebtk.bioinformatics.FastaReader;
import org.jebtk.bioinformatics.genomic.Chromosome;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.ChromosomeService;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.IterTreeMap;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodeExt2Bit {

  // private static final Map<Character, Integer> ENCODE_MAP =
  // new HashMap<Character, Integer>();

  private static final byte[] ENCODE_MAP = new byte[Character.MAX_VALUE];

  static {
    /*
     * ENCODE_MAP.put('A', 0); ENCODE_MAP.put('C', 1); ENCODE_MAP.put('G', 2);
     * ENCODE_MAP.put('T', 3); ENCODE_MAP.put('a', 0); ENCODE_MAP.put('c', 1);
     * ENCODE_MAP.put('g', 2); ENCODE_MAP.put('t', 3); ENCODE_MAP.put('N', 0);
     * ENCODE_MAP.put('n', 0);
     */

    ENCODE_MAP['A'] = 0;
    ENCODE_MAP['C'] = 1;
    ENCODE_MAP['G'] = 2;
    ENCODE_MAP['T'] = 3;
    ENCODE_MAP['a'] = 0;
    ENCODE_MAP['c'] = 1;
    ENCODE_MAP['g'] = 2;
    ENCODE_MAP['t'] = 3;
    // ENCODE_MAP.put('N', 0);
    // ENCODE_MAP.put('n', 0);
  }

  // private char[] mCBuf;

  // private byte[] mN;

  // private byte[] mMask;

  // private byte[] mDna;

  private static final Logger LOG = LoggerFactory
      .getLogger(EncodeExt2Bit.class);

  private static final String N_FILE_EXT = ".n.1bit";

  private static final String MASK_EXT = ".mask.1bit";

  private static final String DNA_EXT = ".dna.2bit";

  public static Path encodeGenome(Genome genome, Path dir, Path outDir)
      throws IOException {
    // Path dir = file.toAbsolutePath().getParent();

    FileUtils.mkdir(outDir);

    List<Path> chrFiles = FileUtils.endsWith(dir, "fa.gz");

    List<Path> files = new ArrayList<Path>();

    char[] buffer = new char[300000000];

    IterMap<Chromosome, Integer> sizeMap = new IterTreeMap<Chromosome, Integer>();

    for (Path file : chrFiles) {
      FastaReader reader = new FastaReader(file);

      int n = reader.next(buffer);

      Chromosome chr = ChromosomeService.getInstance().chr(genome,
          reader.currentName());

      LOG.info("Creating {} in directory {}", chr, outDir);

      LOG.info("Read {} chars.", n);

      byte[] dna = new byte[byten(n, 2)];
      byte[] ns = new byte[byten(n, 1)];
      byte[] mask = new byte[byten(n, 1)];

      char base;
      byte encodedBase;

      int bi = 0;
      int bs = 6;

      for (int i = 0; i < n; ++i) {
        // The first letter is written into the first 2 bits of the byte
        // so we must shift to the last two msb of the btye. Each block is 2
        // bits hence the shift is
        // multiplied by 2
        base = buffer[i];

        encodedBase = ENCODE_MAP[base];

        dna[bi] = (byte) (dna[bi] | (encodedBase << bs));

        bs -= 2;

        if (i % 4 == 3) {
          ++bi;
          bs = 6;
        }
      }

      // N

      bi = 0;
      bs = 7;

      for (int i = 0; i < n; ++i) {
        base = buffer[i];

        if (base == 'n' || base == 'N') {
          encodedBase = 1;
        } else {
          encodedBase = 0;
        }

        ns[bi] = (byte) (ns[bi] | (encodedBase << bs));

        --bs;

        if (i % 8 == 7) {
          ++bi;
          bs = 7;
        }
      }

      //
      // Mask
      //

      bi = 0;
      bs = 7;

      // int count = 0;

      /// CountMap<Integer> map = new CountMap<Integer>();

      for (int i = 0; i < n; ++i) {
        // The first letter is written into the first 2 bits of the byte
        // so we must shift it. Each block is 2 bits hence the shift is
        // multiplied by 2
        // int bitShift = blockIndex(i, 2) * 2;

        base = buffer[i];

        switch (base) {
        case 'a':
        case 'c':
        case 'g':
        case 't':
        case 'u':
        case 'n':
          encodedBase = 1;
          break;
        default:
          encodedBase = 0;
          break;
        }

        mask[bi] = (byte) (mask[bi] | (encodedBase << bs));

        --bs;

        if (i % 8 == 7) {
          ++bi;
          bs = 7;
        }
      }

      Path out = outDir.resolve(chr + DNA_EXT);
      files.add(out);
      write(out, dna);

      //
      // Now we need to encode the blocks for the N
      //

      out = outDir.resolve(chr + N_FILE_EXT);
      files.add(out);
      write(out, ns);

      out = outDir.resolve(chr + MASK_EXT);
      files.add(out);
      write(out, mask);

      sizeMap.put(chr, n);
    }

    // Create a zip

    Path out = outDir.resolve(genome + ".dna.zip");

    FileUtils.zip(out, files);

    // Remove outputs

    for (Path file : files) {
      Files.delete(file);
    }

    // Write the size file

    writeChrs(genome, sizeMap, outDir);

    return out;
  }

  private static void writeChrs(Genome genome,
      IterMap<Chromosome, Integer> sizeMap,
      Path dir) throws IOException {
    BufferedWriter writer = FileUtils
        .newBufferedWriter(dir.resolve(genome + ".chrs.gz"));

    try {
      writer.write(genome.getAssembly());
      writer.newLine();
      writer.write(genome.getAssembly());
      writer.newLine();
      writer.write("id\tchr\tsize");
      writer.newLine();

      for (Entry<Chromosome, Integer> item : sizeMap) {
        writer.write(Integer.toString(item.getKey().getId()));
        writer.write(TextUtils.TAB_DELIMITER);
        writer.write(item.getKey().toString());
        writer.write(TextUtils.TAB_DELIMITER);
        writer.write(Integer.toString(item.getValue()));
        writer.newLine();
      }
    } finally {
      writer.close();
    }
  }

  public void encode(Path file, Chromosome chr, Path dir) throws IOException {
    // Path dir = file.toAbsolutePath().getParent();

    LOG.info("Creating {} in directory {}", chr, dir);

    int n = -1;

    // Enough to store a human chr1
    char[] buffer = new char[300000000];

    BufferedReader reader = FileUtils.newBufferedReader(file);

    try {
      n = reader.read(buffer);
    } finally {
      reader.close();
    }

    LOG.info("Read {} chars.", n);

    byte[] dna = new byte[byten(n, 2)];
    byte[] ns = new byte[byten(n, 1)];
    byte[] mask = new byte[byten(n, 1)];

    char base;
    byte encodedBase;

    int bi = 0;
    int bs = 6;

    for (int i = 0; i < n; ++i) {
      // The first letter is written into the first 2 bits of the byte
      // so we must shift to the last two msb of the btye. Each block is 2 bits
      // hence the shift is
      // multiplied by 2
      // int bitShift = blockIndex(i, 2) * 2;

      base = buffer[i];

      /*
       * if (ENCODE_MAP.containsKey(base)) { encodedBase = ENCODE_MAP[base]; }
       * else { encodedBase = 0; }
       */

      encodedBase = ENCODE_MAP[base];

      dna[bi] = (byte) (dna[bi] | (encodedBase << bs));

      bs -= 2;

      if (i % 4 == 3) {
        ++bi;
        bs = 6;
      }
    }

    // N

    bi = 0;
    bs = 7;

    for (int i = 0; i < n; ++i) {
      base = buffer[i];

      if (base == 'n' || base == 'N') {
        encodedBase = 1;
      } else {
        encodedBase = 0;
      }

      ns[bi] = (byte) (ns[bi] | (encodedBase << bs));

      --bs;

      if (i % 8 == 7) {
        ++bi;
        bs = 7;
      }
    }

    //
    // Mask
    //

    bi = 0;
    bs = 7;

    // int count = 0;

    /// CountMap<Integer> map = new CountMap<Integer>();

    for (int i = 0; i < n; ++i) {
      // The first letter is written into the first 2 bits of the byte
      // so we must shift it. Each block is 2 bits hence the shift is
      // multiplied by 2
      // int bitShift = blockIndex(i, 2) * 2;

      base = buffer[i];

      switch (base) {
      case 'a':
      case 'c':
      case 'g':
      case 't':
      case 'u':
      case 'n':
        encodedBase = 1;
        break;
      default:
        encodedBase = 0;
        break;
      }

      mask[bi] = (byte) (mask[bi] | (encodedBase << bs));

      --bs;

      if (i % 8 == 7) {
        ++bi;
        bs = 7;
      }
    }

    Path out = dir.resolve(chr + DNA_EXT);
    write(out, dna);

    //
    // Now we need to encode the blocks for the N
    //

    out = dir.resolve(chr + N_FILE_EXT);
    write(out, ns);

    out = dir.resolve(chr + MASK_EXT);
    write(out, mask);
  }

  public static void write(Path file, byte[] data) throws IOException {
    DataOutputStream stream = FileUtils.newDataOutputStream(file);

    try {
      stream.write(data);
    } finally {
      stream.close();
    }
  }

  /**
   * Return number of bytes required to encode characters.
   * 
   * @param n
   * @param bits
   * @return
   */
  public static int byten(int n, int bits) {
    switch (bits) {
    case 1:
      return (int) Math.ceil(n / 8.0);
    case 2:
      return (int) Math.ceil(n / 4.0);
    case 4:
      return (int) Math.ceil(n / 2.0);
    case 8:
      return n;
    case 16:
      return n * 2;
    default:
      // 32
      return n * 4;
    }
  }
}
