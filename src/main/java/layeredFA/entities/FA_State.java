package layeredFA.entities;

import entity.Production;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cuihua on 2017/11/14.
 * <p>
 * FA 中的节点
 */
public class FA_State {

    /**
     * 此节点对应的编号
     */
    private int stateID;

    /**
     * 此节点对应的产生式
     */
    private List<Production> productions;

    /**
     * 此节点对应的后续状态链接
     */
    private List<FA_Edge> follows;

    public FA_State(int stateID, List<Production> productions) {
        this.stateID = stateID;
        this.productions = productions;
        this.follows = new LinkedList<>();
    }

    public int getStateID() {
        return stateID;
    }

    public void setStateID(int stateID) {
        this.stateID = stateID;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public void setProductions(List<Production> productions) {
        this.productions = productions;
    }

    public List<FA_Edge> getFollows() {
        return follows;
    }

    public void setFollows(List<FA_Edge> follows) {
        this.follows = follows;
    }
}
