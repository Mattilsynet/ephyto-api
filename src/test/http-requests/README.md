### Før du kan kjøre rest-kall
Kopier filen `http-client.env.json-base` til en ny fil `http-client.env.json`. Denne skal oppdateres med riktig client id og client secret. Disse verdiene finnes i filen  application_default_credentials.json som du får når du logger inn i gcloud. Denne skal ikke commites til github.

### Hvordan kjøre rest-kallene

En av filene du kan bruke for å kjøre kall er `hovedfil.http`.

1. Før du kjører kallene, må du sette variablene. Dette gjør du i hovedfilen med å trykke på play-ikonet
   som er ved siden av `GET localhost:8080/`. Det er lagt til varialer her som trengs for å kjøre kallene.
2. I hovedfilen kan du både kjøre hele filer og noen enkeltkall. Trykk på "play"-ikonet på filene/kallene du
   har lyst til å kjøre.
3. Hvis du trenger å kjøre noen andre kall enn det som er i hovedfilen er dette også mulig ved
   å gå inn på filen og trykke play. Husk at du må sette variabler som du trenger i hovedfilen først. 
