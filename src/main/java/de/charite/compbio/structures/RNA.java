package de.charite.compbio.structures;

import java.util.logging.Logger;

/**
 * class as container for a RNA-sequence
 * 
 * @author marcel87
 *
 */
public class RNA {

	private static final Logger logger = Logger.getLogger(RNA.class.getSimpleName());

	/** RNA-sequence in uppercase letters */
	private String sequence;
	/** whether the RNA was checked for compatible characters */
	private boolean checked = false;

	private final static char[] RNA_letters = { 'A', 'C', 'G', 'U' };
	/** parameters from MiRanda-algorithm */
	private final static int[][] matrix = { { -3, -3, -3, 5 }, { -3, -3, 5, -3 }, { -3, 5, -3, 2 }, { 5, -3, 2, -3 } };

	/**
	 * create a new RNA-object
	 */
	public RNA() {
		// this.sequence = null;
	}

	/**
	 * create an new RNA-object with given sequence
	 * 
	 * @param rna
	 *            sequence of RNA
	 */
	public RNA(String rna) {
		this.sequence = rna.toUpperCase();
		checkRNA();
	}

	/**
	 * performs a check for compatible RNA-letters
	 * 
	 * @throws IllegalArgumentException
	 */
	private void checkRNA() throws IllegalArgumentException {
		// check for illegal RNA-characters
		for (int i = 0; i < this.sequence.length(); i++) {
			switch (this.sequence.charAt(i)) {
			case 'A':
			case 'C':
			case 'G':
			case 'U':
				break;
			case 'T':
				/** when finding a 'T' replace all with 'U' */
				this.sequence = this.sequence.replace('T', 'U');
				break;
			default:
				logger.severe("This is not a valied RNA-Sequence:\n" + this.sequence);
				throw new IllegalArgumentException(this.sequence + " is not a valid RNA-sequence");

			}
		}
		this.checked = true;
	}

	public String getSequence() {
		if (this.checked) {
			return this.sequence;
		} else {
			return null;
		}
	}

	public static int pairValue(char a, char b) {
		int idx1 = -1, idx2 = -1;
		for (int j = 0; j < RNA.RNA_letters.length; j++) {
			if (RNA.RNA_letters[j] == a)
				idx1 = j;
			if (RNA.RNA_letters[j] == b)
				idx2 = j;
			if (idx1 > 0 && idx2 > 0)
				break;
		}
		if (idx1 >= 0 && idx2 >= 0) {
			return RNA.matrix[idx1][idx2];
		} else {
			return -1;
		}
	}

	public void setSequence(String r) {
		this.checked = false;
		this.sequence = r.toUpperCase();
		checkRNA();
	}

	public char getPos(int p) {
		return this.sequence.charAt(p);
	}

	public RNA getReverse() {
		StringBuilder sb = new StringBuilder(this.sequence).reverse();
		RNA r = new RNA(sb.toString());
		return r;
	}

	public RNA getReverseComp() {
		String comp = "";
		int i = this.sequence.length();
		do {
			i--;
			switch (this.sequence.charAt(i)) {
			case 'A':
				comp += 'U';
				break;
			case 'C':
				comp += 'G';
				break;
			case 'G':
				comp += 'C';
				break;
			case 'U':
				comp += 'A';
				break;
			default:
				System.err.println("Symbol " + this.sequence.charAt(i) + " is unknown");
				break;
			}
		} while (i > 0);
		return new RNA(comp);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		// class RNA could be used like a String
		return this.sequence;
	}

	public String substring(int begin, int end) {
		// System.out.printf("return substring from %d to %d", begin, end);
		if (end < begin) {
			/*
			 * acgu -> ugca 2-0 -> 2-4 l=4 ==> l-2 -> l-0
			 */
			int l = this.length();
			// System.out.printf(" (%d, %d)", end, begin);
			// System.out.printf(" = %s\n", this.sequence.substring(end, begin));
			return this.getReverse().sequence.substring(l - begin, l - end);
		}
		// System.out.println(" = " + this.sequence.substring(begin, end));
		return this.sequence.substring(begin, end);
	}

	public int length() {
		return this.sequence.length();
	}

	// @Override public int length() {
	// return sequence.length();
	// }

}
