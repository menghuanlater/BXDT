package NorMachineLearning;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017-10-29.
 * 字典树
 * 没有做修约的最基本版本-->准确度:0.6383
 */
public class NewDictTree {
    //属性
    static final int LENGTH = 10;
    private String shop_id;//节点保存的属性值
    private int count;//计数器,关联次数
    private NewDictTree[] nextLink = new NewDictTree[LENGTH];//链结点
    private boolean[] flags = new boolean[LENGTH];
    private List<Double> accuracy = new ArrayList<>();
    //无参数
    public NewDictTree(){
        shop_id = null;
        count = 0;
        for(int i=0;i<LENGTH;i++){
            nextLink[i] = null;
            flags[i] = false;
        }
    }
    //计数器增加
    public void addCount(){
        this.count++;
    }
    //获得权值
    public int getCount(){return this.count;}
    public void setFlagsAt(int position,boolean status){
        flags[position] = status;
    }
    public boolean getFlagsAt(int position){
        return flags[position];
    }
    public void setShop_id(String shop_id){
        this.shop_id = shop_id;
    }
    public String getShop_id(){return this.shop_id;}
    public void createNextLinkAt(int position){
        nextLink[position] = new NewDictTree();
    }
    public NewDictTree getNextLinkAt(int position){
        return nextLink[position];
    }
    public void addAccuracy(double x){accuracy.add(x);}
    public double getFinalAccuracy(double sum){
        double x1 = count/sum;
        double x2 = 0.0;
        for (Double anAccuracy : accuracy) {
            x2 += anAccuracy;
        }
        x2 = x2/accuracy.size();
        return x1*x2;
    }
}
