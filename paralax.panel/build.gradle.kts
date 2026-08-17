plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.18")
    implementation("io.javalin:javalin:7.2.3")
}

application {
    mainClass = "dev.arsngrobg.paralax.MainKt"
}
