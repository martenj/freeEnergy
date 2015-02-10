package de.charite.compbio.energyParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class parses a file for internal 2x2 loops for calculation of the free energy of RNA-Duplexes
 * 
 * @author marcel87
 * @version 1.0
 *
 */
public class IntLoop22Parser extends EnergyParser {

	private double[] int22 = new double[N_BASEPAIRS * N_BASEPAIRS * (int) Math.pow(N_BASES, 4)];

	// idx: [0,0,0,0,0,0]
	// var: w x y-y z-z
	// min: 0 0 0 0 0 0
	// max: 5 5 3 3 3 3

	/*
	 * this way: 5' ------> 3' y z w x w x y z 3' <------ 5'
	 * 
	 * !different to labeling in int11.dat!
	 */

	private final static String regexHeader = "\\p{Blank}+Y\\p{Blank}+"; /* Y */
	private final static String regexSep = "\\p{Blank}+\\-+\\p{Blank}+"; /* ------------------ ------------------ */
	private final static String regexBases = "(\\p{Blank}+[ACGU])+\\p{Blank}+"; /* A C G U A C G U */
	// TODO: check, how to find a "\ " with regexps ...
	private final static String regexUpperBody = "\\p{Blank}+[ACGU]\\p{Blank}+./\\p{Blank}+._/\\p{Blank}+[ACGU]\\p{Blank}+"; /*
																															 * AX
																															 * AX
																															 */
	// private final static String regexMidBody = ""; /* AX AX */
	private final static String regexLowerBody = "\\p{Blank}+[ACGU]\\p{Blank}+/.\\p{Blank}+\\|\\p{Blank}+[ACGU]\\p{Blank}+"; /*
																															 * AY
																															 * UY
																															 */
	private final static String regexLastLine = "\\p{Blank}+3'\\p{Blank}<-+\\p{Blank}5'\\p{Blank}+";
	private final static String regexData = "(\\-?\\d{0,2}\\.\\d{0,2}\\p{Blank}*)+"; /* . . . -1.10 . -1.40 . */
	private final static String regexDataValues = "\\-?\\d{0,2}\\.\\d{0,2}";

	private Pattern pattHeader;
	private Pattern pattSep;
	private Pattern pattBases;
	private Pattern pattUpperBody;
	// private Pattern pattMidBody;
	private Pattern pattLowerBody;
	private Pattern pattLastLine;
	private Pattern pattData;
	private Pattern pattDataValues;

	public IntLoop22Parser() {
		super();
		// this("data/mfold/int22.dat");
	}

