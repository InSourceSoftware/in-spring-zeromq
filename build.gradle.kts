import io.insource.build.Publishing
import io.insource.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("java")
  id("maven")
  id("signing")
  id("maven-publish")
  id("org.jetbrains.kotlin.jvm") version "1.3.50"
  id("org.jetbrains.kotlin.plugin.spring") version "1.3.50"
}

tasks.register<Jar>("sourcesJar") {
  from(sourceSets.main.get().allSource)
  archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
  from(tasks.javadoc)
  archiveClassifier.set("javadoc")
}

publishing {
  repositories {
    maven {
      url = uri("https://maven.pkg.github.com/InSourceSoftware/in-spring-zeromq")
      credentials {
        username = project.findProperty("servers.github.username") ?: System.getenv("GITHUB_USERNAME")
        password = project.findProperty("servers.github.password") ?: System.getenv("GITHUB_PASSWORD")
      }
    }
  }

  publications {
    create<MavenPublication>("maven") {
      groupId = Publishing.groupId
      version = Publishing.version

      from(components["kotlin"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])

      pom {
        name.set(Publishing.artifactId)
        description.set(Publishing.description)
        url.set(Publishing.url)
        licenses {
          license {
            name.set(Publishing.license)
            url.set(Publishing.licenseUrl)
          }
        }
        developers {
          developer {
            id.set(Publishing.developerUserName)
            name.set(Publishing.developerFullName)
            email.set(Publishing.developerEmailAddress)
          }
        }
        scm {
          connection.set(Publishing.connectionUrl)
          developerConnection.set(Publishing.developerConnectionUrl)
          url.set(Publishing.url)
        }
      }
    }
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  val springBootVersion = Versions.springBoot
  val jzmqApiVersion = Versions.jzmqApi

  implementation(dependencies.platform("org.springframework.boot:spring-boot-parent:$springBootVersion"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.springframework.boot:spring-boot-autoconfigure")
  implementation("org.springframework:spring-context")
  implementation("org.zeromq:jzmq-api:$jzmqApiVersion")
  compileOnly("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }
}