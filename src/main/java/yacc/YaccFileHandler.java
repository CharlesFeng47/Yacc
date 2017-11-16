package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import exceptions.ParsingTableConflictException;
import exceptions.YaccFileInputException;
import utilities.MyResourceFileReader;
import yacc.entities.ParsingTable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 处理 Yacc .y 文件中的数据（只含有文法产生式）
 */
public class YaccFileHandler {

    /**
     * .y 文件的路径
     */
    private static final String path = "yacc_specification.y";

    /**
     * 输入的产生式
     */
    private List<Production> productions;

    /**
     * 输入的字符串与合法字符的映射
     */
    private Map<String, ValidSign> validSignMap;

    public YaccFileHandler() throws YaccFileInputException {
        // 从 .y 文件中读取数据
        List<String> content = new MyResourceFileReader().readFile(path);
        initProductions(content);
    }

    /**
     * 根据 .y 文件中内容生成产生式
     */
    private void initProductions(List<String> yContent) throws YaccFileInputException {
        productions = new LinkedList<>();
        validSignMap = new HashMap<>();

        NonTerminal left = null;
        for (String s : yContent) {
            s = s.trim();
            if (s.contains(":")) {
                // 开头
                String[] parts = s.split(":");
                String leftString = parts[0].trim();
                String[] rightParts = parts[1].trim().split(" ");

                // 增加 map 定义
                if (!isNonTerminal(leftString)) throw new YaccFileInputException(leftString + " 不是全大写，不符合定义");
                validSignMap.putIfAbsent(leftString, new NonTerminal(leftString));
                addVSMap(rightParts);

                left = (NonTerminal) validSignMap.get(leftString);
                List<ValidSign> right = new LinkedList<>();
                for (String temp : rightParts) {
                    right.add(validSignMap.get(temp));
                }
                productions.add(new Production(left, right));

            } else if (s.startsWith("|")) {
                // 前一个产生式未完成的产生式
                String[] rightParts = s.split("\\|")[1].trim().split(" ");
                addVSMap(rightParts);
                List<ValidSign> right = new LinkedList<>();
                for (String temp : rightParts) {
                    right.add(validSignMap.get(temp));
                }
                assert left != null;
                productions.add(new Production(left, right));

            } else if (s.equals(";")) {
                // 此非终结符开头的产生式结束
                left = null;
            }
        }
    }

    /**
     * 含有大写字母的都是非终结符，只有全小写才是终结符
     */
    private boolean isNonTerminal(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) return true;
        }
        return false;
    }

    /**
     * 在 map 中 加入 parts 中没有的对象
     */
    private void addVSMap(String[] parts) {
        for (String temp : parts) {
            if (isNonTerminal(temp)) {
                validSignMap.putIfAbsent(temp, new NonTerminal(temp));
            } else {
                validSignMap.putIfAbsent(temp, new Terminal(temp));
            }
        }
    }


    /**
     * .y 文件对应的预测分析表
     */
    public ParsingTable convertToPT() throws ParsingTableConflictException {
        ParsingTableConstructor constructor = new ParsingTableConstructor(productions);
        return constructor.getParsingTable();
    }


}
