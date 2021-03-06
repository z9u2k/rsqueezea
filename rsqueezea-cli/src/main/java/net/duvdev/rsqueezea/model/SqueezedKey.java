/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.model;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Objects;

public class SqueezedKey {

  private final BigInteger primeP;

  private final @Nullable BigInteger modulus;

  private final @Nullable BigInteger publicExponent;

  public SqueezedKey(BigInteger primeP) {
    this(primeP, null, null);
  }

  public SqueezedKey(
      BigInteger primeP, @Nullable BigInteger modulus, @Nullable BigInteger publicExponent) {
    this.primeP = primeP;
    this.modulus = modulus;
    this.publicExponent = publicExponent;
  }

  public static SqueezedKey fromRSAKey(RSAPrivateCrtKeySpec privateKey) {
    return new SqueezedKey(
        privateKey.getPrimeP(), privateKey.getModulus(), privateKey.getPublicExponent());
  }

  public BigInteger getPrimeP() {
    return primeP;
  }

  public @Nullable BigInteger getModulus() {
    return modulus;
  }

  public @Nullable BigInteger getPublicExponent() {
    return publicExponent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SqueezedKey that = (SqueezedKey) o;
    return Objects.equals(primeP, that.primeP)
        && Objects.equals(modulus, that.modulus)
        && Objects.equals(publicExponent, that.publicExponent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primeP, modulus, publicExponent);
  }
}
