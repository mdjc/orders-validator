package com.github.mdjc.ordersvalidator.model;

import java.time.LocalDateTime;

public class Order {
	private final LocalDateTime timeStamp;
	private final String broker;
	private final Integer sequence;
	private final String type;
	private final String symbol;
	private final Integer quantity;
	private final Double price;
	private final String side;

	public Order(LocalDateTime timeStamp, String broker, Integer sequence, String type, String symbol, Integer quantity,
			Double price, String side) {
		this.timeStamp = timeStamp;
		this.broker = broker;
		this.sequence = sequence;
		this.type = type;
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.side = side;
	}

	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	public String getBroker() {
		return broker;
	}

	public Integer getSequence() {
		return sequence;
	}

	public String getType() {
		return type;
	}

	public String getSymbol() {
		return symbol;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public Double getPrice() {
		return price;
	}

	public String getSide() {
		return side;
	}

	public boolean hasRequiredFields() {
		return isNonNull(timeStamp)
				&& isNonNullOrEmpty(broker)
				&& isNonNull(sequence)
				&& isNonNullOrEmpty(type)
				&& isNonNullOrEmpty(symbol) 
				&& isNonNull(quantity) 
				&& isNonNull(price) 
				&& isNonNullOrEmpty(side);
	}

	@Override
	public String toString() {
		return String.format(
				"timeStamp = %s, broker = %s, sequence = %d, type = %s, symbol = %s, quantity = %d, price = %f, side = %s",
				timeStamp, broker, sequence, type, symbol, quantity, price, side);

	}

	private static boolean isNonNullOrEmpty(String str) {
		return isNonNull(str) && !str.isEmpty();
	}

	private static boolean isNonNull(Object obj) {
		return obj != null;
	}
}
