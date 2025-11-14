package com.iri.mktgmix.upload.service.formula;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry for SQL function strategies. Adding new functions requires only registering a new implementation.
 */
public final class SqlFunctionRegistry {

    private final Map<String, SqlFunction> strategies;

    private SqlFunctionRegistry(Map<String, SqlFunction> strategies) {
        this.strategies = strategies;
    }

    public static SqlFunctionRegistry withDefaults() {
        Map<String, SqlFunction> strategies = new LinkedHashMap<>();

        // String functions - MonetDB equivalents
        strategies.put("CONCAT", args -> "APPEND(" + String.join(", ", args) + ")");
        strategies.put("CONCATENATE", args -> "APPEND(" + String.join(", ", args) + ")");
        strategies.put("TRIM",    args -> "TRIM("    + joinArguments(args, 1) + ")");
        strategies.put("LTRIM",   args -> "LTRIM("   + joinArguments(args, 1) + ")");
        strategies.put("RTRIM",   args -> "RTRIM("   + joinArguments(args, 1) + ")");
        strategies.put("UPPER",   args -> "UPPER("   + joinArguments(args, 1) + ")");
        strategies.put("LOWER",   args -> "LOWER("   + joinArguments(args, 1) + ")");
        strategies.put("LEN",     args -> "LENGTH("  + joinArguments(args, 1) + ")");
        strategies.put("LENGTH",  args -> "LENGTH("  + joinArguments(args, 1) + ")");
        strategies.put("SUBSTRING", args -> "SUBSTRING(" + String.join(", ", args) + ")");
        strategies.put("MID",      args -> "SUBSTRING(" + String.join(", ", args) + ")");
        strategies.put("LEFT",     args -> "LEFT("    + String.join(", ", args) + ")");
        strategies.put("RIGHT",    args -> "RIGHT("   + String.join(", ", args) + ")");
        strategies.put("FIND",     SqlFunctionRegistry::find);
        strategies.put("SEARCH",   SqlFunctionRegistry::find);
        strategies.put("REPLACE",  args -> "REPLACE(" + String.join(", ", args) + ")");
        strategies.put("SUBSTITUTE", args -> "REPLACE(" + String.join(", ", args) + ")");

        // Type conversion and null handling
        strategies.put("COALESCE", args -> "COALESCE(" + String.join(", ", args) + ")");
        strategies.put("CAST",     args -> "CAST(" + args.get(0) + " AS " + args.get(1) + ")");

        // Mathematical functions - MonetDB equivalents
        strategies.put("ABS",      args -> "ABS("    + joinArguments(args, 1) + ")");
        strategies.put("CEIL",     args -> "CEIL("   + joinArguments(args, 1) + ")");
        strategies.put("CEILING",  args -> "CEIL("   + joinArguments(args, 1) + ")");
        strategies.put("FLOOR",    args -> "FLOOR("  + joinArguments(args, 1) + ")");
        strategies.put("ROUND",    SqlFunctionRegistry::round);
        strategies.put("POWER",    args -> "POWER("  + String.join(", ", args) + ")");
        strategies.put("SQRT",     args -> "SQRT("   + joinArguments(args, 1) + ")");
        strategies.put("EXP",      args -> "EXP("    + joinArguments(args, 1) + ")");
        strategies.put("LOG",      args -> "LOG("    + joinArguments(args, 1) + ")");
        strategies.put("LOG10",    args -> "LOG10("  + joinArguments(args, 1) + ")");
        strategies.put("LN",       args -> "LOG("    + joinArguments(args, 1) + ")");
        strategies.put("GREATEST", args -> "GREATEST(" + String.join(", ", args) + ")");
        strategies.put("MAX",      args -> "GREATEST(" + String.join(", ", args) + ")");
        strategies.put("LEAST",    args -> "LEAST("    + String.join(", ", args) + ")");
        strategies.put("MIN",      args -> "LEAST("    + String.join(", ", args) + ")");
        strategies.put("MOD",      args -> "MOD("    + String.join(", ", args) + ")");
        strategies.put("SIGN",     args -> "SIGN("   + joinArguments(args, 1) + ")");
        strategies.put("RAND",     args -> args.isEmpty() ? "RAND()" : "RAND(" + args.get(0) + ")");
        strategies.put("RANDBETWEEN", SqlFunctionRegistry::randBetween);

        // Trigonometric functions
        strategies.put("SIN",      args -> "SIN("    + joinArguments(args, 1) + ")");
        strategies.put("COS",      args -> "COS("    + joinArguments(args, 1) + ")");
        strategies.put("TAN",      args -> "TAN("    + joinArguments(args, 1) + ")");
        strategies.put("ASIN",     args -> "ASIN("   + joinArguments(args, 1) + ")");
        strategies.put("ACOS",     args -> "ACOS("   + joinArguments(args, 1) + ")");
        strategies.put("ATAN",     args -> "ATAN("   + joinArguments(args, 1) + ")");
        strategies.put("ATAN2",    args -> "ATAN2("  + String.join(", ", args) + ")");

        // Date and time functions - MonetDB uses extract()
        strategies.put("YEAR",     args -> "EXTRACT(YEAR FROM " + joinArguments(args, 1) + ")");
        strategies.put("MONTH",    args -> "EXTRACT(MONTH FROM " + joinArguments(args, 1) + ")");
        strategies.put("DAY",      args -> "EXTRACT(DAY FROM " + joinArguments(args, 1) + ")");
        strategies.put("HOUR",     args -> "EXTRACT(HOUR FROM " + joinArguments(args, 1) + ")");
        strategies.put("MINUTE",   args -> "EXTRACT(MINUTE FROM " + joinArguments(args, 1) + ")");
        strategies.put("SECOND",   args -> "EXTRACT(SECOND FROM " + joinArguments(args, 1) + ")");
        strategies.put("NOW",      args -> args.isEmpty() ? "NOW()" : "NOW(" + args.get(0) + ")");
        strategies.put("TODAY",    args -> "CURRENT_DATE");
        strategies.put("CURRENT_DATE", args -> "CURRENT_DATE");
        strategies.put("CURRENT_TIME", args -> "CURRENT_TIME");
        strategies.put("CURRENT_TIMESTAMP", args -> "CURRENT_TIMESTAMP");

        // Logical functions - MonetDB uses CASE WHEN for IF
        strategies.put("IF",       SqlFunctionRegistry::ifFunction);
        strategies.put("AND",      args -> "(" + String.join(" AND ", args) + ")");
        strategies.put("OR",       args -> "(" + String.join(" OR ", args) + ")");
        strategies.put("NOT",      args -> "NOT (" + joinArguments(args, 1) + ")");

        // Comparison functions
        strategies.put("ISNULL",   args -> "(" + joinArguments(args, 1) + " IS NULL)");
        strategies.put("ISBLANK",  args -> "(" + joinArguments(args, 1) + " IS NULL)");
        strategies.put("ISNA",     args -> "(" + joinArguments(args, 1) + " IS NULL)");

        return new SqlFunctionRegistry(Collections.unmodifiableMap(strategies));
    }

