package ch.bbw.pr.tresorbackend.runner;

import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UserService userService;
    private final PasswordEncryptionService passwordEncryptionService;

    // A simple flag to ensure this runs only once, or control via profile/property in a real app
    private static boolean hasRun = false;

    public PasswordMigrationRunner(UserService userService, PasswordEncryptionService passwordEncryptionService) {
        this.userService = userService;
        this.passwordEncryptionService = passwordEncryptionService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (hasRun) {
            logger.info("PasswordMigrationRunner has already been executed. Skipping.");
            return;
        }

        logger.info("Starting password migration for existing users...");

        List<User> users = userService.getAllUsers();
        int migratedCount = 0;

        for (User user : users) {
            String currentPassword = user.getPassword();
            // Check if the password looks like a BCrypt hash (starts with $2a$, $2b$, $2y$)
            // This is a simple check to avoid re-hashing already hashed passwords if the runner executes multiple times by mistake.
            if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$") && !currentPassword.startsWith("$2y$")) {
                logger.info("Migrating password for user: {}", user.getEmail());
                String hashedPassword = passwordEncryptionService.hashPassword(currentPassword);
                user.setPassword(hashedPassword);
                userService.updateUser(user); // Assuming updateUser saves the user
                migratedCount++;
                logger.info("Password for user {} migrated successfully.", user.getEmail());
            } else {
                logger.info("Password for user {} already appears to be hashed or is null. Skipping.", user.getEmail());
            }
        }

        if (migratedCount > 0) {
            logger.info("Password migration completed. {} users were updated.", migratedCount);
        } else {
            logger.info("No passwords needed migration or no users found.");
        }
        hasRun = true;
    }
} 