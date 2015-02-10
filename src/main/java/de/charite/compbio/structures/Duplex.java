package de.charite.compbio.structures;

//import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.Vector;

/**
 * class for creating a duplex RNA-structure options: - create a duplex from sequence only (with internal alignment) -
 * create a duplex from sequence and structure - validation of duplex - alignment type: Smith-Waterman local with
 * MiRanda parameters - print alignment in BLAST-like formatting
 * 
 * @author marcel87
 * @version 0.9
 */
public class Duplex {

	protected final static int INF = Integer.MAX_VALUE;
	protected final static double D_INF = Double.MAX_VALUE;

	protected final static boolean verbose = false;

	public String name = null;

	/** sense-RNA sequence */
	RNA sense_seq;
	/** antisense-RNA sequence */
	RNA antisense_seq;
	/**
	 * a String containing the structure of the duplex, where '()' indicate basepairs, '.' means no pairing and a '&'
	 * connects both sequences
	 */
	String structure;

	String sense_struc;
	String asense_struc;

	/**
	 * create a RNA-Duplex creates an empty Duplex-Structure
	 */
	public Duplex() {
		// this.sense_seq = null;
		// this.antisense_seq = null;
		// this.structure = null;
	}

	/**
	 * create a duplex structure with a sequence given, sense and antisense sequence is separated by '&'; an alignment
	 * is calculated automatically with Smith-Waterman with parameters from MiRanda-algorithnm
	 * 
	 * @param sequence
	 *            sense + '&' + antisense
	 */
	public Duplex(String sequence) {
		if (!sequence.contains("&")) {
			System.err.println("sequence must contain a '&' as seqarator for both sequences!");
		} else {
			String[] seqs = sequence.split("&");
			this.sense_seq = new RNA(seqs[0]);
			this.antisense_seq = new RNA(seqs[1]);
		}
		this.align();
		this.check_duplex();
	}

	/**
	 * create a RNA-duplex with given sequences and a structure
	 * 
	 * @param sequence
	 *            {@link String} sequence of RNA, 5' --> 3' with '&' as linker between sense and antisense sequence
	 * @param structure
	 *            structure secondary-structure between RNAs, notation: pair = "()", other = "."
	 */
	public Duplex(String sequence, String structure) {
		if (!sequence.contains("&")) {
			System.err.println("sequence must contain a '&' as seqarator for both sequences!");
			// this.sense_seq = null;
			// this.antisense_seq = null;
			// this.structure = null;
		} else {
			// divide sequence into parts before and after '&'
			this.sense_seq = new RNA(sequence.split("&")[0]);
			this.antisense_seq = new RNA(sequence.split("&")[1]);
			this.structure = structure;
			// check compatibility
			check_duplex();
		}
	}

	/**
	 * create a RNA-duplex with given sequences and a structure
	 * 
	 * @param sense_rna
	 *            sequence of first RNA, 5' --> 3'
	 * @param antisense_rna
	 *            sequence of second RNA, 5' --> 3'
	 * @param structure
	 *            secondary-structure between RNAs, notation: pair = "()", other = "."
	 */
	public Duplex(RNA sense_rna, RNA antisense_rna, String structure) {
		this.sense_seq = sense_rna;
		this.antisense_seq = antisense_rna;
		this.structure = structure;
		check_duplex();
	}

	private void check_duplex() {
		int a = this.sense_seq.length(), b = this.antisense_seq.length(), c = this.structure.length();
		// structure must have the length of both RNA-sequences
		if (c != (a + b + 1)) { // +1 for '&' character
			throw new IllegalArgumentException("\n" + this.toString() + " have inequal lengths (" + a + " + " + b
					+ " != " + c + ")");
		} else {
			// everything is fine
		}

	}

	@Override
	public String toString() {
		return this.sense_seq + " & " + this.antisense_seq + "\n" + this.structure.split("&")[0] + " & "
				+ this.structure.split("&")[1];
	}

