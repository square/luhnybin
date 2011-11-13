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

public class Masker {
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "US-ASCII"));
    String line;
    while ((line = in.readLine()) != null) System.out.print(mask(line) + "\n");
  }

  static final int MIN_LENGTH = 14;
  static final int MAX_LENGTH = 16;
  static final char MASK = 'X';

  /**
   * Identifies card numbers and replaces the digits with {@literal 'X'}. Strings may contain
   * any character to the left and right of the number (including no character) and may contain
   * any combination of {@literal ' '} or {@literal '-'} within the number itself. Runs O(N).
   */
  public static String mask(String s) {
    if (s.length() < MIN_LENGTH) return s;
    char[] masked = null;
    LuhnBuffer buffer = LuhnBuffer.get();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (digit(c)) {
        buffer.push(c);
        int matchingDigits = buffer.matchingDigits();
        if (matchingDigits > 0) {
          if (masked == null) masked = s.toCharArray();
          mask(masked, i, matchingDigits);
        }
      } else if (!separator(c)) {
        buffer.reset();
      }
    }
    return masked == null ? s : new String(masked);
  }

  /**
   * Replaces count digits with 'X' ending at index last (inclusive). Skips separator characters.
   */
  private static void mask(char[] masked, int last, int count) {
    while (count > 0) {
      char c = masked[last];
      if (digit(c)) {
        masked[last] = MASK;
        count--;
      } else if (c == MASK) {
        // This digit was masked by an overlapping match. Keep going in case the previous
        // match was smaller.
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

  /**
   * A circular buffer that computes Luhn values for sequences of digits in O(1) time.
   *
   * See http://en.wikipedia.org/wiki/Luhn_algorithm
   */
  static class LuhnBuffer {

    int[] currentSums, nextSums;
    int end, length;

    private LuhnBuffer() {
      this.currentSums = new int[32]; // power of 2, at least 1 greater than MAX_LENGTH
      this.nextSums = currentSums.clone();
      reset();
    }

    private static final ThreadLocal<LuhnBuffer> localInstance = new ThreadLocal<LuhnBuffer>() {
      @Override protected LuhnBuffer initialValue() {
        return new LuhnBuffer();
      }
    };

    static LuhnBuffer get() {
      return localInstance.get().reset();
    }

    void push(char digit) {
      // Luhn starts from the right with an unmodified digit.
      int value = digit - '0';
      int doubled = doubleAndSumDigits(value);

      swapArrays();

      accumulate(currentSums, value);
      accumulate(nextSums, doubled);

      end = wrap(end + 1);
      if (length < MAX_LENGTH) length++;
    }

    private static int doubleAndSumDigits(int value) {
      // assert 0 <= value <= 9
      int result = value << 1;
      return result > 9 ? result - 9 : result;
    }

    private void swapArrays() {
      int[] temp = currentSums;
      currentSums = nextSums;
      nextSums = temp;
    }

    private void accumulate(int[] sums, int value) {
      sums[end] = sums[wrap(end - 1)] + value;
    }

    LuhnBuffer reset() {
      end = 1;
      length = currentSums[0] = nextSums[0] = 0;
      return this;
    }

    static int wrap(int index) {
      return index & 31; // & buffer length - 1
    }

    /**
     * Returns the maximum number of digits that look like a card number or {@literal 0} if none
     * of the digits look like a card #.
     */
    int matchingDigits() {
      for (int length = this.length; length >= MIN_LENGTH; length--) {
        int last = wrap(end - 1);
        int base = currentSums[wrap(last - length)];
        int sum = currentSums[last];
        if ((sum - base) % 10 == 0) return length;
      }
      return 0;
    }
  }
}
