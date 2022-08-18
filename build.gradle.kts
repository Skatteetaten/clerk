plugins {
    id("java")
    id("no.skatteetaten.gradle.aurora") version "4.5.3"
}

aurora {
    useKotlinDefaults
    useSpringBootDefaults
    useAsciiDoctor
    useSpringBoot {
        useCloudContract
    }

    versions {
        javaSourceCompatibility = "17"
    }
}

dependencies {
    // for openshift aurora client
    implementation("com.fkorotkov:kubernetes-dsl:2.8.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter:1.4.+")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.7")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.1.8")
    testImplementation("no.skatteetaten.aurora:mockwebserver-extensions-kotlin:1.3.1")
}
