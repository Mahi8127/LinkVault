package com.linkvault;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Temporary database connectivity test runner.
 * Executes on application startup to test the connection to MongoDB Atlas.
 */
@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    private final MongoTemplate mongoTemplate;

    public DatabaseConnectionTest(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("==================================================");
        logger.info("[LinkVault] Testing MongoDB Atlas connectivity...");
        try {
            Document pingResult = mongoTemplate.getDb().runCommand(new Document("ping", 1));
            logger.info("[LinkVault] MongoDB Connection: SUCCESS");
            logger.info("[LinkVault] Connected Database: {}", mongoTemplate.getDb().getName());
            logger.info("[LinkVault] Ping Output: {}", pingResult.toJson());
        } catch (Exception e) {
            String rawMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            // Securely mask any embedded credentials if present in driver error messages
            String safeMessage = rawMessage.replaceAll(":[^@/\\s]+@", ":****@");
            logger.warn("[LinkVault] MongoDB Connection: NOT REACHABLE");
            logger.warn("[LinkVault] Details: {}", safeMessage);
            logger.warn("[LinkVault] Please verify that MONGODB_URI is configured with your valid MongoDB Atlas connection string.");
        }
        logger.info("==================================================");
    }
}
