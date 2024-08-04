plugins {
    id("org.jetbrains.intellij") version "1.5.2"
    java
    idea
}

group = "cn.mrdear.setter"
version = "0.1.17"

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
    version.set("2022.1")
    plugins.addAll("lombok","java","Kotlin")
    downloadSources.set(true)
}


tasks {
    patchPluginXml {
        // 22代表年,后面的代表季度
        sinceBuild.set("221")
        untilBuild.set("244.*")
        changeNotes.set("""
            0.1.17: update intellij 24.*
            <br/>
            0.1.16: update intellij 23.*
            <br/>
            0.1.15: add pluginIcon.svg and modify menu 'Setter Convert' to 'Model Convert'
            <br/>
            0.1.14: add message error pop, not error in IDE<br>
            <em>most example in: <a href="https://github.com/mrdear/SetterHelperExample">https://github.com/mrdear/SetterHelperExample</a> </em>        """.trimIndent())
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}