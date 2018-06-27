/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.loader.RSAPrivateKeyLoader;
import net.duvdev.rsqueezea.model.SqueezedKey;
import net.duvdev.rsqueezea.protocol.Protocol;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import net.duvdev.rsqueezea.protocol.V0Protocol;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class SqueezeController {

  private final RSAPrivateKeyLoader privateKeyLoader;

  private final OutputStream outputStream;

  private final SqueezeType squeezeType;

  public SqueezeController(
      RSAPrivateKeyLoader privateKeyLoader, OutputStream outputStream, SqueezeType squeezeType) {
    this.privateKeyLoader = privateKeyLoader;
    this.outputStream = outputStream;
    this.squeezeType = squeezeType;
  }

  public void run() throws IOException {
    RSAPrivateCrtKeySpec privateKey = privateKeyLoader.load();
    Protocol protocol = new V0Protocol();
    byte[] data = protocol.encodeSqueezedKey(SqueezedKey.fromRSAKey(privateKey), squeezeType);
    IOUtils.write(data, outputStream);
    outputStream.flush();
  }
}
