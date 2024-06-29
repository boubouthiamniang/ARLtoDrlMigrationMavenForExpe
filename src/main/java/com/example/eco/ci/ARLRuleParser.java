package com.example.eco.ci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ARLRuleParser {

    public static Map<String, String> parseARLRule(String ruleText) {
        // Regular expressions for extracting rule components
        Map<String, String> patterns = new HashMap<>();
        patterns.put("name", "rule `(.+?)` \\{");
        patterns.put("priority", "property priority = (\\d+);");
        patterns.put("effective_date", "effectiveDate = new java\\.util\\.Date\\(\"(.+?)\"\\);");
        patterns.put("expiration_date", "expirationDate = new java\\.util\\.Date\\(\"(.+?)\"\\);");
        patterns.put("status", "status = \"(.+?)\";");
        patterns.put("conditions", "when \\{(.*?)\\}");
        patterns.put("actions", "then \\{(.*?)\\}");

        Map<String, String> rule = new HashMap<>();
        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue(), Pattern.DOTALL);
            Matcher matcher = pattern.matcher(ruleText);
            if (matcher.find()) {
                rule.put(entry.getKey(), matcher.group(1).trim());
            }
        }

        return rule;
    }

    public static String convertARLToDRL(Map<String, String> rule) {
        String salience = rule.getOrDefault("priority", "");
        String conditions = rule.getOrDefault("conditions", "");
        String actions = rule.getOrDefault("actions", "");

        // Convert conditions
        conditions = conditions.replace(
            "com.bl.drools.demo.Customer() from $EngineData.this.customer;", 
            "customerObject: Customer()"
        );
        conditions = conditions.replace(
            "evaluate ( $EngineData.this.customer.totalSpending >= 100);", 
            "customerObject: Customer(totalSpending > 100)"
        );

        // Convert actions
        actions = actions.replace(
            "$EngineData.this.customer.discount = 5;", 
            "customerObject.setDiscount(5);"
        );

        String drlRule = String.format(
            "dialect  \"mvel\"\n"+
            "rule \"%s\"\n" +
            "    salience %s\n" +
            "when\n" +
            "    %s\n" +
            "then\n" +
            "    %s\n" +
            "end\n",
            rule.getOrDefault("name", ""),
            salience,
            conditions,
            actions
        );

        return drlRule;
    }

    public static void writeDRLStringToFile (String drlRuleStr, String filePath) {
        try {
            // Convert the file path string to a Path object
            Path path = Paths.get(filePath);

            // Ensure the parent directories exist; create them if they don't
            Files.createDirectories(path.getParent());
            
            // Write the content to the file
            Files.write(path, drlRuleStr.getBytes());
            
            // Print success message
            System.out.println("File written successfully.");
        } catch (IOException e) {
            // Print the exception if an error occurs
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // Example ARL rule string
        String arlRule = 
            "rule `New customer and a big spender at bithday offer` {\n" +
            "  property priority = 8;\n" +
            "  effectiveDate = new java.util.Date(\"6/20/2024 0:00 +0200\");\n" +
            "  expirationDate = new java.util.Date(\"6/23/2024 0:00 +0200\");\n" +
            "  ilog.rules.business_name = \"rule one\";\n" +
            "  ilog.rules.dt = \"\";\n" +
            "  ilog.rules.package_name = \"\";\n" +
            "  status = \"new\";\n" +
            "  when {\n" +
            "    com.bl.drools.demo.Customer() from $EngineData.this.customer;\n" +
            "    evaluate ( $EngineData.this.customer.totalSpending >= 100);\n" +
            "  }\n" +
            "  then {\n" +
            "    $EngineData.this.customer.discount = 5;\n" +
            "  }\n" +
            "}\n";

        // The path to the file
        String filePath = "C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\migration\\odmtodroolsbis\\src\\main\\resources\\rules\\rule.drl";

        // Parsing the ARL rule
        Map<String, String> parsedRule = parseARLRule(arlRule);

        // Converting to DRL format
        String drlRule = convertARLToDRL(parsedRule);

        //Write to drl file
        writeDRLStringToFile(drlRule, filePath);
    }
}

