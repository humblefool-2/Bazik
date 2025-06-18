package com.bazik.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

// Outputs printed on console
public class BazikCLITest {
    @Test
    void testBazikWithHelpOption() {
        String[] args = new String[]{"--help"};
        BazikCLI.main(args);
    }

    @Test
    void testBazikWithNoArgs() {
        String[] args = new String[]{};
        BazikCLI.main(args);
    }


    @Test
    void testBuildTasks() {
        String[] args = new String[]{"compile"};
        BazikCLI.main(args);
    }

    @Test
    void testDocumentTasks() {
        String[] args = new String[]{"process_documents"};
        BazikCLI.main(args);
    }

    @Test
    void testBazikWithInvalidTarget() {
        String[] args = new String[]{"invalidTarget"};
        assertThrows(NullPointerException.class, () -> BazikCLI.main(args));
    }

    @Test
    void testGraphWithInvalidTarget() {
        String[] args = new String[]{"graph"};
        assertThrows(IllegalArgumentException.class, () -> BazikCLI.main(args));
    }

    @Test
    void testGraphWithValidTarget() {
        String[] args = new String[]{"graph test"};
        BazikCLI.main(args);
    }
}
