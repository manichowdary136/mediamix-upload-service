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
        strategies.put("CONCAT", args -> "append(" + String.join(", ", args) + ")");
        strategies.put("CONCATENATE", args -> "append(" + String.join(", ", args) + ")");
        strategies.put("TRIM",    args -> "trim("    + joinArguments(args, 1) + ")");
        strategies.put("LTRIM",   args -> "ltrim("   + joinArguments(args, 1) + ")");
        strategies.put("RTRIM",   args -> "rtrim("   + joinArguments(args, 1) + ")");
        strategies.put("UPPER",   args -> "upper("   + joinArguments(args, 1) + ")");
        strategies.put("LOWER",   args -> "lower("   + joinArguments(args, 1) + ")");
        strategies.put("LEN",     args -> "length("  + joinArguments(args, 1) + ")");
        strategies.put("LENGTH",  args -> "length("  + joinArguments(args, 1) + ")");
        strategies.put("SUBSTRING", args -> "substring(" + String.join(", ", args) + ")");
        strategies.put("MID",      args -> "substring(" + String.join(", ", args) + ")");
        strategies.put("LEFT",     args -> "left("    + String.join(", ", args) + ")");
        strategies.put("RIGHT",    args -> "right("   + String.join(", ", args) + ")");
        strategies.put("FIND",     this::find);
        strategies.put("SEARCH",   this::find);
        strategies.put("REPLACE",  args -> "replace(" + String.join(", ", args) + ")");
        strategies.put("SUBSTITUTE", args -> "replace(" + String.join(", ", args) + ")");

        // Type conversion and null handling
        strategies.put("COALESCE", args -> "coalesce(" + String.join(", ", args) + ")");
        strategies.put("CAST",     args -> "cast(" + args.get(0) + " as " + args.get(1) + ")");

        // Mathematical functions - MonetDB equivalents
        strategies.put("ABS",      args -> "abs("    + joinArguments(args, 1) + ")");
        strategies.put("CEIL",     args -> "ceil("   + joinArguments(args, 1) + ")");
        strategies.put("CEILING",  args -> "ceil("   + joinArguments(args, 1) + ")");
        strategies.put("FLOOR",    args -> "floor("  + joinArguments(args, 1) + ")");
        strategies.put("ROUND",    this::round);
        strategies.put("POWER",    args -> "power("  + String.join(", ", args) + ")");
        strategies.put("SQRT",     args -> "sqrt("   + joinArguments(args, 1) + ")");
        strategies.put("EXP",      args -> "exp("    + joinArguments(args, 1) + ")");
        strategies.put("LOG",      args -> "log("    + joinArguments(args, 1) + ")");
        strategies.put("LOG10",    args -> "log10("  + joinArguments(args, 1) + ")");
        strategies.put("LN",       args -> "log("    + joinArguments(args, 1) + ")");
        strategies.put("GREATEST", args -> "greatest(" + String.join(", ", args) + ")");
        strategies.put("MAX",      args -> "greatest(" + String.join(", ", args) + ")");
        strategies.put("LEAST",    args -> "least("    + String.join(", ", args) + ")");
        strategies.put("MIN",      args -> "least("    + String.join(", ", args) + ")");
        strategies.put("MOD",      args -> "mod("    + String.join(", ", args) + ")");
        strategies.put("SIGN",     args -> "sign("   + joinArguments(args, 1) + ")");
        strategies.put("RAND",     args -> args.isEmpty() ? "rand()" : "rand(" + args.get(0) + ")");
        strategies.put("RANDBETWEEN", this::randBetween);

        // Trigonometric functions
        strategies.put("SIN",      args -> "sin("    + joinArguments(args, 1) + ")");
        strategies.put("COS",      args -> "cos("    + joinArguments(args, 1) + ")");
        strategies.put("TAN",      args -> "tan("    + joinArguments(args, 1) + ")");
        strategies.put("ASIN",     args -> "asin("   + joinArguments(args, 1) + ")");
        strategies.put("ACOS",     args -> "acos("   + joinArguments(args, 1) + ")");
        strategies.put("ATAN",     args -> "atan("   + joinArguments(args, 1) + ")");
        strategies.put("ATAN2",    args -> "atan2("  + String.join(", ", args) + ")");

        // Date and time functions - MonetDB uses extract()
        strategies.put("YEAR",     args -> "extract(year from " + joinArguments(args, 1) + ")");
        strategies.put("MONTH",    args -> "extract(month from " + joinArguments(args, 1) + ")");
        strategies.put("DAY",      args -> "extract(day from " + joinArguments(args, 1) + ")");
        strategies.put("HOUR",     args -> "extract(hour from " + joinArguments(args, 1) + ")");
        strategies.put("MINUTE",   args -> "extract(minute from " + joinArguments(args, 1) + ")");
        strategies.put("SECOND",   args -> "extract(second from " + joinArguments(args, 1) + ")");
        strategies.put("NOW",      args -> args.isEmpty() ? "now()" : "now(" + args.get(0) + ")");
        strategies.put("TODAY",    args -> "current_date");
        strategies.put("CURRENT_DATE", args -> "current_date");
        strategies.put("CURRENT_TIME", args -> "current_time");
        strategies.put("CURRENT_TIMESTAMP", args -> "current_timestamp");

        // Logical functions - MonetDB uses CASE WHEN for IF
        strategies.put("IF",       this::ifFunction);
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
            return "round(" + arguments.get(0) + ")";
        }
        return "round(" + arguments.get(0) + ", " + arguments.get(1) + ")";
    }

    private static String find(List<String> arguments) {
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new FormulaTranslationException("FIND/SEARCH requires 2 or 3 arguments");
        }
        // Excel FIND(find_text, within_text, start_num)
        // MonetDB: position(substring in string) - returns 1-based position
        // For start_num > 1, we need to use substring to handle offset
        if (arguments.size() == 2) {
            return "position(" + arguments.get(0) + " in " + arguments.get(1) + ")";
        }
        // With start_num, we need to adjust: position(substring in substring(string from start))
        return "position(" + arguments.get(0) + " in substring(" + arguments.get(1) + ", " + arguments.get(2) + ")) + " + arguments.get(2) + " - 1";
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
        return "floor(rand() * (" + arguments.get(1) + " - " + arguments.get(0) + " + 1)) + " + arguments.get(0);
    }
}

