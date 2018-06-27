/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.selftest;

import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class RSASelfTest {

  /** Do not instantiate */
  private RSASelfTest() {}

  public static void selfTest(byte[] pkcsPEMPrivateKey, RSAPublicKey publicKey)
      throws RSASelfTestException {
    RSAPrivateCrtKeySpec toTestSpec = loadPrivateKey(pkcsPEMPrivateKey);
    RSAPrivateCrtKey toTest;
    try {
      toTest = (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(toTestSpec);
      testEncrypt(publicKey, toTest);
      testSign(publicKey, toTest);
    } catch (GeneralSecurityException e) {
      throw new RSASelfTestException(e);
    }
  }

  private static RSAPrivateCrtKeySpec loadPrivateKey(byte[] data) throws RSASelfTestException {
    PKCS1PrivateKeyLoader privateKeyLoader =
        new PKCS1PrivateKeyLoader(new ByteArrayInputStream(data));
    try {
      return privateKeyLoader.load();
    } catch (IOException e) {
      throw new RSASelfTestException(e);
    }
  }

  private static void testEncrypt(RSAPublicKey publicKey, RSAPrivateKey privateKey)
      throws GeneralSecurityException {
    String algorithm = "RSA";
    String message = "Test vector";

    // encrypt
    Cipher encryptor = Cipher.getInstance(algorithm);
    encryptor.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] ciphertext = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));

    // decrypt
    Cipher decryptor = Cipher.getInstance(algorithm);
    decryptor.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] plaintext = decryptor.doFinal(ciphertext);

    // check
    String decrypted = new String(plaintext, StandardCharsets.UTF_8);
    if (!message.equals(decrypted)) {
      throw new IllegalArgumentException("Encrypt self-test failed");
    }
  }

  private static void testSign(RSAPublicKey publicKey, RSAPrivateKey privateKey)
      throws GeneralSecurityException {
    String algorithm = "SHA256withRSA";
    String message = "Test vector";
    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

    // sign
    Signature signer = Signature.getInstance(algorithm);
    signer.initSign(privateKey);
    signer.update(messageBytes);
    byte[] signature = signer.sign();

    // verify
    Signature verifier = Signature.getInstance(algorithm);
    verifier.initVerify(publicKey);
    verifier.update(messageBytes);
    boolean valid = verifier.verify(signature);

    if (!valid) {
      throw new IllegalArgumentException("Sign self-test failed");
    }
  }
}
