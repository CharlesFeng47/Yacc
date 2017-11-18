package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ParsingTableConstructor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>十一月 15, 2017</pre>
 */
public class ParsingTableConstructorTest {

    private List<Production> productions;

    private Map<String, ValidSign> validSignMap;

    @Before
    public void before() throws Exception {
        productions = new LinkedList<>();
        validSignMap = new HashMap<>();

        Terminal t1 = new Terminal("i");
        Terminal t2 = new Terminal("e");
        Terminal t3 = new Terminal("a");
        Terminal t4 = new Terminal(";");
        NonTerminal n1 = new NonTerminal("S");

        // iSeS
        List<ValidSign> right1 = new LinkedList<>();
        right1.add(t1);
        right1.add(n1);
        right1.add(t2);
        right1.add(n1);

        // iS
        List<ValidSign> right2 = new LinkedList<>();
        right2.add(t1);
        right2.add(n1);

        // S;S
        List<ValidSign> right3 = new LinkedList<>();
        right3.add(n1);
        right3.add(t4);
        right3.add(n1);

        // a
        List<ValidSign> right4 = new LinkedList<>();
        right4.add(t3);

        Production p1 = new Production(n1, right1);
        Production p2 = new Production(n1, right2);
        Production p3 = new Production(n1, right3);
        Production p4 = new Production(n1, right4);

        productions.add(p1);
        productions.add(p2);
        productions.add(p3);
        productions.add(p4);

        validSignMap.put("i", t1);
        validSignMap.put("e", t2);
        validSignMap.put("a", t3);
        validSignMap.put(";", t4);
        validSignMap.put("S", n1);
        validSignMap.put("$", new Terminal("$"));
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getParsingTable()
     */
    @Test
    public void testGetParsingTable() throws Exception {
        new ParsingTableConstructor(productions, validSignMap.values(), (Terminal) validSignMap.get("$"));
    }
} 
