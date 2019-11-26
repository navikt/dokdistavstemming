package no.nav.dokdistavstemming.avstemmIT;

import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraFeilToOpprettSakForAvstemFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyOpprettSakForAvstemFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.happilyHentForsendelseKvitteringIkkeMottattKanalPrint;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyHentProjectDetails;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyPostVedleggDokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class OpprettJiraSakServiceIT extends AbstractIT {

	private static final String JIRA_MESSAGE = "https://jira-q1.adeo.no/browse/MMA-134";

	@BeforeEach
	public void setUp() {
		jiraService = new JiraService(jiraConsumer, avstemForsendelseService, meterRegistry);
	}

	@Test
	void shouldHappilyOppretteJiraSak() throws Exception {
		happilyHentForsendelseKvitteringIkkeMottattKanalPrint();
		List<AvstemForsendelseResponseTo> result = avstemForsendelseService.avstemmForsendelseDistKanalPrint();
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemFrosendelse();
		jiraHappyPostVedleggDokument();
		File fil = csvProdusere.oppretteCsvFil(result);
		JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak();

		assertThat(jiraSakResponseTo.getMessage(), is(JIRA_MESSAGE));
		assertThat(jiraSakResponseTo.getHttpStatusCode(),is(0));
		assertTrue(fil.exists());
		assertTrue(fil.length() != 0);
		verify(1, postRequestedFor(urlEqualTo("/rest/api/2/issue")).withRequestBody(equalToJson(classpathToString("__files/jirarequest-happy.json"))));
		verify(2, postRequestedFor(urlEqualTo("/rest/api/2/issue/MMA-134/attachments")));
		verify(1, getRequestedFor(urlEqualTo("/rest/api/2/project/MMA")));
	}

	@Test
	void opprettJiraSakThrowsBadRequestErrorMelding() throws Exception {
		happilyHentForsendelseKvitteringIkkeMottattKanalPrint();
		List<AvstemForsendelseResponseTo> result = avstemForsendelseService.avstemmForsendelseDistKanalPrint();
		jiraHappyHentProjectDetails();
		jiraFeilToOpprettSakForAvstemFrosendelse();
		jiraHappyPostVedleggDokument();
		File fil = csvProdusere.oppretteCsvFil(result);

		AvstemForsendelseFunctionalException avstemForsendelseFunctionalException = assertThrows(AvstemForsendelseFunctionalException.class, () ->
				jiraService.oppretteMMAJiraSak());

		assertThat(avstemForsendelseFunctionalException.getMessage(),is(containsString("status:400 BAD_REQUEST ,feilet: 400 Bad Request")));
		assertTrue(fil.exists());
		assertTrue(fil.length() != 0);
		verify(2, postRequestedFor(urlEqualTo("/rest/api/2/issue")));
		verify(2, getRequestedFor(urlEqualTo("/rest/api/2/project/MMA")));
	}
}
