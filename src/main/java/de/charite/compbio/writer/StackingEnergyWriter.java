package de.charite.compbio.writer;

import java.io.IOException;

import de.charite.compbio.energyParsers.StackingParser;

public class StackingEnergyWriter extends EnergyWriter {

	StackingParser stack;

	public StackingEnergyWriter(StackingParser stack) {
		super("src/energyClasses/StackingEnergyClass.java");
		this.stack = stack;
		this.n_bases_needed = true;
	}

	// public StackingEnergyWriter(StackingParser stack) {
	// /FreeEnergy/src/energyClasses/Stacked.java
	// super(stack, "src/energyClasses/StackingEnergyClass.java");
	// this.stack = stack;
	//
	// }

	public StackingEnergyWriter write() {
		// always the same header-information
		head("StackingEnergyClass");
		try {
			// define data-structure
			javaWriter.write("\tprivate double[] stacked;\n\n");
			// class constructor
			javaWriter.write("\tpublic StackingEnergyClass() {\n"
					+ "\t\tthis.stacked = new double[(int) Math.pow(N_BASES, N_BASES)];\n"
					+ "\t\tif (!fillValues()) {\n"
					+ "\t\tSystem.err.println(\"an error occured while generating the array\");\n" + "\t\t}\n"
					+ "\t}\n\n");
			// method fill()
			javaWriter.write("\tprivate boolean fillValues() {\n" + "\t\tfor (int i=0; i<this.stacked.length; i++) {\n"
					+ "\t\t\tthis.stacked[i] = D_INF;\n" + "\t\t}\n" + "//\t\tfill here values != D_INF\n");
			// fill values < D_INF in data-structure
			double[] data = this.stack.getStack37();
			for (int i = 0; i < data.length; i++) {
				if (data[i] < D_INF) {
					javaWriter.write("\t\tthis.stacked[" + i + "] = " + data[i] + ";\n");
					// "\t\tthis.stacked[1] = 0;\n" +
				}
			}
			javaWriter.write("\t\treturn true;\n" + "\t}\n");
			// getter getStackingEnergy()
			javaWriter.write("\tpublic StackingParser getStackingEnergy() {\n"
					+ "\t\tStackingParser stack = new StackingParser();\n" + "\t\tstack.init_small();\n"
					+ "\t\tstack.setStack37(this.stacked);\n" + "\t\treturn stack;\n" + "\t}\n");
			// getter getStacking()
			javaWriter.write("\tpublic double[] getStacking() {\n" + "\t\treturn this.stacked;\n" + "\t}\n");
			// end class
			javaWriter.write("}\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

		end();

		return this;
	}

}
