plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.10"

    // Statisk kodeanalyse
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")
    id("io.gitlab.arturbosch.detekt").version("1.23.5")

    // Soap
    id("com.github.bjornvester.wsdl2java") version "2.0.2"

    // Protobuf
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/Mattilsynet/fisk/")
        credentials {
            username = "token"
            password = System.getenv("READ_SOURCE_AND_PACKAGES")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

dependencies {

    // mattilsynet
    implementation(platform("no.mattilsynet.fisk.libs:virtual-nats-bom:2025.01.13-14.25-c95f91cb5574"))
    implementation("no.mattilsynet.fisk.libs:nats")
    implementation("no.mattilsynet.fisk.libs:spring")
    implementation("no.mattilsynet.fisk.libs:virtual-nats")

    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.4")

    // nats
    implementation("io.nats:jnats")

    // protobuf
    implementation(platform("com.google.protobuf:protobuf-bom:4.29.3"))
    implementation("com.google.protobuf:protobuf-java")

    // gcp
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2024.0.0"))
    implementation(platform("com.google.cloud:spring-cloud-gcp-dependencies:5.10.0"))
    implementation("com.google.cloud:spring-cloud-gcp-starter-secretmanager")
    implementation("com.google.cloud:spring-cloud-gcp-starter-storage")

    // spring xml
    implementation("org.springframework.ws:spring-xml")

    // jax
    implementation("com.sun.xml.ws:rt:4.0.3") {
        exclude(group = "com.sun.xml.bind")
    }
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5")
    implementation("org.glassfish.jaxb:jaxb-core:4.0.5")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    // jetbrains
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

    // logging
    implementation("com.google.cloud:spring-cloud-gcp-starter-logging")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    testImplementation("no.mattilsynet.fisk.libs:spring-test")
}

sonar {
    properties {
        property("sonar.projectKey", "Mattilsynet_ephyto-api")
        property("sonar.projectName", "ephyto-api")
        property("sonar.organization", "mattilsynet")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks {

    bootJar {
        archiveFileName.set("app.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named("build") {
        dependsOn(detekt)
    }

    withType<Test> {
        useJUnitPlatform()
    }

    test {
        // Fjerner warning fra Mockito/Surefire plugin
        // @See: https://github.com/mockito/mockito/issues/3037
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(false)
            dependsOn(test)
        }
    }

    kotlin {
        compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        compilerOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

val ephytoEnvironment = System.getenv("EPHYTO_ENVIRONMENT")
val wsdlPath = "ephyto/${if (ephytoEnvironment == "prod") "prod/ephyto.wsdl" else "uat/uat-ephyto.wsdl"}"

wsdl2java {
    cxfVersion.set("4.0.2")
    includes.set(
        listOf(
            wsdlPath
        )
    )
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.3"
    }
}
