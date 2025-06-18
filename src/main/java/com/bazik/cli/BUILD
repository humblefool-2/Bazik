load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "bazik",
    main_class = "com.bazik.cli.BazikCLI",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/com/bazik/core:core",
        "@maven//:commons_cli_commons_cli",
    ],
    resources = [
        "//docs:raw_docs",
    ],
    data = [
        "//docs:raw_docs",
    ],
    visibility = ["//visibility:public"],
)