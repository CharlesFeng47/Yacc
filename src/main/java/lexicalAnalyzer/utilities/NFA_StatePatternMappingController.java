package lexicalAnalyzer.utilities;

import lexicalAnalyzer.finiteAutomata.entity.NFA;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/2.
 * <p>
 * 统一控制 NFA 与其对应模式的映射
 */
public class NFA_StatePatternMappingController {

    private static Map<NFA, String> map = new HashMap<>();

    private NFA_StatePatternMappingController() {
    }

    public static Map<NFA, String> getMap() {
        return map;
    }

    /**
     * 对终止态 state 添加对应的模式 pattern
     */
    public static boolean add(NFA nfa, String pattern) {
        map.put(nfa, pattern);
        return true;
    }
}
