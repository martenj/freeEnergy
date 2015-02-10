package de.charite.compbio.structures;

import de.charite.compbio.energyParsers.IntLoop11Parser;
import de.charite.compbio.energyParsers.StackingParser;

public class DuplexEnergy extends Duplex {

	StackingParser stack;
	IntLoop11Parser int11;

	public DuplexEnergy() {
		// TODO Auto-generated constructor stub
	}

	public DuplexEnergy(StackingParser stack, IntLoop11Parser int11) {
		this.stack = stack;
		this.int11 = int11;
	}

}
