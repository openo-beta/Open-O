# Maven Build Optimization Configuration

This directory contains Maven configuration files that optimize build performance for developer machines.

## Memory Settings (Adaptive)

**Current Settings (16GB+ machines):**
- Maven heap: 4GB max, 1GB initial
- Compiler processes: 2GB max, 256MB initial
- Parallel threads: 8

**For machines with different RAM:**

### 8GB machines:
```bash
# Edit maven.config and jvm.config:
-Xmx2g  (instead of -Xmx4g)
-Xms512m  (instead of -Xms1g)
-T 4  (instead of -T 8)
```

### 32GB+ machines:
```bash
# Edit maven.config and jvm.config:
-Xmx8g  (instead of -Xmx4g)
-Xms2g  (instead of -Xms1g)
-T 16  (instead of -T 8)
```

## Performance Features

- **G1GC**: Low-latency garbage collector
- **String Deduplication**: Reduces memory usage
- **Tiered Compilation**: Fast startup compilation
- **Forked Compilation**: Separate JVM processes
- **Parallel Builds**: Uses multiple CPU cores

## Quick Commands

```bash
# Fast development build
mvn compile -DskipTests

# Full optimized clean build
mvn clean compile

# Time your build
time mvn clean compile -DskipTests
```