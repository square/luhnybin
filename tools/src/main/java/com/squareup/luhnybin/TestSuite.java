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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test suite.
 *
 * @author Bob Lee (bob@squareup.com)
 */
class TestSuite {

  private TestCase.Builder builder;
  private List<TestCase> testCases = new ArrayList<TestCase>();
  int count = 0;

  Output test(String description) {
    if (builder != null) {
      if (builder.output == null) {
        throw new IllegalStateException("Missing output and input for previous test.");
      }
      throw new IllegalStateException("Missing expected input for previous test.");
    }
    builder = new TestCase.Builder(description, ++count);
    return new Output();
  }

  void writeTo(OutputStream out) throws IOException {
    for (TestCase testCase : testCases) testCase.writeTo(out);
  }

  void check(InputStream in, TestCase.Listener listener) throws IOException {
    for (TestCase testCase : testCases) testCase.check(in, listener);
  }

  class Output {
    Input send(String output) {
      if (builder == null) throw new AssertionError();
      if (builder.output != null) {
        throw new IllegalStateException("Output already specified.");
      }
      builder.output = output;
      return new Input();
    }

    void sendAndExpect(String s) {
      send(s).expect(s);
    }

    Input send(CharSequence output) {
      return send(output.toString());
    }
  }

  class Input {
    void expect(String input) {
      if (builder == null) throw new AssertionError();
      if (builder.expectedInput != null) {
        throw new IllegalStateException("Input already specified.");
      }
      builder.expectedInput = input;
      testCases.add(builder.build());
      builder = null;
    }

    void expect(CharSequence input) {
      expect(input.toString());
    }
  }
}
