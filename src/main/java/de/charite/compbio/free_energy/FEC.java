package de.charite.compbio.free_energy;

//import java.io.File;
import java.io.IOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Date;
//import java.util.Formatter;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
//import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
//import java.util.logging.StreamHandler;

import de.charite.compbio.energyClasses.IntLoop11EnergyClass;
import de.charite.compbio.energyClasses.IntLoop21EnergyClass;
import de.charite.compbio.energyClasses.IntLoop22EnergyClass;
import de.charite.compbio.energyClasses.StackingEnergyClass;
import de.charite.compbio.energyParsers.IntLoop11Parser;
import de.charite.compbio.energyParsers.IntLoop21Parser;
import de.charite.compbio.energyParsers.IntLoop22Parser;
import de.charite.compbio.energyParsers.IntLoopTstackiParser;
import de.charite.compbio.energyParsers.OtherLoopParser;
import de.charite.compbio.energyParsers.StackingParser;
import de.charite.compbio.structures.Duplex;
import de.charite.compbio.structures.InternalLoop;

/**
 * class for calculation of free energy of a duplex-RNA-structure containing: - logging - timer - free-energy-values -
 * calculation
 * 
 * @author marcel87
 * @version 0.9
 */
public class FEC {

	/* Duplex sequence + structure */
	private Duplex d;

	/** logging of events ... */
	private static Level logLvl = Level.INFO;

	private Logger fecGlobal;
	private static Logger fecLog;

	private static long timer;

	/** name of logfile, set = null for no logfile */
	private String logfile = "fec.log";
	private boolean append = false;
	private java.util.logging.Formatter consoleFormatter = new java.util.logging.Formatter() {
		@Override
		public String format(LogRecord log) {
			// TODO Auto-generated method stub
			String msg = (new java.util.Date()).toString() + ": " + log.getMessage() + "\n";
			return msg;
		}
	};

	private java.util.logging.Formatter fileFormatter = new java.util.logging.Formatter() {
		@Override
		public String format(LogRecord log) {
			Date dat = new Date();
			dat.setTime(log.getMillis());
			Object[] objects = new Object[5];
			objects[0] = dat;
			objects[1] = log.getLevel();
			objects[2] = log.getSourceClassName();
			objects[3] = log.getSourceMethodName();
			objects[4] = log.getMessage();
			String msg_format = "{0, date} {0, time} - {1} in {2}:{3} | {4}\n"; // "{0,date} {0,time}";

			MessageFormat mf = new MessageFormat(msg_format);
			StringBuffer sb = new StringBuffer();
			mf.format(objects, sb, null);
			// sb.append(" ").append(log.getLevel()).append(" - ");
			// sb.append((new java.util.Date()).toString()).append(" ");
			// sb.append(log.getSourceClassName()).append(":").append(log.getSourceMethodName()).append(": ");
			// sb.append(log.getMessage());
			// sb.append("\n");
			// String msg = (new java.util.Date()).toString() + log.getSourceClassName() + ": " + log.getMessage() +
			// "\n";
			return sb.toString();
		}
	};

	/**
	 * @return the duplex
	 */
	public Duplex getDuplex() {
		return d;
	}

	/**
	 * @param d
	 *            the duplex to set
	 */
	public void setDuplex(Duplex d) {
		// String message = "set new duplex";
		// if (d.name!=null) {
		// message += ": " + d.name + "\n";
		// } else {
		// message += ":\n";
		// }
		// FEC.fecLog.finer(message + d);
		this.d = d;
	}

	/**
	 * !! not implemented yet !!
	 * 
	 * @param sequence
	 */
	public void setDuplex(String sequence) {
		FEC.fecLog.finer("set new duplex\n" + sequence);
		FEC.fecLog.severe("setting duplex just with sequence is not implemented yet!");
		this.d = new Duplex();
	}

