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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A test case.
 *
 * @author Bob Lee (bob@squareup.com)
 */
class TestCase {

  private static final Charset ASCII = Charset.forName("US-ASCII");

  final String description;
  final int index;
  final String output;
  final String expectedInput;

  private final byte[] outputBytes;
  private final byte[] expectedInputBytes;
  private final byte[] buffer;

  TestCase(String description, int index, String output, String expectedInput) {
    this.description = description;
    this.index = index;
    this.output = withBreak(output);
    this.outputBytes = this.output.getBytes(ASCII);
    this.expectedInput = withBreak(expectedInput);
    this.expectedInputBytes = this.expectedInput.getBytes(ASCII);
    this.buffer = new byte[expectedInputBytes.length];
  }

  private static String withBreak(String s) {
    return s + "\n";
  }

  void writeTo(OutputStream out) throws IOException {
    out.write(outputBytes);
  }

  void check(InputStream in, Listener listener) throws IOException {
    int read = 0;
    while (read < buffer.length) {
      int result = in.read(buffer, read, buffer.length - read);
      if (result == -1) throw new EOFException();
      read += result;
    }

    if (Arrays.equals(expectedInputBytes, buffer)) {
      listener.testPassed(this);
    } else {
      listener.testFailed(this, new String(buffer, ASCII));
    }
  }

  static class Builder {

    final String description;
    String output;
    String expectedInput;
    final int index;

    public Builder(String description, int index) {
      this.description = description;
      this.index = index;
    }

    TestCase build() {
      if (description == null || output == null || expectedInput == null) {
        throw new AssertionError();
      }
      return new TestCase(description, index, output, expectedInput);
    }
  }

  interface Listener {
    void testPassed(TestCase test);
    void testFailed(TestCase test, String actualInput);
  }
}
