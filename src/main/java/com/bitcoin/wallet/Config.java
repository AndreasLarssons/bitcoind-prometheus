package com.bitcoin.wallet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bitcoin.wallet.metrics.MetricsRecorder;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@Configuration
public class Config {

	@Autowired
	PrometheusMeterRegistry registry;

	@PostConstruct
	public void afterStart() {
		Metrics.addRegistry(registry);
	}

	@Bean
	public MetricsRecorder metricsRecorder(@Value("${rpc.user}") String rpcUser, @Value("${rpc.pass}") String rpcPassword) {
		return new MetricsRecorder(rpcUser, rpcPassword);
	}
}
