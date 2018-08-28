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

final class V0Protocol implements Protocol {

  private static final int TYPE_PRIME_P = 0;
  private static final int TYPE_PRIME_WITH_MODULUS = 1;
  private static final int TYPE_PRIME_PQ_WITH_EXPONENT = 2;

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public byte[] encodeSqueezedKey(SqueezedKey key, SqueezeType type) throws IOException {
    BigInteger modulus, publicExponent;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DEROutputStream der = new DEROutputStream(outputStream);
    der.writeObject(new ASN1Integer(getVersion()));
    switch (type) {
      case PRIME_WITH_MODULUS:
        der.writeObject(new ASN1Integer(TYPE_PRIME_WITH_MODULUS));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        modulus = key.getModulus();
        if (modulus == null) {
          throw new IllegalArgumentException("Squeeze type " + type + " requested without modulus");
        }
        der.writeObject(new ASN1Integer(modulus));

        publicExponent = key.getPublicExponent();
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
      case PRIME_PQ_WITH_EXPONENT:
        der.writeObject(new ASN1Integer(TYPE_PRIME_PQ_WITH_EXPONENT));
        der.writeObject(new ASN1Integer(key.getPrimeP()));
        modulus = key.getModulus();
        if (modulus == null) {
          throw new IllegalArgumentException("Squeeze type " + type + " requested without modulus");
        }
        BigInteger primeQ = modulus.divide(key.getPrimeP());
        der.writeObject(new ASN1Integer(primeQ));
        publicExponent = key.getPublicExponent();
        if (publicExponent == null) {
          throw new IllegalArgumentException(
              "Squeeze type " + type + " requested without public exponent");
        }
        der.writeObject(new ASN1Integer(publicExponent));
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
    ASN1Integer version = (ASN1Integer) parser.readObject();
    if (version.getValue().intValueExact() != getVersion()) {
      throw new IOException("Wrong version. Expected 0 but was " + version.getValue());
    }
    ASN1Integer type = (ASN1Integer) parser.readObject();
    BigInteger primeP;
    BigInteger modulus;
    BigInteger publicExponent;

    int intType = type.getValue().intValueExact();
    switch (intType) {
      case TYPE_PRIME_WITH_MODULUS:
        primeP = ((ASN1Integer) parser.readObject()).getValue();
        modulus = ((ASN1Integer) parser.readObject()).getValue();
        publicExponent = ((ASN1Integer) parser.readObject()).getValue();
        break;
      case TYPE_PRIME_P:
        primeP = ((ASN1Integer) parser.readObject()).getValue();
        modulus = null;
        publicExponent = null;
        break;
      case TYPE_PRIME_PQ_WITH_EXPONENT:
        primeP = ((ASN1Integer) parser.readObject()).getValue();
        BigInteger primeQ = ((ASN1Integer) parser.readObject()).getValue();
        publicExponent = ((ASN1Integer) parser.readObject()).getValue();
        modulus = primeP.multiply(primeQ);
        break;
      default:
        throw new IllegalArgumentException(Integer.toString(intType));
    }

    return new SqueezedKey(primeP, modulus, publicExponent);
  }
}
