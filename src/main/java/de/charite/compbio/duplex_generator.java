package de.charite.compbio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/* Exit-Codes:
 *  0: everything fine
 *  1: error with outfile
 *  
 * */

public class duplex_generator {

	/** flag where to print the results */
	final static String output = "";

	Random rand = new Random(System.currentTimeMillis());

	final static int N_SEQ = 50;
	/** length is the number of bases (?) */
	final static int minLen = 15;
	final static int maxLen = 40;

	final static int internalMinLen = 1;
	final static int internalMaxLen = 15;

	final static int bulgeMaxLen = 8;

	/** frequencies for basepairs, int11-, int21-, int22-loops and bigger internal loops */
	final static String[] type = { "stacked", "int11", "int21", "int22", "intLoop", "bulge" };
	final static double[] freq = { 0.8, 0.04, 0.04, 0.04, 0.04, 0.04 };
	final static boolean[] use = { true, true, true, true, true, true };

	String name;
	String sequence;
	String structure;

	static int number;

	String filename;
	File outfile;
	BufferedWriter seqWriter;

	public static void main(String[] args) {
		duplex_generator gen = new duplex_generator();
		/** init arguments */
		gen.name = ">sequence_";
		gen.filename = "data/sequences/Test_duplex_" + N_SEQ + "_seq.fasta";
		// /** create outfile */
		// gen.outfile = new File("data/sequences/GAIL_duplex_" + N_SEQ + "_seq.fasta");
		// if (!gen.outfile.exists()) {
		// // create new file
		// try {
		// gen.outfile.createNewFile();
		// } catch (IOException e) {
		// System.err.println("cannot create outfile " + gen.outfile.getAbsolutePath());
		// e.printStackTrace();
		// System.exit(1);
		// }
		// }
		// if (!gen.outfile.canWrite()) {
		// try {
		// gen.outfile.setWritable(true);
		// } catch (SecurityException e) {
		// System.err.println("cannot write to outfile " + gen.outfile.getAbsolutePath());
		// e.printStackTrace();
		// System.exit(1);
		// }
		// }
		//
		// /** */
		// try {
		// gen.seqWriter = new BufferedWriter(new FileWriter(gen.outfile));
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }

		gen.seqWriter = gen.createOutfile();

