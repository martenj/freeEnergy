package de.charite.compbio.free_energy;

import de.charite.compbio.energyParsers.IntLoop11Parser;
import de.charite.compbio.energyParsers.StackingParser;

public class Calculate {

	StackingParser stack;
	IntLoop11Parser int11;

	public Calculate() {

	}

	public Calculate(StackingParser stack, IntLoop11Parser int11) {
		this.stack = stack;
		this.int11 = int11;
	}

}
