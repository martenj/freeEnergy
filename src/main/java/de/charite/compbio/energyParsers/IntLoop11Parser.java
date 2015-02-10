package de.charite.compbio.energyParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class parses a file for internal 1x1 loops for calculation of the free energy of RNA-Duplexes
 * 
 * @author marcel87
 * @version 1.0
 * 
 */
public class IntLoop11Parser extends EnergyParser {

	private double[] int11 = new double[N_BASEPAIRS * N_BASES * N_BASEPAIRS * N_BASES];

	// idx: [0,0,0,0]
	// var: w x y z
	// min: 0 0 0 0
	// max: 5 3 5 3
	/*
	 * this way: 5' --> 3' x w y w y z 3' <-- 5'
	 * 
	 * !different to labeling in int11.dat!
	 */

	private final static String regexHeader = "(\\p{Blank}+?Y\\p{Blank}+?){2,}"; /* Y Y */
	private final static String regexSep = "(\\p{Blank}+\\-+\\p{Blank}+?)+\\p{Blank}*"; /*
																						 * ------------------
																						 * ------------------
																						 */
	private final static String regexBases = "(\\p{Blank}+[ACGU])+\\p{Blank}*"; /* A C G U A C G U */
	// private final static String regexUpperBody = "(\\p{Blank}+?X\\p{Blank}+?){2,}"; /* AX AX */
	private final static String regexMidBody = "[ACGU]\\p{Blank}[ACGU]"; /* AX AX */
	// private final static String regexLowerBody = "(\\p{Blank}+?Y\\p{Blank}+?){2,}"; /* AY UY */
	private final static String regexLastLine = "(\\p{Blank}+?3' <-- 5')+\\p{Blank}+";
	private final static String regexData = "((\\-?\\d?\\.\\d*)+\\p{Blank}*)+"; /* . . . -1.10 . -1.40 . */
	private final static String regexDataValues = "-?\\d{0,2}\\.\\d{0,2}";

	private Pattern pattHeader;
	private Pattern pattSep;
	private Pattern pattBases;
	// private Pattern pattUpperBody;
	private Pattern pattMidBody;
	// private Pattern pattLowerBody;
	private Pattern pattLastLine;
	private Pattern pattData;
	private Pattern pattDataValues;

	/**
	 * creates a energyParsers for parsing the file of internal 1x1 loops, standart file: data/mfold/int11.dat
	 */
	public IntLoop11Parser() {
		super();
		// this("data/mfold/int11.dat");
	}

	/**
	 * creates a energyParsers for parsing the file of internal 1x1 loops
	 * 
	 * @param filename
	 *            path of file to parse
	 */
	public IntLoop11Parser(String filename) {
		super(filename);
		// initialize
		if (!initialize()) {
			System.err.println("Error initializing the data. Will exit.");
			System.exit(2);
		}
	}

