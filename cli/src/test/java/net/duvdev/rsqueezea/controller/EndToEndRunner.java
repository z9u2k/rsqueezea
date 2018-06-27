/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import net.duvdev.rsqueezea.selftest.RSASelfTest;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public final class EndToEndRunner {

  public static void doEndToEnd(
      RSAPrivateCrtKeySpec privateKey,
      SqueezeType squeezeType,
      String format,
      @Nullable RSAPublicKey publicKey)
      throws Exception {
    // squeeze
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SqueezeController squeezeController =
        new SqueezeController(
            () -> privateKey, squeezeType, CodecFactory.getCodec(format), outputStream);
    squeezeController.run();

    // reassemble
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    ByteArrayOutputStream pkcs1OutputStream = new ByteArrayOutputStream();
    ReassembleController reassembleController =
        new ReassembleController(
            publicKey, inputStream, CodecFactory.getCodec(format), pkcs1OutputStream);
    reassembleController.run();

    // self-test
    RSAPublicKey selfTestPublic =
        (RSAPublicKey)
            KeyFactory.getInstance("RSA")
                .generatePublic(
                    new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
    RSASelfTest.selfTest(pkcs1OutputStream.toByteArray(), selfTestPublic);
  }
}
