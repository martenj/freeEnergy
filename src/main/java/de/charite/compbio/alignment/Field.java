package de.charite.compbio.alignment;

public class Field {

	public int traceX, traceY;
	public double value;
	public boolean isPair;

	public Field() {
		this.value = 0.0;
		this.isPair = false;
	}

	public Field(boolean isPair) {
		this.value = 0.0;
		this.isPair = isPair;
	}

	public void setTrace(int x, int y) {
		this.traceX = x;
		this.traceY = y;
	}

	@Override
	public String toString() {
		// return "val: " + this.value + " pair: " + this.isPair;
		return this.value + "|" + this.isPair;
	}

	public void print() {
		System.out.printf("|%6.2f:%5b ", this.value, this.isPair);
	}

}
