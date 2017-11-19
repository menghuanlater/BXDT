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
        String testTable     = "user_info";
        String resultTable   = "result_train";

        DBConnect db1 = new DBConnect();
        DBConnect db2 = new DBConnect();

        String sql1,sql2;
        sql1 = "select * from " + resultTable;
        db1.deliverSql(sql1,true);
        ResultSet resultSet = db1.pst.executeQuery();

        while (resultSet.next()){
            String shop_id = resultSet.getString(2);
            int id = resultSet.getInt(1);
            sql2 = "select shop_id from "+testTable+" where i_id = "+id;
            db2.deliverSql(sql2,false);
            ResultSet ret = db2.pst.executeQuery();
            if(ret.next()){
                String tmp = ret.getString(1);
                if(!tmp.equals(shop_id)) {
                    errorCount++;
                    //System.out.println("you:"+shop_id+"\t actual:"+tmp);
                }
            }
            db2.pst.close();
            ret.close();
        }
        System.out.println(errorCount);
        db1.dbClose();
        db2.dbClose();

    }
}
