# Generering av testdata for ephyto-api

Testdata skal bare brukes lokalt eller mot uat-miljøet. Det lages testsertifikater med dummyinnhold og litt tilfeldige data.

## Avhengigheter

Først og fremst må du ha installert JAVA 21, og sette opp en JAVA_HOME variabel hvis det ikke har blitt gjort fra før.

- Last ned JAVA fra feks. her: https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
- Finn pathen til javas path ved å kjøre
    - ```/usr/libexec/java_home -V```
- Gå inn i `.zshrc` for å legge til JAVA_HOME variabelen. 
  - ```vi ~/.zshrc```
  - ```i``` for å legge til
  - Legg inn ```echo "export JAVA_HOME=<PATH_TO_JAVA>"```
  - ```esc``` og ```:wq``` for å lagre


Du må legge Mattilsynet-IssuingCA2.cer inn i cacerts. Dette gjør du på Mac ved å eksportere den fra Keychain Access:

1. Åpne Keychain Access
2. Finn “Mattilsynet-IssuingCA2”
3. Velg “export…”
4. Lagre sertifikatet
5. Naviger til mappen du lagret sertifikatet i med terminalen
6. Deretter kjør følgende kommando for å legge sertifikatet inn i java sin truststore 
  - HUSK: Bytt ut `<<<DITT PASSORD>>>` med et passord du lager selv

```"$JAVA_HOME"/bin/keytool -import -alias Mattilsynet-IssuingCA2 -file "Mattilsynet-IssuingCA2.cer" -keystore "$JAVA_HOME"/lib/security/cacerts -storepass <<<DITT PASSORD>>>```

## Opprett keystore 
Skriv disse kommandoene i terminalen (`changeit` er passord):
- Stå der du vil ha `nppo.keystore` og kjør følgende kommando i terminalen: 

```
keytool -genkey -alias nppo1 -keyalg RSA -keysize 2048 -keystore nppo.keystore -validity 3650 -keypass changeit -storepass changeit 
```

- Stå der `nppo.keystore` ble lagt, og gjør følgende kommando i terminalen: 

```
keytool -export -keystore nppo.keystore -alias nppo1 -file nppo.cer -keypass changeit -storepass changeit
```

## Referer til keystore i .zshrc
Legg inn de følgende linjene i `.zshrc`

```
export EPHYTO_KEYSTORE_PATH=/Path/til/keystore/nppo.keystore
export EPHYTO_KEYSTORE_PASSWORD=<< Ditt passord til keystore >>
``` 

## Opplasting av sertifikat til ephyto-hubben
- Fiks profil på hub.ephytoexchange.org
  - Se tabben "Guide to joining" på denne siden: https://hub.ephytoexchange.org/landing/hub/index.html
  - For å aktivere kontoene kan det hende en eksisterende bruker er nødt til å sette profilen som "active" under "Users" i AdminConsole
- Logg inn i AdminConsole her: https://uat-hub.ephytoexchange.org/AdminConsole/
- Trykk på "Configuration" på venstre side
- Trykk på "Certificates" oppe til høyre
- Trykk på "Add" oppe til høyre
- Last opp `nppo.cer` og gi en passende beskrivelse, F.eks. "Ola sitt sertifikat"

## Generere testdata og sende til hub'en

For å sende sertifikater til hub'en, kan man gjøre post til:
```http://localhost:8085/ephyto/v1/send/envelope```

Her kan man sende med status, type og erstatterSertifikatNummer. Det gjøres ingen sjekk på at man sender inn verdier på riktig format. Funksjonen legger inn default verdier dersom man ikke sender inn noe.
Man kan også sende inn antall for å lage flere sertifikater på en gang. antall er default 1.

## Hente envelopes fra hub'en

For å hente sertifikater fra hub'en kan man gjøre post til:
```http://localhost:8085/ephyto/v1/sertifikater```
