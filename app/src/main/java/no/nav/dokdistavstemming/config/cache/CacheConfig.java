package no.nav.dokdistavstemming.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
public class CacheConfig {

	public static final String USER_CACHE = "userCache";
	public static final String STS_CACHE = "stsCache";
	public static final String DOKUMENT_CACHE = "DOKUMENT_CACHE";

	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(USER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(8, TimeUnit.HOURS)
						.maximumSize(10000)
						.recordStats()
						.build()),
				new CaffeineCache(DOKUMENT_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(2, TimeUnit.HOURS)
						.maximumSize(100)
						.recordStats()
						.build()),
				new CaffeineCache(STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, TimeUnit.MINUTES)
						.maximumSize(10000)
						.recordStats()
						.build())
		));
		return manager;
	}
}
