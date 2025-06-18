package com.bazik.cli;

import com.bazik.core.BuildGraph;
import com.bazik.core.Task;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BazikCLI {
    private final BuildGraph graph;
    private final Path workspaceDir;

    public BazikCLI() {
        this.graph = new BuildGraph();
        this.workspaceDir = resolveWorkspaceDirectory();
        initializeTasks();
    }

    private Path resolveWorkspaceDirectory() {
        String workspace = System.getenv("BUILD_WORKSPACE_DIRECTORY");
        return workspace != null ?
                Paths.get(workspace) :
                Paths.get(System.getProperty("user.dir"));
    }

    private void initializeTasks() {
        registerBuildTasks();
        registerDocumentTasks();
    }

    private void registerBuildTasks() {
        // Create build-related tasks
        Task<String, String> compileTask = new Task<>("compile",
                inputs -> "Compiled",
                Collections.emptyList());

        Task<String, String> linkTask = new Task<>("link",
                inputs -> "Linked Compiled",
                List.of("compile"));

        Task<String, String> secondLinkTask = new Task<>("secondLink",
                inputs -> "SecondLinked Compiled",
                List.of("compile"));

        Task<String, String> testTask = new Task<>("test",
                inputs -> "Tested SecondLinked Linked Compiled",
                List.of("link", "secondLink", "compile"));

        // Register all build tasks
        registerTasks(compileTask, linkTask, secondLinkTask, testTask);
    }

    private void registerDocumentTasks() {
        // Create document processing tasks
        Task<Object, List<Path>> listDocumentsTask = new Task<>(
                "list_documents",
                inputs -> listDocuments(),
                Collections.emptyList()
        );

        Task<Object, List<Path>> processDocumentsTask = new Task<>(
                "process_documents",
                inputs -> processDocuments((List<Path>) inputs.get(0)),
                List.of("list_documents")
        );

        Task<Object, List<Integer>> analyzeDocumentsTask = new Task<>(
                "analyze_documents",
                inputs -> analyzeDocuments((List<Path>) inputs.get(0)),
                List.of("process_documents")
        );

        // Register all document tasks
        registerTasks(listDocumentsTask, processDocumentsTask, analyzeDocumentsTask);
    }

    private void registerTasks(Task<?, ?>... tasks) {
        Arrays.stream(tasks).forEach(graph::addTask);
    }

    private List<Path> listDocuments() {
        Path rawDocsPath = workspaceDir.resolve("docs/raw");
        System.out.println("Looking for documents in: " + rawDocsPath);

        if (!Files.exists(rawDocsPath)) {
            System.err.println("Directory not found: " + rawDocsPath);
            return Collections.emptyList();
        }

        try {
            return Files.list(rawDocsPath)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logError("Error listing documents", e);
            return Collections.emptyList();
        }
    }

    private List<Path> processDocuments(List<Path> filesToProcess) {
        Path processedDir = createDirectory("docs/processed");
        if (processedDir == null) return Collections.emptyList();

        return filesToProcess.stream()
                .map(inputPath -> processDocument(inputPath, processedDir))
                .filter(path -> path != null)
                .collect(Collectors.toList());
    }

    private Path processDocument(Path inputPath, Path processedDir) {
        try {
            String content = Files.readString(inputPath);
            String processedContent = content.replaceAll("\\s+", " ").trim();
            Path processedPath = processedDir.resolve(inputPath.getFileName().toString());
            Files.writeString(processedPath, processedContent);
            System.out.println("Processed file: " + inputPath + " -> " + processedPath);
            return processedPath;
        } catch (IOException e) {
            logError("Error processing document " + inputPath, e);
            return null;
        }
    }

    private List<Integer> analyzeDocuments(List<Path> filesToAnalyze) {
        Path analyzedDir = createDirectory("docs/analyzed");
        if (analyzedDir == null) return Collections.emptyList();

        return filesToAnalyze.stream()
                .map(inputPath -> analyzeDocument(inputPath, analyzedDir))
                .filter(count -> count != null)
                .collect(Collectors.toList());
    }

    private Integer analyzeDocument(Path inputPath, Path analyzedDir) {
        try {
            String content = Files.readString(inputPath);
            int wordCount = content.split("\\s+").length;
            String filename = inputPath.getFileName().toString().replace(".txt", "_analysis.txt");
            Path analysisPath = analyzedDir.resolve(filename);
            Files.writeString(analysisPath, "Word Count: " + wordCount);
            System.out.println("Analyzed file: " + inputPath + " -> " + analysisPath +
                    " (Word count: " + wordCount + ")");
            return wordCount;
        } catch (IOException e) {
            logError("Error analyzing document " + inputPath, e);
            return null;
        }
    }

    private Path createDirectory(String path) {
        Path dir = workspaceDir.resolve(path);
        try {
            Files.createDirectories(dir);
            System.out.println("Created directory: " + dir);
            return dir;
        } catch (IOException e) {
            logError("Error creating directory " + path, e);
            return null;
        }
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public static void main(String[] args) {
        CommandLineOptions cliOptions = new CommandLineOptions();
        try {
            String targetName = cliOptions.parseArgs(args);
            if (targetName != null) {
                new BazikCLI().executeTarget(targetName);
            }
        } catch (ParseException e) {
            cliOptions.handleError(e);
        }
    }

    /**
     * Executes the specified target or visualizes its build graph.
     * If the target is prefixed with "graph ", it will visualize the build graph for the specified task.
     * Otherwise, it will execute the target task.
     *
     * @param targetName The name of the target to execute or visualize (may be prefixed with "graph ")
     */
    private void executeTarget(String targetName) {
        System.out.println(targetName);
        if (targetName.startsWith("graph ")) {
            visualizeTaskGraph(targetName);
        } else {
            executeTask(targetName);
        }
    }

    private void visualizeTaskGraph(String graphCommand) {
        String taskName = graphCommand.substring("graph ".length()).trim();
        if (taskName.isEmpty()) {
            System.err.println("Error: No task name provided after 'graph' command.");
            return;
        }

        BuildGraphVisualizer visualizer = new BuildGraphVisualizer(graph);
        visualizer.visualize(taskName);
    }

    private void executeTask(String taskName) {
        System.out.println("Executed " + taskName + ": " + graph.executeTarget(taskName));
    }
}

class CommandLineOptions {
    private final Options options;
    private final CommandLineParser parser;
    private final HelpFormatter formatter;

    public CommandLineOptions() {
        this.options = new Options();
        this.options.addOption("h", "help", false, "Display help information");
        this.parser = new DefaultParser();
        this.formatter = new HelpFormatter();
    }

    public String parseArgs(String[] args) throws ParseException {
        CommandLine cmd = parser.parse(options, args, true);

        if (cmd.hasOption("help") || args.length == 0) {
            printHelp();
            return null;
        }

        List<String> positionalArgs = cmd.getArgList();
        if (positionalArgs.isEmpty()) {
            throw new ParseException("Target task is required");
        }

        // Special handling for "graph" command
        if (positionalArgs.get(0).equals("graph")) {
            if (positionalArgs.size() < 2) {
                throw new IllegalArgumentException("Task name is required after 'graph' command");
            }
            return "graph " + positionalArgs.get(1);
        }

        return positionalArgs.get(0);
    }

    private void printHelp() {
        formatter.printHelp("bazik <target> | bazik graph <target>",
                "Bazik Build Tool",
                options,
                "\nExamples:\n  bazik compile\n  bazik graph test",
                true);
    }

    public void handleError(ParseException e) {
        System.err.println("Error: " + e.getMessage());
        printHelp();
        System.exit(1);
    }
}