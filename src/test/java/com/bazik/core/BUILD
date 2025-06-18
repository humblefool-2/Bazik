load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "core_tests",
    srcs = glob(["*.java"]),
    use_testrunner = False,
    main_class = "org.junit.platform.console.ConsoleLauncher",
    args = [
        "--select-package=com.bazik.core",
        "--details=tree",
    ],
    deps = [
        "//src/main/java/com/bazik/core:core",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_junit_jupiter_junit_jupiter_engine",
        "@maven//:org_junit_platform_junit_platform_console",
        "@maven//:org_junit_platform_junit_platform_launcher",
    ],
)