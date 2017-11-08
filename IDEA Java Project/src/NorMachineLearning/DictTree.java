package NorMachineLearning;

/**
 * Created on 2017-10-29.
 * 字典树
 * 没有做修约的最基本版本-->准确度:0.6383
 */
public class DictTree {
    //属性
    static final int LENGTH = 10;
    private String shop_id;//节点保存的属性值
    private int count;//计数器,关联次数
    private DictTree[] nextLink = new DictTree[LENGTH];//链结点
    private boolean[] flags = new boolean[LENGTH];
    private double signal;//信号

    //无参数
    public DictTree(){
        shop_id = null;
        count = 0;
        signal = 0.0;
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
        nextLink[position] = new DictTree();
    }
    public DictTree getNextLinkAt(int position){
        return nextLink[position];
    }
    public void setSignal(double signal){
        this.signal = signal;
    }
    public double getSignal(){
        return signal;
    }
}
