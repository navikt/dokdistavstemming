package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;

import java.util.List;

@Builder
public record ForsendelseTos(List<ForsendelseTo> forsendelseListe) {
}

