buildscript {
    dependencies {
        classpath("no.skatteetaten.aurora.gradle.plugins:aurora-gradle-plugin:2.0.16-SNAPSHOT")
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
    compile("io.fabric8:openshift-client:4.1.2")
    compile("org.springframework.boot:spring-boot-starter-security")
    compile("org.springframework.boot:spring-boot-starter-web")
    testCompile("org.springframework.cloud:spring-cloud-starter-contract-verifier")
    testCompile("org.springframework.restdocs:spring-restdocs-mockmvc")
    testCompile("org.springframework.security:spring-security-test")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.fabric8:openshift-server-mock:4.1.2")
    testCompile("io.mockk:mockk:1.8.9")
    testCompile("com.willowtreeapps.assertk:assertk-jvm:0.13")
    testCompile("com.fkorotkov:kubernetes-dsl:2.0.1")
    testCompile("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.12.0")
}

