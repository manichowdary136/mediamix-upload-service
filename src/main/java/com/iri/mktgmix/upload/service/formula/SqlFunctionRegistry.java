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

        strategies.put("CONCAT", args -> "concat(" + String.join(", ", args) + ")");

        strategies.put("TRIM",    args -> "trim("    + joinArguments(args, 1) + ")");
        strategies.put("LTRIM",   args -> "ltrim("   + joinArguments(args, 1) + ")");
        strategies.put("RTRIM",   args -> "rtrim("   + joinArguments(args, 1) + ")");
        strategies.put("UPPER",   args -> "upper("   + joinArguments(args, 1) + ")");
        strategies.put("LOWER",   args -> "lower("   + joinArguments(args, 1) + ")");
        strategies.put("SUBSTRING", args -> "substring(" + String.join(", ", args) + ")");
        strategies.put("REPLACE", args -> "replace(" + String.join(", ", args) + ")");

        strategies.put("COALESCE", args -> "coalesce(" + String.join(", ", args) + ")");
        strategies.put("CAST",     args -> "cast(" + args.get(0) + " as " + args.get(1) + ")");

        strategies.put("ABS",      args -> "abs("    + joinArguments(args, 1) + ")");
        strategies.put("CEIL",     args -> "ceil("   + joinArguments(args, 1) + ")");
        strategies.put("CEILING",  args -> "ceil("   + joinArguments(args, 1) + ")");

        strategies.put("FLOOR",    args -> "floor("  + joinArguments(args, 1) + ")");
        strategies.put("ROUND",    this::round);
        strategies.put("POWER",    args -> "power("  + String.join(", ", args) + ")");
        strategies.put("GREATEST", args -> "greatest(" + String.join(", ", args) + ")");
        strategies.put("LEAST",    args -> "least("    + String.join(", ", args) + ")");
        strategies.put("MOD",      args -> "mod("    + String.join(", ", args) + ")");
        strategies.put("SIGN",     args -> "sign("   + joinArguments(args, 1) + ")");
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
}

