/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;

import static junit.framework.TestCase.assertEquals;

public class E2ETest {

  @Test(expected = IllegalArgumentException.class)
  public void withoutModulusNoExternalPublicKey() throws Exception {
    endToEnd(SqueezeType.PRIME_P, null, "DER");
  }

  @Test
  public void withModulusDER() throws Exception {
    endToEnd(SqueezeType.PRIME_WITH_MODULUS, null, "DER");
  }

  @Test
  public void withModulusPEM() throws Exception {
    endToEnd(SqueezeType.PRIME_WITH_MODULUS, null, "PEM");
  }

  @Test
  public void withModulusQR() throws Exception {
    endToEnd(SqueezeType.PRIME_WITH_MODULUS, null, "QR");
  }

  @Test
  public void withoutModulusDER() throws Exception {
    endToEnd(SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, "DER");
  }

  @Test
  public void withoutModulusPEM() throws Exception {
    endToEnd(SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, "DER");
  }

  @Test
  public void withoutModulusQR() throws Exception {
    endToEnd(SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, "QR");
  }

  private void endToEnd(SqueezeType type, @Nullable RSAPublicKey publicKey, String format)
      throws Exception {
    // squeeze
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SqueezeController squeezeController =
        new SqueezeController(
            () -> TestKey.PRIVATE_KEY_SPEC, type, CodecFactory.getCodec(format), outputStream);
    squeezeController.run();

    // reassemble
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    ByteArrayOutputStream pkcs1OutputStream = new ByteArrayOutputStream();
    ReassembleController reassembleController =
        new ReassembleController(
            publicKey, inputStream, CodecFactory.getCodec(format), pkcs1OutputStream);
    reassembleController.run();

    // load
    PKCS1PrivateKeyLoader privateKeyLoader =
        new PKCS1PrivateKeyLoader(new ByteArrayInputStream(pkcs1OutputStream.toByteArray()));
    RSAPrivateCrtKeySpec loadedKey = privateKeyLoader.load();

    // encrypt
    String message = "Hello, world!";
    Cipher encryptor = Cipher.getInstance("RSA");
    encryptor.init(Cipher.ENCRYPT_MODE, TestKey.PUBLIC_KEY);
    byte[] ciphertext = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));

    // decrypt
    Cipher decryptor = Cipher.getInstance("RSA");
    decryptor.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(loadedKey));
    byte[] plaintext = decryptor.doFinal(ciphertext);

    // check
    String decrypted = new String(plaintext, StandardCharsets.UTF_8);
    assertEquals(message, decrypted);
  }
}
