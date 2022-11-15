package no.nav.dokdistavstemming.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

@Configuration
public class LokalCacheConfig {

	public static final String AZURE_TOKEN_CACHE = "AzureToken";

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(singletonList(
				new CaffeineCache(AZURE_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(55, TimeUnit.MINUTES)
						.build()))
		);
		return manager;
	}
}
