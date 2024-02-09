plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:5.0.0-beta.20")
    implementation("org.xerial:sqlite-jdbc:3.7.2")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

}

tasks.test {
    useJUnitPlatform()
}

