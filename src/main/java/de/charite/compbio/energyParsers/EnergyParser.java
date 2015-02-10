package de.charite.compbio.energyParsers;

import java.io.File;
import java.io.IOException;

/**
 * superclass for energy-parsers used to read energy-values for RNA-duplexes from textfiles provides a parent class with
 * implementations of some common-used methods
 * 
 * @author marcel87
 * @version 1.0 beta
 * 
 */
public abstract class EnergyParser {

	/** number of bases, usually 4 */
	protected static final int N_BASES = 4;
	/** number of possible basepairs, usually 6 (AU,CG, GC, UA, GU, UG) */
	protected static final int N_BASEPAIRS = 6;
	/** maximal integer-value for initialization of arrays */
	protected static final int INF = Integer.MAX_VALUE;
	/** maximal double-value for initialization of arrays */
	protected static final double D_INF = Double.MAX_VALUE;

	/** file with energy-values to read */
	protected File infile;

	/** if the infile is validated/exists */
	protected boolean validInfile = false;
	/** if the energyParsers is already initialized */
	protected boolean isInitialized = false;

	// private String outfile = null;

	/**
	 * strings which contain the current bases usually the following order is used: v - changes most seldom ... z -
	 * changes fastest
	 */
	protected String v, w, x, y, z;

	/** index within the strings v-z and for calculation of index in array to store data */
	protected int idx[];
	/** maximal possible changes of index-fields */
	protected int[] maxIdx;

	public EnergyParser() {

	}

	/**
	 * creates a energyParsers for different energy-files to parse
	 * 
	 * @param infile
	 *            path to file with energy-values
	 */
	public EnergyParser(String infile) {
		// 1st) initialize
		this.setInfile(infile);
		// this.infile = new File(infile);
		// if (!this.infile.exists()) {
		// System.err.println("file \"" + this.infile.toString()
		// + " does not exist. Will exit.");
		// System.exit(-1);
		// }
	}

	/**
	 * initializes data values - ONLY FOR USE WITH PREPARED ENERGY-VALUES e.g. StackingEnergyClass, IntLoop22EnergyClass
	 * ...
	 * 
	 * @return true when no errors occur
	 */
	public abstract boolean init_small();

	/**
	 * initializes data values
	 * 
	 * @return true when no errors occur
	 */
	protected abstract boolean initialize();

	/**
	 * starts parsing the file
	 * 
	 * @return this
	 */
	public abstract EnergyParser getData();

	/**
	 * parses the data-file for its energy-values for internal use only intended
	 * 
	 * @return true when no errors occur
	 * @throws IOException
	 */
	protected abstract boolean parseFile() throws IOException;

	/**
	 * encodes the bases into numbers code: A=0, ... U=3
	 * 
	 * @param b
	 *            base name of base as character, e.g. 'A' or 'g'
	 * @return byte-code for each base
	 */
	protected int encode(char b) {

		switch (Character.toUpperCase(b)) {
		case 'A':
			return 0;
		case 'C':
			return 1;
		case 'G':
			return 2;
		case 'U':
			return 3;
		case 'T':
			return 3;
		default:
			return -1;
		}
	}

	/**
	 * encodes a basebair into a number code: AU=0, UA=1, ... UG=5
	 * 
	 * @param bases
	 *            basepair means e.g. "AU", "CG" or "UG"
	 * @return byte-code for basepair
	 */
	protected int encode(String bases) {
		int i;
		if (bases.equals("AU"))
			i = 0;
		else if (bases.equals("CG"))
			i = 1;
		else if (bases.equals("GC"))
			i = 2;
		else if (bases.equals("UA"))
			i = 3;
		else if (bases.equals("GU"))
			i = 4;
		else if (bases.equals("UG"))
			i = 5;
		else {
			i = -1;
			System.err.println("bases " + bases + " cant pair!");
		}
		// System.out.println("encode " + bases + " into " + i);
		return i;
	}

