package de.charite.compbio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.charite.compbio.energyClasses.IntLoop11EnergyClass;
import de.charite.compbio.energyClasses.IntLoop21EnergyClass;
import de.charite.compbio.energyClasses.IntLoop22EnergyClass;
import de.charite.compbio.energyClasses.StackingEnergyClass;
import de.charite.compbio.energyParsers.IntLoop11Parser;
import de.charite.compbio.energyParsers.IntLoop21Parser;
import de.charite.compbio.energyParsers.IntLoop22Parser;
import de.charite.compbio.energyParsers.IntLoopTstackiParser;
import de.charite.compbio.energyParsers.StackingParser;
import de.charite.compbio.free_energy.FEC;
import de.charite.compbio.structures.Duplex;
import de.charite.compbio.structures.RNA;
import de.charite.compbio.writer.IntLoop11Writer;
import de.charite.compbio.writer.IntLoop21Writer;
import de.charite.compbio.writer.IntLoop22Writer;
import de.charite.compbio.writer.StackingEnergyWriter;

public class Energy_calc {

	/**
	 * This class is for testing purpose
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// calculate ernergy of a certain RNA-duplex (structure is known)

		long start = System.currentTimeMillis();

		// misc();
		//
		// stacking();
		// internal11();
		// internal21();
		// internal22();
		// tstacking();

		// evalFile("data/sequences/Test_duplex_50_seq.fasta");
		// evalViennaFile("data/sequences/Test_duplex_50_seq_eval.txt");
		System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
	}

	public static void misc() {
		RNA seq = new RNA("ACUGCUCUCAUC");
		System.out.println(seq.getSequence() + " ist okay");
		System.out.println(seq + " ist auch als \"" + seq.getClass() + "\" okay");

		// Duplex d = new Duplex(seq, seq, "012345678901234567890123");
		// System.out.println(d);
		// try {
		// Duplex d2 = new Duplex(seq, seq, "...........&............");
		// System.out.println(d2);
		// } catch (IllegalArgumentException e) {
		// System.out.println("d2 could not be created ...");
		// }
		// Duplex d3 = new Duplex(seq, seq.getReverse(), "012345678901234567890123");
		// System.out.println(d3);
		// Duplex d4 = new Duplex(seq, seq.getReverseComp(), "012345678901234567890123");
		// System.out.println(d4);
		//
		// Duplex d5;
		// d5 = new Duplex(seq, seq.getReverseComp(), "((((((((((((&))))))))))))");
		// System.out.println(d5);
		// StackingParser stack = new StackingEnergyClass().getStackingEnergy();

		// // Vector<String> s5 = d5.decompose();
		// double energy = 0;
		// for (String string : s5) {
		// System.out.print("element: " + string);
		// System.out.println(" energy: " + stack.getStackedPairValue(string));
		// energy += stack.getStackedPairValue(string);
		// }
		// System.out.println("summed energy: " + energy);
		//
		// Vector<Point> res = d5.getPairs();
		// for (int i = 0; i < res.size(); i++) {
		// System.out.println(res.get(i).x + " - " + res.get(i).y);
		// }
		// Duplex d6 = new Duplex(seq, seq.getReverseComp(), "((.((..(((.(&).)))..)).))");
		// System.out.println(d6.initEnergies().calcEnergy());

		// Vector<Point> res6 = d6.getPairs();
		// for (int i=0; i<res6.size(); i++) {
		// System.out.println(res6.get(i).x + " - " + res6.get(i).y);
		// }
	}

	public static void stacking() {
		// performance-measure
		long t = System.currentTimeMillis();
		StackingParser stacked = new StackingParser("data/mfold/stack.dat").getData();

		System.out.println("time taken for stacking (1): " + (System.currentTimeMillis() - t) + " ms");

		// try writing the stacked-energy-values into a file - works fine
		// System.getProperty("line.separator");
		// System.out.println(System.getProperty("user.dir"));
		new StackingEnergyWriter(stacked).write();

		t = System.currentTimeMillis();
		StackingParser stack2 = new StackingEnergyClass().getStackingEnergy();

		System.out.println("time taken for stacking (2): " + (System.currentTimeMillis() - t) + " ms");

		String[] tmp = { "UCGA", "AUAU", "AAUU", "GUUG", "AU", "CCGG", "GGCC" };
		for (String s : tmp) {
			System.out.println("(s1)" + s + ": " + stacked.getStackedPairValue(s));
			System.out.println("(s2)" + s + ": " + stack2.getStackedPairValue(s));
		}
	}

	public static void internal11() {
		// performance-measure
		long t = System.currentTimeMillis();
		IntLoop11Parser loop11 = new IntLoop11Parser("data/mfold/int11.dat").getData();

		System.out.println("time taken for int.1x1 loops (1): " + (System.currentTimeMillis() - t) + " ms");
		new IntLoop11Writer(loop11).write();

		t = System.currentTimeMillis();
		IntLoop11Parser loop112 = new IntLoop11EnergyClass().getIntLoop11Energy();

		System.out.println("time taken for int.1x1 loops (2): " + (System.currentTimeMillis() - t) + " ms");

		String[] tmp2 = { "AUAUUU", "CGAUAG", "GAUAGC", "CCCGGG", "GGGCCC" };
		for (String s : tmp2) {
			System.out.println("(s1)" + s + ": " + loop11.getInt11LoopValue(s));
			System.out.println("(s2)" + s + ": " + loop112.getInt11LoopValue(s));
		}

	}

	public static void internal21() {
		// performance-measure
		long t = System.currentTimeMillis();
		IntLoop21Parser loop21 = new IntLoop21Parser("data/mfold/int21.dat").getData();

		System.out.println("time taken for int.2x1 loops (1): " + (System.currentTimeMillis() - t) + " ms");
		new IntLoop21Writer(loop21).write();

		t = System.currentTimeMillis();
		IntLoop21Parser loop212 = new IntLoop21EnergyClass().getIntLoop21Energy();
		System.out.println("time taken for int.2x1 loops (2): " + (System.currentTimeMillis() - t) + " ms");

		String[] tmp3 = { "AUAUGUU", "CGAUAAG", "GAUAGGC", "CCCGGGG", "GGGCCCC" };
		for (String s : tmp3) {
			System.out.println("(s1)" + s + ": " + loop21.getInt21LoopValue(s));
			System.out.println("(s2)" + s + ": " + loop212.getInt21LoopValue(s));
		}
	}

	public static void internal22() {
		// performance-measure
		long t = System.currentTimeMillis();
		IntLoop22Parser loop22 = new IntLoop22Parser("data/mfold/int22.dat").getData();

		System.out.println("time taken for int.2x2 loops (1): " + (System.currentTimeMillis() - t) + " ms");
		new IntLoop22Writer(loop22).write();

		t = System.currentTimeMillis();
		IntLoop22Parser loop222 = new IntLoop22EnergyClass().getIntLoop22Energy();
		System.out.println("time taken for int.2x2 loops (2): " + (System.currentTimeMillis() - t) + " ms");

		String[] tmp3 = { "AUUAUGUU", "ACGUACGU", "CGAUAAAG", "GAGUAGGC", "CCCCGGGG", "GGGGCCCC" };
		for (String s : tmp3) {
			System.out.println("(s1)" + s + ": " + loop22.getInt22LoopValue(s));
			System.out.println("(s2)" + s + ": " + loop222.getInt22LoopValue(s));
		}

	}

	public static void tstacking() {
		// OtherLoopParser other = new OtherLoopParser("data/mfold/loop.dat").getData();
		IntLoopTstackiParser tstack = new IntLoopTstackiParser("data/mfold/tstacki.dat");
		tstack.getData();

		String[] tmp = { "UCGA", "AUAU", "AAUU", "GUUG", "AU", "CCGG", "GGCC" };
		for (String s : tmp) {
			System.out.println(s + ": " + tstack.getStackedPairValue(s));
		}

		// for (int i=0; i<41; i++) {
		// System.out.println("bulge " + i + ": " + other.getBulgeValue(i) + " | internal " + i + ": " +
		// other.getInternalLoopValue(i));
		// }

	}

	public static void evalFile(String filename) {
		File infile = new File(filename);
		BufferedReader inbuff = null;
		try {
			inbuff = new BufferedReader(new FileReader(infile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String regexFasta = ">.*";
		String regexSeq = "[ACGU]*&[ACGU]*";
		String regexStruct = "[\\.(]*&[\\.)]*";

		Pattern pattFasta = Pattern.compile(regexFasta);
		Pattern pattSeq = Pattern.compile(regexSeq);
		Pattern pattStruct = Pattern.compile(regexStruct);

		String line;
		Matcher m;
		try {
			while ((line = inbuff.readLine()) != null) {
				m = pattFasta.matcher(line);
				if (m.find()) {
					line = inbuff.readLine();
					m = pattSeq.matcher(line);
					if (m.find()) {
						String seq = m.group();
						line = inbuff.readLine();
						m = pattStruct.matcher(line);
						if (m.find()) {
							Duplex d = new Duplex();
							d.setSequence(seq);
							d.setStructure(m.group());
							// d.initEnergies();
							// System.out.println(d.calcEnergy());
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void evalViennaFile(String filename) {
		File infile = new File(filename);
		BufferedReader inbuff = null;
		try {
			inbuff = new BufferedReader(new FileReader(infile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Vector<Double> myCode = new Vector<Double>(), viennaCode = new Vector<Double>(), foldedEnergies = new Vector<Double>();
		Vector<Long> time = new Vector<Long>();

		String regexFasta = ">.*";
		String regexSeq = "[ACGU]+&[ACGU]+";
		String regexStruct = "[\\.(]+&[\\.)]+";
		String regexEnergy = "\\-?\\d+.\\d+";

		Pattern pattFasta = Pattern.compile(regexFasta);
		Pattern pattSeq = Pattern.compile(regexSeq);
		Pattern pattStruct = Pattern.compile(regexStruct);
		Pattern pattEnergy = Pattern.compile(regexEnergy);

		FEC calc = new FEC();
		calc.initialize();
		calc.setLogLvl(Level.ALL);

		String line;
		Matcher m;
		try {
			while ((line = inbuff.readLine()) != null) {
				m = pattFasta.matcher(line);
				if (m.find()) {
					String name = m.group();
					while ((line = inbuff.readLine()) != null) {
						// skip lines until sequence
						m = pattSeq.matcher(line);
						if (m.find()) {
							String seq = m.group();
							line = inbuff.readLine();
							m = pattStruct.matcher(line);
							if (m.find()) {
								Duplex d = new Duplex();
								d.setName(name);
								d.setSequence(seq);
								d.setStructure(m.group());
								calc.setDuplex(d);
								double e = calc.calcEnergy();
								myCode.addElement(e);
								time.add(calc.getTimer());
								// System.out.println("Energy: " + e);
								// Alignment
								Duplex dAlign = new Duplex(seq);
								System.out.println(dAlign);
								calc.setDuplex(dAlign);
								double eAlign = calc.calcEnergy();
								foldedEnergies.add(eAlign);
								m = pattEnergy.matcher(line);
								if (m.find()) {
									double e_vienna = Double.parseDouble(m.group());
									System.out.println(" >>> name: " + d.name);
									System.out.print("Energy from Vienna: " + e_vienna);
									System.out.println(" my Code: " + e + " with alignment: " + eAlign);
									// dAlign.printAlignment();
									viennaCode.addElement(e_vienna);
								} else {
									System.out.println("did not recognize Vienna-Energy signature!");
								}
								break;
							}
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// try writing values into a tab-separated file ...
		File outfile = new File(filename.replace(".txt", "_comparison_GAIL.tab"));
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
			// header
			out.write("Vienna\tmyCode\taligned\ttime\n");
			// data
			if (viennaCode.size() != myCode.size()) {
				System.err.println("vectors have different number of elements!");
			}
			for (int i = 0; i < viennaCode.size(); i++) {
				out.write(viennaCode.elementAt(i) + "\t" + myCode.elementAt(i) + "\t" + foldedEnergies.elementAt(i)
						+ "\t" + time.elementAt(i) + "\n");
			}

			// close buffered writer
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}