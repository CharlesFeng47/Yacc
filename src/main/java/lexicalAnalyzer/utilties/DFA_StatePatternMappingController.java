package lexicalAnalyzer.utilties;

import lexicalAnalyzer.finiteAutomata.entity.DFA;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/2.
 * <p>
 * 统一控制 DFA 与其对应模式的映射
 */
public class DFA_StatePatternMappingController {

    private static Map<DFA, String> map = new HashMap<>();

    private DFA_StatePatternMappingController() {
    }

    public static Map<DFA, String> getMap() {
        return map;
    }

    /**
     * 对终止态 state 添加对应的模式 pattern
     */
    public static boolean add(DFA dfa, String pattern) {
        map.put(dfa, pattern);
        return true;
    }
}
