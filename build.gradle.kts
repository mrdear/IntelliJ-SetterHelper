plugins {
    id("java")
    id("idea")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "cn.mrdear.setter"
version = "0.1.18"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    // 修改为本地安装目录
    version.set("2023.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("lombok","java","Kotlin"))

}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        // 22代表年,后面的代表季度
        sinceBuild.set("231")
        untilBuild.set("254.*")
    }
}