	/**
	 * 
	 * @param sequence
	 * @param structure
	 */
	public void setDuplex(String sequence, String structure) {
		Duplex d = new Duplex(sequence, structure);
		FEC.fecLog.finer("set new duplex " + d);
		this.d = d;

	}

	/**
	 * get logging level
	 * 
	 * @return current logging level
	 */
	public Level getLogLvl() {
		return logLvl;
	}

	/**
	 * set logging level
	 * 
	 * @param logLvl
	 */
	public void setLogLvl(Level logLvl) {
		FEC.logLvl = logLvl;
	}

	/**
	 * set logging level
	 * 
	 * @param logLvl
	 */
	public void setLogLvl(int logLvl) {
		switch (logLvl) {
		case 0:
			FEC.logLvl = Level.OFF;
			break;
		case 1:
			FEC.logLvl = Level.SEVERE;
			break;
		case 2:
			FEC.logLvl = Level.WARNING;
			break;
		case 3:
			FEC.logLvl = Level.INFO;
			break;
		case 4:
			FEC.logLvl = Level.CONFIG;
			break;
		case 5:
			FEC.logLvl = Level.FINE;
			break;
		case 6:
			FEC.logLvl = Level.FINER;
			break;
		case 7:
			FEC.logLvl = Level.FINEST;
			break;
		default:
			FEC.logLvl = Level.ALL;
			break;
		}
	}

	/**
	 * gets current timer
	 * 
	 * @return timer
	 */
	public long getTimer() {
		return FEC.timer;
	}

	/* structure(s) for energy-tables etc. here: */
	private StackingParser stack = null;
	private IntLoopTstackiParser tstacki = null;
	private IntLoop11Parser int11 = null;
	private IntLoop21Parser int21 = null;
	private IntLoop22Parser int22 = null;
	private OtherLoopParser other = null;

	/* Default pathes for all energy-datafiles here: */
	// private static String path_to_stacked = "data/mfold/stack.dat";
	// private static String path_to_int11 = "data/mfold/int11.dat";
	// private static String path_to_int21 = "data/mfold/int21.dat";
	// private static String path_to_int22 = "data/mfold/int22.dat";
	// private static String path_to_loop = "data/mfold/loop.dat";
	// private static String path_to_miscloop = "data/mfold/miscloop.dat";

	/* internal parameters here */
	protected final static int INF = Integer.MAX_VALUE;
	protected final static double D_INF = Double.MAX_VALUE;

	// always check if files are available
	/*
	 * static { if (!(new File(path_to_stacked)).exists()) {
	 * System.out.println("No file for stacked energy-values found!"); } if (!(new File(path_to_int11)).exists()) {
	 * System.out.println("No file for int11-loop energy-values found!"); } if (!(new File(path_to_int21)).exists()) {
	 * System.out.println("No file for int21-loop energy-values found!"); } if (!(new File(path_to_int22)).exists()) {
	 * System.out.println("No file for int22-loop energy-values found!"); } if (!(new File(path_to_loop)).exists()) {
	 * System.out.println("No file for loop energy-values found!"); } if (!(new File(path_to_miscloop)).exists()) {
	 * System.out.println("No file for miscloop energy-values found!"); } }
	 */

	public FEC() {
		// init logger
		if (!initLogger()) {
			System.err.println("an error occured while initializing the logger");
		}
	}

