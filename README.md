# RSqueezeA

[![Build Status](https://travis-ci.com/z9u2k/rsqueezea.svg?branch=master)](https://travis-ci.com/z9u2k/rsqueezea) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A tool for squeezing RSA private keys for backup purposes.

PKCS RSA private keys contain precalculated values that can be derived from
other values found in the key. This is good to speed up decryption, but a pain
when you want to backup on paper.

Choosing an RSA private key format is a decision in a TMTO. PKCS index on time.
We index on memory.

This tool strips down all redundancies from the key, and re-calculates them
later on.

# Quick Start

```
$ gradle clean cli:jar
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar --help
```

## Squeeze a Key

Input key MUST be PKCS#1, PEM-formatted, unencrypted RSA private key:

```
$ head -n 1 private.pem
-----BEGIN RSA PRIVATE KEY-----
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar squeeze \
  -i private.pem -o with_modulus.der
$ ls -l
-rw-r--r--  1 me  1337  1679 Jun 22 10:51 private.pem
-rw-r--r--  1 me  1338   401 Jun 22 10:51 with_modulus.der
-rw-r--r--  1 me  1339   135 Jun 22 10:51 without_modulus.der
```

Note that we're reduced the private key size from 1,679 bytes to just 401
without losing information (~75% reduction).

We can get additional ~75% reduction on top of that, from 401 to 135, if we use
the `-x` flag, **but only do this if you're certain you will have access to the
public key when you need to restore from backup as we are discarding the
publicly available information!**

### Textual Output

If you want a textual output at the cost of larger size, the tool can also
output PEM encoded structures (`-f PEM`):

With modulus:
```
-----BEGIN SQUEEZED RSA PRIVATE KEY-----
Has-Modulus: 1

AgEBAoGBAODkrGbH8BjmgpU6JBBV8+cfD/RRoIAx5gVnuBUMY+8kNt0CDpvnkv5X
RbzlyFD+GQUpzknTvnGiboaesof1n0v68GmkM+oVipUPKoKWh4ENJ3jIWTe6IGya
ek/PEECElnYCUtxAw63DlrbuCU5ctm+Hrfj17/dH4fZkSYvD56ZbAoIBAQDApfxU
5yvmQonPeF1kfM2W/zoYZLoA+ppg0HO+RG/w/mXaHX8OAZFmRUq/pvvVp9LwOg40
n/Uus9ap9Ln9udGIFwaiHbOxhLqfEMFhE/lRskBVVhklKX5Rwxpu2Ck3l0pRqYEI
xhA/arz3ZvPkuWwUFdTMHDq1mFR6XMatIsCB6/3PtNhfkHBW3CVkCYBFr+yWLVTq
WZxA9oaQboRNR/W2BCzQNS/YWO0UVCL9nOnoFuOycWmmA4mZZPS/NjxVHiQ2mE5H
RuQuWj0fL9IwP1fOaJw7DrbmT4P2IrPYNA2xdchguynUShvht1ViUYAQtgN02BiZ
FlP1S2M6dJzaNcrRAgMBAAE=
-----END SQUEEZED RSA PRIVATE KEY-----
```

Without modulus:
```
-----BEGIN SQUEEZED RSA PRIVATE KEY-----
Has-Modulus: 0

AgEAAoGBAODkrGbH8BjmgpU6JBBV8+cfD/RRoIAx5gVnuBUMY+8kNt0CDpvnkv5X
RbzlyFD+GQUpzknTvnGiboaesof1n0v68GmkM+oVipUPKoKWh4ENJ3jIWTe6IGya
ek/PEECElnYCUtxAw63DlrbuCU5ctm+Hrfj17/dH4fZkSYvD56Zb
-----END SQUEEZED RSA PRIVATE KEY-----
```

### QR Code

The tool can also read and write QR code in PNG format (`-f QR`):

With modulus:

![With modulus](https://raw.githubusercontent.com/z9u2k/rsqueezea/master/example/with_modulus.png)

Without modulus:

![Without modulus](https://raw.githubusercontent.com/z9u2k/rsqueezea/master/example/without_modulus.png)

## Reassemble a Key

Reassemble a key with modulus:

```
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar reassemble \
  -i with_modulus.rsa -o private.pem
```

Reassemble a key without modulus, taking modulus from X.509 certificate:

```
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar reassemble \
  -i without_modulus.rsa -o private.pem -c cert.pem
```

Reassemble a key without modulus, taking modulus from PKCS#1 public key file:

```
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar reassemble \
  -i without_modulus.rsa -o private.pem -p public.pem
```

Reassemble a key without modulus, taking modulus and exponent directly:

```
$ java -jar cli/build/libs/cli-1.0.0-SNAPSHOT.jar reassemble \
  -i without_modulus.rsa -o private.pem -m C0A5FC54E... -e 10001
```

(modulus and exponent are given in hexadecimal base)

# Motivation

Consider the following 2048-bit RSA private key:
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpQIBAAKCAQEAwKX8VOcr5kKJz3hdZHzNlv86GGS6APqaYNBzvkRv8P5l2h1/
DgGRZkVKv6b71afS8DoONJ/1LrPWqfS5/bnRiBcGoh2zsYS6nxDBYRP5UbJAVVYZ
JSl+UcMabtgpN5dKUamBCMYQP2q892bz5LlsFBXUzBw6tZhUelzGrSLAgev9z7TY
X5BwVtwlZAmARa/sli1U6lmcQPaGkG6ETUf1tgQs0DUv2FjtFFQi/Zzp6BbjsnFp
pgOJmWT0vzY8VR4kNphOR0bkLlo9Hy/SMD9XzmicOw625k+D9iKz2DQNsXXIYLsp
1Eob4bdVYlGAELYDdNgYmRZT9UtjOnSc2jXK0QIDAQABAoIBAAiwc+0wcB52qdid
yTibGHrTED/Ba1JX+1aakF3ooFPyZY2s/uoW0AQY5AI4+ertIuqK89ET1e9BVVFd
JHZ5RyYoQ7hUNqKvJeu+ybojOH+i1pwCwieW84ekkTCmt1U2kbeVOai3pkv1+IgA
MMgERQey5GJAc7V1JXpPbPaqNpylgEg9V53F0Q6JC1Gb3pCtVzXEx0yuPBVzoMcX
sVzoMfJsAkzunovoZzpJK6yKqTDaUQTMV/9WwbAIgD7c4i/uSaBV0tlebPgZBQ5/
e+W/fMxuldtgTTWbGXxUqO/G14fyHIJ8IC03CiTcPylyvJJEu8YrEtdR5lmrRNdJ
BM1bpHECgYEA4OSsZsfwGOaClTokEFXz5x8P9FGggDHmBWe4FQxj7yQ23QIOm+eS
/ldFvOXIUP4ZBSnOSdO+caJuhp6yh/WfS/rwaaQz6hWKlQ8qgpaHgQ0neMhZN7og
bJp6T88QQISWdgJS3EDDrcOWtu4JTly2b4et+PXv90fh9mRJi8PnplsCgYEA20uK
tTg9BaeYGn/gkofRgkBh3/NZx4+b6pRoGwXdiwT9Dee0px7v7G1YtUYMOE5+kr7n
/eZ4RiR/7rOkheINbrItVUqgzeJAfM12YYHeaq5BSWI9ljeWv2sPgyzW9R4uQ8SL
aCJQyp5wo4DvVlYB02pR37CYiLWV5Srg7TNdk0MCgYEAvrxTF6zVDllaQPQZqB0u
CkRHBMDCLlejrcvkzT0/+I+vVEwtVb7W5Y3hIK+F8GNBlyZ4xham+7t2oAgyhKsm
GovOoNpaCVuRuJAvTqgabrJYWtEZEfFzFIkD2XJVZ1LMRXP9EL6A93vd9HH4RJTP
SdI6E9+KUSCPHai606YobucCgYEAgo5JyTPvGHO7mWMyZupXL+12l6bAd4+m6pRq
GlR2nfJdWa7tnWVMv3wmCN3oHomUz3a6lS1lw5StWYY318FJ7/JCDPo+G/SsIeEM
rmZr7SVLFw5WzhzQMavic1z5qLrMHmpf+KIdaVPEiYMUelkAA0bT8ZGobhN1ZxcN
DAq9lhMCgYEAxrBYtTJBmCp05s76jRmnQz0IcQLSKBh7iE9NX6cTW8z5CvT/d0zx
4N1tO2VkzOtMzQQEgkBoGPwNlaOdN8JC5n4bmddSlmb9CXnYHgMrDRxJB4WG445n
M0yWhZ5/7nXKvKf4MfKXUblyJtTqb49OcibREmTATcD17ohedJYJ/fo=
-----END RSA PRIVATE KEY-----
```

Different encodings will yield different sizes:

| Format     | Size  |
| ---------- | ----- |
| PKCS#1 PEM | 1,679 |
| PKCS#1 DER | 1,193 |
| PKCS#8 PEM | 1,708 |
| PKCS#8 DER | 1,219 |

For offline (i.e., paper) backup purposes - these sizes are enormous. Available
on-paper digital formats are either very tedious to type in, or suffer from
sensitivity to media degradation (stains, tears, fade).

The less data we have to back up, the more we're likely to successfully recover
it.

But if the key is only 2048-bit long (256 bytes), why do we need to backup more
than 4 times that?

Observe the PKCS#1 structure for the private key (RFC 8017, A.1.2):

```
RSAPrivateKey ::= SEQUENCE {
   version           Version,
   modulus           INTEGER,  -- n
   publicExponent    INTEGER,  -- e
   privateExponent   INTEGER,  -- d
   prime1            INTEGER,  -- p
   prime2            INTEGER,  -- q
   exponent1         INTEGER,  -- d mod (p-1)
   exponent2         INTEGER,  -- d mod (q-1)
   coefficient       INTEGER,  -- (inverse of q) mod p
   otherPrimeInfos   OtherPrimeInfos OPTIONAL
}
```

For performance reasons, the key is kept with the _chinese remainder theorem_
exponents and coefficients, in additional to other values for convenience.

But we don't need those. As a matter of fact - all the values can be calculated
given `e` and any two of `n`, `p`, and `q`.

This tool will strip down all the calculated values from the key, and will
produce a file with the bare-minimum needed to reconstruct it later.

# File Format

In some cases, the modulus and exponent may not be kept with the backup, as
they are available publicly in an X.509 certificate or a key escrow service.
Therefore, there are two _types_ of "squeezed" RSA private key: with and
without the modulus.

```
Type ::= INTEGER { prime-p(0), prime-with-modulus(1) }
```

Both formats are defined as ASN.1 structures, and are encoded by the tool using
DER encoding by default (to save space).

For future compatibility, we add a `version` field to each structure as the
first field, to allow non-backward-compatible changes.

```
Version ::= INTEGER
```

## Prime without Modulus

```
RSQueezeAKeyWithoutModulusV0 ::= SEQUENCE {
  version          Version,
  type             Type,
  prime1           INTEGER  -- p
}
```

* `version` denotes the structure version. It _SHALL_ be `0` for this structure
* `type` denotes the key type. It _SHALL_ be `0` for this structure
* `prime1` is the prime factor `p` of `n`

## Prime with Modulus

```
RSQueezeAKeyWithModulusV0 ::= SEQUENCE {
  version          Version,
  type             Type,
  prime1           INTEGER,  -- p
  modulus          INTEGER,  -- n
  publicExponent   INTEGER   -- e
}
```

* `version` denotes the structure version. It _SHALL_ be `0` for this structure
* `type` denotes the key type. It _SHALL_ be `1` for this structure
* `prime1` is the prime factor `p` of `n`
* `modulus` is the RSA modulus `n`
* `publicExponent` is the RSA public exponent `e`

# QR Code Format

The generated QR code contains the DER structure encoded in Base64, to avoid character-set decoding problems. This is
less efficient, but more portable and has higher chance of recovery.

See `BinaryToQRCodeStringCodec` for implementation details.

# Command-line Reference

```
Usage: <main class> [options] [command] [command options]
  Options:
    -h, --help
      This help message
    -v, --verbose
      Be verbose
      Default: false
  Commands:
    squeeze      Squeeze an RSA private key
      Usage: squeeze [options]
        Options:
          -f, --format
            Output format
            Default: DER
            Possible Values: [DER, PEM, QR]
          -i, --input
            PKCS#1 PEM RSA private key file. Use "-" for STDIN
            Default: -
          -x, --no-modulus
            Don't write public modulus an exponent to output file. Results in 
            a smaller file, but reassembly will need the public key from 
            external source
            Default: false
          -o, --output
            File to write squeezed key to. Use "-" for STDOUT
            Default: -
          --qr-level
            QR code error correction level
            Default: M
            Possible Values: [L, M, Q, H]

    reassemble      Reassemble an RSA private key from a squeezed key
      Usage: reassemble [options]
        Options:
          -c, --crt
            Path to X.509 certificate to get public key from
          -e, --exponent
            Public exponent (hex), if not found in squeezed key
          -f, --format
            Input format
            Default: DER
            Possible Values: [DER, PEM, QR]
          -i, --input
            PKCS#1 PEM RSA private key file. Use "-" for STDIN
            Default: -
          -n, --modulus
            Public modulus (hex), if not found in squeezed key
          -o, --output
            File to write squeezed key to. Use "-" for STDOUT
            Default: -
          -p, --private
            Path to PKCS#1 PEM file to get public key from
```
