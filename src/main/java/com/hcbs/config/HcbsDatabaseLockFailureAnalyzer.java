package com.hcbs.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Explains H2 file-lock errors instead of only showing Hibernate dialect failures.
 */
public class HcbsDatabaseLockFailureAnalyzer extends AbstractFailureAnalyzer<Throwable> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
        if (!isDatabaseFileLocked(cause)) {
            return null;
        }
        return new FailureAnalysis(
                "The H2 database file ./data/hcbs.mv.db is locked by another process.",
                """
                        1. Stop any other running HCBS instance (IDE Run, spring-boot:run, or java -jar).
                        2. On Windows, end stray Java processes if needed: taskkill /F /IM java.exe (only if safe).
                        3. If the project is under OneDrive, wait for sync to finish or pause sync on ./data/.
                        4. As a last resort, stop all HCBS processes, delete ./data/hcbs.lock.db and ./data/hcbs.mv.db, and start again (data will be re-seeded).
                        """,
                cause);
    }

    private boolean isDatabaseFileLocked(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (message.contains("The file is locked")
                    || message.contains("Database may be already in use")
                    || message.contains("Locked by another computer"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
