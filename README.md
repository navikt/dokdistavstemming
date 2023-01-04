Dokdistavstemming
================

Dokdistavstemming gjer regelmessige oppslag i dokumentdistribusjon for å identifisere sendingar som ikkje er ferdig handsama.
Ei sending som ikkje er kvittert frå distribusjonskanal får oppretta ei sak i Jira for vidare oppfylgjing. 
Dei to Cron-jobbane sdist002 og sdist004 er ansvarlege for dette.

Sdist002 sørgjer for oppretting av Jira-oppgåver
- Prod: Kl. 11 kvar måndag-fredag

Sdist004 oppdaterer journalpostar i joark med ekspedertstatus og informasjon om utsending 
- Prod: køyrer kvar time måndag-søndag

## Førespurnadar
Spørsmål om koda eller prosjektet kan stillast på [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)