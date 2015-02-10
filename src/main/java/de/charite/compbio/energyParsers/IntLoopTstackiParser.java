/**
 * 
 */
package de.charite.compbio.energyParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author marcel87
 *
 */
public class IntLoopTstackiParser extends EnergyParser {

	/** contains data-values from file */
	private double[] tstacki37 = new double[(int) Math.pow(N_BASES, N_BASES)];

	/*
	 * this way: 5' --> 3' w x y z 3' <-- 5'
	 * 
	 * !different to labeling in tstacki.dat!
	 */

	// private final static String regexTest = "\\p{Blank}+?[ACGUXY]\\p{Blank}+?";
	private final static String regexHeader = "(\\p{Blank}+?Y\\p{Blank}+?){2,}"; /* Y Y */
	private final static String regexSep = "(\\p{Blank}+\\-+\\p{Blank}+?)+\\p{Blank}*"; /*
																						 * ------------------
																						 * ------------------
																						 */
	private final static String regexBases = "(\\p{Blank}+[ACGU])+\\p{Blank}*"; /* A C G U A C G U */
	private final static String regexUpperBody = "\\p{Blank}+?[ACGU]X"; /* AX AX */
	private final static String regexLowerBody = "\\p{Blank}+?[ACGU]Y"; /* AY UY */
	private final static String regexLastLine = "(\\p{Blank}+?3' <-- 5')+\\p{Blank}+";
	private final static String regexData = "((\\-?\\d?\\.\\d*)+\\p{Blank}*)+"; /* . . . -1.10 . -1.40 . */
	private final static String regexDataValues = "-?\\d{0,2}\\.\\d{0,2}";

	// private Pattern pattTest = Pattern.compile(regexTest);
	private Pattern pattHeader; //
	private Pattern pattSep;
	private Pattern pattBases;
	private Pattern pattUpperBody;
	private Pattern pattLowerBody;
	private Pattern pattLastLine;
	private Pattern pattData;
	private Pattern pattDataValues;

	/**
	 * 
	 */
	public IntLoopTstackiParser() {
		// TODO Auto-generated constructor stub
		super();
	}

	/**
	 * @param infile
	 */
	public IntLoopTstackiParser(String infile) {

		super(infile);

		if (!initialize()) {
			System.err.println("Error initializing the data. Will exit.");
			System.exit(2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see energyParsers.EnergyParser#init_small()
	 */
	@Override
	public boolean init_small() {
		idx = new int[4];
		maxIdx = new int[4];

		for (int i = 0; i < idx.length; i++) {
			idx[i] = 0;
			maxIdx[i] = N_BASES;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see energyParsers.EnergyParser#initialize()
	 */
	@Override
	protected boolean initialize() {

		idx = new int[4];
		maxIdx = new int[4];

		// 1st) fill stacking-energies with INF ...
		for (int i = 0; i < this.tstacki37.length; i++) {
			if (i < idx.length) {
				idx[i] = 0;
				maxIdx[i] = N_BASES;
			}
			this.tstacki37[i] = D_INF;
		}

		w = "";
		x = "ACGU";
		y = "";
		z = "ACGU";

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see energyParsers.EnergyParser#getData()
	 */
	@Override
	public IntLoopTstackiParser getData() {
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
		// TODO Auto-generated method stub

		// System.out.println("start parsing");

		BufferedReader inbuff = new BufferedReader(new FileReader(infile));
		// BufferedReader inbuff = new BufferedReader(new InputStreamReader(Intloo+infile));
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
								m = pattUpperBody.matcher(line);
								while (m.find()) {
									w += m.group().replaceAll("^\\s+", "").charAt(0);
								}
								// System.out.println("First Bases are: " + w);
								// match lower body to know the complementary bases
								m = pattLowerBody.matcher(line);
								while (m.find()) {
									y += m.group().replaceAll("^\\s+", "").charAt(0);
								}
								// System.out.println("Compl Bases are: " + x);
								// check if body is over
								m = pattLastLine.matcher(line);
								if (m.find()) {
									// System.out.println("Reached last line of body");
									lastLine = true;
								}
							}
							// System.out.println("First Bases are: " + w);
							// System.out.println("Compl Bases are: " + x);
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
											String bases = "" + w.charAt(idx[0]) + x.charAt(idx[1]) + y.charAt(idx[2])
													+ z.charAt(idx[3]);
											// System.out.println("Found data value: " + data.group());
											// System.out.println("Write " +
											// w.charAt(idx[0])+x.charAt(idx[1])+y.charAt(idx[2])+z.charAt(idx[3]) +
											// " into pos: " + calcIdx(bases));

											this.tstacki37[calcIdx(bases, maxIdx)] = Double.parseDouble(data.group());
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

		// System.out.println("ended parsing");
		return true;
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public double[] getTstacki37() {
		return tstacki37;
	}

	/**
	 * setter
	 * 
	 * @param tstacki37
	 */
	public void setTstacki37(double[] tstacki37) {
		this.tstacki37 = tstacki37;
	}

	public double getTstack37Value(int index) {
		if (index >= 0 && index < tstacki37.length) {
			return tstacki37[index];
		} else {
			return D_INF;
		}
	}

	/**
	 * returns the free energy-value ofthe given stacked basepair
	 * 
	 * <pre>
	 * 5' <-- 3'
	 *   s0-s1
	 *   s3-s2
	 * 3' --> 5'
	 * </pre>
	 * 
	 * @param bases
	 *            basepairs in order: wxyz
	 * @return free energy of stacked basepairs
	 */
	public double getStackedPairValue(String bases) {
		if (bases.contains("&")) {
			bases = bases.replace("&", "");
		}
		if (bases.length() != 4) {
			System.err.println("stacked basepair must consist of 4 bases!");
			return D_INF;
		} else {
			int[] order = { 0, 1, 3, 2 };
			String b = "";
			for (int i : order) {
				b += bases.charAt(i);
			}
			return tstacki37[calcIdx(b, maxIdx)];
		}
	}

}
