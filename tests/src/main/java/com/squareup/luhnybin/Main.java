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

import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Runs the test suite against mask.sh.
 *
 * @author Bob Lee (bob@squareup.com)
 */
public class Main extends TestSuite {

  public static void main(String[] args) throws IOException {
    if (!new File("mask.sh").exists()) {
      System.err.println("Couldn't find 'mask.sh' in the current directory.");
      System.exit(1);
    }

    final int iterations;
    if (args.length > 0) {
      if (args.length > 1) {
        System.err.println("Usage: ./run.sh [iterations]");
        System.exit(1);
      }

      iterations = Integer.parseInt(args[0]);
      if (iterations < 1) {
        System.err.println("Iterations must be >= 1.");
        System.exit(1);
      }
    } else {
      iterations = 1;
    }

    final LuhnyBinTests luhnyBinTests = new LuhnyBinTests();
    final Process process = new ProcessBuilder("sh", "mask.sh").start();

    // Buffer output for maximum efficiency.
    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    luhnyBinTests.writeTo(bout);

    long start = System.nanoTime();

    new Thread() {
      @Override public void run() {
        try {
          OutputStream out = process.getOutputStream();
          for (int i = 0; i < iterations; i++) bout.writeTo(out);
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();

    new Thread() {
      @Override public void run() {
        try {
          ByteStreams.copy(process.getErrorStream(), System.err);
        } catch (IOException e) { /* ignore */ }
      }
    }.start();

    try {
      for (int i = 0; i < iterations; i++) luhnyBinTests.check(process.getInputStream());
      long elapsed = (System.nanoTime() - start) / 1000000;
      System.out.println("Tests passed! (" + elapsed + "ms)");
    } catch (EOFException e) {
      System.err.println("Error: mask.sh didn't send the expected amount of output.");
    } catch (TestFailure testFailure) {
      TestCase test = testFailure.testCase;
      System.err.println("Test #" + test.index + " of " + luhnyBinTests.count + " failed:"
          + "\n  Description:     " + test.description
          + "\n  Input:           " + showBreaks(test.output)
          + "\n  Expected result: " + showBreaks(test.expectedInput)
          + "\n  Actual result:   " + showBreaks(testFailure.actualInput)
          + "\n");
      process.destroy();
      System.exit(1);
    }
  }

  static String showBreaks(String s) {
    return s.replace("\n", "\\n").replace("\r", "\\r");
  }
}
