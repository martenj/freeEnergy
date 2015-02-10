package de.charite.compbio.writer;

import java.io.IOException;

import de.charite.compbio.energyParsers.IntLoop11Parser;

public class IntLoop11Writer extends EnergyWriter {

	IntLoop11Parser int11;

	public IntLoop11Writer(IntLoop11Parser int11) {
		// /FreeEnergy/src/energyClasses/Stacked.java
		super("src/energyClasses/IntLoop11EnergyClass.java");
		this.int11 = int11;
		this.n_bases_needed = true;
		this.n_basepairs_needed = true;

	}

	/**
	 * 
	 * @return itself
	 */
	public IntLoop11Writer write() {
		// always the same header-information
		head("IntLoop11EnergyClass");
		try {
			// define data-structure
			javaWriter.write("\tprivate double[] int11;\n\n");
			// class constructor
			javaWriter.write("\tpublic IntLoop11EnergyClass() {\n"
					+ "\t\tthis.int11 = new double[N_BASEPAIRS*N_BASES*N_BASEPAIRS*N_BASES];\n"
					+ "\t\tif (!fillValues()) {\n"
					+ "\t\tSystem.err.println(\"an error occured while generating the array\");\n" + "\t\t}\n"
					+ "\t}\n\n");
			// method fill()
			javaWriter.write("\tprivate boolean fillValues() {\n" + "\t\tfor (int i=0; i<this.int11.length; i++) {\n"
					+ "\t\t\tthis.int11[i] = D_INF;\n" + "\t\t}\n" + "//\t\tfill here values != D_INF\n");
			// fill values < D_INF in data-structure
			double[] data = this.int11.getInt11();
			for (int i = 0; i < data.length; i++) {
				if (data[i] < D_INF) {
					javaWriter.write("\t\tthis.int11[" + i + "] = " + data[i] + ";\n");
					// "\t\tthis.stacked[1] = 0;\n" +
				}
			}
			javaWriter.write("\t\treturn true;\n" + "\t}\n");
			// getter getStackingEnergy()
			javaWriter.write("\tpublic IntLoop11Parser getIntLoop11Energy() {\n"
					+ "\t\tIntLoop11Parser intLoop = new IntLoop11Parser();\n" + "\t\tintLoop.init_small();\n"
					+ "\t\tintLoop.setInt11(this.int11);\n" + "\t\treturn intLoop;\n" + "\t}\n");
			// getter getStacking()
			javaWriter.write("\tpublic double[] getInt11() {\n" + "\t\treturn this.int11;\n" + "\t}\n");
			// end class
			javaWriter.write("}\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

		end();

		return this;
	}

}
