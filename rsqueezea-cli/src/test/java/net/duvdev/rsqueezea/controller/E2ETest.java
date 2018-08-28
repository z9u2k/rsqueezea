/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nullable;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class E2ETest {

  private final SqueezeType squeezeType;
  private final @Nullable RSAPublicKey publicKey;
  private final CodecFactory.CodecType format;

  public E2ETest(
      SqueezeType squeezeType, @Nullable RSAPublicKey publicKey, CodecFactory.CodecType format) {
    this.squeezeType = squeezeType;
    this.publicKey = publicKey;
    this.format = format;
  }

  @Parameterized.Parameters(name = "{0} {2}")
  public static Collection<Object[]> data() {
    List<Object[]> data = new ArrayList<>();
    for (CodecFactory.CodecType codecType : CodecFactory.CodecType.values()) {
      data.add(new Object[] {SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, codecType});
      data.add(new Object[] {SqueezeType.PRIME_WITH_MODULUS, null, codecType});
      data.add(new Object[] {SqueezeType.PRIME_PQ_WITH_EXPONENT, null, codecType});
    }
    return data;
  }

  @Test
  public void endToEnd() throws Exception {
    EndToEndRunner.doEndToEnd(TestKey.PRIVATE_KEY_SPEC, squeezeType, format, publicKey);
  }
}
