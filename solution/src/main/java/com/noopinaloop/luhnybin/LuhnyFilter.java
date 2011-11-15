package com.noopinaloop.luhnybin;


public class LuhnyFilter {

	// Indices into dynamic programming array.
	private static final int IDX_N    = 0;		// Number of digits in sequence up to and including this index.		
	private static final int IDX_ODD  = 1;		// Luhny sum of the sequence if this cursor is an odd index (not doubled).
	private static final int IDX_EVEN = 2;		// Luhny sum of the sequence if this cursor is an even index (doubled).

	// Sum of doubled digits.
	private static final int[] DOUBLED = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};
	
	public static final char[] filter(char[] chars) {
		if (chars.length < 14) {
			return chars;
		}
		
		int[][] memo = new int[chars.length+1][3];
		memo[0][IDX_N] = memo[0][IDX_ODD] = memo[0][IDX_EVEN] = 0;
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			switch (c) {
				case '-':
				case ' ':
					memo[i+1][IDX_N]    = memo[i][IDX_N];
					memo[i+1][IDX_ODD]  = memo[i][IDX_ODD];
					memo[i+1][IDX_EVEN] = memo[i][IDX_EVEN];
					continue;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					memo[i+1][IDX_N]    = memo[i][IDX_N]+1;
					memo[i+1][IDX_ODD]  = (memo[i][IDX_EVEN] + (chars[i] - '0')) % 10;
					memo[i+1][IDX_EVEN] = (memo[i][IDX_ODD]  + DOUBLED[(chars[i] - '0')]) % 10;
					int sequenceLen = memo[i+1][IDX_N];
					if (sequenceLen >= 14) {
						for (int checkLen = Math.min(16, sequenceLen); checkLen >= 14; --checkLen) {
							int overage = findOverage(memo, i+1, checkLen);
							if ((memo[i+1][IDX_ODD] - overage) % 10 == 0) {
								mask(chars, i, checkLen);
								break;
							}
						}
					}
					break;
				default:
					memo[i+1][IDX_N] = memo[i+1][IDX_ODD] = memo[i+1][IDX_EVEN] = 0;
					break;
			}
		}
		return chars;
	}
	
	private static final int findOverage(int[][] memo, int idx, int len) {
		// Exactly the right length. No overage recorded.
		if (memo[idx][IDX_N] == len) return 0;

		int overOddEven = (len == 15) ? IDX_EVEN : IDX_ODD;
		int extraLength = memo[idx][IDX_N] - len;
		int i = idx - len;
		while (true) {
			if (memo[i][IDX_N] == extraLength) {
				return memo[i][overOddEven];
			} else {
				i -= memo[i][IDX_N] - extraLength;
			}
		}
	}

	private static final void mask(char[] input, int idx, int len) {
		while(len > 0) {
			char c = input[idx];
			if ((c >= '0' && c <= '9') || c == 'X') {
				input[idx] = 'X';
				--len;
			}
			--idx;
		}
	}
}
