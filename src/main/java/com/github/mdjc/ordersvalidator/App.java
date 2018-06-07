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

import com.github.mdjc.ordersvalidator.model.Order;
import com.github.mdjc.ordersvalidator.model.Validator;
import com.google.common.collect.Sets;

public class App {
	private static final Logger LOGGER = Logger.getLogger(App.class);

	private static final String ACCEPTED_BROKER_SEQ_FILE_NAME = "accepted-broker-seq.txt";
	private static final String REJECTED_BROKER_SEQ_FILE_NAME = "rejected-broker-seq.txt";
	private static final String ACCEPTED_ORDERS_FILE_NAME = "accepted-orders.txt";
	private static final String REJECTED_ORDERS_FILE_NAME = "rejected-orders.txt";

	private static final int BROKER_ORDERS_PER_MINUTE_LIMIT = 3;
	private static final Set<String> validSymbols = Sets.newHashSet("BARK", "CARD", "HOOF", "LOUD", "GLOO", "YLLW",
			"BRIC", "KRIL", "LGHT", "VELL");

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("MM/d/yyyy HH:mm:ss");
	private static Validator validator = new Validator(validSymbols, BROKER_ORDERS_PER_MINUTE_LIMIT);

	private static int processedOrderCount;
	private static int validOrderCount;
	private static int rejectedOrderCount;

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Orders File Path is required");
			System.exit(-1);
		}

		try (Stream<String> stream = Files.lines(Paths.get(args[0]));
				BufferedWriter acceptedBrokerSeqWriter = Files.newBufferedWriter(Paths.get(ACCEPTED_BROKER_SEQ_FILE_NAME));
				BufferedWriter rejectedBrokerSeqWriter = Files.newBufferedWriter(Paths.get(REJECTED_BROKER_SEQ_FILE_NAME));
				BufferedWriter acceptedOrdersWriter = Files.newBufferedWriter(Paths.get(ACCEPTED_ORDERS_FILE_NAME));
				BufferedWriter rejectedOrdersWriter = Files.newBufferedWriter(Paths.get(REJECTED_ORDERS_FILE_NAME))) {

			stream.skip(1)
				  .forEach(line -> 
						process(line, acceptedBrokerSeqWriter, rejectedBrokerSeqWriter, acceptedOrdersWriter, rejectedOrdersWriter));

		}

		LOGGER.info(String.format("processed orders = %d, accepted orders = %d, rejected orders = %d ",
				processedOrderCount, validOrderCount, rejectedOrderCount));
	}

	private static void process(String line, BufferedWriter brokerSeqAcceptedWriter,
			BufferedWriter brokerSeqRejectedWriter, BufferedWriter acceptedOrdersWriter,
			BufferedWriter rejectedOrdersWriter) {
		try {
			Order order = parseOrder(line);

			if (validator.isValid(order)) {
				outpuOrder(order, brokerSeqAcceptedWriter, acceptedOrdersWriter);
				validOrderCount++;
			} else {
				outpuOrder(order, brokerSeqRejectedWriter, rejectedOrdersWriter);
				rejectedOrderCount++;
			}

			processedOrderCount++;
		} catch (Exception e) {
			LOGGER.error(String.format("Error processing line %s", line), e);
		}
	}

	private static Order parseOrder(String line) {
		String[] fields = line.split(",", -1);
		String timeStampStr = fields[0].isEmpty() ? null : fields[0];
		LocalDateTime timeStamp = timeStampStr == null ? null : LocalDateTime.parse(timeStampStr, TIMESTAMP_FORMATTER);
		String broker = fields[1].isEmpty() ? null : fields[1];
		Integer sequence = fields[2].isEmpty() ? null : Integer.valueOf(fields[2]);
		Character type = fields[3].isEmpty() ? null : fields[3].charAt(0);
		String symbol = fields[4].isEmpty() ? null : fields[4];
		Integer quantity = fields[5].isEmpty() ? null : Integer.valueOf(fields[5]);
		Double price = fields[6].isEmpty() ? null : Double.valueOf(fields[6]);
		String sideStr = fields[7].isEmpty() ? null : fields[7];
		return new Order(timeStamp, broker, sequence, type, symbol, quantity, price, sideStr);
	}

	private static void outpuOrder(Order order, BufferedWriter brokerSeqWriter, BufferedWriter ordersWriter)
			throws IOException {
		brokerSeqWriter.write(brokerSequenceStr(order));
		ordersWriter.write(orderStr(order));
	}

	private static String brokerSequenceStr(Order order) {
		String sequenceStr = order.getSequence() == null ? " " : String.valueOf(order.getSequence());
		String brokerSequence = String.format("%s,%s%n", order.getBroker(), sequenceStr);
		return brokerSequence;
	}

	private static String orderStr(Order order) {
		return String.format("%s%n", order.toString());
	}
}
