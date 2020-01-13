package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP_PRINT;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class AvstemForsendelseService {

	private static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
	private static final Long ANTALL_TIMER = 24L;
	private static final Long ANTALL_DAGER = 144L; // 144 timer er 6 dager
	private static final String UKJENT = "Ukjent";
	private final HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
	private final CSVProdusere csvProdusere;
	private final MeterRegistry meterRegistry;
	private final JiraService jiraService;

	public AvstemForsendelseService(HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt, CSVProdusere csvProdusere, MeterRegistry meterRegistry, JiraService jiraService) {
		this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
		this.csvProdusere = csvProdusere;
		this.meterRegistry = meterRegistry;
		this.jiraService = jiraService;
	}

	public Set<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottattService(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(distribusjonKanal, period);
		return avstemForsendelseRequestTos.stream()
				.filter(avstemForsendelse -> !avstemForsendelse.getDokumenter().isEmpty())
				.collect(Collectors.toSet());
	}


	public void oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal() {
		Arrays.stream(DistribusjonKanalCode.values())
				.map(distribusjonKanal -> getMappedAvstemmForsendelseByDistKanal(distribusjonKanal.name()))
				.filter(Objects::nonNull)
				.forEach(avstemForsendelseResponseTo -> {
					File csvFil = csvProdusere.oppretteCsvFil(avstemForsendelseResponseTo);
					jiraService.oppretteMMAJiraSak(avstemForsendelseResponseTo.get(0).getDistribusjonKanal(), csvFil);
				});

	}


	public List<AvstemForsendelseResponseTo> getMappedAvstemmForsendelseByDistKanal(String distribusjonKanal) {
		AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();
		return hentForsendelserKvitteringIkkeMottattService(distribusjonKanal).stream().filter(Objects::nonNull).map(hentForsendelse -> {
			AvstemForsendelseResponseTo avstemForsendelse = PRINT.name().equals(distribusjonKanal) ? avstemForsendelseMapper.mapDokDistPrint(hentForsendelse) : avstemForsendelseMapper.mapDokDistUtenPrint(hentForsendelse);
			incrementFunctionalMetrics(avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getOpprettetDato(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getCountDokument());
			log.info(String.format("DokDistAvstemming har fant forsendelser som kvittering ikke mottatt med forsendelseId=%s, distribusjonStatus=%s,opprettetDato=%s" +
							",distribusjonKanal=%s,antallDokInfo=%s", avstemForsendelse.getForsendelseId(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getOpprettetDato(),
					avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getCountDokument()));
			return avstemForsendelse;
		}).collect(Collectors.toList());
	}


	private void incrementFunctionalMetrics(String distribusjonKanal, String opprettetDato,
											String distribusjonStatus, Long antallDokInfoId) {
		meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
				"distribusjonKanal", distribusjonKanal == null ? UKJENT : distribusjonKanal,
				"opprettetDato", opprettetDato == null ? UKJENT : opprettetDato,
				"distribusjonStatus", distribusjonStatus == null ? UKJENT : distribusjonStatus,
				"antallDokInfoId", antallDokInfoId == null ? UKJENT : String.valueOf(antallDokInfoId)).increment();
	}


}
