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

import io.micrometer.core.instrument.Metrics;

@Service
public class MempoolRecorder implements Recorder {
	private static final Logger logger = LoggerFactory.getLogger(MempoolRecorder.class);
	private final BitcoinCliCommand bitcoinCliCommand;
	private final AtomicLong mempoolbytes = Metrics.gauge("node_mempool_bytes", new AtomicLong(0));

	public MempoolRecorder(BitcoinCliCommand bitcoinCliCommand) {
		this.bitcoinCliCommand = Objects.requireNonNull(bitcoinCliCommand);
	}

	@Override
	public void record() {
		try {
			Process process = bitcoinCliCommand.doCommand("getmempoolinfo");
			String readResult = Reader.read(process.getInputStream());
			try {
				JsonObject result = JsonParser.parseString(readResult).getAsJsonObject();
				long bytes = result.get("bytes").getAsLong();
				mempoolbytes.set(bytes);
				logger.info("Current mempool bytes={}", bytes);
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
		return "mempool";
	}
}

/*{
  "loaded": true,
  "size": 99,
  "bytes": 30626,
  "usage": 119312,
  "maxmempool": 300000000,
  "mempoolminfee": 0.00001000,
  "minrelaytxfee": 0.00001000
}*/