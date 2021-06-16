package BigInt;

import java.math.BigInteger;

public class RSA {
    private static final int CIPHER_BYTES_COUNT = 3;

    public static StreamCoder createEncoder(BigInteger e, BigInteger r) {
        if (r.compareTo(BigInteger.valueOf(0xFF)) < 0)
            throw new RuntimeException("R very small");
        return (in, out) -> {
            int b;
            while ((b = in.read()) != -1) {
                byte[] ret = fastExp(BigInteger.valueOf(b & 0xFFL), e, r).toByteArray();
                if (ret.length > CIPHER_BYTES_COUNT)
                    throw new RuntimeException();
                for (int k = ret.length; k < CIPHER_BYTES_COUNT; k++)
                    out.write(0);
                out.write(ret);
            }
        };
    }
    public static StreamCoder createDecoder(BigInteger d, BigInteger r) {
        if (r.compareTo(BigInteger.valueOf(0xFF)) < 0)
            throw new RuntimeException("R very small");
        return (in, out) -> {
            int l;
            byte[] buf = new byte[CIPHER_BYTES_COUNT];
            while ((l = in.read(buf, 0, buf.length)) != -1) {
                while (l < buf.length)
                    buf[l++] = 0;
                byte[] ret = fastExp(new BigInteger(buf, 0, buf.length), d, r).toByteArray();
                out.write(ret[0] == 0 && ret.length > 1 ? ret[1] : ret[0]);
            }
        };
    }
    public static StreamCoder[] createCoders(BigInteger p, BigInteger q, BigInteger d) {
        if (isNotPrime(p)) throw new ArithmeticException("p should be prime");
        if (isNotPrime(q)) throw new ArithmeticException("q should be prime");

        BigInteger f = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = modInverse(d, f);
        BigInteger r = p.multiply(q);

        return new StreamCoder[]{createEncoder(e, r), createDecoder(d, r)};
    }

    public static boolean isNotPrime(BigInteger n) {
        BigInteger prev = n.subtract(BigInteger.ONE);
        return !(prev.signum() > 0 && prev.nextProbablePrime().compareTo(n) == 0);
    }
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }
    public static BigInteger fastExp(BigInteger a, BigInteger z, BigInteger n) {
        BigInteger a1 = a;
        BigInteger z1 = z;
        BigInteger x = BigInteger.ONE;
        while (z1.signum() != 0) {
            while (z1.mod(BigInteger.TWO).signum() == 0) {
                z1 = z1.shiftRight(1);
                a1 = a1.multiply(a1).mod(n);
            }
            z1 = z1.subtract(BigInteger.ONE);
            x = x.multiply(a1).mod(n);
        }
        return x;
    }
    public static BigInteger[] advancedEuclideanAlgorithm(BigInteger a, BigInteger b) {
        BigInteger d0 = a;
        BigInteger d1 = b;
        BigInteger x0 = BigInteger.ONE;
        BigInteger x1 = BigInteger.ZERO;
        BigInteger y0 = BigInteger.ZERO;
        BigInteger y1 = BigInteger.ONE;


        while (d1.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = d0.divide(d1);
            BigInteger d2 = d0.mod(d1);
            BigInteger x2 = x0.subtract(q.multiply(x1));
            BigInteger y2 = y0.subtract(q.multiply(y1));
            d0 = d1;
            d1 = d2;
            x0 = x1;
            x1 = x2;
            y0 = y1;
            y1 = y2;
        }
        return new BigInteger[]{x1, y1, d1};
    }
    public static BigInteger modInverse(BigInteger d, BigInteger f) {
        if (!(d.compareTo(BigInteger.ONE) > 0))
            throw new ArithmeticException("d should be > 1");
        if (!(d.compareTo(f) < 0))
            throw new ArithmeticException("d should be < f(r)");
        if (gcd(d, f).compareTo(BigInteger.ONE) != 0)
            throw new ArithmeticException("gcd(d, f) not equal 1");

        BigInteger[] coefs = advancedEuclideanAlgorithm(f, d);
        BigInteger y1 = coefs[1];
        while (y1.signum() < 0)
            y1 = y1.add(f);
        return y1;
    }
}