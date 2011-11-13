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

/**
 * A test failure.
 *
 * @author Bob Lee (bob@squareup.com)
 */
class TestFailure extends Exception {

  final TestCase testCase;
  final String actualInput;

  public TestFailure(TestCase testCase, String actualInput) {
    this.testCase = testCase;
    this.actualInput = actualInput;
  }
}