	public void initialize() {

		/* do initialization with compiled java-classes here */
		FEC.timer = System.nanoTime();

		this.stack = new StackingEnergyClass().getStackingEnergy();
		FEC.fecLog.finer("stacking energy values read");
		// TODO: change to precompiled .class
		this.tstacki = new IntLoopTstackiParser("data/mfold/tstacki.dat").getData();
		FEC.fecLog.finer("tstacki energy values read");
		this.int11 = new IntLoop11EnergyClass().getIntLoop11Energy();
		FEC.fecLog.finer("int-11-loop energy values read");
		this.int21 = new IntLoop21EnergyClass().getIntLoop21Energy();
		FEC.fecLog.finer("int-21-loop energy values read");
		this.int22 = new IntLoop22EnergyClass().getIntLoop22Energy();
		FEC.fecLog.finer("int-22-loop energy values read");
		// TODO: change to precompiled .class
		this.other = new OtherLoopParser("data/mfold/loop.dat").getData();
		FEC.fecLog.finer("other loop energy values read");

		if (!checkEnergies()) {
			// System.err.println("an error occured while initializing the precompiled energy-classes");
			FEC.fecLog.severe("an error occured while initializing the precompiled energy-classes");
			// TODO: parse files with values ...

		}
		FEC.timer = (System.nanoTime() - FEC.timer);
		FEC.fecLog.finest("initialization time: " + FEC.timer + "ns");

	}

	/**
	 * check, if the energy-values are initialized
	 * 
	 * @return true when all energies are != null
	 */
	private boolean checkEnergies() {
		if (this.stack == null)
			return false;
		else if (this.int11 == null)
			return false;
		else if (this.int21 == null)
			return false;
		else if (this.int22 == null)
			return false;
		else if (this.other == null)
			return false;
		else
			return true;
	}

