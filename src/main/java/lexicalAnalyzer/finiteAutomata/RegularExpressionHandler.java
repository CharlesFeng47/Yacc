package lexicalAnalyzer.finiteAutomata;

import lexicalAnalyzer.exceptions.UnexpectedRegularExprRuleException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import lexicalAnalyzer.utilties.ExtendedMark;
import lexicalAnalyzer.utilties.SquareBracketMarkInnerType;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by cuihua on 2017/10/25.
 * <p>
 * 输入的正则表达式
 */
public class RegularExpressionHandler {

    private static final Logger logger = Logger.getLogger(RegularExpressionHandler.class);

    /**
     * 不可能存在的正则定义
     */
    private static List<String> unexpectedRERules;

    /**
     * 标准化的正则表达式中优先级序列
     * 优先级越高，越靠后
     */
    private static List<Character> priority;

    /**
     * 匹配如 [a-z]
     */
    private static char[] lowCaseCharSequence = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    /**
     * 匹配如 [A-Z]
     */
    private static char[] upCaseCharSequence = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * 匹配如 [0-9]
     */
    private static char[] intSequence = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public RegularExpressionHandler() {
        unexpectedRERules = new LinkedList<>();
        unexpectedRERules.add("(*");
        unexpectedRERules.add("(|");
        unexpectedRERules.add("|)");
        unexpectedRERules.add("|*");
        unexpectedRERules.add("||");
        unexpectedRERules.add("·)");
        unexpectedRERules.add("·*");
        unexpectedRERules.add("·|");
        unexpectedRERules.add("(·");
        unexpectedRERules.add("|·");
        unexpectedRERules.add("··");

        priority = new LinkedList<>();
        priority.add(0, '(');
        priority.add(1, '·');
        priority.add(2, '|');
        priority.add(3, '*');
    }

    /**
     * 默认re不含有连接符
     * 将扩展符 +、?、{}、[] 用基本符号代替
     * 添加省略的连接符'·'（对所有操作符画出所有的可能情况）
     *
     * @param re 输入的正则表达式
     * @return 标准的没有扩展语法如[], +, ?
     */
    public String standardizeRE(String re) throws UnexpectedRegularExprRuleException {
        // 替换所有空格，便于控制
        re = re.replace(" ", "");

        // 替换扩展符号，result存储替换后的字符串，differ表示替换前后的对当前处理字符的Index差
        StringBuffer result = new StringBuffer().append(re);
        int differ = 0;
        for (int i = 0; i < re.length(); i++) {
            char c = re.charAt(i);

            if (c == '\\') {
                // 转义字符跳过处理
                i++;
            } else {
                int preLength = result.length();
                if (c == '?') {
                    result = standardizeExtendedMark(result, i + differ, ExtendedMark.QUESTION_MARK);
                }
                if (c == '+') {
                    result = standardizeExtendedMark(result, i + differ, ExtendedMark.PLUS_MARK);
                }
                if (c == '{') {
                    result = standardizeExtendedMark(result, i + differ, ExtendedMark.BRACE_MARK);
                }
                if (c == '[') {
                    result = standardizeSquareBracketMark(result, i + differ);
                }
                differ += result.length() - preLength;
            }
        }

        String tempResult = result.toString();
        checkREValidation(tempResult);


        // 补充连接符，joinCount表示连接前后的对当前处理字符的Index差，curCharIsTransferred表示当前字符是否是转义字符
        int joinCount = 0;
        boolean curCharIsTransferred = false;
        for (int i = 0; i < tempResult.length() - 1; i++) {
            char before = tempResult.charAt(i);
            char after = tempResult.charAt(i + 1);

            if (before == '\\') {
                // 转义字符之间不添加连接符，跳过检查下一个操作符
                curCharIsTransferred = true;
                continue;
            }

            // 合法情况下含有连接符号的都不需要处理
            if (before == '·' || after == '·') {
                curCharIsTransferred = false;
                continue;
            }

            if (after == '(' || isValidChar(false, after)) {
                if (before == ')' || before == '*' || isValidChar(curCharIsTransferred, before)) {
                    result = standardizeJoinMark(result, i + joinCount);
                    joinCount++;
                }
            }

            curCharIsTransferred = false;
        }
        return result.toString();
    }

