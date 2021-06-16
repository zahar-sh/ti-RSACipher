package Long;

public class RSA {
    private static final int CIPHER_BYTES_COUNT = 4; // 1...8

    public static StreamCoder createEncoder(long e, long r) {
        if (r < 0xFFL)
            throw new RuntimeException("R very small");
        return (in, out) -> {
            int plain;
            while ((plain = in.read()) != -1) {
                long encoded = fastExp(plain & 0xFFL, e, r);
                for (long i = CIPHER_BYTES_COUNT - 1; i > 0; i--)
                    out.write((int) (encoded >>> (i << 3L)));
                out.write((int) (encoded & 0xFFL));
            }
        };
    }
    public static StreamCoder createDecoder(long d, long r) {
        if (r < 0xFFL)
            throw new RuntimeException("R very small");
        return (in, out) -> {
            byte[] buf = new byte[CIPHER_BYTES_COUNT];
            while (in.read(buf, 0, buf.length) != -1) {
                long encoded = 0;
                for (byte b : buf)
                    encoded = (encoded << 8) | (b & 0xFF);
                long decoded = fastExp(encoded, d, r);
                out.write((byte) (decoded & 0xFFL));
            }
        };
    }
    public static StreamCoder[] createCoders(long p, long q, long d) {
        if (isNotPrime(p)) throw new ArithmeticException("p should be prime");
        if (isNotPrime(q)) throw new ArithmeticException("q should be prime");

        long f = (p - 1) * (q - 1);
        long e = modInverse(d, f);
        long r = p * q;

        return new StreamCoder[]{createEncoder(e, r), createDecoder(d, r)};
    }

    public static boolean isNotPrime(long n) {
        if (n < 2)
            return true;
        if (n == 2)
            return false;
        for (long i = 2; i * i <= n; i++)
            if (n % i == 0)
                return true;
        return false;
    }
    public static long gcd(long a, long b) {
        long na;
        while (a > 0) {
            na = b % a;
            b = a;
            a = na;
        }
        return b;
    }
    public static long fastExp(long a, long z, long n) {
        long a1 = a;
        long z1 = z;
        long x = 1;
        while (z1 != 0) {
            while ((z1 & 1) == 0) { //z1 % 2 == 0
                z1 = z1 >>> 1; // z1 = z1 / 2
                a1 = (a1 * a1) % n;
            }
            z1 = z1 - 1;
            x = (x * a1) % n;
        }
        return x;
    }
    public static long[] advancedEuclideanAlgorithm(long a, long b) {
        long d0 = a;
        long d1 = b;
        long x0 = 1;
        long x1 = 0;
        long y0 = 0;
        long y1 = 1;


        while (d1 > 1) {
            long q = d0 / d1;
            long d2 = d0 % d1;
            long x2 = x0 - (q * x1);
            long y2 = y0 - (q * y1);
            d0 = d1;
            d1 = d2;
            x0 = x1;
            x1 = x2;
            y0 = y1;
            y1 = y2;
        }
        return new long[]{x1, y1, d1};
    }
    public static long modInverse(long d, long f) {
        if (d < 2)
            throw new ArithmeticException("d should be > 1");
        if (d >= f)
            throw new ArithmeticException("d should be less then f(r)=" + f);
        if (gcd(d, f) != 1)
            throw new ArithmeticException("d should be mutually simple with f(r)=" + f);

        long[] coefs = advancedEuclideanAlgorithm(f, d);
        long y1 = coefs[1];
        while (y1 < 0)
            y1 = y1 + f;
        return y1;
    }
}