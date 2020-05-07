package com.bitcoin.wallet.metrics;

import java.io.File;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinCliCommand {

	private final RpcConfig rpcConfig;
	private static final Logger logger = LoggerFactory.getLogger(BitcoinCliCommand.class);

	public BitcoinCliCommand(RpcConfig rpcConfig) {
		this.rpcConfig = Objects.requireNonNull(rpcConfig);
	}

	public Process doCommand(String command) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String fullCommand = "/usr/bin/bitcoin-cli" + " -rpcuser=" + rpcConfig.getRpcUser() + " -rpcpassword=" + rpcConfig.getRpcPassword() + " " + command;
			logger.info("Executing command={}", fullCommand);
			builder.command("sh", "-c", fullCommand);
			builder.directory(new File(System.getProperty("user.home")));
			return builder.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
