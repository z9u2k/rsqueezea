/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class SqueezeFormatTest {

  @Test
  public void withModulusDER() throws Exception {
    withModulus(SqueezeFormat.Encoding.DER);
  }

  @Test
  public void withModulusPEM() throws Exception {
    withModulus(SqueezeFormat.Encoding.PEM);
  }

  private void withModulus(SqueezeFormat.Encoding encoding) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SqueezeFormat.write(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_WITH_MODULUS, encoding, outStream);

    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    SqueezedKey result = SqueezeFormat.read(inStream, encoding, null);

    assertNotNull(result);
    assertEquals(TestKey.SQUEEZE_KEY, result);
  }

  @Test
  public void withoutModulusDER() throws Exception {
    withoutModulus(SqueezeFormat.Encoding.DER);
  }

  @Test
  public void withoutModulusPEM() throws Exception {
    withoutModulus(SqueezeFormat.Encoding.PEM);
  }

  private void withoutModulus(SqueezeFormat.Encoding encoding) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SqueezeFormat.write(TestKey.SQUEEZE_KEY, SqueezeType.PRIME_P, encoding, outStream);

    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    RSAPublicKey publicKey =
        (RSAPublicKey)
            KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(TestKey.N, TestKey.E));
    SqueezedKey result = SqueezeFormat.read(inStream, encoding, publicKey);

    assertNotNull(result);
    assertEquals(TestKey.SQUEEZE_KEY, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void withoutModulusNoKeyDER() throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SqueezeFormat.write(
        TestKey.SQUEEZE_KEY, SqueezeType.PRIME_P, SqueezeFormat.Encoding.DER, outStream);

    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    SqueezeFormat.read(inStream, SqueezeFormat.Encoding.DER, null);
  }
}
