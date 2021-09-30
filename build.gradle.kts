plugins {
    id("org.springframework.cloud.contract")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("org.sonarqube") version "3.0"

    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("org.asciidoctor.convert") version "2.4.0"

    id("com.gorylenko.gradle-git-properties") version "2.2.3"
    id("com.github.ben-manes.versions") version "0.29.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.14"

    id("no.skatteetaten.gradle.aurora") version "3.2.0"
}

extra["jackson-bom.version"] = "2.10.0"

dependencies {
    // for openshift aurora client
    implementation("com.fkorotkov:kubernetes-dsl:2.8.1")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.projectreactor.addons:reactor-extra:3.3.3.RELEASE")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.19")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.0.4")
}
repositories {
    mavenCentral()
}
