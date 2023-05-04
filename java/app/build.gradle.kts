import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.generateProtoTasks
import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.10"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    id("com.google.protobuf")
    id("distribution")

    `maven-publish`
    signing
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    api(libs.grpc.protobuf)
    api(libs.grpc.kotlin.stub)
    api(libs.grpc.stub)
    api(libs.protobuf.kotlin)
}

sourceSets {
    main {
        proto {
            srcDir ("../../repositories/finschia-sdk/proto")
            srcDir ("../../repositories/finschia-sdk/third_party/proto")
            srcDir ("../../repositories/wasmd/proto")
            srcDir ("../../repositories/ibc-go/proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.proto.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.grpckotlin.get()}:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

java.sourceCompatibility = VERSION_1_8
java.targetCompatibility = VERSION_1_8

tasks.jar.configure {
    exclude("**/*.proto")
    includeEmptyDirs = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

java{
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava8Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

publishing {
    val groupIdVal = "io.github.jaeseung-bae.sdk"
    val artifactIdVal = "remove-me-test-purpose"
    val versionVal: String? = System.getProperty("VERSION")

    val pomName = "finschia"
    val pomDesc = artifactIdVal
    val pomUrl = "https://github.com/jaeseung-bae"
    val pomScmConnection = "scm:git:git://github.com/jaeseung.bae/test.git"
    val pomDeveloperConnection = "scm:git:ssh://github.com/jaeseung.bae/test.git"
    val pomScmUrl = "https://github.com/jaeseung-bae"

    val ossrhUserName = System.getenv("OSSRH_USERNAME")
    val ossrhPassword = System.getenv("OSSRH_PW")

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = groupIdVal
            artifactId = artifactIdVal
            version = versionVal?.substring(1) // without v

            from(components["java"])
            pom {
                name.set(pomName)
                description.set(pomDesc)
                url.set(pomUrl)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("dev")
                        name.set("dev")
                        email.set("dev@abc.org")
                    }
                }
                scm {
                    connection.set(pomScmConnection)
                    developerConnection.set(pomDeveloperConnection)
                    url.set(pomScmUrl)
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
            credentials {
                username = ossrhUserName
                password = ossrhPassword
            }
        }
    }
}

signing {
//    val signingKey: String? by project
    val signingKey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
            "\n" +
            "lIYEZEEAVBYJKwYBBAHaRw8BAQdAUE5dt2/tSSoGZfRk8pkzv5/ptIrrJ47oFVMW\n" +
            "0lGuKM/+BwMCHQlO0AzMdWP9Cyd9Vao+CEzFsVL19KAl3FG2KA37+6WOL2Zk5Y+q\n" +
            "UpLXgD3gjLCCv3tDKi/ZprJ2U3Q+uGNa5nFJEXs/33SR/UuVHVvBMbQoamFlc2V1\n" +
            "bmcuYmFlIDxqYWVzZXVuZy5iYWVAbGluZWNvcnAuY29tPoiTBBMWCgA7AhsDBQsJ\n" +
            "CAcCAiICBhUKCQgLAgQWAgMBAh4HAheAFiEEOuNbL5k9bADEqfz1Y5viwoOMffIF\n" +
            "AmRBCYUACgkQY5viwoOMffI/HgD/XALgpWd9g5sTHe+ID/Pev/u17yplAnUfap2O\n" +
            "vrReoVAA/iRPtkB/ZYY+xi+A6QvYMyyWc02WxJpJLmSHcDPDEm4InIsEZEEAVBIK\n" +
            "KwYBBAGXVQEFAQEHQFEKGoWJRHYmDnEsdJ1LDZpbDXDJN/7pX01RMWDosX5EAwEI\n" +
            "B/4HAwJT7uVj661dOf1gctiJgfRleljao2tBXkbJgxkQtTwl7BZVPsOu/2E6oSt5\n" +
            "q+BRyGnnmc0r7vHDSiPSk1UMs18gVx0uWBwOp+sMASN7Tp65iH4EGBYKACYWIQQ6\n" +
            "41svmT1sAMSp/PVjm+LCg4x98gUCZEEAVAIbDAUJA8JnAAAKCRBjm+LCg4x98kqZ\n" +
            "AP9zc279jz3lGJE9w5/jAv1WCSxmxBNPEF0I7oAOdEre5wEAmXWi2jqtCNdozsXm\n" +
            "zR8HpBokNiojIrAMPoOuqBM0EQY=\n" +
            "=8zwV\n" +
            "-----END PGP PRIVATE KEY BLOCK-----\n"
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
