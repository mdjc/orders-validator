package com.github.mdjc.ordersvalidator.model;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Validator {
	private final Set<String> validSymbols;
	private final int brokerOrdersPerMinute;
	private final Map<String, SimpleEntry<LocalDateTime, Integer>> brokerLatestMinuteCountMap;
	private final Map<String, Integer> brokerLastSequenceMap;

	public Validator(Set<String> validSymbols, int brokerOrdersPerMinute) {
		this.validSymbols = validSymbols;
		this.brokerOrdersPerMinute = brokerOrdersPerMinute;
		brokerLatestMinuteCountMap = new HashMap<>();
		brokerLastSequenceMap = new HashMap<>();
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
		SimpleEntry<LocalDateTime, Integer> lastMinuteCountEntry = brokerLatestMinuteCountMap.get(order.getBroker());
		return lastMinuteCountEntry != null && lastMinuteCountEntry.getValue() >= brokerOrdersPerMinute;
	}

	private boolean isBrokerNewSequence(Order order) {
		Integer lastSequenceVal = brokerLastSequenceMap.get(order.getBroker());
		return lastSequenceVal == null || lastSequenceVal != order.getSequence();
	}

	private void cacheBrokerStats(Order order) {
		brokerLastSequenceMap.put(order.getBroker(), order.getSequence());
		cacheBrokerLastestMinuteOrderCount(order);
	}

	private void cacheBrokerLastestMinuteOrderCount(Order order) {
		LocalDateTime zeroSecsTimeStamp = order.getTimeStamp().withSecond(0);
		SimpleEntry<LocalDateTime, Integer> lastMinuteCountEntry = brokerLatestMinuteCountMap.get(order.getBroker());

		if (lastMinuteCountEntry == null || !lastMinuteCountEntry.getKey().equals(zeroSecsTimeStamp)) {
			brokerLatestMinuteCountMap.put(order.getBroker(),
					new SimpleEntry<LocalDateTime, Integer>(zeroSecsTimeStamp, 1));
		} else {
			lastMinuteCountEntry.setValue(lastMinuteCountEntry.getValue() + 1);
		}
	}
}
