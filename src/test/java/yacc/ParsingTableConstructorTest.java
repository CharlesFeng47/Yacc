package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * ParsingTableConstructor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>十一月 15, 2017</pre>
 */
public class ParsingTableConstructorTest {

    private List<Production> productions;

    @Before
    public void before() throws Exception {
        productions = new LinkedList<>();

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
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getParsingTable()
     */
    @Test
    public void testGetParsingTable() throws Exception {
        new ParsingTableConstructor(productions);
    }

    /**
     * Method: isReducible(Production production)
     */
    @Test
    public void testIsReducible() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("isReducible", Production.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: getFollow(NonTerminal nt)
     */
    @Test
    public void testGetFollow() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("getFollow", NonTerminal.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: getFirst(NonTerminal nt)
     */
    @Test
    public void testGetFirst() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("getFirst", NonTerminal.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: containNT(List<ValidSign> right, NonTerminal nt)
     */
    @Test
    public void testContainNT() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("containNT", List<ValidSign>.class, NonTerminal.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: deriveToNull(ValidSign vs)
     */
    @Test
    public void testDeriveToNull() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("deriveToNull", ValidSign.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: getProductionNum(Production production)
     */
    @Test
    public void testGetProductionNum() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = ParsingTableConstructor.getClass().getMethod("getProductionNum", Production.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
