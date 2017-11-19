package HelpAnalysis;

import MySQLAssist.DBConnect;
import NorMachineLearning.NewDictTree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017-10-30.
 * MTPSA:多线程模式的简单算法处理模式
 */
public class RunTrain {
    //最终结果写入的数据库名
    private static String resultTable = "result_train";
    //每次执行程序清空数据表
    private static String defaultSql = "truncate table " + resultTable;
    //信号加权
    private static final int VALUE = 200;

    private static String writeSql = "insert into "+resultTable+" values(?,?)";

    private static DBConnect writeDB = new DBConnect();

    private static int batchNum = 0;

    public static void main(String[] args) throws SQLException, InterruptedException {
        //清空表
        writeDB.deliverSql(defaultSql,false);
        writeDB.pst.executeUpdate();
        writeDB.pst.close();
        //传入插入数据的sql
        writeDB.deliverSql(writeSql,false);

        //线程创建
        Multi multi1 = new Multi(1,60000);
        Multi multi2 = new Multi(60001,120000);
        Multi multi3 = new Multi(120001,180000);
        Multi multi4 = new Multi(180001,240000);
        Multi multi5 = new Multi(240001,300000);
        Multi multi6 = new Multi(300001,360000);
        Multi multi7 = new Multi(360001,420000);
        Multi multi8 = new Multi(420001,480000);

        /*Multi multi1 = new Multi(1,120000);
        Multi multi2 = new Multi(120001,240000);
        Multi multi3 = new Multi(240001,360000);
        Multi multi4 = new Multi(360001,484000);*/

        multi1.start();
        multi2.start();
        multi3.start();
        multi4.start();
        multi5.start();
        multi6.start();
        multi7.start();
        multi8.start();

        //扫描60s一次
        while(multi1.getState() != Thread.State.TERMINATED || multi2.getState()!= Thread.State.TERMINATED
                || multi3.getState() != Thread.State.TERMINATED || multi4.getState() != Thread.State.TERMINATED ||
                multi5.getState() != Thread.State.TERMINATED || multi6.getState() != Thread.State.TERMINATED ||
                multi7.getState() != Thread.State.TERMINATED || multi8.getState() != Thread.State.TERMINATED){
            Thread.sleep(60000);
        }
        /*while(multi1.getState() != Thread.State.TERMINATED || multi2.getState()!= Thread.State.TERMINATED
                || multi3.getState() != Thread.State.TERMINATED || multi4.getState() != Thread.State.TERMINATED){
            Thread.sleep(60000);
        }*/
        //最后一波批量数据插入数据库
        writeDB.pst.executeBatch();
        writeDB.dbClose();
    }

