package lexicalAnalyzer.lex;

import lexicalAnalyzer.lex.entity.Token;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cuihua on 2017/11/1.
 * <p>
 * 与使用词法分析器的用户进行交互
 */
public class UserInteractionController {

    /**
     * 读取用户输入并进行简单处理，返回所有的词素 lexemes
     */
    public List<String> readUserContent() {
        Scanner sc = new Scanner(System.in);

        List<String> lexemes = new LinkedList<>();
        String line;
        while (!(line = sc.nextLine()).equals("###")) {
            String[] parts = line.split(" ");
            for (String lexeme : parts) {
                if (!lexeme.equals(""))
                    lexemes.add(lexeme);
            }
        }

        return lexemes;
    }

    /**
     * 向用户展示所有的词法单元结果
     */
    public void showAllTokens(List<Token> tokens) {
        String s = getTokenOutput(tokens);
        showInConsole(s);
        try {
            showInFile(s);
        } catch (IOException e) {
            System.out.println("Token 序列输出到文件：失败！");
        }
    }

    /**
     * 从 token 序列中获取要输出的内容
     */
    private String getTokenOutput(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------\n");
        for (Token token : tokens) {
            sb.append("< ").append(token.getPatternType());
            if (token.getAttribute() != null) {
                sb.append(", ");
                sb.append(token.getAttribute());
            }
            sb.append(" >").append("\n");
        }
        sb.append("-------------------");
        return sb.toString();
    }

    /**
     * 控制台输出
     */
    private void showInConsole(String s) {
        System.out.println(s);
    }

    /**
     * 文件输出
     */
    private void showInFile(String s) throws IOException {
        File file = new File(System.getProperty("user.dir") + " "+ LocalDateTime.now() + ".txt");
        if (file.createNewFile()) {
            FileWriter writer = new FileWriter(file);
            writer.write(s);
            writer.flush();
            writer.close();
        }

    }
}
