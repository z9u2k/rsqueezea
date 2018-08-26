/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;

public final class X509CertificatePublicKeyLoader implements RSAPublicKeyLoader {

  private final InputStream inputStream;

  public X509CertificatePublicKeyLoader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public RSAPublicKey load() throws IOException {
    try {
      return (RSAPublicKey)
          CertificateFactory.getInstance("X.509").generateCertificate(inputStream).getPublicKey();
    } catch (CertificateException e) {
      throw new IOException(e);
    }
  }
}