	/**
	 * 
	 * @return
	 */
	/*
	 * @Deprecated protected Vector<Point> getPairs() {
	 * 
	 * // int l = this.sense_seq.length(); // String sense_struct = this.structure.substring(0, l); // String
	 * antisense_struct = this.structure.substring(l+1, this.structure.length());
	 * 
	 * // int i=0, j=this.structure.length()-1;
	 * 
	 * Vector<Point> pairs = new Vector<Point>(); // Vector<Integer> antisense_pairs = new Vector<Integer>();
	 * 
	 * // Integer[] tmp = new Integer[2]; Stack<Integer> brackets = new Stack<Integer>();
	 * 
	 * try { for ( int i=0; i<this.structure.split("&")[0].length(); i++ ) { if (this.structure.charAt(i)=='(') {
	 * brackets.push(i); } } for (int i=0; i<this.structure.split("&")[1].length(); i++) { if
	 * (this.structure.split("&")[1].charAt(i)==')') { pairs.add(new Point(brackets.pop(), i+this.sense_seq.length()));
	 * // System.out.println("tmp: " + tmp[0] + " " + tmp[1]); // System.out.println("add: " + pairs.add(tmp)); } } }
	 * catch (EmptyStackException e) { System.err.println("unbalanced brackets"); e.printStackTrace(); System.exit(3); }
	 * if (!brackets.isEmpty()) { System.err.println("unbalanced brackets"); System.out.println(brackets.toString());
	 * System.exit(3); }
	 * 
	 * return pairs; }
	 */

	/**
	 * decomposition of a RNA-duplex a method for decomposition of the structures of a duplex into stacked basepairs,
	 * internal 1x1, 1x2 and 2x2-loops
	 * 
	 * @return Vector of {@link String}s containing all substructures
	 */
	/*
	 * @Deprecated public Vector<String> decompose_old() { // check duplex first this.check_duplex();
	 * 
	 * Vector<Point> pairs = this.getPairs(); Vector<String> structures = new Vector<String>(); // double energy = 0;
	 * Point j = null; int l1 = this.sense_seq.length(); //, l2 = this.antisense_seq.length();
	 * 
	 * for (Point i : pairs) { if (j==null) { j = i; } else { // this way: // * sense: s1-s2-s3-s4-s5-s6-s7s-s8-s9 // *
	 * a.sen: // // System.out.println("point: " + j.toString() + " and " + i.toString()); String s = "" +
	 * this.sense_seq.substring(i.x, j.x+1) + "&" + this.antisense_seq.substring(j.y-l1, i.y-l1+1); structures.add(s); j
	 * = i; } }
	 * 
	 * return structures; }
	 */
	/**
	 * 
	 * @return
	 */
	public Vector<InternalLoop> decompose() {

		this.check_duplex();

		Vector<InternalLoop> loopsReverse = new Vector<InternalLoop>();
		Deque<Integer> brackets = new ArrayDeque<Integer>();
		// int bracky = 0;

		Integer lastPair_x = null, lastPair_y = null;

		// String sense_struc = this.structure.split("&")[0];
		// String asense_struc = this.structure.split("&")[1];
		// System.out.println(sense_struc + "\n" + asense_struc);

		// double energy = 0;
		InternalLoop tmpLoop;
		try {
			for (int i = 0; i < sense_struc.length(); i++) {
				if (this.structure.charAt(i) == '(') {
					brackets.push(i);
					// bracky++;
				}
			}
			for (int i = 0; i < asense_struc.length(); i++) {
				if (asense_struc.charAt(i) == ')') {
					if (lastPair_x == null && lastPair_y == null) {
						lastPair_x = brackets.pop();
						// bracky--;
						lastPair_y = i;
					} else {
						tmpLoop = new InternalLoop(brackets.pop(), lastPair_x, lastPair_y, i, null);
						// tmpLoop = new InternalLoop(bracky--, lastPair_x, lastPair_y, i, null);
						tmpLoop.sequence = this.sense_seq.substring(tmpLoop.i, tmpLoop.j + 1) + "&"
								+ this.antisense_seq.substring(tmpLoop.k, tmpLoop.l + 1);
						// System.out.println(tmpLoop + " (" + tmpLoop.typeOfLoop() + ")");
						loopsReverse.add(tmpLoop);
						lastPair_x = tmpLoop.i;
						lastPair_y = tmpLoop.l;

						// System.out.println("tmp: " + tmp[0] + " " + tmp[1]);
						// System.out.println("add: " + pairs.add(tmp));

					}
				}
			}
		} catch (EmptyStackException e) {
			System.err.println("unbalanced brackets");
			e.printStackTrace();
			System.exit(3);
		}
		if (!brackets.isEmpty()) {
			System.err.println("unbalanced brackets");
			System.out.println(brackets.toString());
			System.exit(3);
		}
		// if (bracky != 0) {
		// System.err.println("unbalanced brackets");
		// // System.out.println(brackets.toString());
		// System.exit(3);
		// }

		Vector<InternalLoop> loops = new Vector<InternalLoop>();

		for (int i = loopsReverse.size(); i > 0; i--) {
			loops.add(loopsReverse.elementAt(i - 1));
		}

		return loops;
	}

