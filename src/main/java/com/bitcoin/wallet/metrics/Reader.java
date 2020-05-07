package com.bitcoin.wallet.metrics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Reader {

	public static String read(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream)).lines()
				.collect(Collectors.joining(""));
	}
}
