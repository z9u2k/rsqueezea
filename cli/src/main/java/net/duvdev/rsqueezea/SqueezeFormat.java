/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DEROutputStream;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public final class SqueezeFormat {

  private static final int TYPE_PRIME_P = 0;
  private static final int TYPE_PRIME_WITH_MODULUS = 1;

  public static void write(SqueezedKey key, SqueezeType type, OutputStream outputStream)
      throws IOException {
    DEROutputStream der = new DEROutputStream(outputStream);
    switch (type) {
      case PRIME_WITH_MODULUS:
        der.writeObject(new ASN1Integer(TYPE_PRIME_WITH_MODULUS));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        der.writeObject(new ASN1Integer(key.getModulus()));
        der.writeObject(new ASN1Integer(key.getPublicExponent()));
        break;
      case PRIME_P:
        der.writeObject(new ASN1Integer(TYPE_PRIME_P));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        break;
      default:
        throw new IllegalArgumentException(type.name());
    }
  }

  public static SqueezedKey read(InputStream inputStream, @Nullable RSAPublicKey publicKey)
      throws IOException {
    ASN1StreamParser parser = new ASN1StreamParser(inputStream);
    ASN1Integer type = (ASN1Integer) parser.readObject();
    BigInteger primeP = ((ASN1Integer) parser.readObject()).getValue();

    BigInteger modulus;
    BigInteger publicExponent;

    int intType = type.getValue().intValueExact();
    if (intType == TYPE_PRIME_WITH_MODULUS) {
      modulus = ((ASN1Integer) parser.readObject()).getValue();
      publicExponent = ((ASN1Integer) parser.readObject()).getValue();
    } else if (intType == TYPE_PRIME_P) {
      if (publicKey == null) {
        // must have public key - no modulus and exponent in squeezed key
        throw new IllegalArgumentException();
      }
      modulus = publicKey.getModulus();
      publicExponent = publicKey.getPublicExponent();
    } else {
      throw new IllegalArgumentException(Integer.toString(intType));
    }

    return new SqueezedKey(primeP, modulus, publicExponent);
  }
}
