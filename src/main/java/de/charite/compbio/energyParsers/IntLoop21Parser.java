package de.charite.compbio.energyParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class parses a file for internal 2x1 loops for calculation of the free energy of RNA-Duplexes
 * 
 * @author marcel87
 * @version 1.0
 * 
 */
public class IntLoop21Parser extends EnergyParser {

	private double[] int21 = new double[6 * 4 * 4 * 6 * 4];
	// v w x y z
	private String v = "";
	private String w = "";
	private String x = "ACGU";
	private String y = "";
	private String z = "ACGU";
	private int[] idx = { 0, 0, 0, 0, 0 };
	// idx: [0,0,0,0,0]
	// var: v w x y z
	// max: 5 3 3 5 3

	/**
	 * this way:<br>
	 * 5' --> 3'<br>
	 * x<br>
	 * v y<br>
	 * v y<br>
	 * z w<br>
	 * 3' <-- 5'<br>
	 * <br>
	 * !different to labeling in int11.dat!
	 * */

	private final static String regexHeader = "(\\p{Blank}+?Y\\p{Blank}+?){2,}"; /* Y Y */
	private final static String regexSep = "(\\p{Blank}+\\-+\\p{Blank}+?)+\\p{Blank}*"; /*
																						 * ------------------
																						 * ------------------
																						 */
	private final static String regexBases = "(\\p{Blank}+[ACGU])+\\p{Blank}*"; /* A C G U A C G U */
	private final static String regexUpperBody = "(\\p{Blank}+?X\\p{Blank}+?){2,}"; /* AX AX */
	private final static String regexMidBody = "[ACGU]\\p{Blank}+[ACGU]"; /* AX AX */
	private final static String regexLowerBody = "Y[ACGU]"; /* AY UY */
	private final static String regexLastLine = "(\\p{Blank}+?3' <-- 5')+\\p{Blank}+";
	private final static String regexData = "((\\-?\\d?\\.\\d*)+\\p{Blank}*)+"; /* . . . -1.10 . -1.40 . */
	private final static String regexDataValues = "-?\\d{0,2}\\.\\d{0,2}";

	private Pattern pattHeader;
	private Pattern pattSep;
	private Pattern pattBases;
	private Pattern pattUpperBody;
	private Pattern pattMidBody;
	private Pattern pattLowerBody;
	private Pattern pattLastLine;
	private Pattern pattData;
	private Pattern pattDataValues;

