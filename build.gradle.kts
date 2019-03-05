buildscript {
    dependencies {
        classpath("no.skatteetaten.aurora.gradle.plugins:aurora-gradle-plugin:2.0.17-SNAPSHOT")
        classpath("org.springframework.cloud:spring-cloud-contract-gradle-plugin:2.1.0.RELEASE")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.21"
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.21"
    id("org.springframework.boot") version "2.1.3.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "6.3.1"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.gorylenko.gradle-git-properties") version "2.0.0"
    id("org.sonarqube") version "2.7"
    id("org.asciidoctor.convert") version "1.6.0"
}

apply(plugin = "spring-cloud-contract")
apply(plugin = "no.skatteetaten.plugins.aurora")

dependencies {
    implementation("io.fabric8:openshift-client:4.1.2")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.fabric8:openshift-server-mock:4.1.2")
    testImplementation("io.mockk:mockk:1.8.9")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.13")
    testImplementation("com.fkorotkov:kubernetes-dsl:2.0.1")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.12.0")
}

tasks.findByPath("compileTestGroovy")?.enabled=false
