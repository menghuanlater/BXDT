package MySQLAssist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created on 2017-10-26.
 * MySQL数据库连接
 */
public class DBConnect {
    private static final String url = "jdbc:mysql://localhost:3306/bxdt?characterEncoding=utf-8&useSSL=true";
    private static final String name = "com.mysql.jdbc.Driver";
    private static final String user = "root";
    private static final String password = "root";


    private Connection connection = null;
    private PreparedStatement pst = null;

    public DBConnect(){
        try{
            Class.forName(name);
            connection = DriverManager.getConnection(url,user,password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    //传入sql语句,递交执行

    //关闭数据库
    public void dbClose(){
        try{
            this.connection.close();
            this.pst.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
