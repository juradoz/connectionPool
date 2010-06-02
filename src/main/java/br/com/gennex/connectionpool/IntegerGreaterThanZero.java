package br.com.gennex.connectionpool;

import java.security.InvalidParameterException;

public class IntegerGreaterThanZero {
	private int value;

	public IntegerGreaterThanZero(int value) {
		if (value <= 0)
			throw new InvalidParameterException("must be greater than zero");
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
