package de.charite.compbio.writer;

import java.io.IOException;

import de.charite.compbio.energyParsers.IntLoop22Parser;

public class IntLoop22Writer extends EnergyWriter {

	IntLoop22Parser int22;
	int step = 1;

	public IntLoop22Writer(IntLoop22Parser int22) {
		super("src/energyClasses/IntLoop22EnergyClass.java");
		this.int22 = int22;
		this.n_bases_needed = true;
		this.n_basepairs_needed = true;

	}

	public IntLoop22Writer write() {
		// always the same header-information
		head("IntLoop22EnergyClass");
		try {
			// define data-structure
			javaWriter.write("\tprivate double[] int22;\n\n");
			// class constructor
			javaWriter.write("\tpublic IntLoop22EnergyClass() {\n"
					+ "\t\tthis.int22 = new double[N_BASEPAIRS*N_BASEPAIRS*(int) Math.pow(N_BASES, 4)];\n"
					+ "\t\tif (!fillValues()) {\n"
					+ "\t\tSystem.err.println(\"an error occured while generating the array\");\n" + "\t\t}\n"
					+ "\t}\n\n");
			// method fill()
			javaWriter.write("\tprivate boolean fillValues() {\n" + "\t\tfor (int i=0; i<this.int22.length; i++) {\n"
					+ "\t\t\tthis.int22[i] = D_INF;\n" + "\t\t}\n" + "//\t\tfill here values != D_INF\n");
			// fill values < D_INF in data-structure
			double[] data = this.int22.getInt22();
			for (int i = 0; i < data.length; i++) {
				if (data[i] < D_INF) {
					counter++;
					if (counter > 1000) {
						insertNewMethod();
						counter = 0;
						step++;
					}
					javaWriter.write("\t\tthis.int22[" + i + "] = " + data[i] + ";\n");
					// "\t\tthis.stacked[1] = 0;\n" +
				}
			}
			javaWriter.write("\t\treturn true;\n" + "\t}\n");
			// getter getStackingEnergy()
			javaWriter.write("\tpublic IntLoop22Parser getIntLoop22Energy() {\n"
					+ "\t\tIntLoop22Parser intLoop = new IntLoop22Parser();\n" + "\t\tintLoop.init_small();\n"
					+ "\t\tintLoop.setInt22(this.int22);\n" + "\t\treturn intLoop;\n" + "\t}\n");
			// getter getStacking()
			javaWriter.write("\tpublic double[] getInt22() {\n" + "\t\treturn this.int22;\n" + "\t}\n");
			// end class
			javaWriter.write("}\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
		// close writer, etc.
		end();

		return this;
	}

	private void insertNewMethod() throws IOException {
		javaWriter.write("\n\t\tif (!fill" + this.step + "()) {\n"
				+ "\t\t\tSystem.err.println(\"an error occured while generating the array\");\n" + "\t\t}\n"
				+ "\t\treturn true;\n" + "\t}\n" + "\t\n" + "\tprivate boolean fill" + this.step + "() {\n");
	}

}
