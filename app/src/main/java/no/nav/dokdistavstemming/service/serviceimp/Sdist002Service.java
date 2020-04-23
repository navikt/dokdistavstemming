package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
public class Sdist002Service {

    private static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
    private static final Long ANTALL_TIMER = 10L;
    private static final Long ANTALL_DAGER = 120L; // 120 timer er 5 dager
    private static final String UKJENT = "Ukjent";
    private final HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
    private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserMapper;
    private final CSVProdusere csvProdusere;
    private final MeterRegistry meterRegistry;
    private final JiraService jiraService;

    public Sdist002Service(HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt, CSVProdusere csvProdusere, MeterRegistry meterRegistry, JiraService jiraService) {
        this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
        this.oppdaterForsendelserMapper = new OppdaterForsendelserAvstemtInfoMapper();
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
                .forEach(distribusjonKanal -> {
                    List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = getAvstemmForsendelseByDistKanal(distribusjonKanal.name());
                    if (avstemForsendelseResponseTos == null) {
                        return;
                    } else {
                        File csvFil = csvProdusere.oppretteCsvFil(avstemForsendelseResponseTos);
                        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(distribusjonKanal.name(), csvFil, avstemForsendelseResponseTos.size());
                        hentForsendelseKvitteringIkkeMottatt.oppdaterForsendelserAvstemDatoOgReferanse(oppdaterForsendelserMapper.map(avstemForsendelseResponseTos, jiraSakResponseTo));
                    }
                });

    }


    public List<AvstemForsendelseResponseTo> getAvstemmForsendelseByDistKanal(String distribusjonKanal) {
        AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();
        Set<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelserKvitteringIkkeMottattService(distribusjonKanal);
        return avstemForsendelseRequestTos.isEmpty() || avstemForsendelseRequestTos == null ? null :
                avstemForsendelseRequestTos.stream()
                        .filter(Objects::nonNull)
                        .map(hentForsendelse -> avstemForsendelseMapper.mapAvstemmForsendelser(hentForsendelse))
                        .flatMap(Collection::stream)
                        .sorted(Comparator.comparing(AvstemForsendelseResponseTo::getOpprettetDato))
                        .map(avstemForsendelse -> {
                            incrementFunctionalMetrics(avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getOpprettetDato(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getJournalpostId());
                            log.info(String.format("Sdist002 har fant avvik forsendelser med forsendelseId=%s, dokumentId=%s, dokumentStatus=%s,opprettetDato=%s" +
                                            ",distribusjonKanal=%s,journalpostId=%s", avstemForsendelse.getForsendelseId(), avstemForsendelse.getDokumentId(), avstemForsendelse.getDokumentStatus(), avstemForsendelse.getOpprettetDato(),
                                    avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getJournalpostId()));
                            return avstemForsendelse;
                        })
						.filter(avstemForsendelseResponseTo -> isAvstemtReferanseNull(avstemForsendelseResponseTo.getAvstemtReferanse()))
						.collect(Collectors.toList());
    }

	private boolean isAvstemtReferanseNull(String avstemtReferense){
		return avstemtReferense==null;
	}

    private void incrementFunctionalMetrics(String distribusjonKanal, String opprettetDato,
                                            String dokumentStatus, String journalpostId) {
        meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
                "distribusjonKanal", distribusjonKanal == null ? UKJENT : distribusjonKanal,
                "opprettetDato", opprettetDato == null ? UKJENT : opprettetDato,
                "dokumentStatus", dokumentStatus == null ? UKJENT : dokumentStatus,
                "journalpostId", journalpostId == null ? UKJENT : journalpostId).increment();
    }


}
