package ExtractWifi;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;

/**
 * Created on 2017-11-07.
 * 分析wifi热点,剔除所有可能的个人热点(移动热点)解决数据冲突
 * 如果采用经纬度判别法,误差不超过0.02
 */
public class AbandonMoveWifi {

    public static void main(String[] args) throws Exception{
        String travelTable = "all_wifi";
        String sourceTable = "wifi";
        String writeTable  = "move_wifi";

        DBConnect travelDB = new DBConnect();
        DBConnect sourceDB = new DBConnect();
        DBConnect writeDB  = new DBConnect();

        String travelSql = "select * from " + travelTable;
        String sourceSql;
        String writeSql = "insert into " + writeTable + " values(?)";

        travelDB.deliverSql(travelSql,true);
        ResultSet travelSet = travelDB.pst.executeQuery();

        writeDB.deliverSql(writeSql,false);

        int count = 1;

        while(travelSet.next()){
            String wifi_id = travelSet.getString(1);
            sourceSql = "select longitude,latitude from " + sourceTable + " where wifi_id = '"+wifi_id+"'";
            sourceDB.deliverSql(sourceSql,false);
            ResultSet sourceSet = sourceDB.pst.executeQuery();

            double maxLongitude,minLongitude,maxLatitude,minLatitude;
            maxLatitude = maxLongitude = 0;
            minLatitude = minLongitude = 360;

            double longitude,latitude;

            while(sourceSet.next()){
                longitude = sourceSet.getDouble(1);
                latitude  = sourceSet.getDouble(2);
                if(longitude>maxLongitude)
                    maxLongitude = longitude;
                if(longitude<minLongitude)
                    minLongitude = longitude;
                if(latitude>maxLatitude)
                    maxLatitude = latitude;
                if(latitude<minLatitude)
                    minLatitude = latitude;
            }

            if(maxLongitude-minLongitude >= 0.02 || maxLatitude-minLatitude>=0.02){
                writeDB.pst.setString(1,wifi_id);
                writeDB.pst.addBatch();
                count++;
            }

            if(count%10000 ==0)
                writeDB.pst.executeBatch();

            sourceDB.pst.close();
            sourceSet.close();
        }

        writeDB.pst.executeBatch();

        travelDB.dbClose();
        sourceDB.dbClose();
        writeDB.dbClose();
    }
}
