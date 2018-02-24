package fr.pturpin.lambdaString;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LambdaToStringLinker {

    private static final Map<String, LambdaToStringStrategy> LINKED_STRATEGY = new ConcurrentHashMap<>();

    /**
     * Method injected in lambda as a <code>toString</code>
     *
     * @param strategyClassName Class name of {@link LambdaToStringStrategy} to use
     * @param lambda            The lambda object itself
     * @param metaInfo          Meta information about the lambda
     * @return <code>toString</code> value of the lambda
     * @throws LambdaToStringException if the an error occurs while generating the toString
     */
    @SuppressWarnings("unused")
    public static String lambdaToString(String strategyClassName, Object lambda, LambdaMetaInfo metaInfo) throws
            BootstrapMethodError,
            LambdaToStringException {
        return linkStrategy(strategyClassName).createToString(lambda, metaInfo);
    }

    static LambdaToStringStrategy linkStrategy(String strategyClassName) throws BootstrapMethodError {
        return LINKED_STRATEGY.computeIfAbsent(strategyClassName, name -> {
            try {
                return createStrategy(name);
            } catch (ReflectiveOperationException e) {
                throw new BootstrapMethodError(e);
            }
        });
    }

    private static LambdaToStringStrategy createStrategy(String strategyClassName) throws
            ReflectiveOperationException {
        ClassLoader classLoader = LambdaToStringLinker.class.getClassLoader();

        Class<?> klass = classLoader.loadClass(strategyClassName);
        if (!LambdaToStringStrategy.class.isAssignableFrom(klass)) {
            throw new ReflectiveOperationException(LambdaToStringStrategy.class + " is not assignable from given class " + klass);
        }
        @SuppressWarnings("unchecked") Class<LambdaToStringStrategy> castKlass = (Class<LambdaToStringStrategy>) klass;

        Constructor<LambdaToStringStrategy> constructor = castKlass.getDeclaredConstructor();
        try {
            constructor.setAccessible(true);
        } catch (SecurityException e) {
            throw new ReflectiveOperationException("No accessible constructor in " + strategyClassName + " class", e);
        }
        return constructor.newInstance();
    }

}