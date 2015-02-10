package de.charite.compbio.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.charite.compbio.energyParsers.EnergyParser;

public class EnergyWriter {

	protected final static double D_INF = Double.MAX_VALUE;

	File outfile;
	BufferedWriter javaWriter;

	protected int counter = 0;

	protected boolean n_bases_needed = false;
	protected boolean n_basepairs_needed = false;

	public EnergyWriter(String file) {
		if (!init(file)) {
			System.err.println("failed initiation. Will exit.");
			System.exit(-1);
		}

	}

	public EnergyWriter(EnergyParser e, String file) {
		if (!init(file)) {
			System.err.println("failed initiation. Will exit.");
			System.exit(-1);
		}

	}

	private boolean init(String file) {
		this.outfile = new File(file);
		// check for file
		if (this.outfile.exists()) {
			System.out.println("File \"" + this.outfile + "\" exists. Will overwrite this file.");
			if (!this.outfile.canWrite()) {
				System.err.println("Error: cannot write file \"" + this.outfile + "\"");
				return false;
			}
		} else {
			System.out.println("Create new file \"" + this.outfile + "\"");
			try {
				this.outfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		// open buffered writer
		try {
			this.javaWriter = new BufferedWriter(new FileWriter(outfile));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void comment() throws IOException {
		javaWriter.write("/**\n" + " * This file is created automatically by Java.\n"
				+ " * For detailed descriptions see the corresponding\n" + " * writer-file in the package \"writer\"\n"
				+ " * @author marcel87\n" + " * @version 1.0\n" + " */\n");
	}

	protected void head(String className) {
		try {
			// javaWriter.write("");
			// write package
			javaWriter.write("package energyClasses;\n\n");
			// imports the corresponding energyParsers
			javaWriter.write("import energyParsers." + className.replace("Energies", "Parser") + ";\n\n");
			comment();
			// class
			javaWriter.write("public class " + className + " {\n\t\n");
			// define constants
			if (n_bases_needed) {
				javaWriter.write("\tprivate static final int N_BASES = 4;\n");
			}
			if (n_basepairs_needed) {
				javaWriter.write("\tprivate static final int N_BASEPAIRS = 6;\n");
			}
			javaWriter.write("\tprivate static final double D_INF = Double.MAX_VALUE;\n\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void end() {
		try {
			this.javaWriter.close();
		} catch (IOException e) {
			System.err.println("Error while closing the file.");
		} finally {
			if (this.javaWriter != null) {
				try {
					this.javaWriter.close();
				} catch (IOException e) {
					System.err.println("Another error while closing the file.");
					e.printStackTrace();
				}
			}

		}
	}

}
