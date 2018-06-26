/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.SqueezeFormat;
import net.duvdev.rsqueezea.SqueezeType;
import net.duvdev.rsqueezea.SqueezedKey;
import net.duvdev.rsqueezea.loader.RSAPrivateKeyLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class SqueezeController {

  private final RSAPrivateKeyLoader privateKeyLoader;

  private final OutputStream outputStream;

  private final SqueezeType squeezeType;

  private final SqueezeFormat.Encoding encoding;

  public SqueezeController(
      RSAPrivateKeyLoader privateKeyLoader,
      OutputStream outputStream,
      SqueezeType squeezeType,
      SqueezeFormat.Encoding encoding) {
    this.privateKeyLoader = privateKeyLoader;
    this.outputStream = outputStream;
    this.squeezeType = squeezeType;
    this.encoding = encoding;
  }

  public void run() throws IOException {
    RSAPrivateCrtKeySpec privateKey = privateKeyLoader.load();
    SqueezeFormat.write(SqueezedKey.fromRSAKey(privateKey), squeezeType, encoding, outputStream);
  }
}
