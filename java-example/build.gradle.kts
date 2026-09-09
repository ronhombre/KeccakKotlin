plugins {
    id("java")
}

group = "asia.hombre.examples.keccak"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":"))
}

tasks.test {
    useJUnitPlatform()
}