    /**
     * 处理扩展符号（+ / ? / {}）
     */
    private StringBuffer standardizeExtendedMark(final StringBuffer re, int markIndex, ExtendedMark mark) throws UnexpectedRegularExprRuleException {
        StringBuffer result = new StringBuffer();

        // ? 前面是括号，需要找到核
        String content;
        int contentStartIndex;

        if (re.charAt(markIndex - 1) == ')') {
            // 核为非单字符
            contentStartIndex = getContentStartIndexOfExtendedMark(re, markIndex);
            content = re.substring(contentStartIndex, markIndex);
        } else {
            // 核直接是前面的单个字符
            if (markIndex >= 2 && re.charAt(markIndex - 2) == '\\') {
                // 核为转义字符
                contentStartIndex = markIndex - 2;
                content = re.substring(contentStartIndex, markIndex);
            } else {
                // 核为普通单个字符
                contentStartIndex = markIndex - 1;
                content = String.valueOf(re.charAt(markIndex - 1));
            }
        }

        result.append(re.substring(0, contentStartIndex));

        if (mark == ExtendedMark.QUESTION_MARK) result.append("(ε|").append(content).append(')');
        else if (mark == ExtendedMark.PLUS_MARK) result.append(content).append(content).append('*');
        else if (mark == ExtendedMark.BRACE_MARK) {
            // 大括号里面的内容
            String sub = re.substring(markIndex + 1);
            int braceEndIndex = sub.indexOf("}");
            int commaIndex = sub.indexOf(",");

            if (braceEndIndex == -1) throw new UnexpectedRegularExprRuleException(re.toString());

            if (commaIndex == -1) {
                // {n} 类型。没有逗号，只有数字，重复数字遍即可
                int times = Integer.parseInt(sub.substring(0, braceEndIndex));
                for (int i = 0; i < times; i++) {
                    result.append(content);
                }
            } else {
                if (commaIndex == 0) {
                    // {, n} 类型，重复0-n遍
                    int times = Integer.parseInt(sub.substring(1, braceEndIndex));
                    for (int i = 0; i < times; i++) {
                        result.append("(ε|").append(content).append(')');
                    }
                } else if (commaIndex == braceEndIndex - 1) {
                    // {n, } 类型，重复最少n遍
                    int times = Integer.parseInt(sub.substring(0, commaIndex));
                    for (int i = 0; i < times; i++) {
                        result.append(content);
                    }
                    result.append(content).append("*");
                } else {
                    // {m, n} 类型，最少m遍，最多n遍
                    int mTimes = Integer.parseInt(sub.substring(0, commaIndex));
                    int nTimes = Integer.parseInt(sub.substring(commaIndex + 1, braceEndIndex));
                    for (int i = 0; i < mTimes; i++) {
                        result.append(content);
                    }
                    for (int i = mTimes; i < nTimes; i++) {
                        result.append("(ε|").append(content).append(')');
                    }
                }
            }

            // 如果 {} 不是最后一个字符，加上后续字符
            if (braceEndIndex != re.length() - 1) result.append(sub.substring(braceEndIndex + 1));
        }

        if (mark == ExtendedMark.QUESTION_MARK | mark == ExtendedMark.PLUS_MARK) {
            // 如果 +/? 不是最后一个字符，加上后续字符
            if (markIndex != re.length() - 1) result.append(re.substring(markIndex + 1));
        }
        logger.debug(result);
        return result;
    }

    /**
     * 找到扩展符号（+ / ? / {}）作用的核的左括号
     */
    private int getContentStartIndexOfExtendedMark(final StringBuffer re, int markIndex) {
        int pairCount = 0;

        int contentStartIndex;
        for (contentStartIndex = markIndex - 1; contentStartIndex >= 0; contentStartIndex--) {
            char c = re.charAt(contentStartIndex);
            if (c == ')') pairCount++;
            else if (c == '(') {
                if (pairCount == 1) break;
                else pairCount--;
            }
        }
        return contentStartIndex;
    }

    /**
     * 增加省略的连接符（·）
     *
     * @param joinIndex 需要在两个字符中间添加连接符号，第一个字符的index
     */
    private StringBuffer standardizeJoinMark(final StringBuffer re, int joinIndex) {
        StringBuffer sb = new StringBuffer();
        sb.append(re.substring(0, joinIndex + 1)).append('·').append(re.substring(joinIndex + 1));
        return sb;
    }

