package com.bitcoin.wallet.metrics.recorders;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bitcoin.wallet.metrics.Reader;
import com.bitcoin.wallet.metrics.BitcoinCliCommand;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

@Service
public class BlockchainInfoRecorder implements Recorder {

	private static final Logger logger = LoggerFactory.getLogger(BlockchainInfoRecorder.class);
	private final Counter blockCounter = Metrics.counter("node_new_block");
	private final AtomicLong blockHeight = Metrics.gauge("node_block_height", new AtomicLong(0));
	private final BitcoinCliCommand bitcoinCliCommand;

	public BlockchainInfoRecorder(BitcoinCliCommand bitcoinCliCommand) {
		this.bitcoinCliCommand = Objects.requireNonNull(bitcoinCliCommand);
	}

	@Override
	public void record() {
		try {
			Process process = bitcoinCliCommand.doCommand("getblockchaininfo");
			String readResult = Reader.read(process.getInputStream());
			try {
				JsonObject result = JsonParser.parseString(readResult).getAsJsonObject();
				long blocks = result.get("blocks").getAsLong();
				logger.info("Current block hash={} height={}", result.get("bestblockhash").getAsString(), blocks);
				blockCounter.increment();
				blockHeight.set(blocks);
			} catch (Exception e) {
				logger.error("Could not parse response s={}", readResult, e);
			}
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

	@Override
	public String name() {
		return "getblockchaininfo";
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
