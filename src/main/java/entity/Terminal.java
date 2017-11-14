package entity;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * 终结符
 */
public class Terminal extends ValidSign {

    /**
     * 终结符的表示，限定为单个字符
     */
    private char repre;

    public Terminal(char repre) {
        this.repre = repre;
    }

    public char getRepre() {
        return repre;
    }

    public void setRepre(char repre) {
        this.repre = repre;
    }
}
