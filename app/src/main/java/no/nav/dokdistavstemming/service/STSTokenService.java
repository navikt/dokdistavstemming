package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.consumer.sts.STSResponse;
import no.nav.dokdistavstemming.consumer.sts.STSRestConsumer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public class STSTokenService {

	private final STSRestConsumer stsRestConsumer;

	public STSTokenService(STSRestConsumer stsRestConsumer) {
		this.stsRestConsumer = stsRestConsumer;
	}

	@Retryable(backoff = @Backoff(delay = 500))
	public STSResponse hentOidcToken() {
		return stsRestConsumer.getServiceuserOIDCToken().getBody();
	}
}
