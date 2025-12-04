package no.nav.dokdistavstemming.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.SlackProperties;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class SlackService {

	private final MethodsClient slackClient;
	private final SlackProperties slackProperties;

	SlackService(SlackProperties slackProperties) {
		this.slackProperties = slackProperties;
		slackClient = Slack.getInstance().methods(slackProperties.token());
	}

	public void sendMelding(String melding) {
		if (slackProperties.alertsEnabled()) {
			try {
				log.info("Sender melding til Slack med melding={}", melding);

				var response = slackClient.chatPostMessage(jobbFeiletMelding(melding));

				var result = response.isOk() ? "OK" : response.getError();
				log.info("Sendte melding med ts={} til Slack med resultat={}", response.getTs(), result);

			} catch (Exception e) {
				log.error("Sending av melding til Slack feilet med feilmelding={}", e.getMessage(), e);
			}
		}
	}

	private ChatPostMessageRequest jobbFeiletMelding(String feilmelding) {
		String headerText = ":rotating_light: Skedulert jobb feilet!";
		String bodyText = """
                 *Applikasjon:* dokdistavstemming
                 *Feilmelding:* %s
                 """.formatted(feilmelding).stripIndent();

		return ChatPostMessageRequest.builder()
				.channel(slackProperties.channel())
				.text(bodyText) //fallback tekst
				.blocks(Arrays.asList(
						HeaderBlock.builder()
								.text(PlainTextObject.builder().text(headerText).build())
								.build(),
						SectionBlock.builder()
								.text(MarkdownTextObject.builder().text(bodyText).build())
								.build()
				))
				.build();
	}
}