    //新数据加入字典树
    //signal信号应该是取平均后的数值
    private static void addOrInsertTree(String shop_id,double accuracy,NewDictTree head){
        String src = shop_id.substring(2);
        NewDictTree obj = head;
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
        obj.addAccuracy(accuracy);
    }
    //遍历字典树
    private static void travelNewDictTree(List<NewDictTree>outComeSet,NewDictTree target){
        if(target.getShop_id()!=null)
            outComeSet.add(target);
        for(int i=0;i<NewDictTree.LENGTH;i++){
            if(target.getFlagsAt(i))
                travelNewDictTree(outComeSet,target.getNextLinkAt(i));
        }
    }
    //找到最优解
    private static NewDictTree findBestNode(List<NewDictTree>outComeSet,int sumWifi){
        if(outComeSet.size()==0)
            return null;
        NewDictTree best = outComeSet.get(0);
        for(int i=1;i<outComeSet.size();i++){
            if(outComeSet.get(i).getFinalAccuracy(sumWifi) > best.getFinalAccuracy(sumWifi))
                best = outComeSet.get(i);
        }
        return best;
    }
    //数据写入数据库
    synchronized private static void writeToMySQL(int row_id,String shop_id) throws SQLException {
        if(batchNum%10000 == 0){
            writeDB.pst.executeBatch();
        }
        writeDB.pst.setInt(1,row_id);
        writeDB.pst.setString(2,shop_id);
        writeDB.pst.addBatch();
        batchNum++;
        System.out.println(batchNum);
    }
    /*检查即将预测的shop_id 是否在测试的mall_id中,不在的话此shop_id作废
    synchronized private static boolean isMatchMallId(String mall_id,String shop_id){
        boolean result = true;
        String sql = "select mall_id from shop_info where shop_id ='"+shop_id+"'";
        dbConnect.deliverSql(sql,false);
        try {
            ResultSet ret = dbConnect.pst.executeQuery();
            if(ret.next()){
                String tmp = ret.getString(1);
                if(!tmp.equals(mall_id))
                    result = false;
            }
            ret.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }*/
    //专属私有类,多线程
    private static class Multi extends Thread{
        private int rowUp;
        private int rowDown;
        //字典树头节点
        private NewDictTree head = null;
        @Override
        public void run(){
            //相关数据表名字
            String targetTableName = "user_info";
            String sourceTableName = "wifi";
            String moveTableName   = "move_wifi";
            //数据库连接
            DBConnect dbConnect = new DBConnect();
            DBConnect dbQuery = new DBConnect();
            DBConnect moveQuery = new DBConnect();
            String sql;
            sql = "select wifi_info from " + targetTableName + " where i_id >=" + rowDown + " and i_id <="+rowUp;
            dbConnect.deliverSql(sql,true);//连接test数据库,遍历读取每一条记录
            ResultSet ret = null;
            try {
                ret = dbConnect.pst.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int row_id = rowDown;
            //循环遍历ret结果集
            try {
                assert ret != null;
                while (ret.next()){
                    int sumWifi = 0;//有效分析的wifi计数
                    String[] wifiLists = ret.getString(1).split(";");
                    head = new NewDictTree();//每分析一行重新建立一个字典树
                    for (String wifiList : wifiLists) {
                        String[] wifi = wifiList.split("\\|");

                        //wifi的名字以及当前wifi的信号数值
                        String wifiName = wifi[0];
                        //检查wifiName是否在move_wifi数据表中
                        String moveSql = "select * from "+moveTableName+" where wifi_id='"+wifiName+"'";
                        moveQuery.deliverSql(moveSql,false);
                        ResultSet tmp = moveQuery.pst.executeQuery();
                        if(tmp.next()){
                            moveQuery.pst.close();
                            tmp.close();
                            continue;
                        }else {
                            moveQuery.pst.close();
                            tmp.close();
                        }
                        //不是移动wifi,继续分析
                        double wifiSignal = Double.valueOf(wifi[1]) + VALUE;
                        if(wifiSignal <= 110) //低于-90dm的忽略
                            continue;
                        //wifi可用于分析,sum++;
                        sumWifi++;
                        //查询wifi对应关联的店铺
                        sql = "select shop_id,dbm,min_dbm,max_dbm from " + sourceTableName + " where wifi_id = '" + wifiName +"' ";
                        dbQuery.deliverSql(sql,false);
                        ResultSet retQuery = dbQuery.pst.executeQuery();
                        //遍历
                        double min,max;
                        while(retQuery.next()) {
                            double retSignal = retQuery.getDouble(2);
                            String shop_id = retQuery.getString(1);
                            min = retQuery.getDouble(3);
                            max = retQuery.getDouble(4);
                            if(wifiSignal<min || wifiSignal>max)
                                continue;
                            double accuracy;
                            accuracy = (1-(Math.abs(wifiSignal-retSignal)/100));
                            addOrInsertTree(shop_id,accuracy, head);
                        }
                        //用完关闭
                        retQuery.close();
                        dbQuery.pst.close();
                    }
                    //数据分析查询完毕,接下来遍历字典树,取出有效节点
                    List<NewDictTree> outComeSet = new ArrayList<>();
                    travelNewDictTree(outComeSet,head);
                    //找到最优
                    NewDictTree best = findBestNode(outComeSet,sumWifi);
                    if(best==null)
                        writeToMySQL(row_id,"null");
                    else
                        writeToMySQL(row_id,best.getShop_id());
                    row_id++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //关闭数据库
            dbConnect.dbClose();
            dbQuery.dbClose();
            moveQuery.dbClose();
        }
        Multi(int rowDown,int rowUp){
            this.rowDown = rowDown;
            this.rowUp = rowUp;
        }
    }
}
