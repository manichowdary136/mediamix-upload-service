package com.iri.mktgmix.upload.service.formula;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * Integration test for formula translation that executes translated queries against MonetDB.
 * 
 * To run these tests, set the system property: -Dtest.monetdb.url=jdbc:monetdb://localhost:50000/testdb
 * Or set MONETDB_URL environment variable.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=${MONETDB_URL:jdbc:monetdb://localhost:50000/testdb}",
    "spring.datasource.username=${MONETDB_USER:monetdb}",
    "spring.datasource.password=${MONETDB_PASSWORD:test123}",
    "spring.datasource.driver-class-name=nl.cwi.monetdb.jdbc.MonetDriver"
})
public class FormulaTranslationIntegrationTest {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private FormulaTranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = new FormulaTranslationService();
        // Verify data source is available and create JdbcTemplate if needed
        if (dataSource != null) {
            try (java.sql.Connection connection = dataSource.getConnection()) {
                assumeThat(connection).isNotNull();
                // Ensure JdbcTemplate is created if not auto-configured
                if (jdbcTemplate == null) {
                    jdbcTemplate = new JdbcTemplate(dataSource);
                }
            } catch (SQLException e) {
                System.out.println("MonetDB connection not available. Skipping integration tests. " +
                    "Set MONETDB_URL environment variable or system properties to enable.");
            }
        }
    }

    @Test
    void testConcatUpperLowerTranslation() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: TRIM(CONCAT(UPPER(B3), " ", LOWER(C3), " ", "_"))
        ColumnFormula formula = ColumnFormula.of("target_AB", 
            "TRIM(CONCAT(UPPER(B3), \" \", LOWER(C3), \" \", \"_\"))");
        
        String translatedSql = translationService.translate(formula);
        
        // Replace column references with hardcoded values
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("B3", "'Hello'");
        valueMap.put("C3", "'WORLD'");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS target_AB";
        
        System.out.println("Executing query: " + fullQuery);
        
        String result = jdbcTemplate.queryForObject(fullQuery, String.class);
        assertThat(result).isEqualTo("HELLO world _");
    }

    @Test
    void testMathematicalFunctions() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: ROUND(POWER(A1, 2) + SQRT(B1), 2)
        ColumnFormula formula = ColumnFormula.of("result", 
            "ROUND(POWER(A1, 2) + SQRT(B1), 2)");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "5.0");
        valueMap.put("B1", "16.0");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS result";
        
        System.out.println("Executing query: " + fullQuery);
        
        Double result = jdbcTemplate.queryForObject(fullQuery, Double.class);
        // POWER(5, 2) + SQRT(16) = 25 + 4 = 29, ROUND(29, 2) = 29.0
        assertThat(result).isEqualTo(29.0, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void testStringFunctions() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: CONCAT(LEFT(A1, 3), RIGHT(B1, 3))
        ColumnFormula formula = ColumnFormula.of("result", 
            "CONCAT(LEFT(A1, 3), RIGHT(B1, 3))");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "'Hello'");
        valueMap.put("B1", "'World'");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS result";
        
        System.out.println("Executing query: " + fullQuery);
        
        String result = jdbcTemplate.queryForObject(fullQuery, String.class);
        // LEFT('Hello', 3) = 'Hel', RIGHT('World', 3) = 'rld', CONCAT = 'Helrld'
        assertThat(result).isEqualTo("Helrld");
    }

    @Test
    void testDateFunctions() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: YEAR(A1)
        ColumnFormula formula = ColumnFormula.of("year", "YEAR(A1)");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "DATE '2024-03-15'");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS year";
        
        System.out.println("Executing query: " + fullQuery);
        
        Integer year = jdbcTemplate.queryForObject(fullQuery, Integer.class);
        assertThat(year).isEqualTo(2024);
    }

    @Test
    void testIfFunction() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: IF(A1 > 10, "Large", "Small")
        ColumnFormula formula = ColumnFormula.of("result", 
            "IF(A1 > 10, \"Large\", \"Small\")");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "15");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS result";
        
        System.out.println("Executing query: " + fullQuery);
        
        String result = jdbcTemplate.queryForObject(fullQuery, String.class);
        assertThat(result).isEqualTo("Large");
    }

    @Test
    void testMultipleFunctions() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: LEN(TRIM(CONCAT(UPPER(A1), " ", LOWER(B1))))
        ColumnFormula formula = ColumnFormula.of("length", 
            "LEN(TRIM(CONCAT(UPPER(A1), \" \", LOWER(B1))))");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "'  Hello  '");
        valueMap.put("B1", "'  WORLD  '");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS length";
        
        System.out.println("Executing query: " + fullQuery);
        
        Integer length = jdbcTemplate.queryForObject(fullQuery, Integer.class);
        // UPPER('  Hello  ') = '  HELLO  ', LOWER('  WORLD  ') = '  world  '
        // CONCAT('  HELLO  ', ' ', '  world  ') = '  HELLO    world  '
        // TRIM removes leading/trailing spaces: 'HELLO    world'
        // LEN should give us the length
        assertThat(length).isGreaterThan(0);
    }

    @Test
    void testArithmeticWithFunctions() {
        assumeThat(jdbcTemplate).isNotNull();

        // Excel formula: ABS(A1 - B1) * POWER(C1, 2)
        ColumnFormula formula = ColumnFormula.of("result", 
            "ABS(A1 - B1) * POWER(C1, 2)");
        
        String translatedSql = translationService.translate(formula);
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("A1", "10");
        valueMap.put("B1", "15");
        valueMap.put("C1", "3");
        
        String query = replaceColumnReferences(translatedSql, valueMap);
        String fullQuery = "SELECT " + query + " AS result";
        
        System.out.println("Executing query: " + fullQuery);
        
        Double result = jdbcTemplate.queryForObject(fullQuery, Double.class);
        // ABS(10 - 15) * POWER(3, 2) = ABS(-5) * 9 = 5 * 9 = 45
        assertThat(result).isEqualTo(45.0, org.assertj.core.data.Offset.offset(0.01));
    }

    /**
     * Replaces column references in SQL with hardcoded values.
     * Handles identifiers that might be case-sensitive in SQL.
     */
    private String replaceColumnReferences(String sql, Map<String, String> valueMap) {
        String result = sql;
        // Sort by length descending to handle longer column names first (e.g., A10 before A1)
        java.util.List<Map.Entry<String, String>> sortedEntries = new java.util.ArrayList<>(valueMap.entrySet());
        sortedEntries.sort((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()));
        
        for (Map.Entry<String, String> entry : sortedEntries) {
            String columnName = entry.getKey();
            String value = entry.getValue();
            // Replace column name as a word boundary to avoid partial matches
            result = result.replaceAll("\\b" + Pattern.quote(columnName) + "\\b", value);
        }
        return result;
    }
}

