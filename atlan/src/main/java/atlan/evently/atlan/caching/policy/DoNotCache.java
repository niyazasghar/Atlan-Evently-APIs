// src/main/java/atlan/evently/atlan/caching/policy/DoNotCache.java
package atlan.evently.atlan.caching.policy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares that the annotated type or method must not be cached.
 * Rationale: booking/availability paths require strong consistency and must
 * always read/write within a DB transaction without cache interference.
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface DoNotCache {}
