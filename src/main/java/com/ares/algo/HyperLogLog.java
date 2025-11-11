package com.ares.algo;

import java.util.Arrays;

public class HyperLogLog {
  private final int m;           // number of registers
  private final int[] registers; // registers
  private final double alphaMM;  // alpha * m^2

  public HyperLogLog(int b) {
    if (b < 4 || b > 16) {
      throw new IllegalArgumentException("Precision must be between 4 and 16");
    }
    this.m = 1 << b;
    this.registers = new int[m];
    this.alphaMM = getAlphaMM(m);
  }

  private double getAlphaMM(int m) {
    // Empirical constants
    return switch (m) {
      case 16 -> 0.673 * m * m;
      case 32 -> 0.697 * m * m;
      case 64 -> 0.709 * m * m;
      default -> (0.7213 / (1 + 1.079 / m)) * m * m;
    };
  }

  private int hash(Object o) {
    // Simple hash: you may want to use MurmurHash or similar in production
    return o.hashCode();
  }

  public void add(Object o) {
    int x = hash(o);
    int idx = x >>> (Integer.SIZE - Integer.numberOfTrailingZeros(m));
    int w = x << Integer.numberOfTrailingZeros(m) | 1;
    int leadingZeros = Integer.numberOfLeadingZeros(w) + 1;
    registers[idx] = Math.max(registers[idx], leadingZeros);
  }

  public double estimate() {
    double sum = 0.0;
    for (int reg : registers) {
      sum += 1.0 / (1 << reg);
    }

    double rawEstimate = alphaMM / sum;

    // Small range correction
    if (rawEstimate <= 2.5 * m) {
      int zeros = 0;
      for (int reg : registers) {
        if (reg == 0)
          zeros++;
      }

      if (zeros != 0) {
        rawEstimate = m * Math.log((double) m / zeros);
      }
    }
    // Large range correction omitted for brevity
    return rawEstimate;
  }

  public void merge(HyperLogLog other) {
    if (other.m != this.m) {
      throw new IllegalArgumentException("Incompatible HyperLogLog instance");
    }
    for (int i = 0; i < m; i++) {
      this.registers[i] = Math.max(this.registers[i], other.registers[i]);
    }
  }

  public void reset() {
    Arrays.fill(registers, 0);
  }

}
