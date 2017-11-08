package ExtractWifi;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 2017-10-27.
 * 该类用于从原生的wifi列表字符串中抽取出所有的wifi信息,包括wifi的出现地理位置
 */
public class ExtractWifi {
    private static final String sourceTableName = "user_info";
    private static final String targetTableName = "wifi_shop";
    private static final int    VALUE           = 200;
    private static final int    BATCH           = 100000;

    public static void main(String[] args) throws SQLException {
        DBConnect dbConnect1 = new DBConnect();
        DBConnect dbConnect2 = new DBConnect();
        String shop_id;
        String wifi_id;
        int wifi_signal;
        String wifiListsInfo;
        ResultSet ret1;
        int idCount = 1;

        String sql1 = String.format("select * from %s",sourceTableName);
        dbConnect1.deliverSql(sql1,true);
        ret1 = dbConnect1.pst.executeQuery();

        String sql2 = "insert into " + targetTableName + " values(?,?,?,?,?,?)";
        dbConnect2.deliverSql(sql2, false);

        int x = 1;

        while(ret1.next()){
            shop_id = ret1.getString(3);//店铺名
            wifiListsInfo = ret1.getString(7);//wifi原生字符串
            double longitude = ret1.getDouble(5);//经度
            double latitude  = ret1.getDouble(6);//纬度
            String wifiLists[] = wifiListsInfo.split(";");//存储逗号分隔的wifi列表
            for (String wifiList : wifiLists) {
                String wifi[] = wifiList.split("\\|");
                wifi_id = wifi[0];
                wifi_signal = VALUE + Integer.parseInt(wifi[1]);
                dbConnect2.pst.setInt(1,idCount++);
                dbConnect2.pst.setString(2, wifi_id);
                dbConnect2.pst.setString(3, shop_id);
                dbConnect2.pst.setInt(4, wifi_signal);
                dbConnect2.pst.setDouble(5,longitude);
                dbConnect2.pst.setDouble(6,latitude);
                dbConnect2.pst.addBatch();
                //System.out.println("更新了数据");
            }
            x++;
            if(x%BATCH == 0){
                dbConnect2.pst.executeBatch();
                System.out.println(x);
            }
        }
        dbConnect2.pst.executeBatch();
        dbConnect1.dbClose();
        dbConnect2.dbClose();
    }
}
