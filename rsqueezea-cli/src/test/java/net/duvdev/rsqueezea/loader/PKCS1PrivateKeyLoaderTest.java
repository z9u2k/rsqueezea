/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import junit.framework.TestCase;
import net.duvdev.rsqueezea.TestKey;
import org.junit.Test;

import java.io.InputStream;
import java.security.spec.RSAPrivateCrtKeySpec;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class PKCS1PrivateKeyLoaderTest {
  @Test
  public void testLoad() throws Exception {
    try (InputStream inputStream = getClass().getResourceAsStream("/pkcs1private.pem")) {
      PKCS1PrivateKeyLoader loader = new PKCS1PrivateKeyLoader(inputStream);
      RSAPrivateCrtKeySpec result = loader.load();
      assertNotNull(result);
      TestCase.assertEquals(TestKey.N, result.getModulus());
      assertEquals(TestKey.E, result.getPublicExponent());
      assertEquals(TestKey.P, result.getPrimeP());
    }
  }
}
