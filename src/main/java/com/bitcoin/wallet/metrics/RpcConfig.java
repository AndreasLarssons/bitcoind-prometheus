package com.bitcoin.wallet.metrics;

import java.util.Objects;

public class RpcConfig {

	private final String rpcUser;
	private final String rpcPassword;

	public RpcConfig(String rpcUser, String rpcPassword) {
		this.rpcUser = Objects.requireNonNull(rpcUser);
		this.rpcPassword = Objects.requireNonNull(rpcPassword);
	}

	public String getRpcUser() {
		return rpcUser;
	}

	public String getRpcPassword() {
		return rpcPassword;
	}
}
