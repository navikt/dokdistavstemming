package no.nav.dokdistavstemming.utils;


import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class RequestBuilder {

	private HttpHeaders headers = new HttpHeaders();
	private Map<String, String> queryParams = new HashMap<>();

	public RequestBuilder basicHeader(String key, String value){
		queryParams.put(key,value);
		return this;
	}

	public HttpEntity<?> buildEntity() {
		return new HttpEntity<>(headers);
	}

	public HttpEntity<?> buildEntity(Object body) {
		return new HttpEntity<>(body, headers);
	}
}
