/*
 * Copyright (C) 2011 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.crazybin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Identifies and masks suspected credit card numbers. Usage:
 *
 * <pre>
 *   String masked = new Masker().mask(rawString);
 * </pre>
 *
 * @author Bob Lee (bob@squareup.com)
 */
public class Masker {
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "US-ASCII"));
    Masker masker = new Masker();

    // TODO: Stream instead of filtering line-by-line.
    String line;
    while ((line = in.readLine()) != null) System.out.println(masker.mask(line));
  }

  private static final int MIN_LENGTH = 14;
  private static final int MAX_LENGTH = 16;
  private static final char MASK = 'X';

  /** Power of 2, > MAX_LENGTH. */
  private static final int CAPACITY = MAX_LENGTH << 1;

  static {
    if (Integer.bitCount(CAPACITY) != 1) {
      throw new AssertionError("CAPACITY isn't a power of 2.");
    }
  }

  private int[] currentSums, nextSums;
  private int end, length, prevLen;

  public Masker() {
    this.currentSums = new int[CAPACITY];
    this.nextSums = new int[CAPACITY];
  }

  /**
   * Identifies card numbers and replaces the digits with {@literal 'X'}. Strings may contain
   * any character to the left and right of the number (including no character) and may contain
   * any combination of {@literal ' '} or {@literal '-'} within the number itself. Runs O(N).
   * Not safe for concurrent use.
   */
  public String mask(String s) {
    prevLen = 0;
    if (s.length() < MIN_LENGTH) return s;
    reset();
    char[] masked = null;
    for (int i = skip(-1, s)+1; i < s.length(); i++) {
      char c = s.charAt(i);
      if (digit(c)) {
        push(c);
        int matchingDigits = matchingDigits();
        if (matchingDigits > 0) {
          if (masked == null) masked = s.toCharArray();
          mask(masked, i, matchingDigits);
        }
      } else if (!separator(c)) {
        reset();
        i=skip(i,s);
      }
    }
    return masked == null ? s : new String(masked);
  }

  /**
   * Replaces count digits with 'X' ending at index last (inclusive). Skips separator characters.
   */
  private void mask(char[] masked, int last, int count) {
    int min = count - prevLen;
    prevLen = count;
    while (count > 0) {
      char c = masked[last];
      if (digit(c)) {
        masked[last] = MASK;
        min--;
        count--;
      } else if (c == MASK) {
        // Already masked. Keep going if previous match was shorter.
        if (min<1) {
          break;
        }
        count--;
      } else {
        // assert separator(c)
      }
      last--;
    }
  }

  /** Returns true if c is a digit. */
  private static boolean digit(char c) {
    return c >= '0' && c <= '9';
  }

  /** Returns true if c is a separator. */
  private static boolean separator(char c) {
    return c == ' ' || c == '-';
  }

  /** Sums of the individual digits after doubling 0-9. */
  private static final int[] DOUBLED_AND_SUMMED = { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 };

  void push(char digit) {
    swapArrays();
    int value = digit - '0';
    accumulate(currentSums, value);
    accumulate(nextSums, DOUBLED_AND_SUMMED[value]);
    end = wrap(end + 1);
    if (length < MAX_LENGTH) length++;
  }

  private void swapArrays() {
    int[] temp = currentSums;
    currentSums = nextSums;
    nextSums = temp;
  }

  /** Stores value plus the previous sum in sums. */
  private void accumulate(int[] sums, int value) {
    sums[end] = sums[wrap(end - 1)] + value;
  }

  private void reset() {
    // We don't care about end or existing values in the arrays.
    length = 0;
  }

  private int skip(int nonDigitNonSepIndex, String s) {
    // Skip the index ahead
    int len = s.length();
    for (int i=nonDigitNonSepIndex+MIN_LENGTH; i<len; i+=MIN_LENGTH) {
      char c = s.charAt(i);
      if (digit(c) || separator(c)) return i-MIN_LENGTH;
    }
    // We've exhausted the string.
    return len;
  }

  private static int wrap(int index) {
    return index & (CAPACITY - 1);
  }

  /**
   * Returns the maximum number of digits that look like a card number or {@literal 0} if none
   * of the digits look like a card #.
   */
  private int matchingDigits() {
    for (int length = this.length; length >= MIN_LENGTH; length--) {
      int last = wrap(end - 1);
      int base = currentSums[wrap(last - length)];
      int sum = currentSums[last];
      if ((sum - base) % 10 == 0) return length;
    }
    return 0;
  }
}