package com.github.mdjc.ordersvalidator.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class ValidatorTest extends TestCase {
	private Validator validator;
	private Set<String> validSymbols;
	private static final int BROKER_ORDERS_PER_MINUTE_LIMIT = 3;

	public void setUp() {
		validSymbols = Sets.newHashSet("BARK", "BRGT", "BRIC");
		validator = new Validator(validSymbols, BROKER_ORDERS_PER_MINUTE_LIMIT);
	}

	public void testIsValidGivenOrderWithNullReturnsFalse() {
		LocalDateTime timeStamp = LocalDateTime.of(2014, 1, 5, 8, 30, 44);
		String broker = "Ameriprise Financial";
		int sequence = 1;
		char type = 'K';
		String symbol = firstValidSymbol();
		int quantity = 500;
		double price = 200.00;
		String side = "Buy";

		assertFalse(validator.isValid(new Order(null, null, null, null, null, null, null, null)));
		assertFalse(validator.isValid(new Order(null, broker, sequence, type, symbol, quantity, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, null, sequence, type, symbol, quantity, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, null, type, symbol, quantity, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, sequence, null, symbol, quantity, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, sequence, type, null, quantity, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, sequence, type, symbol, null, price, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, sequence, type, symbol, quantity, null, side)));
		assertFalse(validator.isValid(new Order(timeStamp, broker, sequence, type, symbol, quantity, price, null)));

	}

	public void testIsValidGivenOrderWithNonNullReturnsFalse() {
		assertTrue(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30), "Ameriprise Financial", 1, 'K',
				firstValidSymbol(), 500, 200.00, "Buy")));
	}

	public void testIsValidGivenOrderWithInvalidSymbolReturnsFalse() {
		Set<String> invalidSymbols = Sets.newHashSet("CARD","LEFT", "LGHT");
		invalidSymbols.forEach(s -> assertFalse(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30),
				"Ameriprise Financial", 1, 'K', s, 500, 200.00, "Buy"))));
	}

	public void testIsValidGivenOrderWithValidSymbolReturnTrue() {
		int[] sequenceHolder = { 1 };
		validSymbols.forEach(s -> assertTrue(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30),
				"Ameriprise Financial", sequenceHolder[0]++, 'K', s, 500, 200.00, "Buy"))));
	}

	public void testIsValidNonExcedingOrdersPerMinuteLimitReturnsTrue() {
		IntStream.rangeClosed(1, BROKER_ORDERS_PER_MINUTE_LIMIT)
				.forEach(i -> assertTrue(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30, i),
						"Ameriprise Financial", i + 1, 'K', firstValidSymbol(), 500, 200.00, "Buy"))));
	}

	public void testIsValidExcedingOrdersPerMinuteLimitReturnsFalse() {
		IntStream.rangeClosed(1, BROKER_ORDERS_PER_MINUTE_LIMIT)
				.forEach(i -> validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30, i),
						"Ameriprise Financial", i + 1, 'K', firstValidSymbol(), 500, 200.00, "Buy")));

		IntStream.range(0, 59)
				.forEach(i -> assertFalse(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30, i),
						"Ameriprise Financial", i + 1, 'K', firstValidSymbol(), 500, 200.00, "Buy"))));
	}

	public void testIsValidGivenDuplicateSequenceReturnsFalse() {
		validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30), "Ameriprise Financial", 1, 'K',
				firstValidSymbol(), 500, 200.00, "Buy"));

		for (int i = 0; i < 500; i++) {
			assertFalse(validator.isValid(new Order(LocalDateTime.of(2014, 1, 5, 8, 30), "Ameriprise Financial", 1, 'K',
					firstValidSymbol(), 500, 200.00, "Buy")));
		}
	}

	private String firstValidSymbol() {
		return validSymbols.stream().findFirst().get();
	}
}