	public IntLoop22Parser(String infile) {
		super(infile);
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
	public IntLoop22Parser getData() {
		try {
			if (!parseFile())
				System.err.println("error while parsing file " + this.infile);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public boolean init_small() {
		idx = new int[6];
		maxIdx = new int[6];

		// fill with Inf-values
		for (int i = 0; i < idx.length; i++) {
			idx[i] = 0;
			maxIdx[i] = N_BASES;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[1] = N_BASEPAIRS;

		return true;
	}

	@Override
	protected boolean initialize() {

		idx = new int[6];
		maxIdx = new int[6];

		// fill with Inf-values
		for (int i = 0; i < this.int22.length; i++) {
			if (i < idx.length) {
				idx[i] = 0;
				maxIdx[i] = N_BASES;
			}
			this.int22[i] = D_INF;
		}

		maxIdx[0] = N_BASEPAIRS;
		maxIdx[1] = N_BASEPAIRS;

		w = "";
		x = "";
		y = "AAACAGAUCACCCGCUGAGCGGGUUAUCUGUU";
		z = "AAACAGAUCACCCGCUGAGCGGGUUAUCUGUU";

		pattHeader = Pattern.compile(regexHeader);
		pattSep = Pattern.compile(regexSep);
		pattBases = Pattern.compile(regexBases);
		pattUpperBody = Pattern.compile(regexUpperBody);
		pattLowerBody = Pattern.compile(regexLowerBody);
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

		while ((line = inbuff.readLine()) != null) {
			m = pattHeader.matcher(line);
			if (m.matches()) {
				line = inbuff.readLine();
				m = pattSep.matcher(line);
				if (m.matches()) {
					line = inbuff.readLine();
					m = pattBases.matcher(line);
					if (m.matches()) {
						line = inbuff.readLine();
						m = pattBases.matcher(line);
						if (m.matches()) {
							line = inbuff.readLine();
							m = pattSep.matcher(line);
							if (m.matches()) {
								// System.out.println("HEADER FOUND!");
								boolean lastLine = false;
								String[] s; // container to store temporary split() bases
								// parse for bases
								while (!lastLine) {
									line = inbuff.readLine();
									m = pattUpperBody.matcher(line);
									if (m.matches()) {
										// System.out.println("upper body found");
										// m.find();
										s = m.group().split("\\p{Blank}+");
										// System.out.println("upper body base 0: " + s[1] + " base 1: " +
										// s[s.length-1]);
										w += s[1];
										x += s[s.length - 1];
									}
									m = pattLowerBody.matcher(line);
									if (m.matches()) {
										// System.out.println("lower body found");
										s = m.group().split("\\p{Blank}+");
										// System.out.println("lower body base 0: " + s[1] + " base 1: " +
										// s[s.length-1]);
										w += s[1];
										x += s[s.length - 1];
									}
									// check if last line of the header is reached
									m = pattLastLine.matcher(line);
									lastLine = m.matches();
								}
								// System.out.println("w: " + w);
								// System.out.println("x: " + x);
								// parse for data-values:
								boolean dataValues = true;
								Matcher data;
								while (dataValues) {
									line = inbuff.readLine();
									if (line != null) {
										// System.out.println("line: " + line);
										m = pattData.matcher(line);
										if (m.find()) {
											data = pattDataValues.matcher(line);
											while (data.find()) {
												int b1 = 2 * N_BASES * idx[2] + 2 * idx[3], b2 = 2 * N_BASES * idx[4]
														+ 2 * idx[5];
												String bases = "" + w + x + y.substring(b1, b1 + 2)
														+ z.substring(b2, b2 + 2);
												// System.out.println("data value: " + data.group());
												// System.out.println("write into field: " + calcIdx(bases, maxIdx));
												int22[calcIdx(bases, maxIdx)] = Double.parseDouble(data.group());
												idx = increment(idx, maxIdx);
											}
										} else {
											dataValues = false;
										}
									} else {
										break;
									}
								}
							}
							w = "";
							x = "";
						}
					}
				}
			}
		}

		System.out.println("ended parsing");

		return true;
	}

	public double[] getInt22() {
		return int22;
	}

	public double getInt22LoopValue(int idx) {
		if ((idx >= 0) && (idx < int22.length)) {
			return int22[idx];
		} else {
			System.err.println("Warning: Array index out of range! Return +Inf.");
			return D_INF;
		}
	}

	public double getInt22LoopValue(String bases) {
		if (bases.contains("&")) {
			bases = bases.replace("&", "");
		}
		if (bases.length() != 8) {
			System.err.println("internal 2x2 loop must consist of 8 bases!");
			return Double.MAX_VALUE;
		}
		if (this.isInitialized) {
			/*
			 * convert AUUAUGUU 01234567 -> A U U A U G U U <pre> 5' --> 3' s1-s2 s0/ \s3 s7\ /s4 s6-s5 3' <-- 5' </pre>
			 */
			int[] order = { 0, 7, 3, 4, 1, 6, 2, 5 };
			String b = "";
			for (int i : order) {
				b += bases.charAt(i);
			}
			// System.out.println("bases: " + b);
			int idx = calcIdx(b, maxIdx);
			if (idx >= 0 & idx < int22.length) {
				return int22[idx];
			} else {
				return D_INF;
			}

		} else {
			System.err.println("Warning: data is not initialized! Run \"getData()\" first!");
			return D_INF;
		}
	}

	/**
	 * set the values for the free energy of internal 2x2 loops
	 * 
	 * @param int22
	 *            array of doubles with pre-filled values
	 * 
	 *            use with caution only, wrong values could be stored!
	 */
	public void setInt22(double[] int22) {
		this.int22 = int22;
		this.isInitialized = true;
	}

}
