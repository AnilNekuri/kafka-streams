package com.anil.poc.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
public class KafkaStreamsApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG,"streams-starter-app");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:29092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		StreamsBuilder builder = new StreamsBuilder();
		KStream<String,String> wordCountInput = builder.stream("word-count-input");
		final KTable<String, Long> wordCounts = wordCountInput
				.mapValues(textLine -> textLine.toLowerCase())
				.flatMapValues(lowerCaseTextLine -> Arrays.asList(lowerCaseTextLine.split(" ")))
				.selectKey((ignoredKey, word) -> word)
				.groupByKey()
				.count();

		wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

		KafkaStreams streams = new KafkaStreams(builder.build(), config);
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
}
