import io.insource.build.Publishing
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("java")
  id("signing")
  id("maven-publish")
  id("org.jetbrains.kotlin.jvm") version "1.9.20"
  id("org.jetbrains.kotlin.plugin.spring") version "1.9.20"
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
      name = "github"
      url = uri("https://maven.pkg.github.com/InSourceSoftware/in-spring-zeromq")
      credentials {
        username = project.findProperty("servers.github.username")?.toString() ?: System.getenv("GITHUB_USERNAME")
        password = project.findProperty("servers.github.password")?.toString() ?: System.getenv("GITHUB_PASSWORD")
      }
    }
    maven {
      name = "ossrh"
      url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = project.findProperty("servers.ossrh.username")?.toString() ?: System.getenv("OSSRH_USERNAME")
        password = project.findProperty("servers.ossrh.password")?.toString() ?: System.getenv("OSSRH_PASSWORD")
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

signing {
  sign(publishing.publications["maven"])
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  val jzmqApiVersion = "0.2.0"
  val springBootVersion = "3.2.0"

  implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

  api("org.zeromq:jzmq-api:$jzmqApiVersion")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.springframework.boot:spring-boot-autoconfigure")
  implementation("org.springframework:spring-context")
  implementation("jakarta.annotation:jakarta.annotation-api")
  compileOnly("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
