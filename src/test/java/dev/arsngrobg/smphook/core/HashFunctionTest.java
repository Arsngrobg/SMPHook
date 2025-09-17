package dev.arsngrobg.smphook.core;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;

@DisplayName("HashFunction tests")
public interface HashFunctionTest<T> {
    /**
     * <p><b>Idempotence</b>: <i>the property of an operation to produce the same result when applied multiple times as it does when applied once.</i></p>
     * <p>Checks to see if the input {@code arg} produces an exepcted value.</p>
     *
     * @param  arg the input value to test
     * @author     Arsngrobg
     * @since      0.0.0-pre-alpha
     */
    void test_idempotence(T arg);

    /**
     * <p>Validates that the {@ode HashFunction} handles {@code null} arguments, whether that be throwing an error or return a default value.</p>
     *
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    void test_voidSafe();

    /**
     * <p>Validates that the {@code HashFunction} produces expected value upon input.</p>
     *
     * @param  arg the input value to test
     * @author     Arsngrobg
     * @since      0.0.0-pre-alpha
     */
    void test_expected(T arg);

    /**
     * <p>Returns the {@code HashFunction} that this test applies to.</p>
     *
     * @return the {@code HashFunction} that this test applies to
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    HashFunction<T> getHashFunction();

    /**
     * <p>Returns the parameterized values to be used in tests.</p>
     *
     * @return the parameterized values to be used in its tests
     * @author Arsngrobg
     * @since  0.0.0-pre-alpha
     */
    Stream<T> getParameterizedValues();
}
