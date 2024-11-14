# Generering av testdata for ephyto-api

Testdata skal bare brukes lokalt eller mot uat-miljøet. Det lages testsertifikater med dummyinnhold og litt tilfeldige data.

## Generere testdata og sende til hub'en

For å sende sertifikater til hub'en, kan man gjøre post til:
```http://localhost:8085/ephyto/v1/send/envelope```

Her kan man sende med status, type og erstatterSertifikatNummer. Det gjøres ingen sjekk på at man sender inn verdier på riktig format. Funksjonen legger inn default verdier dersom man ikke sender inn noe.
Man kan også sende inn antall for å lage flere sertifikater på en gang. antall er default 1.

## Hente envelopes fra hub'en

For å hente sertifikater fra hub'en kan man gjøre post til:
```http://localhost:8085/ephyto/v1/sertifikater```
