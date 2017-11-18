package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * ParsingTableConstructor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>十一月 15, 2017</pre>
 */
public class ParsingTableConstructorTest2 {

    private List<Production> productions;

    private Map<String, ValidSign> validSignMap;

    @Before
    public void before() throws Exception {
        productions = new LinkedList<>();
        validSignMap = new HashMap<>();

        Terminal t1 = new Terminal("+");
        Terminal t2 = new Terminal("*");
        Terminal t3 = new Terminal("(");
        Terminal t4 = new Terminal(")");
        Terminal t5 = new Terminal("id");
        Terminal t6 = new Terminal("ε");
        NonTerminal n1 = new NonTerminal("E");
        NonTerminal n2 = new NonTerminal("E`");
        NonTerminal n3 = new NonTerminal("T");
        NonTerminal n4 = new NonTerminal("T`");
        NonTerminal n5 = new NonTerminal("F");

        // E -> TE`
        List<ValidSign> right1 = new LinkedList<>();
        right1.add(n3);
        right1.add(n2);

        // E` -> +TE`
        List<ValidSign> right2 = new LinkedList<>();
        right2.add(t1);
        right2.add(n3);
        right2.add(n2);

        // E` -> ε
        List<ValidSign> right3 = new LinkedList<>();
        right3.add(t6);

        // T -> FT`
        List<ValidSign> right4 = new LinkedList<>();
        right4.add(n5);
        right4.add(n4);

        // T` -> *FT`
        List<ValidSign> right5 = new LinkedList<>();
        right5.add(t2);
        right5.add(n5);
        right5.add(n4);

        // T` -> ε
        List<ValidSign> right6 = new LinkedList<>();
        right6.add(t6);

        // F -> (E)
        List<ValidSign> right7 = new LinkedList<>();
        right7.add(t3);
        right7.add(n1);
        right7.add(t4);

        // F -> id
        List<ValidSign> right8 = new LinkedList<>();
        right8.add(t5);


        Production p1 = new Production(n1, right1);
        Production p2 = new Production(n2, right2);
        Production p3 = new Production(n2, right3);
        Production p4 = new Production(n3, right4);
        Production p5 = new Production(n4, right5);
        Production p6 = new Production(n4, right6);
        Production p7 = new Production(n5, right7);
        Production p8 = new Production(n5, right8);

        productions.add(p1);
        productions.add(p2);
        productions.add(p3);
        productions.add(p4);
        productions.add(p5);
        productions.add(p6);
        productions.add(p7);
        productions.add(p8);


        validSignMap.put("+", t1);
        validSignMap.put("*", t2);
        validSignMap.put("(", t3);
        validSignMap.put(")", t4);
        validSignMap.put("id", t5);
        validSignMap.put("ε", t6);
        validSignMap.put("E", n1);
        validSignMap.put("E`", n2);
        validSignMap.put("T", n3);
        validSignMap.put("T`", n4);
        validSignMap.put("F", n5);
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
