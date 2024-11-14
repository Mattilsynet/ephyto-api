# ephyto-api

## Introduction

Hi there to anyone out there who stumbled upon this repo in search of answers on how one would integrate
with the Ephyto HUB to exchange ephyto certificates. This is the Norwegian Food Safety Authority's implementation
of our connection to send and receive ephyto certificates.

While the code base requires some internal dependencies and a lot of the code is in Norwegian, we hope that this
can serve as an inspiration for anyone looking to do the same type of integration. For instance, the way we read
the [ephyto keystore](https://github.com/Mattilsynet/ephyto-api/blob/master/src/main/kotlin/no/mattilsynet/ephyto/api/EphytoKeystorePropertySetter.kt)
may be of particular interest for anyone struggling with this in a Spring Boot context.

The Ephyto WSDL is generated using the wsdl2java plugin. We generate two artifacts with different WSDL's
when we build the application to enable testing towards UAT and Production environment.

If you have any questions please contact us at [digiplant@mattilsynet.no](mailto:digiplant@mattilsynet.no)

## Avhengigheter

Først og fremst må du ha installert JAVA 21, og sette opp en JAVA_HOME variabel.

- Last ned JAVA fra feks. her: https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
- Finn pathen til javas path ved å kjøre
    - ```/usr/libexec/java_home -V```
- Kjør følgende kommando for å lage JAVA_HOME environment variabel:
    - ```echo "export JAVA_HOME=<PATH_TO_JAVA>"```

Legg til github PAT generert i [Gradle setup](#gradle-setup):  
```export READ_SOURCE_AND_PACKAGES="GITHUB_PAT"```

## Generere testdata

Se [testdata](https://github.com/Mattilsynet/ephyto-api/tree/master/src/main/kotlin/no/mattilsynet/ephyto/api/controllers)

## Lokal utvikling

For å kjøre applikasjonen lokalt, endre ```spring.profiles.active``` til ```local``` i ```application.yml``` slik at du får tilgang på h2-console for å se på databasen.

### Gradle setup

Lag en personal access token (PAT) i GitHub developer settings med scope `read:packages` og legg den til i environment variabelen `READ_SOURCE_AND_PACKAGES`. Tokenet må være autorisert til å lese pakker fra `Mattilsynet` organisasjonen.
