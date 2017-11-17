package yacc;

import entities.NonTerminal;
import entities.Production;
import entities.Terminal;
import entities.ValidSign;
import exceptions.ParsingTableConflictException;
import exceptions.YaccFileInputException;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.exceptions.NotMatchingException;
import lexicalAnalyzer.lex.entity.Token;
import utilities.MyResourceFileReader;
import yacc.entities.ParsingTable;

import java.util.*;

/**
 * Created by cuihua on 2017/11/16.
 * <p>
 * 处理 Yacc .y 文件中的数据（只含有文法产生式）
 */
class YaccFileHandler {

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

    /**
     * .l 文件生成的词法分析器
     */
    private LexicalAnalyzer lexicalAnalyzer;

    /**
     * .l  文件生成的所有模式
     */
    private List<String> allPattern;

    YaccFileHandler(LexicalAnalyzer lexicalAnalyzer) throws YaccFileInputException {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.allPattern = lexicalAnalyzer.getAllPattern();

        // 从 .y 文件中读取数据
        List<String> content = new MyResourceFileReader().readFile(path);
        initProductions(content);
    }

    List<Production> getProductions() {
        return productions;
    }

    Map<String, ValidSign> getValidSignMap() {
        return validSignMap;
    }

    /**
     * 根据 .y 文件中内容生成产生式
     */
    private void initProductions(List<String> yContent) throws YaccFileInputException {
        productions = new LinkedList<>();

        // 保证后续传参的 value set 未被 hash 过
        validSignMap = new LinkedHashMap<>();

        NonTerminal left = null;
        for (String s : yContent) {
            s = s.trim();
            if (s.contains(":")) {
                // 开头
                String[] parts = s.split(":");
                String leftString = parts[0].trim();
                String[] rightParts = parts[1].trim().split(" ");

                // 增加 map 定义
                if (isTerminal(leftString))
                    throw new YaccFileInputException(leftString + " 是一个定义的终结词素，不能作为产生式的左部");
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

        // 加入结尾 $ 的映射
        validSignMap.put("$", new Terminal("$"));
    }

    /**
     * 在 map 中 加入 parts 中没有的对象
     */
    private void addVSMap(String[] parts) {
        for (String temp : parts) {
            if (isTerminal(temp)) {
                validSignMap.putIfAbsent(temp, new Terminal(temp));
            } else {
                validSignMap.putIfAbsent(temp, new NonTerminal(temp));
            }
        }
    }

    /**
     * 出现在 .l 文件模式定义里的即是终结符，否则即为非终结符
     */
    private boolean isTerminal(String s) {
        return allPattern.contains(s);
    }

    /**
     * .y 文件对应的预测分析表
     */
    ParsingTable convertToPT() throws ParsingTableConflictException {
        ParsingTableConstructor constructor = new ParsingTableConstructor(productions,
                validSignMap.values(), (Terminal) validSignMap.get("$"));
        return constructor.getParsingTable();
    }


}
