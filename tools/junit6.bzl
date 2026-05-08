load("@rules_java//java:defs.bzl", "java_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

JUNIT_DEPS = [
    artifact("org.junit.jupiter:junit-jupiter-api"),
    artifact("org.junit.jupiter:junit-jupiter-params"),
]

JUNIT_RUNTIME_DEPS = [
    artifact("org.junit.jupiter:junit-jupiter-engine"),
    artifact("org.junit.platform:junit-platform-launcher"),
    artifact("org.junit.platform:junit-platform-console"),
]

def junit6_test(
        name,
        srcs,
        deps = [],
        runtime_deps = [],
        package = None):

    if package == None:
        fail("package must be specified")

    test_class = package + "." + name

    java_test(
        name = name,
        srcs = srcs,

        use_testrunner = False,
        main_class = "org.junit.platform.console.ConsoleLauncher",

        args = [
            "--select-class",
            test_class,
        ],

        deps = deps + JUNIT_DEPS,

        runtime_deps = runtime_deps + JUNIT_RUNTIME_DEPS,

        jvm_flags = [
            "-Djava.security.manager=disallow",
        ],
    )