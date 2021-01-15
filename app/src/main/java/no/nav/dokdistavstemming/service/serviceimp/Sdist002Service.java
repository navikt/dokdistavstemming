package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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

    private static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dokdist_antall_delay_kvittering_counter";
    private static final String UKJENT = "Ukjent";
    private final HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
    private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserMapper;
    private final CSVProdusere csvProdusere;
    private final MeterRegistry meterRegistry;
    private final JiraService jiraService;
    private final DokdistavstemmingProp dokdistavstemmingProp;

    public Sdist002Service(HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt,
                           CSVProdusere csvProdusere, MeterRegistry meterRegistry, JiraService jiraService,
                           DokdistavstemmingProp dokdistavstemmingProp) {
        this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
        this.oppdaterForsendelserMapper = new OppdaterForsendelserAvstemtInfoMapper();
        this.csvProdusere = csvProdusere;
        this.meterRegistry = meterRegistry;
        this.jiraService = jiraService;
        this.dokdistavstemmingProp = dokdistavstemmingProp;
    }

    public Set<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottattService(String distribusjonKanal) {
        int period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? dokdistavstemmingProp.getDelayTimePrint() : dokdistavstemmingProp.getDelayTimeSDP();
        List<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(distribusjonKanal, period);

        return new HashSet<>(avstemForsendelseRequestTos);
    }


    public void oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal() {
        Arrays.stream(DistribusjonKanalCode.values())
                .forEach(distribusjonKanal -> {
                    List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = getForsendelserByDistirbusjonKanal(distribusjonKanal.name());
                    if (avstemForsendelseResponseTos == null) {
                        return;
                    } else {
                        File csvFil = csvProdusere.oppretteCsvFil(avstemForsendelseResponseTos);
                        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(distribusjonKanal.name(), csvFil, avstemForsendelseResponseTos.size());
                        hentForsendelseKvitteringIkkeMottatt.oppdaterForsendelserAvstemtDatoOgReferanse(oppdaterForsendelserMapper.map(avstemForsendelseResponseTos, jiraSakResponseTo));
                    }
                });

    }


    public List<AvstemForsendelseResponseTo> getForsendelserByDistirbusjonKanal(String distribusjonKanal) {
        AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();
        Set<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelserKvitteringIkkeMottattService(distribusjonKanal);
        return avstemForsendelseRequestTos.isEmpty() || avstemForsendelseRequestTos == null ? null :
                avstemForsendelseRequestTos.stream()
                        .filter(Objects::nonNull)
                        .map(avstemForsendelseMapper::mapAvstemmForsendelser)
                        .flatMap(Collection::stream)
                        .sorted(Comparator.comparing(AvstemForsendelseResponseTo::getOpprettetDato))
                        .map(avstemForsendelse -> {
                            incrementFunctionalMetrics(avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getDistribusjonStatus());
                            log.info(String.format("Sdist002 har fant avvik forsendelser med forsendelseId=%s, dokumentId=%s, dokumentStatus=%s,opprettetDato=%s" +
                                            ",distribusjonKanal=%s,journalpostId=%s", avstemForsendelse.getForsendelseId(), avstemForsendelse.getDokumentId(), avstemForsendelse.getDokumentStatus(), avstemForsendelse.getOpprettetDato(),
                                    avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getJournalpostId()));
                            return avstemForsendelse;
                        })
                        .filter(avstemForsendelseResponseTo -> isAvstemtReferanseNull(avstemForsendelseResponseTo.getAvstemtReferanse()))
                        .collect(Collectors.toList());
    }

    private boolean isAvstemtReferanseNull(String avstemtReferanse) {
        return avstemtReferanse == null;
    }

    private void incrementFunctionalMetrics(String distribusjonKanal,
                                            String dokumentStatus) {
        meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
                "distribusjonKanal", distribusjonKanal == null ? UKJENT : distribusjonKanal,
                "dokumentStatus", dokumentStatus == null ? UKJENT : dokumentStatus).increment();
    }


}