	/**
	 * do an Smith-Waterman-Alignment with given sequences and create structure use same parameters as the
	 * MiRanda-algorithm
	 */
	private void align() {
		String s1 = "-" + this.sense_seq.getSequence();
		String s2 = "-" + this.antisense_seq.getReverse().getSequence();
		int l1 = s1.length(), l2 = s2.length();

		int[][] m = new int[l1][l2];
		int[][] tr = new int[l1][l2];

		// System.out.print(" ");
		// for (char c : s2.toCharArray()) {
		// System.out.print("   " + c);
		// }
		// System.out.print("\n" + s1.charAt(0));
		// init matrix
		for (int i = 0; i < (l1 > l2 ? l1 : l2); i++) {
			if (i < l1) {
				m[i][0] = 0;
				tr[i][0] = 0;
				// System.out.print("   " + m[i][0]);
			}
			if (i < l2) {
				m[0][i] = 0;
				tr[0][i] = 0;
			}
		}

		// do matching
		/*
		 * matrix: - A C G j +---+---+---+---+ | - | 0 | 0 | 0 | 0 | V +---+---+---+---+ a U | 0 | 1 | | | n
		 * +---+---+---+---+ t G | 0 | | 1 | | i +---+---+---+---+ s C | 0 | | | X | <- current field (i,j) e
		 * +---+---+---+---+ n 0 --> nothing, new start s e
		 * 
		 * trace: \ i -> sense - A C j +---+---+---+---+ | | | | | | V +---+---+---+---+ a - | | | | | n
		 * +---+---+---+---+ t U | | | 2 | 3 | i +---+---+---+---+ s G | | | 1 | X | <- current field (i,j) e
		 * +---+---+---+---+ n 0 --> nothing, new start s e
		 */
		// System.out.println();
		int globalMax = 0, globalMaxX = 0, globalMaxY = 0;
		// char sign = ' ';
		int max, maxtr, left, diag, upper;
		for (int i = 1; i < l1; i++) {
			// System.out.printf("%c %3d", s1.charAt(i), 0);
			for (int j = 1; j < l2; j++) {
				max = 0;
				maxtr = 0;
				left = m[i - 1][j];
				// affine gap-costs
				if (tr[i - 1][j] == 1) {
					left -= 2;
				} else {
					left -= 8;
				}
				diag = RNA.pairValue(s1.charAt(i), s2.charAt(j)) + m[i - 1][j - 1];
				upper = m[i][j - 1];
				// affine gap costs
				if (tr[i][j - 1] == 3) {
					upper -= 2;
				} else {
					upper -= 8;
				}

				// char sign = ' ';
				if (left > max) {
					max = left;
					maxtr = 1;
					// sign = '^';
				}
				if (diag > max) {
					max = diag;
					maxtr = 2;
					// sign = '\\';
				}
				if (upper > max) {
					max = upper;
					maxtr = 3;
					// sign = '<';
				}
				m[i][j] = max;
				tr[i][j] = maxtr;
				if (max > globalMax) {
					globalMax = max;
					globalMaxX = i;
					globalMaxY = j;
				}
				// System.out.printf(" %c%2d", sign, max);
			}
			// System.out.println();
		}

		// System.out.println("global max (" + globalMax + ") at " + globalMaxX + " - " + globalMaxY);

		// now traceback ...

		int i = l1 - 1, j = l2 - 1;
		// String senseStruc = "", antisenseStruc = "";

		// fill remaining space of alignment with "."

		StringBuffer senseStruc = new StringBuffer();
		StringBuffer antisenseStruc = new StringBuffer();
		while (i > globalMaxX) {
			senseStruc.append(".");
			i--;
		}
		while (j > globalMaxY) {
			antisenseStruc.append(".");
			j--;
		}

		while (i >= 0 && j >= 0) {
			if (tr[i][j] == 2) {
				if (RNA.pairValue(s1.charAt(i), s2.charAt(j)) > 0) {
					// bases were matched
					senseStruc.insert(0, "(");
					antisenseStruc.append(")");
				} else {
					senseStruc.insert(0, ".");
					antisenseStruc.append(".");
				}
				i--;
				j--;
			} else if (tr[i][j] == 3) {
				// senseStruc += "(";
				antisenseStruc.append(".");
				j--;
			} else if (tr[i][j] == 1) {
				senseStruc.insert(0, ".");
				// antisenseStruc = "." + antisenseStruc;
				i--;
			} else if (tr[i][j] == 0) {
				// local alignment end
				// System.out.println("STOP");

				while (i > 0) {
					senseStruc.insert(0, ".");
					i--;
				}
				while (j > 0) {
					antisenseStruc.append(".");
					j--;
				}
				break;
			}
		}
		// System.out.println("structure: " + senseStruc + " & " + antisenseStruc);
		this.structure = senseStruc + "&" + antisenseStruc;
		this.sense_struc = senseStruc.toString();
		this.asense_struc = antisenseStruc.toString();
	}

