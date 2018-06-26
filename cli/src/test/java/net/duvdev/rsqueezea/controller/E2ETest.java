/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.SqueezeType;
import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.encoder.EncoderFactory;
import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;
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
  @Test
  public void withModulusDER() throws Exception {
    endToEnd(SqueezeType.PRIME_WITH_MODULUS, null, "DER");
  }

  @Test
  public void withModulusPEM() throws Exception {
    endToEnd(SqueezeType.PRIME_WITH_MODULUS, null, "PEM");
  }

  @Test
  public void withoutModulusDER() throws Exception {
    endToEnd(SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, "DER");
  }

  @Test
  public void withoutModulusPEM() throws Exception {
    endToEnd(SqueezeType.PRIME_P, TestKey.PUBLIC_KEY, "DER");
  }

  private void endToEnd(SqueezeType type, @Nullable RSAPublicKey publicKey, String format)
      throws Exception {
    // squeeze
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SqueezeController squeezeController =
        new SqueezeController(
            () -> TestKey.PRIVATE_KEY_SPEC,
            EncoderFactory.wrapOutputStream(format, type, outputStream),
            type);
    squeezeController.run();

    // reassemble
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    ByteArrayOutputStream pkcs1OutputStream = new ByteArrayOutputStream();
    ReassembleController reassembleController =
        new ReassembleController(
            publicKey, EncoderFactory.wrapInputStream(format, inputStream), pkcs1OutputStream);
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
