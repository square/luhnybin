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

import org.junit.Assert;
import org.junit.Test;

public class LastDigitTest {

  @Test public void computeLastDigit() {
    Assert.assertEquals('1', LuhnyBinTests.computeLast("411111111111111"));
    Assert.assertEquals('2', LuhnyBinTests.computeLast("422222222222"));
    Assert.assertEquals('1', LuhnyBinTests.computeLast("37144963539843"));
    Assert.assertEquals('0', LuhnyBinTests.computeLast("561059108101825"));
    Assert.assertEquals('5', LuhnyBinTests.computeLast("356600202036050"));
    Assert.assertEquals('4', LuhnyBinTests.computeLast("555555555555444"));
  }
}
