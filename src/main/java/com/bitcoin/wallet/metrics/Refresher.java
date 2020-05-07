package com.bitcoin.wallet.metrics;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitcoin.wallet.metrics.recorders.Recorder;

public class Refresher {

	private final ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor();
	private static final Logger logger = LoggerFactory.getLogger(Refresher.class);

	public Refresher(List<Recorder> recorders) {
		refresher.scheduleWithFixedDelay(() -> {
			recorders.forEach(recorder -> {
				try {
					recorder.record();
				} catch (Exception e) {
					logger.error("Failed record name={}", recorder.name(), e);
				}
			});
		}, 10, 120, TimeUnit.SECONDS);
	}
}
