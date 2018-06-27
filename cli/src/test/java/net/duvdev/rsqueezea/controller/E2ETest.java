/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import net.duvdev.rsqueezea.selftest.RSASelfTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class E2ETest {

  private final SqueezeType squeezeType;
  private final @Nullable RSAPublicKey publicKey;
  private final String format;

  public E2ETest(SqueezeType squeezeType, @Nullable RSAPublicKey publicKey, String format) {
    this.squeezeType = squeezeType;
    this.publicKey = publicKey;
    this.format = format;
  }

  @Parameterized.Parameters(name = "{0} {2}")
  public static Collection<Object[]> data() {
    List<Object[]> data = new ArrayList<>();
    for (CodecFactory.CodecType codecType : CodecFactory.CodecType.values()) {
      data.add(new Object[] {SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, codecType.formatName()});
      data.add(new Object[] {SqueezeType.PRIME_WITH_MODULUS, null, codecType.formatName()});
    }
    return data;
  }

  @Test
  public void endToEnd() throws Exception {
    // squeeze
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SqueezeController squeezeController =
        new SqueezeController(
            () -> TestKey.PRIVATE_KEY_SPEC,
            squeezeType,
            CodecFactory.getCodec(format),
            outputStream);
    squeezeController.run();

    // reassemble
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    ByteArrayOutputStream pkcs1OutputStream = new ByteArrayOutputStream();
    ReassembleController reassembleController =
        new ReassembleController(
            publicKey, inputStream, CodecFactory.getCodec(format), pkcs1OutputStream);
    reassembleController.run();

    // self-test
    RSASelfTest.selfTest(pkcs1OutputStream.toByteArray(), TestKey.PUBLIC_KEY);
  }
}
