/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.protocol;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.model.SqueezedKey;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class V0ProtocolTest {

  @Test
  public void withModulus() throws Exception {
    V0Protocol protocol = new V0Protocol();
    byte[] data = protocol.encodeSqueezedKey(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_WITH_MODULUS);
    SqueezedKey result = protocol.decodeSqueezedKey(data);

    assertNotNull(result);
    assertEquals(TestKey.SQUEEZE_KEY, result);
  }

  @Test
  public void withoutModulus() throws Exception {
    V0Protocol protocol = new V0Protocol();
    byte[] data = protocol.encodeSqueezedKey(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_P);
    SqueezedKey result = protocol.decodeSqueezedKey(data);

    assertNotNull(result);
    assertEquals(new SqueezedKey(TestKey.P), result);
  }
}
