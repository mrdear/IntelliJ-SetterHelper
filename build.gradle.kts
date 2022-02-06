plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    java
}

group = "cn.mrdear.setter"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    // 修改为本地安装目录
    localPath.set("/Users/quding/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/213.6777.52/IntelliJ IDEA.app")
    plugins.addAll("lombok")
}

tasks {
    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}