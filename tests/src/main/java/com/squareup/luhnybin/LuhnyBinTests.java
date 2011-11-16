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
package com.squareup.luhnybin;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * Luhny Bin test cases.
 *
 * @author Bob Lee (bob@squareup.com)
 */
public class LuhnyBinTests extends TestSuite {

  private static final char MASK = 'X';

  private static final int MIN_LENGTH = 14;
  private static final int MAX_LENGTH = 16;

  private static final Random random = new Random(0xDEADBEEF);

  LuhnyBinTests() {
    test("line feed preservation").sendAndExpect("LF only ->\n<- LF only");

    for (int i = MIN_LENGTH; i <= MAX_LENGTH; i++) {
      test("valid " + i + "-digit #")
          .send(randomNumber(i))
          .expect(mask(i));
    }

    for (int i = MIN_LENGTH; i <= MAX_LENGTH; i++) {
      test("non-matching " + i + "-digit #").sendAndExpect(nonMatchingSequence(i));
    }

    test("not enough digits").sendAndExpect(nonMatchingSequence(MIN_LENGTH - 1));

    String tooMany = nonMatchingSequence(MAX_LENGTH);
    tooMany += computeLast(tooMany);
    test("too many digits").sendAndExpect(tooMany);

    test("14-digit # prefixed with 0s")
        .send("00" + randomNumber(14))
        .expect(mask(16));

    test("14-digit # embedded in a 16-digit #")
        .send(nestedNumber())
        .expect(mask(16));

    // 1111111111111abc

    // 15:
    //  111111111111abc
    //  121212121212aBc
    //  (18 + a + B + c) % 10 = 0

    // 16:
    // 1111111111111abc
    // 2121212121212aBc
    // (20 + a + B + c) % 10 = 0

    // (18 + a + B + c) % 10 = (20 + a + B + c) % 10

    // a: 0-9
    // B: 0-9
    // c: 0-9

    test("16-digit # flanked by non-matching digits")
        .send("9875610591081018250321")
        .expect("987XXXXXXXXXXXXXXXX321");

    testFormatted(" ");
    testFormatted("-");
    testFormatted("  ");
    testFormatted("- ");

    testFormatted(" ", 2);
    testFormatted(" ", 3);
    
    test("exception message containing a card #")
        .send("java.lang.FakeException: " + formattedNumber(" ") + " is a card #.")
        .expect("java.lang.FakeException: " + formattedMask(" ") + " is a card #.");

    test("non-matching message").sendAndExpect("4111 1111 1111 111 doesn't have enough digits.");

    test("non-matching message").sendAndExpect("56613959932535089 has too many digits.");

    test("sequence of zeros")
        .send(repeatingSequence('0', 1000))
        .expect(mask(1000));

    testOverlappingMatches();

    test("long sequence of digits with no matches").sendAndExpect(nonMatchingSequence(1000));
    
    for (int i=0; i<100; i++) {
      test("realistic log message #1").sendAndExpect("[Sun Sep 25 17:27:22 2011] [notice] Digest: generating secret for digest authentication ...");
      test("realistic log message #2").sendAndExpect("[Sun Sep 25 17:27:22 2011] [notice] Digest: done");
      test("realistic log message #3").sendAndExpect("[Sun Sep 25 17:27:22 2011] [notice] Apache/2.2.17 (Unix) mod_ssl/2.2.17 OpenSSL/0.9.8r DAV/2 Phusion_Passenger/3.0.8 configured -- resuming normal operations");
      test("realistic log message #4").sendAndExpect("[Sat Oct 08 12:11:21 2011] [warn] (2)No such file or directory: Cannot reinit SSLMutex");
    }
  }

  private void testFormatted(String delimeter) {
    test("16-digit # delimited with '" + delimeter + "'")
        .send(formattedNumber(delimeter))
        .expect(formattedMask(delimeter));
  }

  private void testFormatted(String delimeter, int subLength) {
	    test("16-digit # delimited with '" + delimeter + "' with segment lengths of " + subLength)
        .send(formattedNumber(delimeter, subLength))
        .expect(formattedMask(delimeter, subLength));
  }
  
  private static String formattedNumber(String delimeter) {
    return formatNumber(randomNumber(16), delimeter);
  }

  private static String formattedNumber(String delimeter, int subLength) {
	return formatNumber(randomNumber(16), delimeter, subLength);
  }

  static String formatNumber(String number, String delimeter) {
	return formatNumber(number, delimeter, 4);
  }
  
