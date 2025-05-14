package com.ares.jse.consistenthash;

/**
 * Implementation of FNV-1a (Fowler-Noll-Vo) hash function This is a simple but effective
 * non-cryptographic hash function
 */
public class FNVHashFunction implements HashFunction {

  private static final int FNV_32_PRIME = 0x01000193;
  private static final int FNV_32_INIT = 0x811c9dc5;

  @Override
  public int hash(String key) {
    int hash = FNV_32_INIT;
    for (int i = 0; i < key.length(); i++) {
      hash ^= key.charAt(i);
      hash *= FNV_32_PRIME;
    }

    // Make sure the result is positive
    return Math.abs(hash);
  }
}
