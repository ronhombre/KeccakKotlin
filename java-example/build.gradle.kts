plugins {
    id("java")
}

group = "asia.hombre.examples.keccak"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":"))
}

tasks.test {
    useJUnitPlatform()
}