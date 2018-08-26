/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.function.BiFunction;

import static junit.framework.TestCase.assertEquals;

public class KeyReassemblerTest {

  private static final BigInteger N =
      new BigInteger(
          "C0A5FC54E72BE64289CF785D647CCD96FF3A1864BA00FA9A60D073BE446FF0FE"
              + "65DA1D7F0E019166454ABFA6FBD5A7D2F03A0E349FF52EB3D6A9F4B9FDB9D188"
              + "1706A21DB3B184BA9F10C16113F951B24055561925297E51C31A6ED82937974A"
              + "51A98108C6103F6ABCF766F3E4B96C1415D4CC1C3AB598547A5CC6AD22C081EB"
              + "FDCFB4D85F907056DC2564098045AFEC962D54EA599C40F686906E844D47F5B6"
              + "042CD0352FD858ED145422FD9CE9E816E3B27169A603899964F4BF363C551E24"
              + "36984E4746E42E5A3D1F2FD2303F57CE689C3B0EB6E64F83F622B3D8340DB175"
              + "C860BB29D44A1BE1B75562518010B60374D818991653F54B633A749CDA35CAD1",
          16);
  private static final BigInteger P =
      new BigInteger(
          "E0E4AC66C7F018E682953A241055F3E71F0FF451A08031E60567B8150C63EF24"
              + "36DD020E9BE792FE5745BCE5C850FE190529CE49D3BE71A26E869EB287F59F4B"
              + "FAF069A433EA158A950F2A829687810D2778C85937BA206C9A7A4FCF10408496"
              + "760252DC40C3ADC396B6EE094E5CB66F87ADF8F5EFF747E1F664498BC3E7A65B",
          16);
  private static final BigInteger E = new BigInteger("65537");
  private static RSAPublicKey publicKey;

  private static RSAPublicKey generatedPublicKey;
  private static BigInteger generatedPrimeP;

  @BeforeClass
  public static void setUp() throws Exception {
    KeyFactory kf = KeyFactory.getInstance("RSA");

    // fixed public key
    publicKey = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(N, E));

    // generated key pair
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(1024);
    KeyPair kp = kpg.generateKeyPair();
    generatedPublicKey = (RSAPublicKey) kp.getPublic();
    RSAPrivateCrtKeySpec privateSpec = kf.getKeySpec(kp.getPrivate(), RSAPrivateCrtKeySpec.class);
    generatedPrimeP = privateSpec.getPrimeP();
  }

  @Test
  public void eulerFixed() {
    testRSA(publicKey, P, KeyReassembler.EULER_TOTIENT_FUNCTION, this::decrypt);
  }

  @Test
  public void eulerGenerated() {
    testRSA(
        generatedPublicKey, generatedPrimeP, KeyReassembler.EULER_TOTIENT_FUNCTION, this::decrypt);
  }

  @Test
  public void eulerCrtFixed() {
    testRSA(publicKey, P, KeyReassembler.EULER_TOTIENT_FUNCTION, this::decryptCrt);
  }

  @Test
  public void eulerCrtGenerated() {
    testRSA(
        generatedPublicKey,
        generatedPrimeP,
        KeyReassembler.EULER_TOTIENT_FUNCTION,
        this::decryptCrt);
  }

  @Test
  public void carmichaelFixed() {
    testRSA(publicKey, P, KeyReassembler.CARMICHAEL_TOTIENT_FUNCTION, this::decrypt);
  }

  @Test
  public void carmichaelGenerated() {
    testRSA(
        generatedPublicKey,
        generatedPrimeP,
        KeyReassembler.CARMICHAEL_TOTIENT_FUNCTION,
        this::decrypt);
  }

  @Test
  public void carmichaelCrtFixed() {
    testRSA(publicKey, P, KeyReassembler.CARMICHAEL_TOTIENT_FUNCTION, this::decryptCrt);
  }

  @Test
  public void carmichaelCrtGenerated() {
    testRSA(
        generatedPublicKey,
        generatedPrimeP,
        KeyReassembler.CARMICHAEL_TOTIENT_FUNCTION,
        this::decryptCrt);
  }

  private BigInteger randomMessage(BigInteger modulus) {
    SecureRandom secureRandom = new SecureRandom();
    int bits = modulus.bitLength() / 2;
    return new BigInteger(bits, secureRandom);
  }

  private BigInteger encrypt(BigInteger m, RSAPublicKey key) {
    return m.modPow(key.getPublicExponent(), key.getModulus());
  }

  private BigInteger decrypt(BigInteger ciphertext, RSAPrivateCrtKeySpec key) {
    return ciphertext.modPow(key.getPrivateExponent(), key.getModulus());
  }

  private BigInteger decryptCrt(BigInteger ciphertext, RSAPrivateCrtKeySpec key) {
    BigInteger m1 = ciphertext.modPow(key.getPrimeExponentP(), key.getPrimeP());
    BigInteger m2 = ciphertext.modPow(key.getPrimeExponentQ(), key.getPrimeQ());
    BigInteger h = key.getCrtCoefficient().multiply(m1.subtract(m2)).mod(key.getPrimeP());
    return m2.add(h.multiply(key.getPrimeQ()));
  }

  public void testRSA(
      RSAPublicKey publicKey,
      BigInteger primeP,
      KeyReassembler.TotientFunction totientFunction,
      BiFunction<BigInteger, RSAPrivateCrtKeySpec, BigInteger> decryptor) {
    BigInteger message = randomMessage(publicKey.getModulus());
    BigInteger ciphertext = encrypt(message, publicKey);

    RSAPrivateCrtKeySpec key =
        KeyReassembler.reassemble(
            publicKey.getModulus(), publicKey.getPublicExponent(), primeP, totientFunction);

    BigInteger plaintext = decryptor.apply(ciphertext, key);

    assertEquals(message, plaintext);
  }
}
