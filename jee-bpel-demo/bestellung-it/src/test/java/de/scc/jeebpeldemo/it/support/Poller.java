package de.scc.jeebpeldemo.it.support;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Poller {

    private Poller() {
    }

    public static <T> T waitUntil(Supplier<T> check, Predicate<T> condition, Duration timeout, Duration interval) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        T last = null;
        while (System.currentTimeMillis() < deadline) {
            last = check.get();
            if (condition.test(last)) {
                return last;
            }
            Thread.sleep(interval.toMillis());
        }
        throw new AssertionError("Bedingung nicht erfuellt innerhalb von " + timeout + ", letzter Wert: " + last);
    }
}
