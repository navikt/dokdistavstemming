package no.nav.dokdistavstemming.service;


import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederKvitteringForsendelse;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;
import no.nav.dokdistavstemming.domain.ForsendelseKanalCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DokDistAvstemmingService {

	private static final Long ANTALL_TIMER = 6L;
	private static final Long ANTALL_DAGER = 120L; // 120 timer er 5 dager
	private final HentUekspederKvitteringForsendelse hentUekspederKvitteringForsendelse;
	private ForsendelseKanalCode forsendelseKanalCode;

	public DokDistAvstemmingService(HentUekspederKvitteringForsendelse hentUekspederKvitteringForsendelse) {
		this.hentUekspederKvitteringForsendelse = hentUekspederKvitteringForsendelse;
	}

	public static void main(String[] args) {


	}

	public List<DokDistAvstemmingForsendelse> hentUekspederForsendelserService() {

		List<DokDistAvstemmingForsendelse> uekspederForsendelser = new ArrayList<>();
		switch (forsendelseKanalCode) {
			case PRINT:
				String print = forsendelseKanalCode.PRINT.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(print, ANTALL_DAGER);
				break;
			case SDP:
				String sdp = forsendelseKanalCode.SDP.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(sdp, ANTALL_TIMER);
				break;
			case SDP_PRINT:
				String sdpPrint = forsendelseKanalCode.SDP_PRINT.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(sdpPrint, ANTALL_TIMER);
				break;
			case E_HANDEL:
				String eHandel = forsendelseKanalCode.E_HANDEL.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(eHandel, ANTALL_TIMER);
				break;
			case DITTNAV:
				String dittNav = forsendelseKanalCode.DITTNAV.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(dittNav, ANTALL_TIMER);
				break;
			case TRYGDERETTEN:
				String trygdeRetten = forsendelseKanalCode.TRYGDERETTEN.name();
				uekspederForsendelser = hentUekspederKvitteringForsendelse.hentUekspederKvitteringForsendelse(trygdeRetten, ANTALL_TIMER);
				break;
		}
		return uekspederForsendelser;
	}


}
