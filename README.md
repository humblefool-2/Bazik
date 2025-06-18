# Initialize and download dependencies
```shell
bazel mod deps
```

# Build the entire project
```shell
bazel build //...
```

# Run the CLI

### For simple function as task dependencies
```shell
bazel run //src/main/java/com/bazik/cli:bazik test
bazel run //src/main/java/com/bazik/cli:bazik compile
bazel run //src/main/java/com/bazik/cli:bazik link
bazel run //src/main/java/com/bazik/cli:bazik secondLink
```

### For files as task dependencies
```shell
bazel run //src/main/java/com/bazik/cli:bazik list_documents
bazel run //src/main/java/com/bazik/cli:bazik process_documents
bazel run //src/main/java/com/bazik/cli:bazik analyze_documents
```

### For visualising the graph on console
```shell
bazel run //src/main/java/com/bazik/cli:bazik graph analyze_documents
bazel run //src/main/java/com/bazik/cli:bazik graph test
```

# Run tests
```shell
bazel test //...

# Run CLI tests
bazel test //src/test/java/com/bazik/cli:cli_tests

# Run Core Directory tests
bazel test //src/test/java/com/bazik/core:core_tests
```

# Checkstyle
```shell
bazel run //:checkstyle -- -c //:checkstyle.xml $(find src -name "*.java")
```


# TODO
* Verify console outputs
* Add support for persistent cache
* Beautify the graph output and support various formats
* Refactoring possible