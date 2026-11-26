package cake.web.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import cake.web.configuration.CakeWebConfig;
import cake.web.configuration.CakeWebRegistry;

/**
 * Security tests for {@link CakeWebRegistry} verifying base package restrictions.
 */
class CakeWebRegistrySecurityTest {

    @AfterEach
    void tearDown() {
        CakeWebRegistry.reset();
    }

    @Test
    void shouldRejectClassesOutsideBasePackage() {
        // Arrange
        CakeWebConfig config = new CakeWebConfig();
        config.setBasePackage("com.bank.loan");
        CakeWebRegistry.initialize(config);

        // Act + Assert
        assertTrue(CakeWebRegistry.isClassLoadAllowed("com.bank.loan.Customer"));
        assertTrue(CakeWebRegistry.isClassLoadAllowed("com.bank.loan.capture.Address"));

        assertFalse(CakeWebRegistry.isClassLoadAllowed("com.attacker.Evil"));
        assertFalse(CakeWebRegistry.isClassLoadAllowed("java.lang.System"));
        assertFalse(CakeWebRegistry.isClassLoadAllowed("org.springframework.Context"));
    }

    @Test
    void shouldRejectPartialPackageMatch() {
        // Arrange
        CakeWebConfig config = new CakeWebConfig();
        config.setBasePackage("com.bank.loan");
        CakeWebRegistry.initialize(config);

        // Act + Assert: "com.bank.loans" must NOT match "com.bank.loan"
        assertFalse(CakeWebRegistry.isClassLoadAllowed("com.bank.loans.Customer"));
        assertFalse(CakeWebRegistry.isClassLoadAllowed("com.bank.loanx.Customer"));
    }

    @Test
    void shouldAllowEverythingWhenNoBasePackageConfigured() {
        // Arrange
        CakeWebConfig config = new CakeWebConfig();
        // basePackage defaults to ""
        CakeWebRegistry.initialize(config);

        // Act + Assert
        assertTrue(CakeWebRegistry.isClassLoadAllowed("com.bank.loan.Customer"));
        assertTrue(CakeWebRegistry.isClassLoadAllowed("java.lang.System"));
        assertTrue(CakeWebRegistry.isClassLoadAllowed("anything"));
    }

    @Test
    void shouldThrowWhenInitializedTwice() {
        // Arrange
        CakeWebRegistry.initialize(new CakeWebConfig());

        // Act + Assert
        assertThrows(IllegalStateException.class,
            () -> CakeWebRegistry.initialize(new CakeWebConfig()));
    }

    @Test
    void shouldResetCorrectly() {
        // Arrange
        CakeWebConfig config = new CakeWebConfig();
        config.setBasePackage("com.bank.loan");
        CakeWebRegistry.initialize(config);

        // Act
        CakeWebRegistry.reset();

        // Assert
        assertEquals("", CakeWebRegistry.basePackage());
    }
}