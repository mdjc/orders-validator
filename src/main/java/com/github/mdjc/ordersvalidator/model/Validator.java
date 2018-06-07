package com.github.mdjc.ordersvalidator.model;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Validator {
	private final Set<String> validSymbols;
	private final int ordersPerMinuteLimit;
	private final Map<String, SimpleEntry<LocalDateTime, Integer>> brokerLatestMinuteOrderCountMap;
	private final Map<String, Integer> brokerLastSentSequenceMap;

	public Validator(Set<String> validSymbols, int amountOfOrdersPerMinute) {
		this.validSymbols = validSymbols;
		this.ordersPerMinuteLimit = amountOfOrdersPerMinute;
		brokerLatestMinuteOrderCountMap = new HashMap<>();
		brokerLastSentSequenceMap = new HashMap<>();
	}

	public boolean isValid(Order order) {
		if (!order.hasRequiredFields())
			return false;

		boolean valid = hasValidSymbol(order) && !brokerExceedsOrdersLimit(order) && isBrokerNewSequence(order);
		cacheBrokerStats(order);
		return valid;
	}

	private boolean hasValidSymbol(Order order) {
		return validSymbols.contains(order.getSymbol());
	}

	private boolean brokerExceedsOrdersLimit(Order order) {
		SimpleEntry<LocalDateTime, Integer> timeStampCountEntry = brokerLatestMinuteOrderCountMap
				.get(order.getBroker());
		return timeStampCountEntry != null && timeStampCountEntry.getValue() >= ordersPerMinuteLimit;
	}

	private boolean isBrokerNewSequence(Order order) {
		Integer lastSequenceVal = brokerLastSentSequenceMap.get(order.getBroker());
		return lastSequenceVal == null || lastSequenceVal != order.getSequence();
	}

	private void cacheBrokerStats(Order order) {
		brokerLastSentSequenceMap.put(order.getBroker(), order.getSequence());
		cacheBrokerLastestMinuteOrderCount(order);
	}

	private void cacheBrokerLastestMinuteOrderCount(Order order) {
		LocalDateTime zeroSecsTimeStamp = order.getTimeStamp().withSecond(0);
		SimpleEntry<LocalDateTime, Integer> entry = brokerLatestMinuteOrderCountMap.get(order.getBroker());

		if (entry == null || !entry.getKey().equals(zeroSecsTimeStamp)) {
			brokerLatestMinuteOrderCountMap.put(order.getBroker(),
					new SimpleEntry<LocalDateTime, Integer>(zeroSecsTimeStamp, 1));
		} else {
			SimpleEntry<LocalDateTime, Integer> simpleEntry = brokerLatestMinuteOrderCountMap.get(order.getBroker());
			int count = simpleEntry.getValue();
			simpleEntry.setValue(++count);
		}
	}
}
