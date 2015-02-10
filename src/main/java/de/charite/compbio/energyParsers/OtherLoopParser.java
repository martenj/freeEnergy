package de.charite.compbio.energyParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * this class parses a file with other loop data ('loop.dat') for calculation of the free energy of RNA-Duplexes
 * 
 * @author marcel87
 * @version 0.8 beta
 * 
 */
public class OtherLoopParser extends EnergyParser {
	
	/** 
	 * further parameters from file 'miscloop.dat nessecary for calculation
	 * */

	/** maximum correction-value for Ninio-equation */
	public final static double maxNinio = 3.0;
	
	/** the only used value from the Ninio-array */
	public final static double arrayNinio = 0.5;
	
	/** terminal AU-penalty */
	public final static double penAU = 0.5;
	
	/** intermolecular initiation energy */
	public final static double initEnergy = 4.1;
	
	/** whether the GAIL-rule should be used */
	public final static boolean useGAIL = true;
//	public final static boolean useGAIL = false;
	
	
	/**
	 * header of file 'loop.dat' has 4 colMap: 'size', 'internal', 'bulge', 'hairpin'
	 * use column 'size' as iterator
	 */
	
	/**	current maximal size of values in table */
	private final static int MAX_SIZE = 31;
	
	/** parameter for extrapolation with  */
	private final static double param = 1.079;
	
	private double[] internal = new double[MAX_SIZE];
	private double[] bulge = new double[MAX_SIZE];
	private double[] hairpin = new double[MAX_SIZE];
	
	private int minSizeInternal = MAX_SIZE;
	private int minSizeBulge = MAX_SIZE;
	private int minSizeHairpin = MAX_SIZE;
	
	private final static String separator = "\\p{Blank}+";
	private final static String noValue = ".";
	private final static String regexHeaderEnd = "-+";
	
	private Pattern patternHeaderEnd;
	
	// TODO: merge both arrays into a hashtable!
	private String[] colNames = {"SIZE", "INTERNAL", "BULGE", "HAIRPIN"};
	private HashMap<String, Integer> colMap = new HashMap<String, Integer>();
//	private int[] colIndex = new int[colNames.length];

	public OtherLoopParser() {
		super();
	}
	
	public OtherLoopParser(String infile) {
//		create constructor from superclass
		super(infile);
//		initialize
		if (!initialize()) {
			System.err.println("Error initializing the data. Will exit.");
			System.exit(2);
		}
		
	}
	

	@Override
	public boolean init_small() {
		// TODO fill anything here?
		return true;
	}

	@Override
	protected boolean initialize() {
		
		patternHeaderEnd = Pattern.compile(regexHeaderEnd);
		
		this.isInitialized = true;
		return true;
	}

	@Override
	public OtherLoopParser getData() {
		try {
			if (!parseFile())
				System.err.println("error while parsing file " + this.infile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	protected boolean parseFile() throws IOException {
		
//		System.out.println("start parsing");
		
		BufferedReader inbuff = new BufferedReader(new FileReader(infile));
		String line, lastHeader = null;
		Matcher m;
//		Matcher t;
		
//		1st: find last line of header an determine order of columns in colMap
		while ((line = inbuff.readLine()) != null) {
//			System.out.println("line: " + line);
			m = patternHeaderEnd.matcher(line);
			if (!m.find()) {
				lastHeader = line;
			} else {
//				System.out.println("found header-sep");
				break;
			}
		}
		
//		System.out.println("last line: " + lastHeader);
		
		String[] columns;
		
		columns = lastHeader.split(separator);
		for (int i=0; i<columns.length; i++) {
			for (int j=0; j<colNames.length; j++) {
				if (colNames[j].equals(columns[i])) {
					this.colMap.put(colNames[j], i);
//					System.out.println(colNames[j] + " in col " + i);
					break;
				}
			}
		}
//		now you can find colName 'i' in column colIndex['i']
//		hope it's not too confusing
		
//		2nd: parse other lines
		int index;
		while ((line = inbuff.readLine()) != null) {
			columns = line.split(separator);
			index = Integer.parseInt(columns[colMap.get("SIZE")]);
//			parse values
//			internal:
			if (!columns[colMap.get("INTERNAL")].equals(noValue)) {
				internal[index] = Double.parseDouble(columns[colMap.get("INTERNAL")]);
				minSizeInternal = Math.min(minSizeInternal, index);
			} else {
				internal[index] = D_INF;
			}
//			bulge:
			if (!columns[colMap.get("BULGE")].equals(noValue)) {
				bulge[index] = Double.parseDouble(columns[colMap.get("BULGE")]);
				minSizeBulge = Math.min(minSizeBulge, index);
			} else {
				bulge[index] = D_INF;
			}
//			hairpin:
			if (!columns[colMap.get("HAIRPIN")].equals(noValue)) {
				hairpin[index] = Double.parseDouble(columns[colMap.get("HAIRPIN")]);
				minSizeHairpin = Math.min(minSizeHairpin, index);
			} else {
				hairpin[index] = D_INF;
			}
			
//			System.out.println("index: " + index + " internal: " + internal[index] + " bulge: " + bulge[index] + " hairpin: " + hairpin[index]);
		}
		
//		System.out.println("min sizes: " + minSizeInternal + " " + minSizeBulge + " " + minSizeHairpin);
			
		return true;
	}
	
	public double getBulgeValue(int bulgeLength) {
//		check range
		if (bulgeLength<1) {
//			maybe warning?
			return D_INF;
		} else  if (bulgeLength >= MAX_SIZE) {
//			not implemented yet
			return extrapolate(bulgeLength, getBulgeValue(30));
		} else {
			return bulge[bulgeLength];
		}
	}
	
	public double getInternalLoopValue (int loopLength) {
		if (loopLength < 4) {
//			maybe warning?
			return D_INF;
		} else if (loopLength >= MAX_SIZE) {
//			not implemented yet
			return extrapolate(loopLength, getInternalLoopValue(30));
		} else {
			return internal[loopLength];
		}
	}
	
	private double extrapolate(int l, double ddG30) {
		
//		double e = ddG30 + param * Math.log(Double.valueOf(l)/30.0);
		return ddG30 + param * Math.log(Double.valueOf(l)/30.0);
	}
//	private int getCol(String name) {
//		for (int i=0; i<colNames.length; i++) {
//			if (colNames[i].equals(name)) {
//				return i;
//			}
//		}
//		return -1;
//	}

}
