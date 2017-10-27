package ExtractWifi;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 2017-10-27.
 * 该类用于从原生的wifi列表字符串中抽取出所有的wifi信息
 */
public class ExtractWifi {
    private static final String sourceTableName = "user_info";
    private static final String targetTableName = "wifi_shop";
    private static final int    VALUE           = 200;

    public static void main(String[] args) throws SQLException {
        DBConnect dbConnect1 = new DBConnect();
        DBConnect dbConnect2 = new DBConnect();
        String shop_id;
        String wifi_id;
        int wifi_signal;
        String wifiListsInfo;
        ResultSet ret1,ret2;
        //int idCount = 1;

        String sql1 = String.format("select * from %s",sourceTableName);
        dbConnect1.deliverSql(sql1,true);
        ret1 = dbConnect1.getResultSet();
        if(ret1==null){
            System.exit(0);
        }
        while(ret1.next()){
            shop_id = ret1.getString(3);//店铺名
            wifiListsInfo = ret1.getString(7);//wifi原生字符串
            String wifiLists[] = wifiListsInfo.split(";");//存储逗号分隔的wifi列表
            for (String wifiList : wifiLists) {
                String wifi[] = wifiList.split("\\|");
                wifi_id = wifi[0];
                wifi_signal = VALUE + Integer.parseInt(wifi[1]);
                String sql2 = "select * from " + targetTableName + " where wifi_id = '" + wifi_id + "' and " +
                        "shop_id = '" + shop_id + "'";
                dbConnect2.deliverSql(sql2, false);
                ret2 = dbConnect2.getResultSet();
                if (ret2.next()) {
                    wifi_signal = (wifi_signal + ret2.getInt(4)) / 2;
                    sql2 = "update " + targetTableName + " set wifi_signal=" + wifi_signal + " where wifi_id = '" + wifi_id + "' and " +
                            "shop_id = '" + shop_id + "'";
                    dbConnect2.deliverSql(sql2, false);
                    dbConnect2.pst.executeUpdate();//更新数据
                } else {
                    sql2 = "insert into " + targetTableName + "(wifi_id,shop_id,signal) values(?,?,?)";
                    dbConnect2.deliverSql(sql2, false);
                    dbConnect2.pst.setString(1, wifi_id);
                    dbConnect2.pst.setString(2, shop_id);
                    dbConnect2.pst.setInt(3, wifi_signal);
                }
                ret2.close();
            }
        }
        dbConnect1.dbClose();
        dbConnect2.dbClose();
    }
}
