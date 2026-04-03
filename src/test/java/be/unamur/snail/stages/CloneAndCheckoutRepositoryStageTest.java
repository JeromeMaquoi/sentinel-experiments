package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CloneAndCheckoutRepositoryStage.
 * Validation and "skip existing" tests use no real Git remote.
 * Clone and overwrite tests use a local JGit repository as a stand-in for the remote.
 */
class CloneAndCheckoutRepositoryStageTest {

    @TempDir
    Path tempDir;

    private Context context;
    private Config.RepoConfig repo;

    @BeforeEach
    void setUp() {
        Config config = new Config();
        repo = new Config.RepoConfig();
        config.setRepoForTests(repo);
        Config.setInstanceForTests(config);

        context = new Context();
        context.setLogger(new ConsolePipelineLogger(CloneAndCheckoutRepositoryStageTest.class));
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    // ── getRepoDir ────────────────────────────────────────────────────────────

    @Test
    void getRepoDirTest() {
        assertEquals("my-repo-dir", new CloneAndCheckoutRepositoryStage("my-repo-dir").getRepoDir());
    }

    // ── Config validation ─────────────────────────────────────────────────────

    /**
     * Parameterized test covering every missing-config combination that should
     * cause an IllegalArgumentException before any network activity.
     */
    @ParameterizedTest(name = "url={0}, commit={1}, targetDir={2} → IllegalArgumentException")
    @MethodSource("missingConfigProvider")
    void throwsIllegalArgumentExceptionWhenConfigMissingTest(
            String url, String commit, String targetDir) {
        repo.setUrlForTests(url);
        repo.setCommitForTests(commit);
        repo.setTargetDirForTests(targetDir);

        CloneAndCheckoutRepositoryStage stage = new CloneAndCheckoutRepositoryStage("repo");
        assertThrows(IllegalArgumentException.class, () -> stage.execute(context));
    }

    static Stream<Arguments> missingConfigProvider() {
        String validUrl    = "https://example.com/repo.git";
        String validCommit = "abc123";
        String validDir    = "/tmp";
        return Stream.of(
            Arguments.of(null,     validCommit, validDir),   // missing URL
            Arguments.of(validUrl, null,        validDir),   // missing commit
            Arguments.of(validUrl, validCommit, null)        // missing target-dir
        );
    }

    // ── Skip when directory already exists ───────────────────────────────────

    @Test
    void skipsCloneWhenDirectoryAlreadyExistsAndNoOverwriteTest() throws IOException {
        String repoDir = "cloned-repo";
        Path existingDir = tempDir.resolve(repoDir);
        Files.createDirectories(existingDir);

        repo.setUrlForTests("https://example.com/repo.git");
        repo.setCommitForTests("abc123");
        repo.setTargetDirForTests(tempDir + "/");
        repo.setOverwriteForTests(false);

        new CloneAndCheckoutRepositoryStage(repoDir).execute(context);

        assertEquals(existingDir.toString(), context.getRepoPath());
        assertEquals("abc123", context.getCommit());
    }

    // ── Clone a fresh repository ──────────────────────────────────────────────

    @Test
    void clonesRepositoryAndChecksOutCommitTest() throws Exception {
        String commitHash = initLocalRepo(tempDir.resolve("source-repo"), "README.md", "hello");

        Path targetBase = tempDir.resolve("target");
        Files.createDirectories(targetBase);
        String repoDir = "cloned-repo";

        repo.setUrlForTests(tempDir.resolve("source-repo").toUri().toString());
        repo.setCommitForTests(commitHash);
        repo.setTargetDirForTests(targetBase + "/");
        repo.setOverwriteForTests(false);

        new CloneAndCheckoutRepositoryStage(repoDir).execute(context);

        Path expectedCloneDir = targetBase.resolve(repoDir);
        assertEquals(expectedCloneDir.toString(), context.getRepoPath());
        assertEquals(commitHash, context.getCommit());
        assertTrue(Files.exists(expectedCloneDir.resolve("README.md")));
    }

    // ── Overwrite an existing directory and re-clone ──────────────────────────

    @Test
    void deletesExistingDirectoryAndReclonesWhenOverwriteEnabledTest() throws Exception {
        String commitHash = initLocalRepo(tempDir.resolve("source-repo-overwrite"), "README.md", "hello overwrite");

        Path targetBase = tempDir.resolve("target-overwrite");
        String repoDir = "existing-repo";
        Path existingDir = targetBase.resolve(repoDir);

        // Populate existing directory with a stale file
        Files.createDirectories(existingDir);
        Files.writeString(existingDir.resolve("old-file.txt"), "stale content");

        repo.setUrlForTests(tempDir.resolve("source-repo-overwrite").toUri().toString());
        repo.setCommitForTests(commitHash);
        repo.setTargetDirForTests(targetBase + "/");
        repo.setOverwriteForTests(true);

        new CloneAndCheckoutRepositoryStage(repoDir).execute(context);

        assertFalse(Files.exists(existingDir.resolve("old-file.txt")), "Stale file should have been deleted");
        assertTrue(Files.exists(existingDir.resolve("README.md")), "Cloned file should be present");
        assertEquals(existingDir.toString(), context.getRepoPath());
        assertEquals(commitHash, context.getCommit());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Initialises a local Git repository in {@code repoDir}, writes {@code fileName}
     * with {@code content}, commits it, and returns the full commit SHA-1 hash.
     */
    private String initLocalRepo(Path repoDir, String fileName, String content) throws Exception {
        Files.createDirectories(repoDir);
        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Files.writeString(repoDir.resolve(fileName), content);
            git.add().addFilepattern(fileName).call();
            RevCommit commit = git.commit()
                    .setMessage("initial commit")
                    .setAuthor("tester", "tester@test.com")
                    .call();
            return commit.getName();
        }
    }
}