    public SqlFunction resolve(String functionName) {
        SqlFunction function = strategies.get(functionName.toUpperCase(Locale.ROOT));
        if (function == null) {
            throw new FormulaTranslationException("Unsupported function: " + functionName);
        }
        return function;
    }

    private static String joinArguments(List<String> arguments, int expectedSize) {
        if (arguments.size() != expectedSize) {
            throw new FormulaTranslationException("Expected " + expectedSize + " arguments but received " + arguments.size());
        }
        return arguments.get(0);
    }

    private static String round(List<String> arguments) {
        if (arguments.isEmpty() || arguments.size() > 2) {
            throw new FormulaTranslationException("ROUND requires one or two arguments");
        }
        if (arguments.size() == 1) {
            return "ROUND(" + arguments.get(0) + ")";
        }
        return "ROUND(" + arguments.get(0) + ", " + arguments.get(1) + ")";
    }

    private static String find(List<String> arguments) {
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new FormulaTranslationException("FIND/SEARCH requires 2 or 3 arguments");
        }
        // Excel FIND(find_text, within_text, start_num)
        // MonetDB: position(substring in string) - returns 1-based position
        // For start_num > 1, we need to use substring to handle offset
        if (arguments.size() == 2) {
            return "POSITION(" + arguments.get(0) + " IN " + arguments.get(1) + ")";
        }
        // With start_num, we need to adjust: position(substring in substring(string from start))
        return "POSITION(" + arguments.get(0) + " IN SUBSTRING(" + arguments.get(1) + ", " + arguments.get(2) + ")) + " + arguments.get(2) + " - 1";
    }

    private static String ifFunction(List<String> arguments) {
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new FormulaTranslationException("IF requires 2 or 3 arguments");
        }
        if (arguments.size() == 2) {
            // IF(condition, value_if_true) - if false, returns NULL
            return "CASE WHEN " + arguments.get(0) + " THEN " + arguments.get(1) + " ELSE NULL END";
        }
        // IF(condition, value_if_true, value_if_false)
        return "CASE WHEN " + arguments.get(0) + " THEN " + arguments.get(1) + " ELSE " + arguments.get(2) + " END";
    }

    private static String randBetween(List<String> arguments) {
        if (arguments.size() != 2) {
            throw new FormulaTranslationException("RANDBETWEEN requires 2 arguments");
        }
        // RANDBETWEEN(low, high) in Excel returns integer between low and high inclusive
        // In MonetDB: floor(rand() * (high - low + 1)) + low
        return "FLOOR(RAND() * (" + arguments.get(1) + " - " + arguments.get(0) + " + 1)) + " + arguments.get(0);
    }
}

