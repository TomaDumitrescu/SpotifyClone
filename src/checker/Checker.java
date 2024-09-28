package checker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Checker {
    private static int gitScore;
    private static int readmeScore;
    private static int totalScore = 0;

    private Checker() {
    }

    private static void calculateScoreGit() {
        System.out.print(".GIT score = ");

        Path path = Paths.get(".git");
        if (Files.exists(path)) {
            System.out.println("PASS");
        } else {
            System.out.println("ERROR");
        }
    }

    private static void calculateScoreReadme() {
        System.out.println("-----------------------------------------------------");
        System.out.print("README ");
        Path path1 = Paths.get("README");
        Path path2 = Paths.get("README.md");
        Path path3 = Paths.get("README.txt");

        if (Files.exists(path1) || Files.exists(path2) || Files.exists(path3)) {
            System.out.println("PRESENT");

        } else {
            System.out.println("NOT PRESENT");
        }
    }

    /**
     * This method is used to calculate total score of the implementation and checkstyle
     */
    public static void calculateScore() throws IOException {
        System.out.println();
        calculateScoreAllTests();
        int checkstyleScore = calculateScoreCheckstyle();
        calculateScoreGit();
        calculateScoreReadme();

        int finalScore = totalScore + gitScore + readmeScore + checkstyleScore;
    }

    /**
     * This method is used to calculate the score of checkstyle
     */
    private static int calculateScoreCheckstyle() {
        return Checkstyle.testCheckstyle();
    }

    /**
     * This method is used to calculate score of implementation
     *
     * 18 tests (80 points maximum)
     */
    private static void calculateScoreAllTests() throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        List<String> listFile = Arrays.asList(Objects.requireNonNull(directory.list()));
        Collections.sort(listFile);
        for (String file : listFile) {
            if (file.startsWith("library")) {
                continue;
            }

            totalScore += calculateScore(file);
        }
    }

    /**
     * This method calculates the score of only one single test
     * @return the score of that test
     */
    public static int calculateScore(final String input) {
        if (checkOutput(input)) {
            System.out.print(input + " ");
            for (int i = 1;  i <= CheckerConstants.LEN_LONGEST_TEST_NAME - input.length(); i++) {
                System.out.print("-");
            }
            System.out.println("--------------------------------------------- PASSED");
            return getScoreForTest(input);
        } else {
            System.out.print(input + " ");
            for (int i = 1; i <= CheckerConstants.LEN_LONGEST_TEST_NAME - input.length(); i++) {
                System.out.print("-");
            }
            System.out.println("--------------------------------------------- FAILED");
            return 0;
        }
    }

    /**
     * @param file the test you want to check
     * @return
     *          if the two files are equal or not
     */
    private static boolean checkOutput(final String file) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode output = mapper.readTree(new File(CheckerConstants.OUT_PATH + file));
            JsonNode ref = mapper.readTree(new File(CheckerConstants.REF_PATH + file));
            return output.equals(ref);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param input the test you want to calculate score for
     * @return  the score of that test
     */
    private static int getScoreForTest(final String input) {
        if (input.contains(CheckerConstants.ERROR_TEST)) {
            return CheckerConstants.ERROR_TEST_POINTS;
        }

        if (input.contains(CheckerConstants.COMPLEX_TEST)) {
            return CheckerConstants.COMPLEX_TEST_POINTS;
        }

        return CheckerConstants.SIMPLE_TEST_POINTS;
    }
}
