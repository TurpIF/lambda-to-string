package fr.pturpin.lambdastring;

import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Measure the time difference between getting the {@link Object#toString()} without any injection or injecting a
 * strategy recomputing the {@link Object#toString()}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
public class OriginalToStringInjectionComparisonBenchmark {

    @Param({ "true", "false" })
    public boolean isInjected;

    @Setup
    public void setup() {
        if (isInjected) {
            LambdaAgentLoader.loadAgent(OriginalToStringStrategy.class.getName());
        }
    }

    @State(Scope.Benchmark)
    public static class Data {

        final Lambda lambda;
        final Lambda methodRef;

        public Data() {
            this.lambda = () -> {};
            this.methodRef = LambdaTestHolder::body;
        }
    }

    @Benchmark
    public void staticLambda(Data data, Blackhole bh) {
        bh.consume(data.lambda.toString());
    }

    @Benchmark
    public void staticMethodRef(Data data, Blackhole bh) {
        bh.consume(data.methodRef.toString());
    }

    @Benchmark
    public void dynamicLambda(Blackhole bh) {
        Lambda lambda = () -> {};
        bh.consume(lambda.toString());
    }

    @Benchmark
    public void dynamicMethodRef(Blackhole bh) {
        Lambda lambda = LambdaTestHolder::body;
        bh.consume(lambda.toString());
    }


    @SuppressWarnings("unused")
    private static final class OriginalToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return lambda.getClass().getName() + "@" + Integer.toHexString(lambda.hashCode());
        }
    }

}
