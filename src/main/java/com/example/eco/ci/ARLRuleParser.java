package com.example.eco.ci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARLRuleParser {

    public static List<String> getListOfFilesPathsForExtension(String directoryPath, String typeFilter) {
        List<String> arlFilePaths = new ArrayList<>();
        Path dirPath = Paths.get(directoryPath);

        // Filter for ARL files
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, typeFilter)) {
            for (Path entry : stream) {
                arlFilePaths.add(entry.toString()); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arlFilePaths;
    }

    public static String readARLFileToString (String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

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
            "dialect  \"mvel\"\n\n"+
            "rule \"%s\"\n" +
            "salience %s\n" +
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

    public static String getFileNameFromParsedRuleMap (Map<String, String> parsedRule) {
        for (Map.Entry<String, String> entry : parsedRule.entrySet()) {
            if (entry.getKey().equals("name")) {
                return entry.getValue();
            }
        }
        return "unamed.drl";
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

    public static void generateDRLFilesFromARLs () {
        //List of arl file path
        List<String> arlFilePaths = getListOfFilesPathsForExtension("C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\ODM\\Demo\\règles", "*.arl");

        //For each arl file
        for(String arlFilePath: arlFilePaths) {
             //Read the arl file
            String arlRule = readARLFileToString(arlFilePath);

            // Parsing the ARL rule
            Map<String, String> parsedRule = parseARLRule(arlRule);

            // Converting to DRL format
            String drlRule = convertARLToDRL(parsedRule);

            // The path to the file
            String fileName = getFileNameFromParsedRuleMap(parsedRule);
            
            //Construct target path
            String filePath = "C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\migration\\odmtodroolsbis\\src\\main\\resources\\rules\\"+fileName+".drl";
            
            //Write to drl file
            writeDRLStringToFile(drlRule, filePath);
        }
    }

    public static void main(String[] args) {
        generateDRLFilesFromARLs();
    }
}

