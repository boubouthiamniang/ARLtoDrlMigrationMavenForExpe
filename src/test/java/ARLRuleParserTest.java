import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.example.eco.ci.ARLRuleParser;

import java.util.Map;

@RunWith(JUnit4.class)
public class ARLRuleParserTest {
    
    @Test
    public void testParseARLRule() {
        String arlRule = "rule `New customer and a big spender at bithday offer` {\n" +
                "  property priority = 8;\n" +
                "  effectiveDate = new java.util.Date(\"6/20/2024 0:00 +0200\");\n" +
                "  expirationDate = new java.util.Date(\"6/23/2024 0:00 +0200\");\n" +
                "  status = \"new\";\n" +
                "  when {\n" +
                "    com.bl.drools.demo.Customer() from $EngineData.this.customer;\n" +
                "    evaluate ( $EngineData.this.customer.totalSpending >= 100);\n" +
                "  }\n" +
                "  then {\n" +
                "    $EngineData.this.customer.discount = 5;\n" +
                "  }\n" +
                "}\n";

        Map<String, String> parsedRule = ARLRuleParser.parseARLRule(arlRule);

        assertEquals("New customer and a big spender at bithday offer", parsedRule.get("name"));
        assertEquals("8", parsedRule.get("priority"));
        assertEquals("6/20/2024 0:00 +0200", parsedRule.get("effective_date"));
        assertEquals("6/23/2024 0:00 +0200", parsedRule.get("expiration_date"));
        assertEquals("new", parsedRule.get("status"));
        assertTrue(parsedRule.containsKey("conditions"));
        assertTrue(parsedRule.containsKey("actions"));
    }

    @Test
    public void testConvertARLToDRL() {
        Map<String, String> rule = Map.of(
                "name", "Test Rule",
                "priority", "10",
                "conditions", "com.bl.drools.demo.Customer() from $EngineData.this.customer;",
                "actions", "$EngineData.this.customer.discount = 5;"
        );

        String drlRule = ARLRuleParser.convertARLToDRL(rule);

        assertTrue(drlRule.contains("rule \"Test Rule\""));
        assertTrue(drlRule.contains("salience 10"));
        assertTrue(drlRule.contains("when"));
        assertTrue(drlRule.contains("then"));
        assertTrue(drlRule.contains("customerObject: Customer()"));
        assertTrue(drlRule.contains("customerObject.setDiscount(5);"));
    }
}
