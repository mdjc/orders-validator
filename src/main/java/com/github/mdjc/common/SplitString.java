package com.github.mdjc.common;

public class SplitString {
	private final String[] values;

	public SplitString(String[] values) {
		this.values = values;
	}

	public String value(int index) {
		return isValid(index) ? values[index] : "";
	}

	public static SplitString of(String str) {
		return of(str, ",");
	}
	
	public static SplitString of(String str, String regexDelimiter) {
		return new SplitString(str.split(regexDelimiter, -1));
	}

	private boolean isValid(int index) {
		return index >= 0 && index < values.length;
	}
}
