/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import net.duvdev.rsqueezea.misc.BouncyCastleInitializer;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.interfaces.RSAPublicKey;

public final class PKCS1PublicKeyLoader implements RSAPublicKeyLoader {

  static {
    BouncyCastleInitializer.initialize();
  }

  private final InputStream inputStream;

  public PKCS1PublicKeyLoader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public RSAPublicKey load() throws IOException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
    Object object = pemParser.readObject();

    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    return (RSAPublicKey) converter.getPublicKey((SubjectPublicKeyInfo) object);
  }
}
