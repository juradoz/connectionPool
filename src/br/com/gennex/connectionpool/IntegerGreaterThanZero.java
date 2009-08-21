package br.com.gennex.connectionpool;

public class IntegerGreaterThanZero {
	private int value;

	public IntegerGreaterThanZero(int value) {
		if (value <= 0)
			throw new IllegalArgumentException("must be greater than zero");
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
