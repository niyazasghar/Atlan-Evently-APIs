// src/main/java/atlan/evently/atlan/config/CacheConfig.java
package atlan.evently.atlan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;

import java.time.Duration;

/**
 * Central cache configuration.
 * Uses Caffeine (local, in-process) with short TTLs suitable for read-heavy endpoints.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        // Declare cache names used by @Cacheable/@CacheEvict in services
        CaffeineCacheManager mgr = new CaffeineCacheManager("eventDetail", "eventListUpcoming");
        // Global defaults; can be overridden per cache if needed
        mgr.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)               // cap memory usage
                .expireAfterWrite(Duration.ofSeconds(60)) // default TTL for entries
                .recordStats());                    // expose hit/miss stats
        return mgr;
    }
}