	/**
	 * this method starts the parsing of the file
	 * 
	 * @return the object, which it is called from
	 */
	@Override
	public IntLoop11Parser getData() {
		try {
			if (!parseFile())
				System.err.println("error while parsing file " + this.infile);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public boolean init_small() {
		idx = new int[4];
		maxIdx = new int[4];

		// fill stacking-energies with INF ...
		for (int i = 0; i < idx.length; i++) {
			idx[i] = 0;
			maxIdx[i] = N_BASES;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[2] = N_BASEPAIRS;

		return true;
	}

	@Override
	protected boolean initialize() {
		idx = new int[4];
		maxIdx = new int[4];

		// fill stacking-energies with INF ...
		for (int i = 0; i < this.int11.length; i++) {
			if (i < idx.length) {
				idx[i] = 0;
				maxIdx[i] = N_BASES;
			}
			this.int11[i] = D_INF;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[2] = N_BASEPAIRS;

		w = "";
		x = "ACGU";
		y = "";
		z = "ACGU";

		// compile regular expressions
		pattHeader = Pattern.compile(regexHeader);
		pattSep = Pattern.compile(regexSep);
		pattBases = Pattern.compile(regexBases);
		// pattUpperBody = Pattern.compile(regexUpperBody);
		pattMidBody = Pattern.compile(regexMidBody);
		// pattLowerBody = Pattern.compile(regexLowerBody);
		pattLastLine = Pattern.compile(regexLastLine);
		pattData = Pattern.compile(regexData);
		pattDataValues = Pattern.compile(regexDataValues);

		this.isInitialized = true;

		return true;
	}

	@Override
	protected boolean parseFile() throws IOException {

		System.out.println("start parsing");

		BufferedReader inbuff = new BufferedReader(new FileReader(infile));
		String line;
		Matcher m;
		// Matcher t;

		while ((line = inbuff.readLine()) != null) { // readLine() reads line to line end, but removes symbol for line
														// ending!
		// System.out.println("read line: " + line);

			m = pattHeader.matcher(line);
			// t = pattTest.matcher(line);
			// System.out.println("Matcher: " + m.matches());
			if (m.find()) {
				// System.out.println("Found " + m.group());
				line = inbuff.readLine();
				m = pattSep.matcher(line);
				if (m.find()) {
					// System.out.println("Found: " + m.group());
					line = inbuff.readLine();
					m = pattBases.matcher(line);
					if (m.find()) {
						// System.out.println("Found: " + m.group());
						line = inbuff.readLine();
						m = pattSep.matcher(line);
						if (m.find()) {
							// System.out.println("Found: " + m.group());
							// System.out.println(">>> COMPLETE HEADER FOUND!");
							// set bases w and x
							boolean lastLine = false;
							while (!lastLine) {
								line = inbuff.readLine();
								// match upper line of the body to know the fist base
								// TODO is upperBody nessecary?
								// m = pattUpperBody.matcher(line); // finds multiple "  X  "
								m = pattMidBody.matcher(line);
								// read 2 lines for basepairs
								// TODO: check, if there is a method to see, if a pattern matches
								if (m.find()) {
									// System.out.println("Found MidBody!");
									StringBuilder sbw = new StringBuilder(w), sby = new StringBuilder(y);
									sbw.append(m.group().charAt(0));
									sby.append(m.group().charAt(m.group().length() - 1));

									while (m.find()) {
										sbw.append(m.group().charAt(0));
										sby.append(m.group().charAt(m.group().length() - 1));
									}
									line = inbuff.readLine();
									m = pattMidBody.matcher(line);
									int i = 0;
									while (m.find()) {
										sbw.insert(2 * i + 1, m.group().charAt(0));
										sby.insert(2 * i + 1, m.group().charAt(m.group().length() - 1));
										i++;
									}
									w = sbw.toString();
									y = sby.toString();
									// System.out.println("Basepairs are: " + w);
									// System.out.println("and: " + y);
								}
								// check if body is over
								m = pattLastLine.matcher(line);
								if (m.find()) {
									// System.out.println("Reached last line of body");
									lastLine = true;
								}
							}

							// System.out.println("Try parsing data values");
							for (int i = 0; i < N_BASES; i++) {
								line = inbuff.readLine();
								m = pattData.matcher(line);
								Matcher data;
								if (m.find()) {
									// System.out.println("Data values in line; " + m.group());
									data = pattDataValues.matcher(line);
									while (data.find()) {
										// System.out.println("data record:" + data.group() + " idx: " + idx[0] + idx[1]
										// + idx[2] + idx[3]);
										if (!data.group().equals(".")) {

											// String bases = "" + w.charAt(idx[0]) + x.charAt(idx[1]) +
											// y.charAt(idx[2]) + z.charAt(idx[3]);
											String bases = "" + w.substring(2 * idx[0], 2 * idx[0] + 2)
													+ x.charAt(idx[1]) + y.substring(2 * idx[2], 2 * idx[2] + 2)
													+ z.charAt(idx[3]);
											// System.out.println("Found data value: " + data.group());
											// System.out.println("Write " + bases + " into pos: " + calcIdx(bases));

											this.int11[calcIdx(bases, maxIdx)] = Double.parseDouble(data.group());
										}
										idx = increment(idx, maxIdx);
									}
								}
							}
							w = "";
							y = "";
						}
					}
				}
			}
		}

		System.out.println("ended parsing");

		return true;
	}

	@Override
	protected int calcIdx(String bases) {
		int i = 0;
		i += encode(bases.substring(0, 2)) * N_BASES * N_BASEPAIRS * N_BASES; //
		i += encode(bases.charAt(2)) * N_BASES * N_BASEPAIRS; //
		i += encode(bases.substring(3, 5)) * N_BASES; //
		i += encode(bases.charAt(5)) * 1; //
		return i;
	}

	@Override
	protected int[] increment(int[] idx) {
		//
		// int i = idx.length-1;
		idx[3]++;
		if (idx[3] >= N_BASES) {
			idx[3] = 0;
			idx[2]++;
		}
		if (idx[2] >= N_BASEPAIRS) {
			idx[2] = 0;
			idx[1]++;
		}
		if (idx[1] >= N_BASES) {
			idx[1] = 0;
			idx[0]++;
		}
		if (idx[0] >= N_BASEPAIRS) {
			// break;
		}
		return idx;
	}

	// /**
	// * sets the path to file with data-values
	// * @param infile path/to/file
	// */
	// @Override
	// public IntLoop11Parser setInfile(String infile) {
	// this.infile = new File(infile);
	// if (!this.infile.exists()) {
	// System.err.println("file \"" + this.infile.toString() + "\" does not exist. Will exit.");
	// System.exit(-1);
	// }
	// return this;
	// }

	// /**
	// * sets the path to file with data-values
	// * @param infile (Java) File
	// */
	// @Override
	// public IntLoop11Parser setInfile(File infile) {
	// this.infile = infile;
	// return this;
	// }

	public double[] getInt11() {
		return int11;
	}

	public double getInt11LoopValue(int idx) {
		if (idx < int11.length)
			return int11[idx];
		else
			return Double.MAX_VALUE;
	}

	/**
	 * bases are given the following way:
	 * 
	 * <pre>
	 * 5' --> 3'
	 *     s1
	 *  s0/  \s2
	 *  s5\  /s3
	 *     s4
	 *  3' <-- 5'
	 * </pre>
	 * 
	 * @param bases
	 *            String of bases forming the loop
	 * @return energy-value for this loop
	 */
	public double getInt11LoopValue(String bases) {
		if (bases.contains("&")) {
			bases = bases.replace("&", "");
		}
		if (bases.length() != 6) {
			System.err.println("Internal 1x1 Loop must have length of 6!");
			return Double.MAX_VALUE;
		} else {
			bases = "" + bases.charAt(0) + bases.charAt(5) + bases.substring(1, 5);
			// System.out.println("re-ordered bases: " + bases);
			return int11[calcIdx(bases)];
		}
	}

	public void setInt11(double[] int11) {
		this.int11 = int11;
	}
}
