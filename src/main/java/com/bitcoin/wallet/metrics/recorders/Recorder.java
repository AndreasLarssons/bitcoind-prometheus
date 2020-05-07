package com.bitcoin.wallet.metrics.recorders;

public interface Recorder {

	void record();

	String name();
}
