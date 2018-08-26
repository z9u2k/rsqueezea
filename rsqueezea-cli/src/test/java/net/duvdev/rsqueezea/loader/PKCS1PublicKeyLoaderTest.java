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

public class PKCS1PublicKeyLoaderTest {
  @Test
  public void testLoad() throws Exception {
    try (InputStream inputStream = getClass().getResourceAsStream("/pkcs1public.pem")) {
      PKCS1PublicKeyLoader loader = new PKCS1PublicKeyLoader(inputStream);
      RSAPublicKey result = loader.load();
      assertNotNull(result);
      TestCase.assertEquals(TestKey.N, result.getModulus());
      assertEquals(TestKey.E, result.getPublicExponent());
    }
  }
}
