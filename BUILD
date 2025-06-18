load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "checkstyle",
    main_class = "com.puppycrawl.tools.checkstyle.Main",
    runtime_deps = [
        "@maven//:com_puppycrawl_tools_checkstyle",
    ],
)