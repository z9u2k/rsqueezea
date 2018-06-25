/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import net.duvdev.rsqueezea.misc.BouncyCastleInitializer;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class PKCS1PrivateKeyLoader implements RSAPrivateKeyLoader {

  static {
    BouncyCastleInitializer.initialize();
  }

  private final InputStream inputStream;

  public PKCS1PrivateKeyLoader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public RSAPrivateCrtKeySpec load() throws IOException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
    Object pemObject = pemParser.readObject();
    PrivateKey key;

    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    if (pemObject instanceof PEMKeyPair) {
      key = converter.getPrivateKey(((PEMKeyPair) pemObject).getPrivateKeyInfo());
    } else {
      throw new IOException(pemObject.getClass().getCanonicalName());
    }

    try {
      return KeyFactory.getInstance("RSA").getKeySpec(key, RSAPrivateCrtKeySpec.class);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IOException(e);
    }
  }
}