    /**
     * 将方括号里面的内容替换为普通的表达式
     */
    private StringBuffer standardizeSquareBracketMark(final StringBuffer re, int markIndex)
            throws UnexpectedRegularExprRuleException {
        StringBuffer result = new StringBuffer();
        result.append(re.substring(0, markIndex));

        // 方括号里面的内容
        String sub = re.substring(markIndex + 1);
        int bracketEndIndex = sub.indexOf("]");

        if (bracketEndIndex == -1) throw new UnexpectedRegularExprRuleException(re.toString());

        String bracketContent = sub.substring(0, bracketEndIndex);

        List<StringBuffer> bracketCompleted = new LinkedList<>();
        for (int i = 0; i < bracketContent.length(); ) {
            if (i < bracketContent.length() - 1 && bracketContent.charAt(i + 1) == '-') {
                // 是连字符形式，按范围或起来
                char start = bracketContent.charAt(i);
                char end = bracketContent.charAt(i + 2);

                int startIndex, endIndex;
                if (ArrayUtils.contains(lowCaseCharSequence, start)) {
                    // 小写字母
                    startIndex = ArrayUtils.indexOf(lowCaseCharSequence, start);
                    endIndex = ArrayUtils.indexOf(lowCaseCharSequence, end);
                    bracketCompleted.add(standardizeSquareBracketMarkSeparatorToCompleted(startIndex, endIndex,
                            SquareBracketMarkInnerType.LOW_CHAR));
                } else if (ArrayUtils.contains(upCaseCharSequence, start)) {
                    // 大写字母
                    startIndex = ArrayUtils.indexOf(upCaseCharSequence, start);
                    endIndex = ArrayUtils.indexOf(upCaseCharSequence, end);
                    bracketCompleted.add(standardizeSquareBracketMarkSeparatorToCompleted(startIndex, endIndex,
                            SquareBracketMarkInnerType.UP_CHAR));
                } else if (ArrayUtils.contains(intSequence, start)) {
                    // 数字
                    startIndex = ArrayUtils.indexOf(intSequence, start);
                    endIndex = ArrayUtils.indexOf(intSequence, end);
                    bracketCompleted.add(standardizeSquareBracketMarkSeparatorToCompleted(startIndex, endIndex,
                            SquareBracketMarkInnerType.INT));
                }

                i += 3;
            } else {
                // 没有被跳过最后一个字（单个字符）／不是连字符形式（单个字符），或起来
                StringBuffer sb = new StringBuffer();
                sb.append(bracketContent.charAt(i));
                bracketCompleted.add(sb);

                i++;
            }
        }


        // 将 bracketCompleted 中的结果集或起来
        if (bracketCompleted.size() > 1) {
            result.append("(").append(bracketCompleted.get(0));
            for (int i = 1; i < bracketCompleted.size(); i++) {
                result.append("|").append(bracketCompleted.get(i));

            }
            result.append(")");
        } else {
            result.append(bracketCompleted.get(0));
        }

        if (bracketEndIndex != sub.length() - 1) result.append(sub.substring(bracketEndIndex + 1));
        logger.debug(result);
        return result;
    }

