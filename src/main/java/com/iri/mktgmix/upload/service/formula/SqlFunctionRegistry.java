package com.iri.mktgmix.upload.service.formula;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

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
        strategies.put("CONCAT", arguments -> "CONCAT(" + String.join(", ", arguments) + ")");
        strategies.put("TRIM", arguments -> "TRIM(" + joinArguments(arguments, 1) + ")");
        strategies.put("LTRIM", arguments -> "LTRIM(" + joinArguments(arguments, 1) + ")");
        strategies.put("RTRIM", arguments -> "RTRIM(" + joinArguments(arguments, 1) + ")");
        strategies.put("UPPER", arguments -> "UPPER(" + joinArguments(arguments, 1) + ")");
        strategies.put("LOWER", arguments -> "LOWER(" + joinArguments(arguments, 1) + ")");
        strategies.put("SUBSTRING", arguments -> "SUBSTRING(" + String.join(", ", arguments) + ")");
        strategies.put("COALESCE", arguments -> "COALESCE(" + String.join(", ", arguments) + ")");
        strategies.put("ROUND", arguments -> round(arguments));
        strategies.put("ABS", arguments -> "ABS(" + joinArguments(arguments, 1) + ")");
        strategies.put("FLOOR", arguments -> "FLOOR(" + joinArguments(arguments, 1) + ")");
        strategies.put("CEIL", arguments -> "CEIL(" + joinArguments(arguments, 1) + ")");
        strategies.put("POWER", arguments -> "POWER(" + String.join(", ", arguments) + ")");
        strategies.put("GREATEST", arguments -> "GREATEST(" + String.join(", ", arguments) + ")");
        strategies.put("LEAST", arguments -> "LEAST(" + String.join(", ", arguments) + ")");
        strategies.put("REPLACE", arguments -> "REPLACE(" + String.join(", ", arguments) + ")");
        strategies.put("CAST", arguments -> "CAST(" + arguments.get(0) + " AS " + arguments.get(1) + ")");
        strategies.put("DATE_TRUNC", arguments -> "DATE_TRUNC(" + String.join(", ", arguments) + ")");
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