	/**
	 * used to increment a multi-dimensional array with a maximum-value for each dimension example: (max=3)<br>
	 * a[0,0,0,0]++ = a[0,0,0,1]<br>
	 * a[0,1,3,3]++ = a[0,2,0,0]<br>
	 * 
	 * @param idx
	 *            array given to increment
	 * @return incremented array
	 */
	@Deprecated
	protected int[] increment(int[] idx) {
		// int l = idx.length;
		int i = idx.length - 1;
		idx[i]++;
		while (idx[i] >= N_BASES) {
			if (i - 1 < 0) {
				break;
				// System.err.println("Index exceeds max range! Exit.");
				// System.exit(-1);
			} else {
				idx[i] = 0;
				idx[i - 1]++;
			}
			i--;
		}
		return idx;
	}

	/**
	 * used to increment a multi-dimensional array for each element in idx there is a maximum-value in maxIdx if this
	 * value is reached, there is an overflow to the next field and the current field is reset
	 * 
	 * @param idx
	 *            index-array
	 * @param maxIdx
	 *            maximum-value, which should not be reached
	 * @return incremented array idx
	 */
	protected int[] increment(int[] idx, int[] maxIdx) {
		// System.out.println("advanced increment " + idx.length + ":" + maxIdx.length);
		int i = idx.length - 1;
		idx[i]++;
		while (idx[i] >= maxIdx[i]) {
			if (i - 1 < 0) {
				// System.out.println("break");
				break;
			} else {
				// System.out.println("next row");
				idx[i] = 0;
				idx[i - 1]++;
			}
			i--;
		}
		return idx;
	}

	/**
	 * 
	 * @param bases
	 * @return
	 */
	@Deprecated
	protected int calcIdx(String bases) {
		// System.out.println("calc index of " + bases);
		int idx = 0;
		for (int i = 0; i < bases.length(); i++) {
			idx += encode(bases.charAt(i)) * Math.pow(N_BASES, bases.length() - i - 1);
		}
		return idx;
	}

	/**
	 * 
	 * @param bases
	 * @param maxIdx
	 * @return
	 */
	protected int calcIdx(String bases, int[] maxIdx) {
		// System.out.print("Calc advanced index of \"" + bases + "\" ... ");
		// System.out.print("idx = 0");
		int idx = 0, l = maxIdx.length;
		int m = 1; // multiplicator
		for (int i = 1, j = bases.length() - 1; i <= l & j >= 0; i++, j--) {
			// System.out.print("[" + maxIdx[l-i] + "]");
			switch (maxIdx[l - i]) {
			case N_BASES:
				// System.out.print("+" + (encode(bases.charAt(j))*m) + "(4*" + bases.charAt(j) + ")");
				idx += encode(bases.charAt(j)) * m;
				m *= N_BASES;
				break;
			case N_BASEPAIRS:
				// System.out.print("+" + (encode(bases.substring(j-1, j+1))*m) + "(6*" + bases.substring(j-1, j+1) +
				// ")");
				idx += encode(bases.substring(j - 1, j + 1)) * m;
				m *= N_BASEPAIRS;
				j--;
				break;
			default:
				System.out.print("???");
				break;
			}
		}
		// System.out.println("\nresult: " + idx);
		return idx;
	}

	/**
	 * sets the path to file with data-values
	 * 
	 * @param infile
	 *            path/to/file
	 */
	public EnergyParser setInfile(String infile) {
		this.setInfile(new File(infile));
		return this;
	}

	/**
	 * sets the path to file with data-values checks if the file exists
	 * 
	 * @param infile
	 *            (Java) File
	 */
	public EnergyParser setInfile(File infile) {
		this.infile = infile;
		if (!this.infile.exists()) {
			System.err.println("file \"" + this.infile.toString() + "\" does not exist. Will exit.");
			System.exit(1);
		}
		this.isInitialized = true;
		return this;
	}

}
