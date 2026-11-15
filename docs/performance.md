# Performance Comparison

Benchmark comparing decompilation throughput of toolarium-java-decompiler against
Vineflower, CFR, and Procyon on a real-world library.


## Environment

| Property | Value |
|---|---|
| Java | OpenJDK 21.0.10 (Eclipse Adoptium) |
| OS | Windows 11 (amd64) |
| CPU | 8 cores |
| Methodology | 1 warmup run, 3 measured runs, wall-clock time from process start to exit |
| Settings | All decompilers invoked via CLI JAR with default settings |


## Decompiler Versions

| Decompiler | Version |
|---|---|
| toolarium-java-decompiler | 0.9.0 |
| Vineflower | 1.11.1 |
| CFR | 0.152 |
| Procyon | 0.6.0 |


## Results

### commons-lang3 3.17.0 (396 classes, 658 KB)

| Decompiler | Avg. time | Classes/sec |
|---|---|---|
| **toolarium (parallel)** | **7,105 ms** | **56** |
| **toolarium (sequential)** | **8,525 ms** | **46** |
| Vineflower | 24,290 ms | 16 |
| CFR | 25,216 ms | 16 |
| Procyon | 54,905 ms | 7 |


## Observations

- toolarium is approximately 3.4x faster than Vineflower and CFR, and
  7.7x faster than Procyon.
- Parallel mode provides a ~17% speedup over sequential on this workload.
- Procyon is the slowest decompiler on this benchmark.


## Reproducing

Run the Gradle performance task (downloads competitor JARs automatically):

```bash
cb performance
```

Results are written to `build/performance/results/benchmark.csv`.
Competitor JARs are downloaded to `build/performance/tools/` and cleaned up by `cb clean`.
