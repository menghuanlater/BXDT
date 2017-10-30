package Core;

import MySQLAssist.DBConnect;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017-10-28.
 * 实现的第一种算法:
 * 根据测试数据的wifi_info,抽取其中的wifi列表,从训练数据中找出每一个wifi相关联的店铺,
 * 根据每一行的wifi列表找出相关联数最高的店铺作为预测结果
 */
public class SimilarityAlgorithm {
    //最终输出文件名
    private static final String fileName = "result.csv";
    //字典树头节点
    private static DictTree head = null;
    //信号加权
    private static final int VALUE = 200;
    //row_id
    private static int row_id = 1;

    public static void main(String[] args) throws IOException, SQLException {
        //输出文件的相关定义
        Path outFile = Paths.get(fileName);
        BufferedWriter bw = Files.newBufferedWriter(outFile);
        bw.write("row_id,shop_id\n");
        //相关数据表名字
        String targetTableName = "test";
        String sourceTableName = "wifi";
        //数据库连接
        DBConnect dbConnect = new DBConnect();
        DBConnect dbQuery = new DBConnect();

        String sql = "select * from " + targetTableName;
        dbConnect.deliverSql(sql,true);//连接test数据库,遍历读取每一条记录
        ResultSet ret = dbConnect.pst.executeQuery();
        //循环遍历ret结果集
        //int x = 1;
        while (ret.next()){
            //if(x<298926){
                //x++;
               // row_id++;
                //continue;
            //}
            String[] wifiLists = ret.getString(2).split(";");
            head = new DictTree();//每分析一行重新建立一个字典树
            for (String wifiList : wifiLists) {
                String[] wifi = wifiList.split("\\|");

                //wifi的名字以及当前wifi的信号数值
                String wifiName = wifi[0];
                double wifiSignal = Double.valueOf(wifi[1]) + VALUE;

                //查询wifi对应关联的店铺
                sql = "select shop_id,AVG(dbm) from " + sourceTableName + " where wifi_id = '" + wifiName +"' " +
                        "group by shop_id";
                dbQuery.deliverSql(sql,false);
                ResultSet retQuery = dbQuery.pst.executeQuery();
                //遍历
                while(retQuery.next())
                    addOrInsertTree(retQuery.getString(1),
                            (retQuery.getDouble(2)+wifiSignal)/2);
                //用完关闭
                retQuery.close();
                dbQuery.pst.close();
            }
            //数据分析查询完毕,接下来遍历字典树,取出有效节点
            List<DictTree> outComeSet = new ArrayList<>();
            travelDictTree(outComeSet,head);
            //找到最优
            DictTree best = findBestNode(outComeSet);
            if(best==null)
                bw.write(row_id+",null\n");
            else
                bw.write(row_id+","+best.getShop_id()+"\n");
            bw.flush();
            row_id++;
        }
        bw.flush();
        bw.close();
        //关闭数据库
        dbConnect.dbClose();
        dbQuery.dbClose();
    }
    //新数据加入字典树
    //signal信号应该是取平均后的数值
    private static void addOrInsertTree(String shop_id,double signal){
        String src = shop_id.substring(2);
        DictTree obj = head;
        boolean isNewData = false;
        for(int i=0;i<src.length();i++){
            int num = src.charAt(i) - '0';
            if(obj.getFlagsAt(num)){
                obj = obj.getNextLinkAt(num);//继续下一个分支树
            }else{
                isNewData = true;
                obj.setFlagsAt(num,true);
                obj.createNextLinkAt(num);//创建新节点
                obj = obj.getNextLinkAt(num);
            }
        }
        if(isNewData)
            obj.setShop_id(shop_id);
        obj.addCount();
        obj.setSignal((obj.getSignal()+signal)/2);
    }
    //遍历字典树
    private static void travelDictTree(List<DictTree>outComeSet,DictTree target){
        if(target.getShop_id()!=null)
            outComeSet.add(target);
        for(int i=0;i<DictTree.LENGTH;i++){
            if(target.getFlagsAt(i))
                travelDictTree(outComeSet,target.getNextLinkAt(i));
        }
    }
    //找到最优解
    private static DictTree findBestNode(List<DictTree>outComeSet){
        if(outComeSet.size()==0)
            return null;
        DictTree best = outComeSet.get(0);
        for(int i=1;i<outComeSet.size();i++){
            if(outComeSet.get(i).getCount()>best.getCount() || (outComeSet.get(i).getCount() == best.getCount() &&
                outComeSet.get(i).getSignal()>best.getSignal()))
                best = outComeSet.get(i);
        }
        return best;
    }
}
