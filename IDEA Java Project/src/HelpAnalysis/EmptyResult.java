package HelpAnalysis;

import MySQLAssist.DBConnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 2017-11-08.
 * 检查有多少结果为null
 */
public class EmptyResult {
    public static void main(String[] args) throws SQLException{
        String sql = "select Count(*) from result where shop_id='null'";
        DBConnect dbConnect = new DBConnect();
        dbConnect.deliverSql(sql,true);

        ResultSet resultSet = dbConnect.pst.executeQuery();
        if(resultSet.next())
            System.out.println(resultSet.getInt(1));
        dbConnect.dbClose();
    }
}