	/**
	 * initialize the logger
	 * 
	 * @return true when initilaization had no errors
	 */
	private boolean initLogger() {

		this.fecGlobal = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		FEC.fecLog = Logger.getLogger(FEC.class.getName() + "_Logger");
		// System.out.println(FEC.class.toString() + "_Logger");

		this.fecGlobal.setUseParentHandlers(false);
		FEC.fecLog.setUseParentHandlers(false);

		try {
			FEC.fecLog.setLevel(FEC.logLvl);

			FEC.fecLog.info("Started logging");

			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(this.consoleFormatter);

			FEC.fecLog.addHandler(ch);
			System.out.println("added console handler ...");
			FEC.fecLog.config("added console-handler to logger");

			if (this.logfile != null) {
				FileHandler fh = new FileHandler(this.logfile, this.append);
				fh.setFormatter(this.fileFormatter);
				FEC.fecLog.addHandler(fh);
				FEC.fecLog.config("write logfile to " + this.logfile);
			}

			// Handler[] logHandlers = {new ConsoleHandler(), new StreamHandler(System.out, consoleFormatter)};

			// } catch (SecurityException se ) {
			// se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		FEC.fecLog.info("created class for free energy calculation");

		/*
		 * fecLog.severe("test log level severe"); fecLog.warning("test log level warning");
		 * fecLog.info("test log level info"); fecLog.config("test log level config");
		 * fecLog.fine("test log level fine"); fecLog.finer("test log level finer");
		 * fecLog.finest("test log level finest");
		 */

		return true;
	}

	/**
	 * function calculates the free-energy of the RNA-duplex calculation of the free energy of a given duplex is
	 * performed with decomposition of the duplex and summation of all single elements
	 * 
	 * @return free energy of the duplex
	 */
	public double calcEnergy() {

		FEC.timer = System.nanoTime();

		FEC.fecLog.entering(this.getClass().getName(), "calcEnergy");
		FEC.fecLog.finer("start calculation of free energy");

		Vector<InternalLoop> loops = this.d.decompose();

		/** intramolecular initiation-energy, see miscloop */
		FEC.fecLog.finest("initial energy is " + OtherLoopParser.initEnergy);
		double energy = OtherLoopParser.initEnergy;

		// penalty if first or last basepair A=U / U=A
		String firstPair = loops.firstElement().firstPair();
		if (firstPair.equals("GC") || firstPair.equals("CG")) {
			// do nothing
		} else {
			FEC.fecLog.finest("add " + OtherLoopParser.penAU + " for first pair: " + firstPair);
			energy += OtherLoopParser.penAU;
		}

		String lastPair = loops.lastElement().lastPair();
		if (lastPair.equals("GC") || lastPair.equals("CG")) {
			// do nothing
		} else {
			FEC.fecLog.finest("add " + OtherLoopParser.penAU + " for last pair: " + lastPair);
			energy += 0.5;
		}

		double e;
		String finer_message, msg;
		for (InternalLoop l : loops) {
			finer_message = "process " + l + "; ";
			msg = "";
			// int loopSize;

			switch (l.type) {
			case 0:
				// stacked
				e = stack.getStackedPairValue(l.sequence);
				finer_message += "loop is a stacked basepair; energy=" + e;
				energy += e;
				break;
			case 1:
				// upper bulge
				e = bulgeLoop(l);
				finer_message += "loop is an upper bulge; energy=" + e;
				energy += e;
				break;
			case 2:
				// lower bulge
				e = bulgeLoop(l);
				finer_message += "loop is an lower bulge; energy=" + e;
				energy += e;
				break;
			case 3:
				// internal 1x1
				e = int11.getInt11LoopValue(l.sequence);
				finer_message += "loop is an internal 1x1-loop; energy=" + e;
				energy += e;
				break;
			case 4:
				// internal 2x1
				e = int21.getInt21LoopValue(l.sequence);
				finer_message += "loop is an internal 2x1-loop; energy=" + e;
				energy += e;
				break;
			case 5:
				// internal 1x2
				e = int21.getInt21LoopValue(l.sequence);
				finer_message += "loop is an internal 1x2-loop; energy=" + e;
				energy += e;
				break;
			case 6:
				// internal 2x2
				e = int22.getInt22LoopValue(l.sequence);
				finer_message += "loop is an internal 2x2-loop; energy=" + e;
				energy += e;
				break;
			case 7:
				// lower bulge_N
				e = bulgeNLoop(l);
				finer_message += "loop is an lower bulge_N; energy=" + e;
				energy += e;
				break;
			case 8:
				// upper bulge_N
				e = bulgeNLoop(l);
				// loopSize = l.j - l.i - 1;
				// e = other.getBulgeValue(loopSize);
				// msg += "(bulge_N size " + loopSize + ") " + e;
				// // System.out.println("bulge " + l + " : " + other.getBulgeValue(l.j - l.i - 1));
				// // add AU-penalty
				// if (l.firstPair().equals("GC") || l.firstPair().equals("CG")) {
				// // do nothing
				// } else {
				// // System.out.println("add +0.5");
				// msg += " + (A=U-penalty first pair) " + OtherLoopParser.penAU;
				// e += OtherLoopParser.penAU;
				// }
				// if (l.lastPair().equals("GC") || l.lastPair().equals("CG")) {
				// // do nothing
				// } else {
				// // System.out.println("add +0.5");
				// msg += " + (A=U-penalty last pair) " + OtherLoopParser.penAU;
				// e += OtherLoopParser.penAU;
				// }
				finer_message += "loop is an upper bulge_N; energy=" + e;
				energy += e;
				break;
			case 9:
				// internal Nx1 GAIL
				// System.out.println("first stacked: " + l.firstStackedPair() + " loop: " + l);
				// System.out.println("last stacked: " + l.lastStackedPair() + " loop: " + l);
				//
				// System.out.println("Not implemented yet! Cannot calculate " + l);
				// break;
			case 10:
				// internal 1xN GAIL
				// System.out.println("first stacked: " + l.firstStackedPair() + " loop: " + l);
				// System.out.println("last stacked: " + l.lastStackedPair() + " loop: " + l);
				// System.out.println("Not implemented yet! Cannot calculate " + l);
				// System.out.println("will calculate free energy of internal loop ...");
				finer_message += "loop is an ";
				finer_message += l.type == 9 ? "Nx1" : "1xN";
				finer_message += " GAIL; ";
				// this.fecLog.finest(paramString)
				e = OtherLoopParser.useGAIL ? internalGAIL(l) : internalLoopEnergy(l);
				finer_message += "energy=" + e;
				energy += e;
				break;
			case 11:
				// symmetric loop NxN
				// System.out.println("first stacked: " + l.firstStackedPair() + " loop: " + l);
				// System.out.println("last stacked: " + l.lastStackedPair() + " loop: " + l);
				// System.out.println("Not implemented yet! Cannot calculate " + l);
				// break;
			case 12:
				// asymmetric upper MxN
				// System.out.println("first stacked: " + l.firstStackedPair() + " loop: " + l);
				// System.out.println("last stacked: " + l.lastStackedPair() + " loop: " + l);
				// System.out.println("Not implemented yet! Cannot calculate " + l);
				// break;
			case 13:
				// asymmetric lower NxM
				// System.out.println("first stacked: " + l.firstStackedPair() + " loop: " + l);
				// System.out.println("last stacked: " + l.lastStackedPair() + " loop: " + l);
				// System.out.println("Not implemented yet! Cannot calculate " + l);
				// System.out.println("will calculate free energy of internal loop ...");
				finer_message += "loop is a";
				finer_message += l.type == 11 ? " symmetric NxN" : l.type == 12 ? "n asymmetric MxN"
						: "n asymmetric NxM";
				finer_message += " loop; ";
				e = internalLoopEnergy(l);
				finer_message += "energy=" + e;
				energy += e;
				break;

			default:
				// System.err.println("nothing found for " + l + " type: " + l.type);
				FEC.fecLog.warning("nothing found for " + l + " type: " + l.type);
				finer_message += "NOTHING FOUND ! ! !";
				break;
			}

			// FEC.fecLog.finer(finer_message);
			// if (msg.length()>0) {
			// FEC.fecLog.finest(msg);
			// }
		}

		// round the energy at 2 decimals
		energy = ((double) Math.round(energy * 1000)) / 1000;

		FEC.fecLog.fine("calculated energy: " + energy);
		// System.out.println("energy: " + energy);
		FEC.fecLog.exiting(this.getClass().getName(), "calcEnergy", energy);
		FEC.timer = System.nanoTime() - FEC.timer;
		FEC.fecLog.finest("calculation time: " + FEC.timer + "ns");

		return energy;
	}

	private double bulgeLoop(InternalLoop l) {
		/**
		 * calculation: e = bulge-value from loop.dat + stacking energy of bulge with non-paired base left out
		 * 
		 * NO A=U-penalty
		 */

		// System.out.println("calc bulge");

		double energy = 0, e;
		FEC.fecLog.entering(this.getClass().getName(), "bulgeLoop", l);
		String finestMessage = "";

		int l1 = l.j - l.i - 1, l2 = l.l - l.k - 1;
		// System.out.println("l1: " + l1 + " l2: " + l2);
		if (l1 == 1) {
			e = other.getBulgeValue(l1);
			finestMessage += "(bulge size " + l1 + ") " + e;
		} else if (l2 == 1) {
			e = other.getBulgeValue(l2);
			finestMessage += "(bulge size " + l2 + ") " + e;
		} else {
			// ??? loop to big?
			FEC.fecLog.warning("unexpected length: " + l1 + " and " + l2 + " of bulge");
			// System.out.println("???");
			e = D_INF;
		}
		if (e > 1000) {
			System.out.println("bulge size " + l1 + " = " + e);
		}
		energy += e;
		// System.out.println("bulge " + l + " : " + other.getBulgeValue(l.j - l.i - 1));

		// add stacking energy
		String seq = l.sequence;
		if (l1 == 1) {
			seq = "" + seq.charAt(0) + seq.charAt(2) + seq.substring(3);
			e = stack.getStackedPairValue(seq);
			finestMessage += " + (stacked " + seq + ") " + e;
			// System.out.println(seq);
		} else if (l2 == 1) {
			seq = seq.substring(0, 3) + seq.charAt(3) + seq.charAt(5);
			e = stack.getStackedPairValue(seq);
			finestMessage += " + (stacked " + seq + ") " + e;
			// System.out.println(seq);
		} else {
			// System.out.println(">> ???");
			FEC.fecLog.warning("unexpected length: " + l1 + " and " + l2 + " of bulge");
			e = D_INF;
		}
		if (e > 1000) {
			FEC.fecLog.warning("unexpected energy in bulge: " + e);
			// System.out.println("stacked pair " + seq + " = " + e);
		}
		energy += e;
		// round the energy at 2 decimals
		energy = ((double) Math.round(energy * 1000)) / 1000;

		FEC.fecLog.finest(finestMessage);

		FEC.fecLog.exiting(this.getClass().getName(), "bulgeLoop", energy);
		return energy;
	}

	private double bulgeNLoop(InternalLoop l) {
		/**
		 * calculation: e = bulge-value from loop.dat + A=U-penalty for first/last pair
		 * 
		 * nothing else
		 */

		FEC.fecLog.entering(this.getClass().getName(), "bulgeNLoop", l);

		double energy = 0.0, e;
		String msg = "";

		int l1 = l.j - l.i - 1, l2 = l.l - l.k - 1;
		if (l1 == 0) {
			e = other.getBulgeValue(l2);
			msg += "(bulge_N size " + l2 + ") " + e;
		} else if (l2 == 0) {
			e = other.getBulgeValue(l1);
			msg += "(bulge_N size " + l1 + ") " + e;
		} else {
			e = D_INF;
		}
		energy += e;

		// add AU-penalty
		if (l.firstPair().equals("GC") || l.firstPair().equals("CG")) {
			// do nothing
		} else {
			// System.out.println("add +0.5");
			msg += " + (A=U-penalty first pair) " + OtherLoopParser.penAU;
			e = OtherLoopParser.penAU;
			energy += e;
		}

		if (l.lastPair().equals("GC") || l.lastPair().equals("CG")) {
			// do nothing
		} else {
			// System.out.println("add +0.5");
			msg += " + (A=U-penalty last pair) " + OtherLoopParser.penAU;
			e = OtherLoopParser.penAU;
			energy += e;
		}

		// e = this.tstacki.getStackedPairValue(l.firstStackedPair());
		// msg += " + (first pair " + l.firstStackedPair() + ") " + e;
		// energy += e;
		// // System.out.println("first pair: " + l.firstStackedPair() + " -> " +
		// this.tstack.getStackedPairValue(l.firstStackedPair()));
		//
		// e = this.tstacki.getStackedPairValue(l.lastStackedPair());
		// msg += " + (last pair " + l.lastStackedPair() + ") " + e;
		// energy += e;
		// // System.out.println("last pair: " + l.lastStackedPair() + " -> " +
		// this.tstack.getStackedPairValue(l.lastStackedPair()));
		//
		// e = Math.min(OtherLoopParser.maxNinio, l.symmetry()*OtherLoopParser.arrayNinio);
		// msg += " + (Ninio " + l.symmetry() + ") " + e;
		// energy += e;
		// System.out.println("symmetry: " + l.symmetry() + " -> Ninio: " + Math.min(OtherLoopParser.maxNinio,
		// l.symmetry()*OtherLoopParser.arrayNinio));

		// round the energy at 2 decimals
		energy = ((double) Math.round(energy * 1000)) / 1000;

		FEC.fecLog.finest(msg);

		FEC.fecLog.exiting(this.getClass().getName(), "bulgeNLoop", energy);
		return energy;
	}

	private double internalGAIL(InternalLoop l) {
		/**
		 * calculation: e = internal loop energy (from table loop.dat), but second baspair is always A=A! +
		 * mismatch-score (a) + mismatch-score (b) + min(Ninio-MAX, asymmetry x Ninio[2])
		 */

		FEC.fecLog.entering(this.getClass().getName(), "internalGAIL", l);
		String finestMessage = "";

		double energy = 0.0;
		double e;

		e = other.getInternalLoopValue(l.size());
		finestMessage += "(loop size " + l.size() + ") " + e;
		energy += e;
		// System.out.println("loop is " + l.size() + " big -> " + energy);

		String firstStackedPair = l.firstPair();
		firstStackedPair = firstStackedPair.charAt(0) + "A&A" + firstStackedPair.charAt(1);
		e = this.tstacki.getStackedPairValue(firstStackedPair);
		finestMessage += " + (first pair) " + e;
		energy += e;
		// System.out.println("first pair: " + firstStackedPair + " -> " +
		// this.tstack.getStackedPairValue(firstStackedPair));

		String lastStackedPair = l.lastPair();
		lastStackedPair = lastStackedPair.charAt(1) + "A&A" + lastStackedPair.charAt(0);
		e = this.tstacki.getStackedPairValue(lastStackedPair);
		finestMessage += " + (last pair) " + e;
		energy += e;
		// System.out.println("last pair: " + lastStackedPair + " -> " +
		// this.tstack.getStackedPairValue(lastStackedPair));

		e = Math.min(OtherLoopParser.maxNinio, l.symmetry() * OtherLoopParser.arrayNinio);
		finestMessage += " + (Ninio " + l.symmetry() + ") " + e;
		energy += e;
		// System.out.println("symmetry: " + l.symmetry() + " -> Ninio: " + Math.min(OtherLoopParser.maxNinio,
		// l.symmetry()*OtherLoopParser.arrayNinio));

		// System.out.println("energy of GAIL: " + energy);
		FEC.fecLog.finest(finestMessage);

		// round the energy at 2 decimals
		energy = ((double) Math.round(energy * 1000)) / 1000;

		FEC.fecLog.exiting(this.getClass().getName(), "internalGAIL", energy);
		return energy;
	}

	private double internalLoopEnergy(InternalLoop l) {
		/**
		 * calculation: e = internal loop energy (from table loop.dat) + mismatch-score (a) + mismatch-score (b) +
		 * min(Ninio-MAX, asymmetry x Ninio[2])
		 */

		FEC.fecLog.entering(this.getClass().getName(), "internalLoopEnergy", l);
		String msg = "";

		double energy = 0.0;
		double e;

		e = other.getInternalLoopValue(l.size());
		msg += "(loop size " + l.size() + ") " + e;
		energy += e;
		// System.out.println("loop is " + l.size() + " big -> " + energy);

		e = this.tstacki.getStackedPairValue(l.firstStackedPair());
		msg += " + (first pair " + l.firstStackedPair() + ") " + e;
		energy += e;
		// System.out.println("first pair: " + l.firstStackedPair() + " -> " +
		// this.tstack.getStackedPairValue(l.firstStackedPair()));

		e = this.tstacki.getStackedPairValue(l.lastStackedPair());
		msg += " + (last pair " + l.lastStackedPair() + ") " + e;
		energy += e;
		// System.out.println("last pair: " + l.lastStackedPair() + " -> " +
		// this.tstack.getStackedPairValue(l.lastStackedPair()));

		e = Math.min(OtherLoopParser.maxNinio, l.symmetry() * OtherLoopParser.arrayNinio);
		msg += " + (Ninio " + l.symmetry() + ") " + e;
		energy += e;
		// System.out.println("symmetry: " + l.symmetry() + " -> Ninio: " + Math.min(OtherLoopParser.maxNinio,
		// l.symmetry()*OtherLoopParser.arrayNinio));

		FEC.fecLog.finest(msg);

		// round the energy at 2 decimals
		energy = ((double) Math.round(energy * 1000)) / 1000;

		FEC.fecLog.exiting(this.getClass().getName(), "internalLoopEnergy", energy);
		return energy;
	}

	public boolean compileClasses() {
		/* create new .java-files for use in 'initialize()' */

		/* Q: will this work??? */
		/* A: ??? */

		return false;
	}

}
