package org.ipoliakov.dmap.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, jvmArgs = {"-XX:MaxDirectMemorySize=4g"})
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ConcurrentMapBenchmark {

    private static final int CAPACITY = 2048;
    private static String[] KEYS = new String[CAPACITY];

    private HashMap<String, String> hashMap;
    private ConcurrentHashMap<String, String> concurrentHashMap;
    private ConcurrentOptimisticMap<String, String> optimisticMap;

    @Setup(Level.Trial)
    public void setUp() {
        hashMap = new HashMap<>(CAPACITY);
        optimisticMap = new ConcurrentOptimisticMap<>(hashMap);
        concurrentHashMap = new ConcurrentHashMap<>(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            hashMap.put(Integer.toString(i), Integer.toString(i));
            concurrentHashMap.put(Integer.toString(i), Integer.toString(i));
        }
    }

    @Benchmark
    public void optimisticMap_get(Blackhole blackhole) {
        mapGet(blackhole, optimisticMap);
    }

    @Benchmark
    public void concurrentHashMap_get(Blackhole blackhole) {
        mapGet(blackhole, concurrentHashMap);
    }

    @Benchmark
    public void hashMap_get(Blackhole blackhole) {
        mapGet(blackhole, hashMap);
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void mapGet(Blackhole blackhole, Map<?, ?> map) {
        for (String key : KEYS) {
            Object value = map.get(key);
            blackhole.consume(value);
        }
    }
}
