/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class SqueezeFormatTest {

  @Test
  public void withModulus() throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SqueezeFormat.write(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_WITH_MODULUS, outStream);

    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    SqueezedKey result = SqueezeFormat.read(inStream);

    assertNotNull(result);
    assertEquals(TestKey.SQUEEZE_KEY, result);
  }

  @Test
  public void withoutModulus() throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SqueezeFormat.write(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_P, outStream);

    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    SqueezedKey result = SqueezeFormat.read(inStream);

    assertNotNull(result);
    assertEquals(new SqueezedKey(TestKey.P), result);
  }
}
