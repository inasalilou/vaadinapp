package com.inas.vaadinapp.util;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Singleton utilitaire pour générer des codes uniques.
 */
public final class ReservationCodeGenerator {

    private static final ReservationCodeGenerator INSTANCE = new ReservationCodeGenerator();

    private final SecureRandom random = new SecureRandom();

    private ReservationCodeGenerator() {
    }

    public static ReservationCodeGenerator getInstance() {
        return INSTANCE;
    }

    public String generateUnique(Predicate<String> exists) {
        Objects.requireNonNull(exists, "exists");

        String code;
        do {
            int n = random.nextInt(100000); // 0..99999
            code = String.format("EVT-%05d", n);
        } while (exists.test(code));

        return code;
    }
}
