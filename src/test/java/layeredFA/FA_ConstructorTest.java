package layeredFA;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * FA_Constructor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>十一月 15, 2017</pre>
 */
public class FA_ConstructorTest {

    private static final Logger logger = Logger.getLogger(FA_Constructor.class);

    private List<Production> productions;
    private List<ValidSign> validSigns;

    private List<Production> toTest1;
    private List<Production> toTest2;
    private List<Production> toTest3;
    private List<Production> toTest4;
    private List<Production> toTest5;


    @Before
    public void before() throws Exception {
        productions = new LinkedList<>();

        Terminal t1 = new Terminal("i");
        Terminal t2 = new Terminal("e");
        Terminal t3 = new Terminal("a");
        Terminal t4 = new Terminal(";");
        NonTerminal n1 = new NonTerminal("S");

        NonTerminal newStartProLeft = new NonTerminal("&");

        List<ValidSign> right0 = new LinkedList<>();
        right0.add(n1);

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

        Production p0 = new Production(newStartProLeft, right0);
        Production p1 = new Production(n1, right1);
        Production p2 = new Production(n1, right2);
        Production p3 = new Production(n1, right3);
        Production p4 = new Production(n1, right4);

        productions.add(p0);
        productions.add(p1);
        productions.add(p2);
        productions.add(p3);
        productions.add(p4);

        validSigns = new LinkedList<>();
        validSigns.add(t1);
        validSigns.add(t2);
        validSigns.add(t3);
        validSigns.add(t4);
        validSigns.add(n1);


        Production toTestP1 = new Production(n1, right1, 3);
        Production toTestP2 = new Production(n1, right1, 4);
        Production toTestP3 = new Production(n1, right3, 1);
        Production toTestP4 = new Production(n1, right3, 2);
        Production toTestP5 = new Production(n1, right1, 1);
        Production toTestP6 = new Production(n1, right2, 1);

        // iSe·S
        toTest1 = new LinkedList<>();
        toTest1.add(toTestP1);

        // iSeS·
        // S·;S
        toTest2 = new LinkedList<>();
        toTest2.add(toTestP2);
        toTest2.add(toTestP3);

        // S;·S
        toTest3 = new LinkedList<>();
        toTest3.add(toTestP4);

        // i·SeS
        // i·S
        toTest4 = new LinkedList<>();
        toTest4.add(toTestP5);
        toTest4.add(toTestP6);

        // i·SeS
        // S·;S
        toTest5 = new LinkedList<>();
        toTest5.add(toTestP5);
        toTest5.add(toTestP3);


    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: parse()
     */
    @Test
    public void testParse() throws Exception {
        new FA_Constructor(productions, validSigns).parse();
    }


    /**
     * Method: closureProduction(List<Production> productions)
     */
    @Test
    public void testClosureProduction() throws Exception {
        try {
            Method method = FA_Constructor.class.getDeclaredMethod("closureProduction", List.class);
            method.setAccessible(true);
//            method.invoke(new FA_Constructor(productions, validSigns), toTest1);
//            method.invoke(new FA_Constructor(productions, validSigns), toTest2);
            method.invoke(new FA_Constructor(productions, validSigns), toTest3);
//            method.invoke(new FA_Constructor(productions, validSigns), toTest4);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        }

    }

    /**
     * Method: move(FA_State cur, String label)
     */
    @Test
    public void testMove() throws Exception {
        try {
            Method method = FA_Constructor.class.getDeclaredMethod("move", List.class, ValidSign.class);
            method.setAccessible(true);
//            method.invoke(new FA_Constructor(productions, validSigns), toTest5, new NonTerminal("S"));
            method.invoke(new FA_Constructor(productions, validSigns), toTest5, new Terminal(";"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }
    }

    /**
     * Method: getAllValidSigns()
     */
    @Test
    public void testGetAllValidSigns() throws Exception {
        try {
            Method method = FA_Constructor.class.getDeclaredMethod("getAllValidSigns");
            method.setAccessible(true);
            method.invoke(new FA_Constructor(productions, validSigns));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }
    }

}
