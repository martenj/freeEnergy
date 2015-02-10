/**
 * 
 */
package de.charite.compbio.structures;

/**
 * Helper class to structure internal loops
 * 
 * @version 0.9
 * @author marcel87
 *
 */
public class InternalLoop {

	public int i, j, k, l;
	public String sequence;

	public int type;

	public InternalLoop() {

	}

	public InternalLoop(int i, int j, int k, int l, String sequence) {

		this.i = i;
		this.j = j;
		this.l = l;
		this.k = k;
		this.sequence = sequence;

		this.type = this.typeOfLoop();

	}

	/**
	 * searches the first basepair of the given loop
	 * 
	 * @return first basepair
	 */
	public String firstPair() {

		return "" + sequence.charAt(0) + sequence.charAt(sequence.length() - 1);
	}

	/**
	 * searches the closing basepair of the given loop
	 * 
	 * @return last basepair
	 */
	public String lastPair() {

		String[] sequences = sequence.split("&");
		return "" + sequences[0].charAt(sequences[0].length() - 1) + sequences[1].charAt(0);
	}

	public String firstStackedPair() {

		if (this.sequence.length() >= 4) {
			return "" + sequence.charAt(0) + sequence.charAt(1) + "&" + sequence.charAt(sequence.length() - 2)
					+ sequence.charAt(sequence.length() - 1);
		} else {
			return null;
		}
	}

	public String lastStackedPair() {
		String[] sequences = sequence.split("&");
		int l1 = sequences[0].length(), l2 = sequences[1].length();
		if (l1 >= 2 && l2 >= 2) {
			String r = "" + sequences[1].charAt(0) + sequences[1].charAt(1) + "&" + sequences[0].charAt(l1 - 2)
					+ sequences[0].charAt(l1 - 1);
			// System.out.println(r);
			return r;
		} else {
			return null;
		}
	}

	public int size() {
		int l1 = this.j - this.i - 1;
		int l2 = this.l - this.k - 1;

		return l1 + l2;
	}

	/**
	 * measure for symmetry of internal loop
	 * 
	 * @return length-difference of both strands forming the loop
	 */
	public int symmetry() {

		int l1 = this.j - this.i;
		int l2 = this.l - this.k;
		// System.out.println("loop symmetry is |" + l1 + " - " + l2 + "| = " + Math.abs(l1-l2));
		return Math.abs(l1 - l2);
	}

	/**
	 * 
	 * @return a value for the type of internal loop
	 */
	public int typeOfLoop() {

		// TODO: check if loop is valid (i, j, k, l!=null, i<j, k<l, seq!=null)

		if (j == i + 1) {
			if (l == k + 1) {
				// stacked basepair
				return 0;
			} else if (l == k + 2) {
				// bulge
				return 2;
			} else if (l > k + 2) {
				// bigger bulge
				return 7;
			} else {
				// ???
				System.err.println("not found: " + this);

			}
		} else if (j == i + 2) {
			if (l == k + 1) {
				// bulge
				return 1;
			} else if (l == k + 2) {
				// internal 1x1 loop
				return 3;
			} else if (l == k + 3) {
				// internal 1x2 loop
				return 5;
			} else if (l > k + 3) {
				// internal 1xN loop - GAIL
				// System.out.println("number 9!");
				return 9;
			} else {
				// ???
				System.err.println("not found: " + this);

			}
		} else if (j == i + 3) {
			if (l == k + 1) {
				// bigger bulge
				return 8;
			} else if (l == k + 2) {
				// internal 2x1 loop
				return 4;
			} else if (l == k + 3) {
				// internal 2x2 loop
				return 6;
			} else if (l > k + 3) {
				// internal MxN loop, M<N
				return 13;
			} else {
				// ???
				System.err.println("not found: " + this);

			}
		} else if (j > i + 3) {
			if (l == k + 1) {
				// bigger bulge
				return 8;
			} else if (l == k + 2) {
				// internal Nx1 loop - GAIL
				// System.out.println("number 10!");
				return 10;
			} else if (l > k + 2) {
				// bigger interior loop, check symmetry
				if (j - i == l - k) {
					// symmetric internal NxN loop
					return 11;
				} else if (j - i < l - k) {
					// asymmetric internal MxN loop, M<N
					return 13;
				} else {
					// asymmetric internal NxM loop, M<N
					return 12;
				}
			}
		} else {
			// ???
			System.err.println("not found: " + this);

		}

		return Integer.MIN_VALUE;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "loop: " + this.i + " " + this.j + " " + this.k + " " + this.l + " " + this.sequence;
	}

}