	/**
	 * print a BLAST-like alignment of both sequences
	 */
	public void printAlignment() {
		/*
		 * UACGUACCU & ACCUACGU .(((((..( & )..)))))
		 * 
		 * turn into: UACGUACCU ||||| | UGCAUCCA
		 */

		// System.out.println(this);
		int l1 = sense_seq.length(), l2 = antisense_seq.length();
		String sense = "", struc = "", asense = "";

		int i = 0, j = this.structure.length() - 1;

		while (i + 1 < j - 1) {
			char c1 = this.structure.charAt(i), c2 = this.structure.charAt(j);
			// System.out.printf("i(%c): %2d j(%c): %2d%n", c1, i, c2, j);
			if (c1 == '(' && c2 == ')') {
				sense += this.sense_seq.getPos(i);
				struc += "|";
				asense += this.antisense_seq.getPos(j - l1 - 1);
				i++;
				j--;
			} else if (c1 == '(' && c2 == '.') {
				sense += "-";
				struc += " ";
				asense += this.antisense_seq.getPos(j - l1 - 1);
				j--;
			} else if (c1 == '.' && c2 == ')') {
				sense += this.sense_seq.getPos(i);
				struc += " ";
				asense += "-";
				i++;
			} else if (c1 == '.' && c2 == '.') {
				sense += this.sense_seq.getPos(i);
				struc += " ";
				asense += this.antisense_seq.getPos(j - l1 - 1);
				i++;
				j--;
			} else if (c1 == '&') {
				struc += " ";
				asense += this.antisense_seq.getPos(j - l1 - 1);
				j--;
			} else if (c2 == '&') {
				sense += this.sense_seq.getPos(i);
				struc += " ";
				i++;
			} else {
				System.err.println("not handles exception: " + c1 + " and " + c2 + " at " + i + " and " + j);
			}

		}

		System.out.println(sense + "\n" + struc + "\n" + asense);

	}

	public void setName(String name) {
		while (name.startsWith(">")) {
			name = name.replaceFirst(">", "");
		}
		this.name = name;
	}

	/**
	 * 
	 * @return sequence
	 */
	public String getSequence() {
		return sense_seq + "&" + antisense_seq;
	}

	public Duplex setSequence(String sequence) {
		if (sequence.split("&").length > 1) {
			this.sense_seq = new RNA(sequence.split("&")[0]);
			this.antisense_seq = new RNA(sequence.split("&")[1]);
		} else {
			System.err.println("sense an antisense sequence must be separated by a '&'!");
		}
		return this;
	}

	/**
	 * getter for the sense-sequence
	 * 
	 * @return sense-sequence
	 */
	public RNA getSense_seq() {
		return sense_seq;
	}

	/**
	 * setter for sense-sequence
	 * 
	 * @param sense_seq
	 *            RNA sequence
	 */
	public void setSense_seq(RNA sense_seq) {
		this.sense_seq = sense_seq;
	}

	/**
	 * setter for sense-sequence
	 * 
	 * @param sense_seq
	 *            {@link String} sense-sequence
	 */
	public void setSense_seq(String sense_seq) {
		this.sense_seq = new RNA(sense_seq);
	}

	/**
	 * getter for antisense-sequence
	 * 
	 * @return antisense-sequence
	 */
	public RNA getAntisense_seq() {
		return antisense_seq;
	}

	/**
	 * setter for antisense-sequence
	 * 
	 * @param antisense_seq
	 *            RNA antisense-sequence
	 */
	public void setAntisense_seq(RNA antisense_seq) {
		this.antisense_seq = antisense_seq;
	}

	/**
	 * setter for antisense-sequence
	 * 
	 * @param antisense_seq
	 *            {@link String} antisense-sequence
	 */
	public void setAntisense_seq(String antisense_seq) {
		this.antisense_seq = new RNA(antisense_seq);
	}

	/**
	 * setter for the structure-sequence
	 * 
	 * @return {@link String} sequence of structure
	 */
	public String getStructure() {
		return structure;
	}

	/**
	 * getter for the structure-sequence
	 * 
	 * @param structure
	 *            {@link String} structure-sequence
	 */
	public Duplex setStructure(String structure) {
		if (structure.contains("&")) {

		}
		this.structure = structure;
		return this;
	}

}