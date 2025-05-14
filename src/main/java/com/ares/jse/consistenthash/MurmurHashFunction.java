package com.ares.jse.consistenthash;

public class MurmurHashFunction implements HashFunction {

  private final int seed;

  public MurmurHashFunction() {
    this(0);
  }

  public MurmurHashFunction(int seed) {
    this.seed = seed;
  }

  @Override
  public int hash(String key) {
    byte[] data = key.getBytes();
    return hash(data, 0, data.length, seed);
  }

  private int hash(byte[] data, int offset, int length, int seed) {
    int h1 = seed;

    int i;
    for (i = 0; i + 4 <= length; i += 4) {
      int k1 = (data[offset + i] & 0xFF) | ((data[offset + i + 1] & 0xFF) << 8) |
          ((data[offset + i + 2] & 0xFF) << 16) | ((data[offset + i + 3] & 0xFF) << 24);

      k1 *= 0xcc9e2d51;
      k1 = Integer.rotateLeft(k1, 15);
      k1 *= 0x1b873593;

      h1 ^= k1;
      h1 = Integer.rotateLeft(h1, 13);
      h1 = h1 * 5 + 0xe6546b64;
    }

    int k1 = 0;
    switch (length - i) {
      case 3:
        k1 ^= (data[offset + i + 2] & 0xFF) << 16;
      case 2:
        k1 ^= (data[offset + i + 1] & 0xFF) << 8;
      case 1:
        k1 ^= (data[offset + i] & 0xFF);
        k1 *= 0xcc9e2d51;
        k1 = Integer.rotateLeft(k1, 15);
        k1 *= 0x1b873593;
        h1 ^= k1;
    }

    h1 ^= length;

    h1 ^= h1 >>> 16;
    h1 *= 0x85ebca6b;
    h1 ^= h1 >>> 13;
    h1 *= 0xc2b2ae35;
    h1 ^= h1 >>> 16;

    return Math.abs(h1);
  }
}
