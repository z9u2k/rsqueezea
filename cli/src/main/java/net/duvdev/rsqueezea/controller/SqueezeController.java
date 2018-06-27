/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.codec.Codec;
import net.duvdev.rsqueezea.codec.EncoderException;
import net.duvdev.rsqueezea.loader.RSAPrivateKeyLoader;
import net.duvdev.rsqueezea.model.SqueezedKey;
import net.duvdev.rsqueezea.protocol.Protocol;
import net.duvdev.rsqueezea.protocol.ProtocolFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import net.duvdev.rsqueezea.selftest.RSASelfTest;
import net.duvdev.rsqueezea.selftest.RSASelfTestException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public final class SqueezeController {

  private final RSAPrivateKeyLoader privateKeyLoader;

  private final SqueezeType squeezeType;

  private final Codec<byte[], byte[]> codec;

  private final OutputStream outputStream;

  public SqueezeController(
      RSAPrivateKeyLoader privateKeyLoader,
      SqueezeType squeezeType,
      Codec<byte[], byte[]> codec,
      OutputStream outputStream) {
    this.privateKeyLoader = privateKeyLoader;
    this.outputStream = outputStream;
    this.codec = codec;
    this.squeezeType = squeezeType;
  }

  public void run() throws IOException {
    RSAPrivateCrtKeySpec privateKey = privateKeyLoader.load();
    Protocol protocol = ProtocolFactory.getLatest();
    byte[] data = protocol.encodeSqueezedKey(SqueezedKey.fromRSAKey(privateKey), squeezeType);
    byte[] encoded;
    try {
      encoded = codec.encode(data);
    } catch (EncoderException e) {
      throw new IOException(e.getMessage(), e);
    }

    selfTest(encoded, privateKey);

    IOUtils.write(encoded, outputStream);
    outputStream.flush();
  }

  private void selfTest(byte[] encoded, RSAPrivateCrtKeySpec privateKey) throws IOException {
    try {
      RSAPublicKey publicKey =
          (RSAPublicKey)
              KeyFactory.getInstance("RSA")
                  .generatePublic(
                      new RSAPublicKeySpec(
                          privateKey.getModulus(), privateKey.getPublicExponent()));
      byte[] toTestBytes = reassembleKey(encoded, publicKey);
      RSASelfTest.selfTest(toTestBytes, publicKey);
    } catch (IOException | GeneralSecurityException | RSASelfTestException e) {
      throw new IOException("Self-test failed", e);
    }
  }

  private byte[] reassembleKey(byte[] encoded, RSAPublicKey publicKey) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ReassembleController controller =
        new ReassembleController(publicKey, new ByteArrayInputStream(encoded), codec, output);
    controller.run();
    return output.toByteArray();
  }
}
