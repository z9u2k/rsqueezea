/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class StressTest {
  private final String privateKeyFile;
  private final SqueezeType squeezeType;
  private final boolean reassembleWithPublicKey;
  private final CodecFactory.CodecType format;

  public StressTest(
      String privateKeyFile,
      SqueezeType squeezeType,
      boolean reassembleWithPublicKey,
      CodecFactory.CodecType format) {
    this.privateKeyFile = privateKeyFile;
    this.squeezeType = squeezeType;
    this.reassembleWithPublicKey = reassembleWithPublicKey;
    this.format = format;
  }

  @Parameterized.Parameters(name = "{0}: {1} {3}")
  public static Collection<Object[]> data() {
    List<Object[]> data = new ArrayList<>();
    for (int i = 1; i <= 11; ++i) {
      for (CodecFactory.CodecType codecType : CodecFactory.CodecType.values()) {
        String keyFile = "/stress/" + i + ".key";
        data.add(new Object[] {keyFile, SqueezeType.PRIME_P, true, codecType});
        data.add(new Object[] {keyFile, SqueezeType.PRIME_WITH_MODULUS, false, codecType});
      }
    }
    return data;
  }

  @Test
  public void stress() throws Exception {
    PKCS1PrivateKeyLoader loader =
        new PKCS1PrivateKeyLoader(getClass().getResourceAsStream(privateKeyFile));
    RSAPrivateCrtKeySpec privateKeySpec = loader.load();
    RSAPublicKey publicKey =
        (RSAPublicKey)
            KeyFactory.getInstance("RSA")
                .generatePublic(
                    new RSAPublicKeySpec(
                        privateKeySpec.getModulus(), privateKeySpec.getPublicExponent()));
    EndToEndRunner.doEndToEnd(
        privateKeySpec, squeezeType, format, reassembleWithPublicKey ? publicKey : null);
  }
}
