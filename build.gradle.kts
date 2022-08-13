plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    java
    idea
}

group = "cn.mrdear.setter"
version = "0.1.5"

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
    version.set("2021.2")
    plugins.addAll("lombok","java","Kotlin")
    downloadSources.set(true)
}


tasks {
    patchPluginXml {
        // 21代表年,后面的代表季度
        sinceBuild.set("212")
        untilBuild.set("224.*")
        changeNotes.set("""
            0.1.15: add pluginIcon.svg and modify menu 'Setter Convert' to 'Model Convert'
            <br/>
            0.1.14: add message error pop, not error in IDE<br>
            <em>most example in: <a href="https://github.com/mrdear/SetterHelperExample">https://github.com/mrdear/SetterHelperExample</a> </em>        """.trimIndent())
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}