		/** start sequence generation */
		for (int i = 1; i <= N_SEQ; i++) {
			number = i;
			gen.init();
			int l = minLen + gen.rand.nextInt(maxLen - minLen);
			gen.basePair();
			for (int j = 0; j < l; j = gen.length()) {
				double r = gen.rand.nextDouble() * freq[0] + freq[1] + freq[2] + freq[3] + freq[4] + freq[5];
				if (r < freq[0]) {
					gen.basePair();
				} else if (r < freq[0] + freq[1]) {
					gen.int11();
				} else if (r < freq[0] + freq[1] + freq[2]) {
					gen.int21();
				} else if (r < freq[0] + freq[1] + freq[2] + freq[3]) {
					gen.int22();
				} else if (r < freq[0] + freq[1] + freq[2] + freq[3] + freq[4]) {
					gen.internalLoop();
				} else if (r < freq[0] + freq[1] + freq[2] + freq[3] + freq[4] + freq[5]) {
					gen.bulge();
				}
			}
			// System.out.println(gen.name + i);
			// System.out.println(gen.sequence);
			// System.out.println(gen.structure);
			/** write each sequence with header/sequence/structure in file */
			try {
				gen.seqWriter.write(gen.name + i + "\n");
				gen.seqWriter.write(gen.sequence + "\n");
				gen.seqWriter.write(gen.structure + "\n\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/** check if all ok */

		/** close file */
		try {
			gen.seqWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int length() {
		String[] s = sequence.split("&");
		if (s.length == 2) {
			return Math.max(s[0].length(), s[1].length());
		} else {
			return 0;
		}
	}

	void init() {
		this.sequence = "&";
		this.structure = "&";
	}

	void basePair() {
		String[] basePairs = { "A&U", "G&C", "C&G", "U&A", "G&U", "U&G" };

		extend(basePairs[rand.nextInt(basePairs.length)], "(&)");
	}

	void int11() {
		String seq = mismatch();
		extend(seq, ".&.");
		basePair();
	}

	void int21() {
		// TODO: check if in this loop 2 bases must be mismatch!
		if (rand.nextBoolean()) {
			String seq = mismatch();
			seq = mismatchTo(seq.charAt(2)) + seq;
			extend(seq, "..&.");
		} else {
			String seq = mismatch();
			seq += mismatchTo(seq.charAt(0));
			extend(seq, ".&..");
		}
		basePair();
	}

	void int22() {
		// String seq = "" + singleBase() + singleBase() + "&" + singleBase() + singleBase();
		// extend(seq, "..&..");
		extend(mismatch(), ".&.");
		extend(mismatch(), ".&.");
		basePair();
	}

	void internalLoop() {
		int diff = internalMaxLen - internalMinLen;
		int l1 = rand.nextInt(diff) + internalMinLen, l2 = rand.nextInt(diff) + internalMinLen;
		// System.out.println("internal sizes: " + l1 + " and " + l2);
		internalLoop(l1, l2);
	}

	void internalLoop(int l1, int l2) {
		// System.out.println("internal loop");

		boolean swap = l1 > l2;
		// sort seq-length
		int l = l1 > l2 ? l1 : l2;
		l1 = l1 > l2 ? l2 : l1; // l1 is shorter
		l2 = l; // l2 is longer
		if (l2 == 1) {
			System.out.println("GAIL in seq " + number);
		}

		String sense = "", antisense = "", struct = "";

		// create sense seq and structure
		sense += singleBase();
		struct += "(";
		for (int i = 0; i < l1; i++) {
			sense += singleBase();
			struct += ".";
		}

		sense += singleBase();
		struct += "(";
		struct += "&";

		// create antisense seq

		// System.out.println("antisense: " + antisense);

		antisense += matchTo(sense.charAt(sense.length() - 1));
		struct += ")";
		// System.out.println("antisense: " + antisense);
		antisense += mismatchTo(sense.charAt(sense.length() - 2));
		struct += ".";

		for (int i = 0; i < l2; i++) {
			antisense += singleBase();
			struct += ".";
		}

		// System.out.println("antisense: " + antisense);
		antisense += mismatchTo(sense.charAt(1));
		struct += ".";
		// System.out.println("antisense: " + antisense);
		antisense += matchTo(sense.charAt(0));
		// System.out.println("antisense: " + antisense);
		struct += ")";

		// System.out.println("internal loop: l1=" + l1 + " l2=" + l2);
		// System.out.println("sequences: |s|=" + sense.length() + " |as|=" + antisense.length());
		// System.out.println("structure length: " + (struct.length()-1));

		String sequence = sense + "&" + antisense;
		// System.out.println(sequence + "\n" + struct);

		if (sequence.length() != struct.length()) {
			System.err.println("inequal seq-struct: " + sequence.length() + " - " + struct.length());
		}

		if (swap) {
			// System.out.println("swap seqs");
			String swapSeq = "";
			String swapStruct = "";
			for (int i = sequence.length(); i > 0; i--) {
				swapSeq += sequence.charAt(i - 1);
				if (struct.charAt(i - 1) == '(') {
					swapStruct += ")";
				} else if (struct.charAt(i - 1) == ')') {
					swapStruct += "(";
				} else {
					swapStruct += struct.charAt(i - 1);
				}
			}
			// sequence += "&";
			// swapStruct += "&";
			// for (int i = sense.length()-1; i > 0; i--) {
			// sequence += sense.charAt(i);
			// swapStruct += struct.charAt(i);
			// }
			sequence = swapSeq;
			struct = swapStruct;
		}
		// System.out.println(sequence + "\n" + struct);

		extend(sequence, struct);

	}

	void bulge() {
		int l = rand.nextInt(bulgeMaxLen);
		if (l > 1) {
			// System.out.println("bulge_N is sequence " + number);
		}
		bulge(l);
	}

	void bulge(int l) {
		boolean upperSide = rand.nextBoolean();
		basePair();
		String sequence = "", struct = "";
		for (int i = 0; i < l; i++) {
			sequence += singleBase();
			struct += ".";
		}
		if (upperSide) {
			sequence += "&";
			struct += "&";
		} else {
			sequence = "&" + sequence;
			struct = "&" + struct;
		}
		extend(sequence, struct);
		basePair();
	}

	String singleBase() {
		String bases = "ACGU";
		return "" + bases.charAt(rand.nextInt(bases.length()));
	}

	String mismatch() {
		String[] mm = { "A&A", "A&C", "A&G", "G&A", "G&G", "C&A", "C&C", "C&U", "U&C", "U&U" };
		int r = rand.nextInt(mm.length);
		return mm[r];
	}

	String matchTo(char base) {
		switch (base) {
		case 'A':
			return "U";
		case 'C':
			return "G";
		case 'G':
			return "C";
		case 'U':
			return "A";
		default:
			return null;
		}
	}

	String mismatchTo(char base) {
		String a = "ACG";
		String c = "ACU";
		String g = "AG";
		String u = "CU";

		switch (base) {
		case 'A':
			return "" + a.charAt(rand.nextInt(3));
		case 'C':
			return "" + c.charAt(rand.nextInt(3));
		case 'G':
			return "" + g.charAt(rand.nextInt(2));
		case 'U':
			return "" + u.charAt(rand.nextInt(2));
		default:
			return "";
		}
	}

	void extend(String seq, String struct) {
		if (this.sequence.length() > 1) {
			this.sequence = this.sequence.split("&")[0] + seq + this.sequence.split("&")[1];
			this.structure = this.structure.split("&")[0] + struct + this.structure.split("&")[1];
		} else {
			this.sequence = seq;
			this.structure = struct;
		}
	}

	BufferedWriter createOutfile() {
		/** create outfile */
		this.outfile = new File(this.filename);
		if (!this.outfile.exists()) {
			// create new file
			try {
				this.outfile.createNewFile();
			} catch (IOException e) {
				// TODO: handle exception
				System.err.println("cannot create outfile " + this.outfile.getAbsolutePath());
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (!this.outfile.canWrite()) {
			try {
				this.outfile.setWritable(true);
			} catch (SecurityException e) {
				// TODO: handle exception
				System.err.println("cannot write to outfile " + this.outfile.getAbsolutePath());
				e.printStackTrace();
				System.exit(1);
			}
		}

		/**  */
		try {
			seqWriter = new BufferedWriter(new FileWriter(this.outfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		return seqWriter;
	}
}