	/**
	 * 
	 */
	public IntLoop21Parser() {
		super();
		// this("data/mfold/int21.dat");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param infile
	 */
	public IntLoop21Parser(String infile) {
		super(infile);
		// initialize
		if (!initialize()) {
			System.err.println("Error initializing the data. Will exit.");
			System.exit(2);
		}
	}

	/**
	 * 
	 */
	@Override
	public boolean init_small() {
		idx = new int[5];
		maxIdx = new int[5];

		// fill table with D_INF
		for (int i = 0; i < idx.length; i++) {
			idx[i] = 0;
			maxIdx[i] = N_BASES;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[3] = N_BASEPAIRS;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see energyParsers.EnergyParser#initialize()
	 */
	@Override
	protected boolean initialize() {

		idx = new int[5];
		maxIdx = new int[5];

		// fill table with D_INF
		for (int i = 0; i < this.int21.length; i++) {
			if (i < idx.length) {
				idx[i] = 0;
				maxIdx[i] = N_BASES;
			}
			this.int21[i] = D_INF;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[3] = N_BASEPAIRS;

		v = "";
		w = "";
		x = "ACGU";
		y = "";
		z = "ACGU";

		this.pattHeader = Pattern.compile(regexHeader);
		this.pattSep = Pattern.compile(regexSep);
		this.pattBases = Pattern.compile(regexBases);
		this.pattUpperBody = Pattern.compile(regexUpperBody);
		this.pattMidBody = Pattern.compile(regexMidBody);
		this.pattLowerBody = Pattern.compile(regexLowerBody);
		this.pattLastLine = Pattern.compile(regexLastLine);
		this.pattData = Pattern.compile(regexData);
		this.pattDataValues = Pattern.compile(regexDataValues);

		this.isInitialized = true;

		return true;
	}

	/**
	 * this method starts the parsing of the file
	 * 
	 * @return the object, which it is called from
	 */
	public IntLoop21Parser getData() {
		try {
			if (!parseFile())
				System.err.println("error while parsing file " + this.infile);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see energyParsers.EnergyParser#parseFile()
	 */
	@Override
	protected boolean parseFile() throws IOException {

		System.out.println("start parsing " + this.infile);

		BufferedReader inbuff = new BufferedReader(new FileReader(infile));
		String line;
		Matcher m;

		while ((line = inbuff.readLine()) != null) { // readLine() reads line to line end, but removes symbol for line
														// ending!

			m = pattHeader.matcher(line);
			if (m.find()) {
				line = inbuff.readLine();
				m = pattSep.matcher(line);
				if (m.find()) {
					line = inbuff.readLine();
					m = pattBases.matcher(line);
					if (m.find()) {
						line = inbuff.readLine();
						m = pattSep.matcher(line);
						if (m.find()) {
							// header complete
							boolean lastLine = false;
							while (!lastLine) {
								line = inbuff.readLine();
								m = pattUpperBody.matcher(line);
								if (m.find()) {
									// System.out.println("upper body found!");
								}
								m = pattMidBody.matcher(line);
								if (m.find()) {
									// System.out.println("Found MidBody!");
									StringBuilder sbv = new StringBuilder(v), /* sbw = new StringBuilder(w), */sby = new StringBuilder(
											y);
									sbv.append(m.group().charAt(0));
									sby.append(m.group().charAt(m.group().length() - 1));

									while (m.find()) {
										sbv.append(m.group().charAt(0));
										sby.append(m.group().charAt(m.group().length() - 1));
									}
									line = inbuff.readLine();
									m = pattMidBody.matcher(line);
									int i = 0;
									while (m.find()) {
										sbv.insert(2 * i + 1, m.group().charAt(0));
										sby.insert(2 * i + 1, m.group().charAt(m.group().length() - 1));
										i++;
									}
									v = sbv.toString();
									y = sby.toString();
									// System.out.println("Basepairs are: " + v);
									// System.out.println("and: " + y);

								}

								m = pattLowerBody.matcher(line);
								while (m.find()) {
									w += m.group().charAt(m.group().length() - 1);
								}

								m = pattLastLine.matcher(line);
								if (m.find()) {
									lastLine = true;
								}

							}
							// System.out.println("other bases: " + w);

							// parse data values
							// System.out.println("try parsing data values ...");

							for (int i = 0; i < N_BASES; i++) {
								line = inbuff.readLine();
								m = pattData.matcher(line);
								Matcher data;
								if (m.find()) {
									data = pattDataValues.matcher(line);
									while (data.find()) {

										if (!data.group().equals(".")) {
											String bases = "" + v.substring(2 * idx[0], 2 * idx[0] + 2)
													+ w.charAt(idx[1]) + x.charAt(idx[2])
													+ y.substring(2 * idx[3], 2 * idx[3] + 2) + z.charAt(idx[4]);
											// System.out.println("insert " + data.group() + " into pos: " +
											// calcIdx(bases));
											int21[calcIdx(bases, maxIdx)] = Double.parseDouble(data.group());
										}
										idx = increment(idx, maxIdx);
									}
								}
							}

							v = "";
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
		// 6-4-4-6-4
		int i = 0;
		i += encode(bases.substring(0, 2)) * N_BASES * N_BASES * N_BASEPAIRS * N_BASES;
		i += encode(bases.charAt(2)) * N_BASES * N_BASEPAIRS * N_BASES;
		i += encode(bases.charAt(3)) * N_BASES * N_BASEPAIRS;
		i += encode(bases.substring(4, 6)) * N_BASES;
		i += encode(bases.charAt(6)) * 1; // Math.pow(N_BASES, 0);
		return i;
	}

	@Override
	protected int[] increment(int[] idx) {
		// 6-4-4-6-4
		// int i = idx.length-1;
		idx[4]++;
		if (idx[4] >= N_BASES) {
			idx[4] = 0;
			idx[3]++;
		}
		if (idx[3] >= N_BASEPAIRS) {
			idx[3] = 0;
			idx[2]++;
		}
		if (idx[2] >= N_BASES) {
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

	/**
	 * sets the path to file with data-values
	 * 
	 * @param infile
	 *            path/to/file
	 */
	// @Override
	// public IntLoop21Parser setInfile(String infile) {
	// this.infile = new File(infile);
	// if (!this.infile.exists()) {
	// System.err.println("file \"" + this.infile.toString() + "\" does not exist. Will exit.");
	// System.exit(-1);
	// }
	// return this;
	// }

	/**
	 * sets the path to file with data-values
	 * 
	 * @param infile
	 *            (Java) File
	 */
	// @Override
	// public IntLoop21Parser setInfile(File infile) {
	// this.infile = infile;
	// return this;
	// }

	/**
	 * 
	 * @return value
	 */
	public double[] getInt21() {
		return int21;
	}

	/**
	 * 
	 * @param idx
	 * @return value
	 */
	public double getInt11LoopValue(int idx) {
		if (idx < int21.length)
			return int21[idx];
		else
			return Double.MAX_VALUE;
	}

	/**
	 * 
	 * @param int21
	 * @return value
	 */
	public IntLoop21Parser setInt21(double[] int21) {
		this.int21 = int21;
		return this;
	}

	/**
	 * bases are given the following way:
	 * 
	 * <pre>
	 * 5' --> 3'
	 *      s1
	 *  s0/    \s2
	 *  s6\    /s3
	 *     s5s4
	 *  3' <-- 5'
	 * </pre>
	 * 
	 * @param bases
	 *            String of bases forming the loop
	 * @return energy-value for this loop
	 */
	public double getInt21LoopValue(String bases) {
		if (bases.contains("&")) {
			String[] parts = bases.split("&");
			int l0 = parts[0].length(), l1 = parts[1].length();
			bases = l0 < l1 ? parts[0] + parts[1] : parts[1] + parts[0];
		}
		if (bases.length() != 7) {
			System.err.println("Internal 2x1 Loop must have length of 7!");
			return Double.MAX_VALUE;
		} else {
			bases = "" + bases.charAt(0) + bases.charAt(6) + bases.charAt(4) + bases.substring(1, 4) + bases.charAt(5);
			// System.out.println("re-ordered bases: " + bases);
			return int21[calcIdx(bases)];
		}
	}

}
