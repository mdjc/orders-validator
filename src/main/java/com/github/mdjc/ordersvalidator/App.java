package com.github.mdjc.ordersvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.github.mdjc.common.SplitString;
import com.github.mdjc.ordersvalidator.model.Order;
import com.github.mdjc.ordersvalidator.model.Validator;
import com.google.common.collect.Sets;

public class App {
	private static final Logger LOGGER = Logger.getLogger(App.class);

	private static final String ACCEPTED_BROKER_SEQ_FILE_NAME 	= "accepted-broker-seq.txt";
	private static final String REJECTED_BROKER_SEQ_FILE_NAME 	= "rejected-broker-seq.txt";
	private static final String ACCEPTED_ORDERS_FILE_NAME 		= "accepted-orders.txt";
	private static final String REJECTED_ORDERS_FILE_NAME 		= "rejected-orders.txt";

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("MM/d/yyyy HH:mm:ss");

	private static final Set<String> VALID_SYMBOLS = 
			Sets.newHashSet("BARK", "CARD", "HOOF", "LOUD", "GLOO", "YLLW", "BRIC", "KRIL", "LGHT", "VELL");
	private static final int BROKER_ORDERS_PER_MINUTE = 3;
	private static final Validator VALIDATOR = new Validator(VALID_SYMBOLS, BROKER_ORDERS_PER_MINUTE);

	private static int processedOrderCount;
	private static int validOrderCount;
	private static int rejectedOrderCount;

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Orders File Path is required");
			System.exit(-1);
		}

		try (Stream<String> stream = Files.lines(Paths.get(args[0]));
				BufferedWriter acceptedBrokerSeqWriter 	= Files.newBufferedWriter(Paths.get(ACCEPTED_BROKER_SEQ_FILE_NAME));
				BufferedWriter rejectedBrokerSeqWriter 	= Files.newBufferedWriter(Paths.get(REJECTED_BROKER_SEQ_FILE_NAME));
				BufferedWriter acceptedOrdersWriter 	= Files.newBufferedWriter(Paths.get(ACCEPTED_ORDERS_FILE_NAME));
				BufferedWriter rejectedOrdersWriter 	= Files.newBufferedWriter(Paths.get(REJECTED_ORDERS_FILE_NAME))) {

			stream.skip(1)
					.forEach(line -> 
						process(line, acceptedBrokerSeqWriter, rejectedBrokerSeqWriter, acceptedOrdersWriter, rejectedOrdersWriter));

		}

		LOGGER.info(String.format("processed orders = %d, accepted orders = %d, rejected orders = %d ",
				processedOrderCount, validOrderCount, rejectedOrderCount));
	}

	private static void process(String line, BufferedWriter acceptedBrokerSeqWriter,
			BufferedWriter rejectedBrokerSeqWriter, BufferedWriter acceptedOrdersWriter,
			BufferedWriter rejectedOrdersWriter) {
		try {
			Order order = parseOrder(line);

			if (VALIDATOR.isValid(order)) {
				outpuOrder(order, acceptedBrokerSeqWriter, acceptedOrdersWriter);
				validOrderCount++;
			} else {
				outpuOrder(order, rejectedBrokerSeqWriter, rejectedOrdersWriter);
				rejectedOrderCount++;
			}

			processedOrderCount++;
		} catch (Exception e) {
			LOGGER.error(String.format("Error - processing line %s", line), e);
		}
	}

	private static Order parseOrder(String line) {
		SplitString splitStr = SplitString.of(line);
		LocalDateTime timeStamp = LocalDateTime.parse(splitStr.value(0), TIMESTAMP_FORMATTER);
		String broker = splitStr.value(1);
		Integer sequence = Integer.valueOf(splitStr.value(2));
		String type = splitStr.value(3);
		String symbol = splitStr.value(4);
		Integer quantity = Integer.valueOf(splitStr.value(5));
		Double price = Double.valueOf(splitStr.value(6));
		String sideStr = splitStr.value(7);
		return new Order(timeStamp, broker, sequence, type, symbol, quantity, price, sideStr);
	}

	private static void outpuOrder(Order order, BufferedWriter brokerSeqWriter, BufferedWriter ordersWriter)
			throws IOException {
		brokerSeqWriter.write(brokerSequenceStr(order));
		ordersWriter.write(orderStr(order));
	}

	private static String brokerSequenceStr(Order order) {
		String sequenceStr = String.valueOf(order.getSequence());
		return String.format("%s,%s%n", order.getBroker(), sequenceStr);
	}

	private static String orderStr(Order order) {
		return String.format("%s%n", order.toString());
	}
}
