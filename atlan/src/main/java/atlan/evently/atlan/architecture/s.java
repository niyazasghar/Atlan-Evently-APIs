// src/test/java/atlan/evently/atlan/architecture/NoCacheOnBookingWritesTest.java
package atlan.evently.atlan.architecture;


@AnalyzeClasses(packages = "atlan.evently.atlan")
public class NoCacheOnBookingWritesTest {

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule booking_services_must_not_use_cacheable =
            ArchRuleDefinition.noMethods()
                    .that().areDeclaredInClassesThat().resideInAnyPackage("..booking.service..")
                    .should().beAnnotatedWith(org.springframework.cache.annotation.Cacheable.class)
                    .because("Booking/availability paths must never be cached for correctness.");
}
