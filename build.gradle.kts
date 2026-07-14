//file:noinspection GroovyAssignabilityCheck
//file:noinspection GroovyAccessibility
plugins {
	`maven-publish`
	alias(libs.plugins.loom)
	alias(libs.plugins.minotaur)
	alias(libs.plugins.yumi.licenser)
}

val modVersion: String by project
val branchName: String by project
val slug: String by project
val modId: String by project
val modAuthors: String by project
val group: String by project
val user: String by project
val github_slug: String by project
val modDescription: String by project
val license: String by project
val compatibleVersions: String by project
val compatibleLoaders: String by project
val modName: String by project
val contributors: String by project

version = "$modVersion+$branchName"
base.archivesName = slug

repositories {
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
	}
	exclusiveContent {
		forRepository {
			maven {
				name = "Modrinth Maven"
				url = uri("https://api.modrinth.com/maven")
			}
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	minecraft(libs.mc)

	implementation(libs.fl)
	implementation(libs.fapi)
	
	implementation(libs.yumi.commons.core)
	include(libs.yumi.commons.core)
	
	localRuntime(libs.sodium)
	localRuntime(libs.modmenu)
}

loom {
	splitEnvironmentSourceSets()
	
	mods {
		register(modId) {
			sourceSet("main")
			sourceSet("client")
		}
	}
	
	runs {
		getByName("client") {
			client()
			
			source(sourceSets["client"])
		}
	}
}

tasks.processResources {
	val members = if (contributors.isNotEmpty()) {
		". Contributions by ${contributors}."
	} else {
		""
	};
	val meta = mapOf(
		"version"       to version,
		"modId"         to modId,
		"modName"       to modName,
		"modDescription" to modDescription,
		"homepage"      to "https://modrinth.com/mod/${slug}",
		"issues"        to "https://github.com/${user}/${github_slug}/issues",
		"sources"       to "https://github.com/${user}/${github_slug}",
		"license"       to license,
		"authors"       to modAuthors.split(", ").joinToString("\",\n    \""),
		"contributors"  to contributors.split(", ").joinToString("\",\n    \""),
		"members"       to "${modAuthors}${members}",
		"mc"            to compatibleVersions.split(", ")[0],
		"fl"            to libs.versions.fl.get(),
		"fapi"          to libs.versions.fapi.get(),
	)
	inputs.properties(meta)
	filesMatching("*.mod.json") { expand(meta) }
	filesMatching("META-INF/*mods.toml") { expand(meta) }
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release = 25
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}


publishing {
	repositories {
	}
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}
}

modrinth {
	token = "${System.getenv("MODRINTH_TOKEN")}"
	projectId = slug
	versionNumber.set(project.version as String?)
	uploadFile.set(tasks.jar)
	gameVersions = compatibleVersions.split(", ").toList()
	loaders = compatibleLoaders.split(", ").toList()
	changelog = "${System.getenv("CHANGELOG")}"
	syncBodyFrom = "<!--DO NOT EDIT MANUALLY: synced from gh readme-->\n" + rootProject.file("README.md").readText()
	dependencies {
		required.version("fabric-api", libs.versions.fapi.get())
	}
}

tasks.compileJava {
	dependsOn(tasks.applyLicenses)
}

license {
	rule(file("codeformat/HEADER"))
	
	include("**/*.java")
}
