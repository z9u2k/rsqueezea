/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

public final class SqueezeFormat {

  private static final int TYPE_PRIME_P = 0;
  private static final int TYPE_PRIME_WITH_MODULUS = 1;

  public static void write(
      SqueezedKey key, SqueezeType type, Encoding encoding, OutputStream outputStream)
      throws IOException {
    switch (encoding) {
      case DER:
        writeDER(key, type, outputStream);
        break;
      case PEM:
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        writeDER(key, type, byteStream);
        PemObject pemObject =
            new PemObject(
                "SQUEEZED RSA PRIVATE KEY",
                Arrays.asList(
                    new PemHeader("Has-Modulus", type == SqueezeType.PRIME_P ? "0" : "1")),
                byteStream.toByteArray());
        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(outputStream));
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        break;
      default:
        throw new IllegalArgumentException(encoding.name());
    }
  }

  private static void writeDER(SqueezedKey key, SqueezeType type, OutputStream outputStream)
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

  public static SqueezedKey read(
      InputStream inputStream, Encoding encoding, @Nullable RSAPublicKey publicKey)
      throws IOException {
    switch (encoding) {
      case DER:
        return readDER(inputStream, publicKey);
      case PEM:
        PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
        PemObject pemObject = pemParser.readPemObject();
        return readDER(new ByteArrayInputStream(pemObject.getContent()), publicKey);
      default:
        throw new IllegalArgumentException(encoding.name());
    }
  }

  private static SqueezedKey readDER(InputStream inputStream, @Nullable RSAPublicKey publicKey)
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
        throw new IllegalArgumentException(
            "Squeezed key doesn't contain public modulus - but no external modulus provided!");
      }
      modulus = publicKey.getModulus();
      publicExponent = publicKey.getPublicExponent();
    } else {
      throw new IllegalArgumentException(Integer.toString(intType));
    }

    return new SqueezedKey(primeP, modulus, publicExponent);
  }

  public enum Encoding {
    DER,
    PEM;
  }
}
