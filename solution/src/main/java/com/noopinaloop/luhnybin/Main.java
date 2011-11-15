package com.noopinaloop.luhnybin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String input;
			while ((input = reader.readLine()) != null) {
				System.out.println(new String(LuhnyFilter.filter(input.toCharArray())));
			}
			reader.close();
		} catch (IOException ioe) {
			// exit. :(
		}
	}

}
