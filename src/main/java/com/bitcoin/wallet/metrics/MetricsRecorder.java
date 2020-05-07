package com.bitcoin.wallet.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

public class MetricsRecorder implements Runnable {

	private final ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor();
	private static final Logger logger = LoggerFactory.getLogger(MetricsRecorder.class);
	private final Counter blockCounter = Metrics.counter("node_new_block");
	private final AtomicLong blockHeight = Metrics.gauge("node_block_height", new AtomicLong(0));
	private final String rpcUser;
	private final String rpcPassword;

	public MetricsRecorder(String rpcUser, String rpcPassword) {
		this.rpcUser = rpcUser;
		this.rpcPassword = rpcPassword;
		refresher.scheduleWithFixedDelay(this, 10, 120, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String command = "/usr/bin/bitcoin-cli" + " -rpcuser=" + rpcUser + " -rpcpassword=" + rpcPassword + " getblockchaininfo";
			builder.command("sh", "-c", command);
			builder.directory(new File(System.getProperty("user.home")));
			Process process = builder.start();

			new StreamConsumer(process.getInputStream(), s -> {
				try {
					JsonObject result = JsonParser.parseString(s).getAsJsonObject();
					long blocks = result.get("blocks").getAsLong();
					logger.info("Current block hash={} height={}", result.get("bestblockhash").getAsString(), blocks);
					blockCounter.increment();
					blockHeight.set(blocks);
				} catch (Exception e) {
					logger.error("Could not parse response s={}", s, e);
				}
			}).run();
			boolean waitFor = process.waitFor(10, TimeUnit.SECONDS);
			if (waitFor) {
				logger.info("Successfully completed");
			} else {
				logger.error("Did not complete in time");
			}

		} catch (Exception e) {
			logger.error("Could not get block data", e);
		}
	}

	private static class StreamConsumer implements Runnable {
		private final InputStream inputStream;
		private final Consumer<String> consumer;

		public StreamConsumer(InputStream inputStream, Consumer<String> consumer) {
			this.inputStream = inputStream;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			String collect = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining(""));
			consumer.accept(collect);
		}
	}


	/*{
  "chain": "main",
  "blocks": 633994,
  "headers": 633994,
  "bestblockhash": "00000000000000000354b8ae685005615f5c98d3c12a308c208aa57ded8c45d9",
  "difficulty": 202532750926.2829,
  "mediantime": 1588817936,
  "verificationprogress": 0.999999644934875,
  "initialblockdownload": false,
  "chainwork": "0000000000000000000000000000000000000000012ff5212fae5c3ab4c02578",
  "size_on_disk": 167064871378,
  "pruned": false,
  "softforks": {
  },
  "warnings": ""
}*/
}
