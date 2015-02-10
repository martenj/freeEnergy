package de.charite.compbio.writer;

import java.io.IOException;

import de.charite.compbio.energyParsers.IntLoop21Parser;

public class IntLoop21Writer extends EnergyWriter {

	IntLoop21Parser int21;

	public IntLoop21Writer(IntLoop21Parser int21) {
		super("src/energyClasses/IntLoop21EnergyClass.java");
		this.int21 = int21;
		this.n_bases_needed = true;
		this.n_basepairs_needed = true;

	}

	public IntLoop21Writer write() {
		// always the same header-information
		head("IntLoop21EnergyClass");
		try {
			// define data-structure
			javaWriter.write("\tprivate double[] int21;\n\n");
			// class constructor
			javaWriter.write("\tpublic IntLoop21EnergyClass() {\n"
					+ "\t\tthis.int21 = new double[N_BASEPAIRS*N_BASES*N_BASES*N_BASEPAIRS*N_BASES];\n"
					+ "\t\tif (!fillValues()) {\n"
					+ "\t\tSystem.err.println(\"an error occured while generating the array\");\n" + "\t\t}\n"
					+ "\t}\n\n");
			// method fill()
			javaWriter.write("\tprivate boolean fillValues() {\n" + "\t\tfor (int i=0; i<this.int21.length; i++) {\n"
					+ "\t\t\tthis.int21[i] = D_INF;\n" + "\t\t}\n" + "//\t\tfill here values != D_INF\n");
			// fill values < D_INF in data-structure
			double[] data = this.int21.getInt21();
			for (int i = 0; i < data.length; i++) {
				if (data[i] < D_INF) {
					javaWriter.write("\t\tthis.int21[" + i + "] = " + data[i] + ";\n");
					// "\t\tthis.stacked[1] = 0;\n" +
				}
			}
			javaWriter.write("\t\treturn true;\n" + "\t}\n");
			// getter getStackingEnergy()
			javaWriter.write("\tpublic IntLoop21Parser getIntLoop21Energy() {\n"
					+ "\t\tIntLoop21Parser intLoop = new IntLoop21Parser();\n" + "\t\tintLoop.init_small();\n"
					+ "\t\tintLoop.setInt21(this.int21);\n" + "\t\treturn intLoop;\n" + "\t}\n");
			// getter getStacking()
			javaWriter.write("\tpublic double[] getInt21() {\n" + "\t\treturn this.int21;\n" + "\t}\n");
			// end class
			javaWriter.write("}\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
		// close writer, etc.
		end();

		return this;
	}

}
