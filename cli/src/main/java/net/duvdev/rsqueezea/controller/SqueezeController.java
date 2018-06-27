/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.codec.Codec;
import net.duvdev.rsqueezea.codec.EncoderException;
import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;
import net.duvdev.rsqueezea.loader.RSAPrivateKeyLoader;
import net.duvdev.rsqueezea.model.SqueezedKey;
import net.duvdev.rsqueezea.protocol.Protocol;
import net.duvdev.rsqueezea.protocol.ProtocolFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public final class SqueezeController {

  private final RSAPrivateKeyLoader privateKeyLoader;

  private final SqueezeType squeezeType;

  private final Codec<byte[], byte[]> codec;

  private final OutputStream outputStream;

  public SqueezeController(
      RSAPrivateKeyLoader privateKeyLoader,
      SqueezeType squeezeType,
      Codec<byte[], byte[]> codec,
      OutputStream outputStream) {
    this.privateKeyLoader = privateKeyLoader;
    this.outputStream = outputStream;
    this.codec = codec;
    this.squeezeType = squeezeType;
  }

  public void run() throws IOException {
    RSAPrivateCrtKeySpec privateKey = privateKeyLoader.load();
    Protocol protocol = ProtocolFactory.getLatest();
    byte[] data = protocol.encodeSqueezedKey(SqueezedKey.fromRSAKey(privateKey), squeezeType);
    byte[] encoded;
    try {
      encoded = codec.encode(data);
    } catch (EncoderException e) {
      throw new IOException(e.getMessage(), e);
    }

    selfTest(encoded, privateKey);

    IOUtils.write(encoded, outputStream);
    outputStream.flush();
  }

  private void selfTest(byte[] encoded, RSAPrivateCrtKeySpec privateKey) throws IOException {
    try {
      RSAPublicKey publicKey =
          (RSAPublicKey)
              KeyFactory.getInstance("RSA")
                  .generatePublic(
                      new RSAPublicKeySpec(
                          privateKey.getModulus(), privateKey.getPublicExponent()));
      byte[] toTestBytes = reassembleKey(encoded, publicKey);
      RSAPrivateCrtKeySpec toTestSpec = loadPrivateKey(toTestBytes);
      RSAPrivateCrtKey toTest =
          (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(toTestSpec);

      testEncrypt(publicKey, toTest);
      testSign(publicKey, toTest);
    } catch (IOException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | BadPaddingException
        | IllegalBlockSizeException
        | InvalidKeyException
        | SignatureException e) {
      throw new IOException("Self-test failed", e);
    }
  }

  private byte[] reassembleKey(byte[] encoded, RSAPublicKey publicKey) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ReassembleController controller =
        new ReassembleController(publicKey, new ByteArrayInputStream(encoded), codec, output);
    controller.run();
    return output.toByteArray();
  }

  private RSAPrivateCrtKeySpec loadPrivateKey(byte[] data) throws IOException {
    PKCS1PrivateKeyLoader privateKeyLoader =
        new PKCS1PrivateKeyLoader(new ByteArrayInputStream(data));
    return privateKeyLoader.load();
  }

  private void testEncrypt(RSAPublicKey publicKey, RSAPrivateKey privateKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
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

  private void testSign(RSAPublicKey publicKey, RSAPrivateKey privateKey)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
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
