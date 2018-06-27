/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.protocol;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.model.SqueezedKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(Parameterized.class)
public class V0ProtocolTest {

  private final SqueezeType squeezeType;
  private final SqueezedKey expected;

  public V0ProtocolTest(SqueezeType squeezeType, SqueezedKey expected) {
    this.squeezeType = squeezeType;
    this.expected = expected;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[] {SqueezeType.PRIME_WITH_MODULUS, TestKey.SQUEEZE_KEY},
        new Object[] {SqueezeType.PRIME_P, new SqueezedKey(TestKey.P)});
  }

  @Test
  public void testProtocolRoundtrip() throws Exception {
    V0Protocol protocol = new V0Protocol();
    byte[] data = protocol.encodeSqueezedKey(TestKey.SQUEEZE_KEY, squeezeType);
    SqueezedKey actual = protocol.decodeSqueezedKey(data);

    assertNotNull(actual);
    assertEquals(expected, actual);
  }
}
