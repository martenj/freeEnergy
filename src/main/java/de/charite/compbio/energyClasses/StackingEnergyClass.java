package de.charite.compbio.energyClasses;

import de.charite.compbio.energyParsers.StackingParser;

/**
 * This file is created automatically by Java. For detailed descriptions see the corresponding writer-file in the
 * package "writer"
 * 
 * @author marcel87
 * @version 1.0
 */
public class StackingEnergyClass {

	private static final int N_BASES = 4;
	private static final double D_INF = Double.MAX_VALUE;

	private double[] stacked;

	public StackingEnergyClass() {
		this.stacked = new double[(int) Math.pow(N_BASES, N_BASES)];
		if (!fillValues()) {
			System.err.println("an error occured while generating the array");
		}
	}

	private boolean fillValues() {
		for (int i = 0; i < this.stacked.length; i++) {
			this.stacked[i] = D_INF;
		}
		// fill here values != D_INF
		this.stacked[15] = -0.9;
		this.stacked[30] = -2.2;
		this.stacked[45] = -2.1;
		this.stacked[47] = -0.6;
		this.stacked[60] = -1.1;
		this.stacked[62] = -1.4;
		this.stacked[75] = -2.1;
		this.stacked[90] = -3.3;
		this.stacked[105] = -2.4;
		this.stacked[107] = -1.4;
		this.stacked[120] = -2.1;
		this.stacked[122] = -2.1;
		this.stacked[135] = -2.4;
		this.stacked[143] = -1.3;
		this.stacked[150] = -3.4;
		this.stacked[158] = -2.5;
		this.stacked[165] = -3.3;
		this.stacked[167] = -1.5;
		this.stacked[173] = -2.1;
		this.stacked[175] = -0.5;
		this.stacked[180] = -2.2;
		this.stacked[182] = -2.5;
		this.stacked[188] = -1.4;
		this.stacked[190] = 1.3;
		this.stacked[195] = -1.3;
		this.stacked[203] = -1.0;
		this.stacked[210] = -2.4;
		this.stacked[218] = -1.5;
		this.stacked[225] = -2.1;
		this.stacked[227] = -1.0;
		this.stacked[233] = -1.4;
		this.stacked[235] = 0.3;
		this.stacked[240] = -0.9;
		this.stacked[242] = -1.3;
		this.stacked[248] = -0.6;
		this.stacked[250] = -0.5;
		return true;
	}

	public StackingParser getStackingEnergy() {
		StackingParser stack = new StackingParser();
		stack.init_small();
		stack.setStack37(this.stacked);
		return stack;
	}

	public double[] getStacking() {
		return this.stacked;
	}
}
