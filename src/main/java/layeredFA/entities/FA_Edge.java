package layeredFA.entities;

import entities.ValidSign;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * FA 中的链接
 */
public class FA_Edge {

    /**
     * 这条边上的标记，可以是终结符，也可以是非终结符
     */
    private ValidSign label;

    /**
     * 这条边指向的后记状态
     */
    private FA_State pointTo;


    public FA_Edge(ValidSign label, FA_State pointTo) {
        this.label = label;
        this.pointTo = pointTo;
    }

    public ValidSign getLabel() {
        return label;
    }

    public void setLabel(ValidSign label) {
        this.label = label;
    }

    public FA_State getPointTo() {
        return pointTo;
    }

    public void setPointTo(FA_State pointTo) {
        this.pointTo = pointTo;
    }
}
