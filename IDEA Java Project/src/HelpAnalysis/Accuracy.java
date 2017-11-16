package HelpAnalysis;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 2017-11-15.
 * 分析预测的shop_id与实际的mall_id 是否一致
 */
public class Accuracy {
    private static int errorCount = 0;//错误预测计数器
    public static void main(String[] args) throws SQLException {
        String shopInfoTable = "shop_info";
        String testTable     = "test";
        String resultTable   = "result";

        DBConnect db1 = new DBConnect();
        DBConnect db2 = new DBConnect();
        DBConnect db3 = new DBConnect();

        String sql1,sql2,sql3;
        sql1 = "select shop_id from " + resultTable;
        db1.deliverSql(sql1,true);
        ResultSet resultSet = db1.pst.executeQuery();

        sql2 = "select mall_id from " + testTable;
        db2.deliverSql(sql2,true);
        ResultSet resultSet1 = db2.pst.executeQuery();

        while (resultSet.next() && resultSet1.next()){
            String shop_id = resultSet.getString(1);
            if(shop_id.equals("null"))
                continue;
            String mall_id = resultSet1.getString(1);

            sql3 = "select mall_id from " + shopInfoTable + " where shop_id = '"+shop_id+"'";
            db3.deliverSql(sql3,false);
            ResultSet tmp = db3.pst.executeQuery();

            if(tmp.next()){
                String haha = tmp.getString(1);
                if(!haha.equals(mall_id)){
                    System.out.print("测试mall:"+mall_id);
                    System.out.print("\t预测shop:"+shop_id);
                    System.out.println("\t预测所在mall:"+haha);
                    errorCount++;
                }
            }

            db3.pst.close();
            tmp.close();
        }
        System.out.println(errorCount);
        db1.dbClose();
        db2.dbClose();
        db3.dbClose();

    }
}
