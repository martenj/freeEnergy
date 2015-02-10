package de.charite.compbio.alignment;

import de.charite.compbio.energyClasses.IntLoop11EnergyClass;
import de.charite.compbio.energyClasses.IntLoop21EnergyClass;
import de.charite.compbio.energyClasses.IntLoop22EnergyClass;
import de.charite.compbio.energyClasses.StackingEnergyClass;
import de.charite.compbio.energyParsers.IntLoop11Parser;
import de.charite.compbio.energyParsers.IntLoop21Parser;
import de.charite.compbio.energyParsers.IntLoop22Parser;
import de.charite.compbio.energyParsers.StackingParser;
import de.charite.compbio.structures.RNA;

public class Alignment {

	StackingParser stack = new StackingEnergyClass().getStackingEnergy();
	IntLoop11Parser int11 = new IntLoop11EnergyClass().getIntLoop11Energy();
	IntLoop21Parser int21 = new IntLoop21EnergyClass().getIntLoop21Energy();
	IntLoop22Parser int22 = new IntLoop22EnergyClass().getIntLoop22Energy();

	RNA seq = new RNA("ACGUACGUACGUGU");
	RNA aseq = new RNA("UGCAUGCAUG");

	Field[][] alignment; // initialized array

	public Alignment() {

	}

	public Alignment(String seq) {

	}

	public void align() {

		int l1 = seq.length(), l2 = aseq.length();
		int l = l1 > l2 ? l1 : l2;

		alignment = new Field[l1 + 1][l2 + 1];

		double globalBest = 0;
		int maxX = 0, maxY = 0;

		// double[][] matrix = new double[l1][l2]; // contains data-values
		// int[][][] tr = new int[l1][l2][2]; // yields traceback

		// init
		for (int i = 0; i < l + 1; i++) {
			if (i < l1 + 1) {
				alignment[i][0] = new Field();
			}
			if (i < l2 + 1) {
				alignment[0][i] = new Field();
			}
		}

		for (int i = 1; i < l1 + 1; i++) {
			for (int j = 1; j < l2 + 1; j++) {
				alignment[i][j] = new Field(isBasePair(seq.getPos(i - 1), aseq.getPos(j - 1)));
				// alignment[i][j].isPair = isBasePair(seq.getPos(i-1), aseq.getPos(j-1));
				// double field = 0; // standart value for energy
				int trace = 0; // no trace from this field
				// System.out.println(alignment[i+1][j+1]);
				/**
				 * trace: \ i -> j +---+---+---+---+ | | 9 | 8 | 7 | | V +---+---+---+---+ | 6 | 5 | 4 | |
				 * +---+---+---+---+ | 3 | 2 | 1 | | +---+---+---+---+ | | | | X | <- current field (i,j)
				 * +---+---+---+---+ 1: stacked pair (-1,-1) 2: -- 3: -- 4: -- 5: 1x1-loop (-2,-2) 6: 2x1-loop (-2,-3)
				 * 7: -- 8: 1x2-loop (-3,-2) 9: 2x2-loop (-3,-3)
				 * */
				// check if bases form a pair
				if (alignment[i][j].isPair) {
					// System.out.println("pair at i: " + i + " j: " + j);
					// calculate value for a stacked pair
					if ((i > 1) && (j > 1) && alignment[i - 1][j - 1].isPair) {
						String stackedBases = "" + seq.substring(i - 2, i) + "&" + aseq.substring(j, j - 2);
						double e = alignment[i - 1][j - 1].value + stack.getStackedPairValue(stackedBases);
						System.out.println(stackedBases + " = " + e);
						if (e < alignment[i][j].value) {
							alignment[i][j].value = e;
							trace = 1;
						}
					}
					// calc value for a int11 loop
					if ((i > 2) && (j > 2) && alignment[i - 2][j - 2].isPair) {
						String int11Bases = "" + seq.substring(i - 3, i) + "&" + aseq.substring(j, j - 3);
						double e = alignment[i - 2][j - 2].value + int11.getInt11LoopValue(int11Bases);
						System.out.println(int11Bases + " = " + e);
						if (e < alignment[i][j].value) {
							alignment[i][j].value = e;
							trace = 5;
						}
					}
					// calc value for a int12 loop
					if ((i > 2) && (j > 3) && alignment[i - 2][j - 3].isPair) {
						String int12Bases = "" + seq.substring(i - 3, i) + "&" + aseq.substring(j, j - 4);
						double e = alignment[i - 2][j - 3].value + int21.getInt21LoopValue(int12Bases);
						System.out.println(int12Bases + " = " + e);
						if (e < alignment[i][j].value) {
							alignment[i][j].value = e;
							trace = 8;
						}
					}
					// calc value for a int21 loop
					if ((i > 3) && (j > 2) && alignment[i - 3][j - 2].isPair) {
						String int21Bases = "" + aseq.substring(i - 3, i) + "&" + seq.substring(j, j - 4);
						double e = alignment[i - 3][j - 2].value + int21.getInt21LoopValue(int21Bases);
						System.out.println(int21Bases + " = " + e);
						if (e < alignment[i][j].value) {
							alignment[i][j].value = e;
							trace = 6;
						}
					}
					// calc value for a int22 loop
					if ((i > 3) && (j > 3) && alignment[i - 3][j - 3].isPair) {
						String int22Bases = "" + seq.substring(i - 4, i) + "&" + aseq.substring(j, j - 4);
						double e = alignment[i - 3][j - 3].value + int22.getInt22LoopValue(int22Bases);
						System.out.println(int22Bases + " = " + e);
						if (e < alignment[i][j].value) {
							alignment[i][j].value = e;
							trace = 9;
						}
					}
				} else {
					// no basepair. no changes

				}
				// alignment[i][j].value = field;
				globalBest = Math.min(globalBest, alignment[i][j].value);
				switch (trace) {
				case 1:
					alignment[i][j].setTrace(i - 1, j - 1);
					break;
				case 5:
					alignment[i][j].setTrace(i - 2, j - 2);
					break;
				case 6:
					alignment[i][j].setTrace(i - 2, j - 3);
					break;
				case 8:
					alignment[i][j].setTrace(i - 3, j - 2);
					break;
				case 9:
					alignment[i][j].setTrace(i - 3, j - 3);
					break;
				default:
					alignment[i][j].setTrace(0, 0);
					break;
				}

			}
		}

		for (int i = 0; i < l1 + 1; i++) {
			for (int j = 0; j < l2 + 1; j++) {
				// System.out.print(alignment[i][j] + " | ");
				alignment[i][j].print();
			}
			System.out.println("|");
		}

		System.out.printf("global best value: %+.2f%n", globalBest);

	}

	static boolean isBasePair(char a, char b) {
		switch (a) {
		case 'A':
			if (b == 'U')
				return true;
			break;
		case 'C':
			if (b == 'G')
				return true;
			break;
		case 'G':
			if ((b == 'C') || (b == 'U'))
				return true;
			break;
		case 'U':
			if ((b == 'A') || (b == 'G'))
				return true;
			break;
		default:
			break;
		}
		return false;
	}

}
