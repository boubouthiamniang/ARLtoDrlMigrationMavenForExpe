package com.example.eco.ci;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ARLFlowParser {

    public static List<Map<String, Object>> extractRuleInfoFromFlowBrl () {
        // List to hold all rule task maps
        List<Map<String, Object>> ruleTaskMaps = new ArrayList<>();

        try {
            // Load the BRL file (which contains XML content)
            File inputFile = new File("C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\ODM\\Demo\\règles\\Flow A.rfl");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Get all RuleTasks
            NodeList ruleTaskList = doc.getElementsByTagName("RuleTask");

            // Iterate over each RuleTask
            for (int i = 0; i < ruleTaskList.getLength(); i++) {
                Node ruleTaskNode = ruleTaskList.item(i);

                if (ruleTaskNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element ruleTaskElement = (Element) ruleTaskNode;
                    String ruleTaskId = ruleTaskElement.getAttribute("Identifier");
                    String executionMode = ruleTaskElement.getAttribute("ExecutionMode");
                    String exitCriteria = ruleTaskElement.getAttribute("ExitCriteria");
                    String ordering = ruleTaskElement.getAttribute("Ordering");

                    // Create a map to store rule task details
                    Map<String, Object> ruleTaskMap = new HashMap<>();
                    ruleTaskMap.put("Identifier", ruleTaskId);
                    ruleTaskMap.put("ExecutionMode", executionMode);
                    ruleTaskMap.put("ExitCriteria", exitCriteria);
                    ruleTaskMap.put("Ordering", ordering);

                    // Get the RuleList within the current RuleTask
                    NodeList ruleList = ruleTaskElement.getElementsByTagName("Rule");
                    List<String> rules = new ArrayList<>();

                    // Iterate over each Rule in the RuleList
                    for (int j = 0; j < ruleList.getLength(); j++) {
                        Node ruleNode = ruleList.item(j);

                        if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element ruleElement = (Element) ruleNode;
                            String ruleUuid = ruleElement.getAttribute("Uuid");
                            rules.add(ruleUuid);
                        }
                    }

                    // Add the list of rules to the rule task map
                    ruleTaskMap.put("Rules", rules);

                    // Add the rule task map to the list of rule task maps
                    ruleTaskMaps.add(ruleTaskMap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ruleTaskMaps;
    }

    public static String convertFlowToDRL(Map<String, String> rule, String salience) {
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

    public static String findBrlFilesWithUUID(String uuidToFind) {
        String directoryPath = "C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\ODM\\Demo\\règles\\";
        final String[] pathOfFoundFile = {""}; // Use an array because lambda require final if simple String, but we need to assign new value

        try {
            // Create a Path object for the directory
            Path directory = Paths.get(directoryPath);

            // Traverse the directory recursively
            try (Stream<Path> paths = Files.walk(directory)) {
                paths
                    .filter(Files::isRegularFile) // Filter regular files
                    .filter(path -> path.toString().endsWith(".brl")) // Filter BRL files
                    .forEach(path -> {
                        try {
                            // Read the content of the file as a string
                            String content = new String(Files.readAllBytes(path));

                            // Check if the content contains the UUID
                            String uuidValue = extractNodeValue(content, "<uuid>", "</uuid>");
                            if (uuidValue.equals(uuidToFind)) {
                                System.out.println("Found in file: " + path.toString());
                                System.out.println("UUID value: " + uuidValue);

                                // Extract the value of <name> node
                                pathOfFoundFile[0] = path.toString();
                            }

                          
                            
                            //System.out.println("Found in file: " + path.toString() + ", Name: " + ruleName);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathOfFoundFile[0];
    }

    public static String transformBRLPathToARLPath(String brlPath) {
        if (brlPath.endsWith(".brl")) {
            brlPath = brlPath.substring(0, brlPath.length() - 4) + ".arl";
        }
        return brlPath; 
    }

    //To add in utile class
    public static String extractNodeValue(String xmlContent, String nodeOpenName, String nodeCloseName) {
        int startIdx = xmlContent.indexOf(nodeOpenName);
        int endIdx = xmlContent.indexOf(nodeCloseName);
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return xmlContent.substring(startIdx + nodeOpenName.length(), endIdx).trim();
        }
        return "";
    }

    public static void main(String[] args) {
        List<Map<String, Object>> ruleTaskMaps = extractRuleInfoFromFlowBrl ();

        int initSalience = ruleTaskMaps.size();

        String drlRule =  "dialect  \"mvel\"\n\n";

        // Print all rule task maps
        for (Map<String, Object> map : ruleTaskMaps) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equals("Rules")) {
                    Object value = entry.getValue();
                    if (value instanceof ArrayList) {
                        ArrayList<String> uuidStrings = (ArrayList<String>) value;
                        for (String uuidString : uuidStrings) {
                            String pathOfFoundFile = findBrlFilesWithUUID(uuidString);
                            String arlPath = transformBRLPathToARLPath(pathOfFoundFile);
                            System.out.println(arlPath);

                            String arlRule = ARLRuleParser.readARLFileToString(arlPath);

                            // Parsing the ARL rule
                            Map<String, String> parsedRule = ARLRuleParser.parseARLRule(arlRule);
                
                            // Converting to DRL format
                            String cuurentDrlRule = convertFlowToDRL(parsedRule, String.valueOf(initSalience));
                
                            // The path to the file
                            //String fileName = ARLRuleParser.getFileNameFromParsedRuleMap(parsedRule);
                            drlRule += cuurentDrlRule;
                            drlRule += "\n\n\n";
                        }
                    }
                    initSalience = initSalience-1;
                }
            }
        }

        //Construct target path
        String filePath = "C:\\Users\\boubouthiam.niang\\workspace\\bl\\rbms\\migration\\odmtodroolsbis\\src\\main\\resources\\rules\\"+"flowA"+".drl";
                            
        //Write to drl file
        ARLRuleParser.writeDRLStringToFile(drlRule, filePath);

        System.out.println(ruleTaskMaps);

        String pathOfFoundFile = findBrlFilesWithUUID("8104d31b-327e-44b1-a29c-54736e393a99");

        System.out.println(pathOfFoundFile);

        //String changedFile = transformBRLPathToARLPath("C:\Users\boubouthiam.niang\workspace\bl\rbms\ODM\Demo\règles\rule one.brl");
    }
}
