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
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.security.spec.RSAPrivateCrtKeySpec;

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
    IOUtils.write(encoded, outputStream);
    outputStream.flush();
  }
}
