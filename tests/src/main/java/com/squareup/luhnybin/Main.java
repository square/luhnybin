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

    int iterations = 1;
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
    }

    final LuhnTests luhnTests = new LuhnTests();
    final Process process = new ProcessBuilder("mask.sh").start();

    long start = System.nanoTime();

    new Thread() {
      @Override public void run() {
        try {
          OutputStream out = process.getOutputStream();
          luhnTests.writeTo(out);
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
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();

    try {
      for (int i = 0; i < iterations; i++) luhnTests.check(process.getInputStream());
      // TODO: Check for extraneous output.
      long elapsed = (System.nanoTime() - start) / 1000000;
      System.out.println("Tests passed! (" + elapsed + "ms)");
    } catch (TestFailure testFailure) {
      System.err.println("Test failed: " + testFailure.testCase.description);
      System.err.println("Output:          " + testFailure.testCase.output);
      System.err.println("Expected result: " + testFailure.testCase.expectedInput);
      System.err.println("Actual result:   " + testFailure.actualInput);
    }
  }
}
