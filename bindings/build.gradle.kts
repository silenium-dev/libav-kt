plugins {
    java
    `maven-publish`
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}
