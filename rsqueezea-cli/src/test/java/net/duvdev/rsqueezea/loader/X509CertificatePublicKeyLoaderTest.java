/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import junit.framework.TestCase;
import net.duvdev.rsqueezea.TestKey;
import org.junit.Test;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class X509CertificatePublicKeyLoaderTest {
  @Test
  public void testLoad() throws Exception {
    try (InputStream inputStream = getClass().getResourceAsStream("/x509.crt.pem")) {
      X509CertificatePublicKeyLoader loader = new X509CertificatePublicKeyLoader(inputStream);
      RSAPublicKey result = loader.load();
      assertNotNull(result);
      TestCase.assertEquals(TestKey.N, result.getModulus());
      assertEquals(TestKey.E, result.getPublicExponent());
    }
  }
}
