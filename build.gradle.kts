plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    java
    idea
}

group = "cn.mrdear.setter"
version = "0.0.4-SNAPSHOT"

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
    version.set("2021.2.4")
    plugins.addAll("lombok","java")
    downloadSources.set(true)
}


tasks {
    patchPluginXml {
        // 21代表年,后面的代表季度
        sinceBuild.set("211")
        untilBuild.set("221.*")
        changeNotes.set("""
            init plugin version .<br>
            <em>most example in https://github.com/mrdear/SetterHelperExample </em>        """.trimIndent())
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}