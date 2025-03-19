
"$JAVA_HOME/bin/keytool" -import -alias Mattilsynet-IssuingCA2 -file "Mattilsynet-IssuingCA2.cer" -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit

# Generering av testdata for ephyto-api

Testdata skal bare brukes lokalt eller mot uat-miljøet. Det lages testsertifikater med dummyinnhold og litt tilfeldige data.

## Avhengigheter

Først og fremst må du ha installert JAVA 21, og sette opp en JAVA_HOME variabel hvis det ikke har blitt gjort fra før.

- Last ned JAVA fra feks. her: https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
- Finn pathen til javas path ved å kjøre
    - ```/usr/libexec/java_home -V```
- Gå inn i vim for å legge til JAVA_HOME variabelen. 
  - ```vi ~/.zshrc```
  - ```i``` for å legge til
  - Legg inn ```echo "export JAVA_HOME=<PATH_TO_JAVA>"```
  - ```esc``` og ```:wq``` for å lagre


Du må legge Mattilsynet-IssuingCA2.cer inn i cacerts. Dette gjør du på Mac ved å eksportere den fra Keychain Access:

1. Åpne Keychain Access
2. Finn “Mattilsynet-IssuingCA2”
3. Velg “export…”
4. Lagre sertifikatet
Deretter kjør følgende kommando for å legge sertifikatet inn i java sin truststore(Bytt ut changeit med et passord du lager):

```"$JAVA_HOME"/bin/keytool -import -alias Mattilsynet-IssuingCA2 -file "Mattilsynet-IssuingCA2.cer" -keystore "$JAVA_HOME"/lib/security/cacerts -storepass changeit```

## Opprett keystore 
Skriv disse kommandoene i terminalen(changeit er passord):
- Stå der Mattilsynet-RootCA ligger```"$JAVA_HOME"/bin/keytool.exe -import -alias Mattilsynet-RootCA -file C:/Users/$USER/Downloads/Mattilsynet-RootCA.cer -keystore```
- ```"$JAVA_HOME"/security -storepass changeit```
- Stå der du vil ha nppo.keystore ```keytool -genkey -alias nppo1 -keyalg RSA -keysize 2048 -keystore nppo.keystore -validity 3650 -keypass changeit -storepass changeit```
- Stå der nppo.keystore er ```keytool -export -keystore nppo.keystore -alias nppo1 -file nppo.cer -keypass changeit -storepass changeit```. nppo.cer som blir generert skal legges i ephyto-hubben. 

## Generere testdata og sende til hub'en

For å sende sertifikater til hub'en, kan man gjøre post til:
```http://localhost:8085/ephyto/v1/send/envelope```

Her kan man sende med status, type og erstatterSertifikatNummer. Det gjøres ingen sjekk på at man sender inn verdier på riktig format. Funksjonen legger inn default verdier dersom man ikke sender inn noe.
Man kan også sende inn antall for å lage flere sertifikater på en gang. antall er default 1.

## Hente envelopes fra hub'en

For å hente sertifikater fra hub'en kan man gjøre post til:
```http://localhost:8085/ephyto/v1/sertifikater```
