package com.bitcoin.wallet;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bitcoin.wallet.metrics.recorders.Recorder;
import com.bitcoin.wallet.metrics.Refresher;
import com.bitcoin.wallet.metrics.BitcoinCliCommand;
import com.bitcoin.wallet.metrics.RpcConfig;

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
	public BitcoinCliCommand rpcCommand(RpcConfig rpcConfig) {
		return new BitcoinCliCommand(rpcConfig);
	}

	@Bean
	public Refresher refresher(List<Recorder> recorders) {
		return new Refresher(recorders);
	}

	@Bean
	public RpcConfig rpcConfig(@Value("${rpc.user}") String rpcUser, @Value("${rpc.pass}") String rpcPassword) {
		return new RpcConfig(rpcUser, rpcPassword);
	}
}