  static String formatNumber(String number, String delimeter, int subLength) {
    if (number.length() != 16) throw new IllegalArgumentException("Expected length of 16.");
    StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < 16/subLength; i++) {
      formatted.append(number.substring(i * subLength, (i * subLength) + subLength));
      if (i < (16/subLength) - 1) formatted.append(delimeter);
    }
    return formatted.toString();
  }

  private static String formattedMask(String delimeter) {
	  return formattedMask(delimeter, 4);
  }

  private static String formattedMask(String delimeter, int subLength) {
    StringBuilder mask = new StringBuilder();
    for (int i = 0; i < 16/subLength; i++) {
      mask.append(repeatingSequence(MASK, subLength));
      if (i < (16/subLength) - 1) mask.append(delimeter);
    }
    return mask.toString();
  }

  /** Generates a sequence of digits with the specified length and no card #s. */
  private String nonMatchingSequence(int length) {
    StringBuilder builder = new StringBuilder();
    DigitSet excluded = new DigitSet();
    for (int lastIndex = 0; lastIndex < length; lastIndex++) {
      excluded.clear();

      // Compute digits that would result in valid card #s.
      for (int subLength = MIN_LENGTH; subLength <= MAX_LENGTH; subLength++) {
        int start = lastIndex - (subLength - 1);
        if (start < 0) break;
        excluded.add(computeLast(builder.subSequence(start, lastIndex)));
      }

      // Find a digit that doesn't result in a valid card #.
      char digit;
      do {
        digit = randomDigit();
      } while (excluded.contains(digit));
      builder.append(digit);
    }

    return builder.toString();
  }

  private void testOverlappingMatches() {
    StringBuilder output = new StringBuilder(randomNumber(MAX_LENGTH));
    for (int i = 0; i < 1000 - MAX_LENGTH; i++) {
      output.append(computeLast(output.subSequence(i + 1, i + MAX_LENGTH)));
    }

    test("long sequence of overlapping, valid #s")
        .send(output.toString())
        .expect(mask(output.length()));
  }

  /** Creates a 16-digit card # with a 14-digit number embedded inside. */
  private static String nestedNumber() {
    StringBuilder number = new StringBuilder(16);
    number.setLength(16);
    setRandomDigits(number, 0, 14);
    number.setCharAt(14, computeLast(number.subSequence(1, 14)));
    number.setCharAt(15, computeLast(number.subSequence(0, 15)));
    return number.toString();
  }

  /** Computes a random, valid card # with the specified number of digits. */
  private static String randomNumber(int digits) {
    StringBuilder number = new StringBuilder(digits);
    number.setLength(digits);
    setRandomDigits(number, 0, digits - 1);
    number.setCharAt(digits - 1, computeLast(number.subSequence(0, digits - 1)));
    return number.toString();
  }

  /** Creates a sequence of mask characters with the given length. */
  private static String mask(int length) {
    return repeatingSequence(MASK, length);
  }

  /** Creates a sequence of c with the given length. */
  private static String repeatingSequence(char c, int length) {
    char[] mask = new char[length];
    Arrays.fill(mask, c);
    return new String(mask);
  }

  private static void setRandomDigits(StringBuilder builder, int start, int end) {
    for (int i = start; i < end; i++) builder.setCharAt(i, randomDigit());
  }

  /** Generates a random digit. */
  private static char randomDigit() {
    return (char) ('0' + random.nextInt(10));
  }

  /** Computes the last digit necessary to pass the Luhn check. */
  static char computeLast(CharSequence allButLast) {
    int sum = 0;
    for (int i = allButLast.length() - 1; i >= 0; i -= 2) {
      int value = (checkDigit(allButLast.charAt(i)) - '0') << 1;
      sum += value > 9 ? value - 9 : value;
    }
    for (int i = allButLast.length() - 2; i >= 0; i -= 2) {
      sum += checkDigit(allButLast.charAt(i)) - '0';
    }
    int remainder = sum % 10;
    return remainder == 0 ? '0' : (char) ((10 - remainder) + '0');
  }

  private static char checkDigit(char c) {
    if (c < '0' || c > '9') throw new IllegalArgumentException("Not a digit: " + c);
    return c;
  }

  private static int intValue(char c) {
    checkDigit(c);
    return c - '0';
  }

  static class DigitSet {
    private final BitSet bitSet = new BitSet();

    void add(char digit) {
      bitSet.set(intValue(digit));
    }

    boolean contains(char digit) {
      return bitSet.get(intValue(digit));
    }

    void clear() {
      bitSet.clear();
    }
  }
}