    /**
     * 对 [m-n] 类型的字符串进行补全
     *
     * @param startIndex 补全的第一个字母（含）
     * @param endIndex   补全的最后一个字母（含）
     */
    private StringBuffer standardizeSquareBracketMarkSeparatorToCompleted(int startIndex, int endIndex,
                                                                          SquareBracketMarkInnerType innerType) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        switch (innerType) {
            case LOW_CHAR:
                for (int i = startIndex; i < endIndex; i++) {
                    sb.append(lowCaseCharSequence[i]).append("|");
                }
                sb.append(lowCaseCharSequence[endIndex]);
                break;

            case UP_CHAR:
                for (int i = startIndex; i < endIndex; i++) {
                    sb.append(upCaseCharSequence[i]).append("|");
                }
                sb.append(upCaseCharSequence[endIndex]);
                break;

            case INT:
                for (int i = startIndex; i < endIndex; i++) {
                    sb.append(intSequence[i]).append("|");
                }
                sb.append(intSequence[endIndex]);
                break;
        }
        sb.append(")");
        return sb;
    }

    /**
     * 检查标准化正则定义的正确性
     */
    private void checkREValidation(final String re) throws UnexpectedRegularExprRuleException {
        for (int i = 0; i < re.length() - 1; i++) {
            char before = re.charAt(i);
            char after = re.charAt(i + 1);

            // 输入RE不合法
            String temp = before + "" + after;
            if (unexpectedRERules.contains(temp)) {
                throw new UnexpectedRegularExprRuleException(temp);
            }
        }

        // 转义字符不合法
        // {m, n} 形式已在标准化时处理
        String toCheckComma = re;
        int commaIndex;
        while ((commaIndex = toCheckComma.indexOf(",")) != -1) {
            if (commaIndex == 0) throw new UnexpectedRegularExprRuleException(re);
            if (toCheckComma.charAt(commaIndex - 1) != '\\') throw new UnexpectedRegularExprRuleException(re);
            else toCheckComma = toCheckComma.substring(commaIndex + 1);
        }

        // [m-n] 形式已在标准化时处理
        String toCheckSeparator = re;
        int separatorIndex;
        while ((separatorIndex = toCheckSeparator.indexOf("-")) != -1) {
            if (separatorIndex == 0) throw new UnexpectedRegularExprRuleException(re);
            if (toCheckSeparator.charAt(separatorIndex - 1) != '\\') throw new UnexpectedRegularExprRuleException(re);
            else toCheckSeparator = toCheckSeparator.substring(separatorIndex + 1);
        }

    }


    /**
     * 判断 re 中的输入字符 c 在条件 isTransferred 下是否合法
     */
    private boolean isValidChar(boolean isTransferred, char toTest) {
        if (isTransferred) {
            // 转义字符
            return isOperand(toTest);
        } else {
            // 普通字符
            return !isOperand(toTest);

        }
    }

    /**
     * 判断字符 c 是不是操作符
     */
    private boolean isOperand(char c) {
        return (c == '·' || c == '|' || c == '*' || c == '(' || c == ')' || c == '+' || c == '?' || c == '{' || c == '}'
                || c == '[' || c == ']' || c == '-') || c == ',';
    }


    /**
     * 将标准化后的正则定义的中缀表达式改为后缀表达式
     * 只含有并、或、闭包，括号
     */
    public String convertInfixToPostfix(String re) {
        // 存储结果的后缀字符串
        StringBuilder sb = new StringBuilder(re.length());

        // 操作符的栈
        Stack<Character> operandStack = new Stack<>();

        // 判断当前字符是否是转义字符
        boolean curCharIsTransferred = false;
        for (int i = 0; i < re.length(); i++) {
            char c = re.charAt(i);

            // 转义的操作符
            if (c == '\\') {
                sb.append(c);
                curCharIsTransferred = true;
                continue;
            }

            // 非操作符
            if (isValidChar(curCharIsTransferred, c)) {
                sb.append(c);
                curCharIsTransferred = false;
                continue;
            }

            // 操作符
            if (c == '(') operandStack.push('(');
            else if (c == ')') {
                // 退栈至匹配的'('
                char top;
                while ((top = operandStack.pop()) != '(') {
                    sb.append(top);
                }
            } else {
                if (!operandStack.empty()) {
                    char top = operandStack.peek();

                    while (true) {
                        // 退栈高优先级的操作符，最后再压栈当前操作符
                        // 没有优先级更高的操作符时跳出
                        if (comparePriority(c, top)) {
                            operandStack.pop();
                            sb.append(top);
                        } else break;

                        // 操作栈不为空时继续比较，否则跳出
                        if (!operandStack.empty()) {
                            top = operandStack.peek();
                        } else break;
                    }

                    operandStack.push(c);
                } else {
                    // 操作符栈中之前无堆栈，将此操作符压栈
                    operandStack.push(c);
                }

            }

            curCharIsTransferred = false;
        }

        // 栈中剩余操作符
        while (!operandStack.empty()) {
            char top = operandStack.pop();
            sb.append(top);
        }

        return sb.toString();
    }


    /**
     * @param curChar 当前读取的操作符
     * @param top     当前符号栈的栈顶操作符
     * @return true 如果 curChar 优先级小于等于 top 优先级，top 需要被弹出。false otherwise
     */
    private boolean comparePriority(char curChar, char top) {
        int curCharIndex = priority.indexOf(curChar);
        int topCharIndex = priority.indexOf(top);

        boolean result = (curCharIndex - topCharIndex) <= 0;
        logger.debug("优先级：当前符号" + curChar + "小于等于栈顶符号" + top + ": " + result);
        return result;
    }

}
