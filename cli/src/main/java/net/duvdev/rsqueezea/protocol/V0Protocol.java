/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.protocol;

import net.duvdev.rsqueezea.model.SqueezedKey;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DEROutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public final class V0Protocol implements Protocol {

  private static final int TYPE_PRIME_P = 0;
  private static final int TYPE_PRIME_WITH_MODULUS = 1;

  @Override
  public byte[] encodeSqueezedKey(SqueezedKey key, SqueezeType type) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DEROutputStream der = new DEROutputStream(outputStream);
    switch (type) {
      case PRIME_WITH_MODULUS:
        der.writeObject(new ASN1Integer(TYPE_PRIME_WITH_MODULUS));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        BigInteger modulus = key.getModulus();
        if (modulus == null) {
          throw new IllegalArgumentException("Squeeze type " + type + " requested without modulus");
        }
        der.writeObject(new ASN1Integer(modulus));

        BigInteger publicExponent = key.getPublicExponent();
        if (publicExponent == null) {
          throw new IllegalArgumentException(
              "Squeeze type " + type + " requested without public exponent");
        }
        der.writeObject(new ASN1Integer(publicExponent));
        break;
      case PRIME_P:
        der.writeObject(new ASN1Integer(TYPE_PRIME_P));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        break;
      default:
        throw new IllegalArgumentException(type.name());
    }
    der.flush();
    return outputStream.toByteArray();
  }

  @Override
  public SqueezedKey decodeSqueezedKey(byte[] data) throws IOException {
    ASN1StreamParser parser = new ASN1StreamParser(new ByteArrayInputStream(data));
    ASN1Integer type = (ASN1Integer) parser.readObject();
    BigInteger primeP = ((ASN1Integer) parser.readObject()).getValue();

    BigInteger modulus;
    BigInteger publicExponent;

    int intType = type.getValue().intValueExact();
    if (intType == TYPE_PRIME_WITH_MODULUS) {
      modulus = ((ASN1Integer) parser.readObject()).getValue();
      publicExponent = ((ASN1Integer) parser.readObject()).getValue();
    } else if (intType == TYPE_PRIME_P) {
      modulus = null;
      publicExponent = null;
    } else {
      throw new IllegalArgumentException(Integer.toString(intType));
    }

    return new SqueezedKey(primeP, modulus, publicExponent);
  }
}
