package entity;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 非终结符
 */
public class NonTerminal extends ValidSign {

    /**
     * 非终结符的表示，可能存在多个字符（如 if ）
     */
    private String repre;

    public NonTerminal(String repre) {
        this.repre = repre;
    }

    public String getRepre() {
        return repre;
    }

    public void setRepre(String repre) {
        this.repre = repre;
    }
}
