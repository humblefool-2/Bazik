load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "cli_tests",
    srcs = glob(["*.java"]),
    use_testrunner = False,
    main_class = "org.junit.platform.console.ConsoleLauncher",
    args = [
        "--select-package=com.bazik.cli",
        "--details=tree",
    ],
    deps = [
        "//src/main/java/com/bazik/cli:bazik",
        "//src/main/java/com/bazik/core:core",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_junit_jupiter_junit_jupiter_engine",
        "@maven//:org_junit_platform_junit_platform_console",
        "@maven//:org_junit_platform_junit_platform_launcher",
    ],
)
