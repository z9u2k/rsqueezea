/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.function.BiFunction;

/** Reassembles a CRT private key from a public key and the prime P factor. */
public final class KeyReassembler {

  /** Euler totient function: phi(p, q) = (p - 1)(q - 1) */
  public static final TotientFunction EULER_TOTIENT_FUNCTION =
      (p, q) -> p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

  /** Carmichael's totient function: lambda(n) = lcm(lambda(p), lambda(q)) = lcm(p - 1, q - 1) */
  public static final TotientFunction CARMICHAEL_TOTIENT_FUNCTION =
      (p, q) -> lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

  /** Do not instantiate */
  private KeyReassembler() {}

  /**
   * Derive an RSA private key from the given RSA public key and the prime factor P using the
   * default totient function (Carmichael).
   *
   * @param publicKey RSA public key to derive private key from
   * @param primeP The P factor of the public modulus
   * @return An RSA private key with the chinese remainder theorem coefficients
   */
  public static RSAPrivateCrtKeySpec reassemble(RSAPublicKey publicKey, BigInteger primeP) {
    return reassemble(publicKey.getModulus(), publicKey.getPublicExponent(), primeP);
  }

  /**
   * Derive an RSA private key from the given RSA public key and the prime factor P using the given
   * totient function.
   *
   * @param publicKey RSA public key to derive private key from
   * @param primeP The P factor of the public modulus
   * @param totientFunction Totient function
   * @return An RSA private key with the chinese remainder theorem coefficients
   */
  public static RSAPrivateCrtKeySpec reassemble(
      RSAPublicKey publicKey, BigInteger primeP, TotientFunction totientFunction) {
    return reassemble(
        publicKey.getModulus(), publicKey.getPublicExponent(), primeP, totientFunction);
  }

  /**
   * Derive an RSA private key from the given RSA public modulus, exponent, and the prime factor P
   * using the default totient function (Carmichael).
   *
   * @param modulus The public modulus (p * q)
   * @param publicExponent The public exponent (e)
   * @param primeP The P factor of the public modulus
   * @return An RSA private key with the chinese remainder theorem coefficients
   */
  public static RSAPrivateCrtKeySpec reassemble(
      BigInteger modulus, BigInteger publicExponent, BigInteger primeP) {
    return reassemble(modulus, publicExponent, primeP, CARMICHAEL_TOTIENT_FUNCTION);
  }

  /**
   * Derive an RSA private key from the given RSA public modulus, exponent, and the prime factor P
   * using the given totient function.
   *
   * @param modulus The public modulus (p * q)
   * @param publicExponent The public exponent (e)
   * @param primeP The P factor of the public modulus
   * @param totientFunction Totient function
   * @return An RSA private key with the chinese remainder theorem coefficients
   */
  public static RSAPrivateCrtKeySpec reassemble(
      BigInteger modulus,
      BigInteger publicExponent,
      BigInteger primeP,
      TotientFunction totientFunction) {
    BigInteger primeQ = modulus.divide(primeP);
    BigInteger totient = totientFunction.apply(primeP, primeQ);
    BigInteger privateExponent = publicExponent.modInverse(totient);
    BigInteger primeExponentP = privateExponent.mod(primeP.subtract(BigInteger.ONE));
    BigInteger primeExponentQ = privateExponent.mod(primeQ.subtract(BigInteger.ONE));
    BigInteger crtCoefficient = primeQ.modInverse(primeP);
    return new RSAPrivateCrtKeySpec(
        modulus,
        publicExponent,
        privateExponent,
        primeP,
        primeQ,
        primeExponentP,
        primeExponentQ,
        crtCoefficient);
  }

  /** @return Least common multiple of a and b */
  private static BigInteger lcm(BigInteger a, BigInteger b) {
    return a.multiply(b).abs().divide(a.gcd(b));
  }

  /** An interface for some totient function */
  public interface TotientFunction extends BiFunction<BigInteger, BigInteger, BigInteger> {}
}
