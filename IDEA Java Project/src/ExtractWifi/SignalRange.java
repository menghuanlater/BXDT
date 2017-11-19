package ExtractWifi;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 2017-11-17.
 * 确定wifi信号的波动范围
 */
public class SignalRange {
    public static void main(String[] args) throws SQLException {
        String source = "wifi_x";
        String object = "wifi";

        DBConnect dbConnect1 = new DBConnect();
        DBConnect dbConnect2 = new DBConnect();
        DBConnect dbConnect3 = new DBConnect();

        String sql1 = "select wifi_id,shop_id from " + object;
        dbConnect1.deliverSql(sql1,true);
        ResultSet resultSet = dbConnect1.pst.executeQuery();

        int count = 0;

        while(resultSet.next()){
            String wifi_id = resultSet.getString(1);
            String shop_id = resultSet.getString(2);
            String sql2 = "select min(dbm),max(dbm) from " + source + " where wifi_id='"+
                    wifi_id+"' and shop_id = '"+shop_id+"'";
            dbConnect2.deliverSql(sql2,false);
            ResultSet resultSet1 = dbConnect2.pst.executeQuery();
            double min=0.0,max = 0.0;
            if(resultSet1.next()){
                min = resultSet1.getDouble(1);
                max = resultSet1.getDouble(2);
            }
            String sql3 = "update " + object + " set min = " + min +",max = "+max+" where wifi_id = '"+
                    wifi_id+"' and shop_id = '"+shop_id+"'";
            dbConnect3.stmt.addBatch(sql3);
            count++;
            if(count%100000==0){
                dbConnect3.stmt.executeBatch();
                dbConnect3.stmt.clearBatch();
            }
            dbConnect2.pst.close();
            resultSet1.close();
            System.out.println(count);
        }

        dbConnect1.dbClose();
        dbConnect2.dbClose();
        dbConnect3.dbClose();
    }
}
