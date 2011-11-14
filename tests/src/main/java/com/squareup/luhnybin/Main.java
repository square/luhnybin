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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
      }
    });

    final LuhnyBinTests luhnyBinTests = new LuhnyBinTests();
    final Process process = new ProcessBuilder("sh", "mask.sh").start();

    // Copy error stream from child process.
    executor.execute(new Runnable() {
      public void run() {
        try {
          ByteStreams.copy(process.getErrorStream(), System.err);
        } catch (IOException e) { /* ignore */ }
      }
    });

    // Buffer output for maximum efficiency.
    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    luhnyBinTests.writeTo(bout);

    final OutputStream out = process.getOutputStream();
    final InputStream in = process.getInputStream();
    long start = System.nanoTime();
    try {
      // Time/iteration in ms.
      long[] times = new long[iterations];

      for (int i = 0; i < iterations; i++) {
        long iterationStart = System.nanoTime();

        // Write in the background. Writing can block if the buffer fills up.
        executor.execute(new Runnable() {
          public void run() {
            try {
              bout.writeTo(out);
              out.flush();
            } catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
            }
          }
        });
        
        luhnyBinTests.check(in);
        times[i] = (System.nanoTime() - iterationStart) / 1000000;
      }

      out.close();

      long elapsed = (System.nanoTime() - start) / 1000000;
      System.out.println();
      System.out.println("Tests passed!");
      System.out.printf("Total time:   %dms%n", elapsed);

      if (iterations > 1) {
        long sum = 0;
        for (long time : times) sum += time;
        System.out.printf("Mean time:    %dms%n", sum / times.length);
        System.out.printf("Median time:  %dms%n", times[times.length / 2]);
        System.out.printf("Fastest time: %dms%n", times[0]);
        Arrays.sort(times);
      }

      System.exit(0);
    } catch (EOFException e) {
      System.err.println("Error: mask.sh didn't send the expected amount of output.");
      System.exit(1);
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
