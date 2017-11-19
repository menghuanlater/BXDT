package MySQLAssist;

import java.sql.*;

/**
 * Created on 2017-10-26.
 * MySQL数据库连接
 */
public class DBConnect {
    private static final String url = "jdbc:mysql://localhost:3306/bxdt?characterEncoding=utf-8&useSSL=true&rewriteBatchedStatements=true";
    private static final String name = "com.mysql.jdbc.Driver";
    private static final String user = "root";
    private static final String password = "root";


    private Connection connection = null;
    public PreparedStatement pst = null;
    public Statement stmt;

    public DBConnect(){
        try{
            Class.forName(name);
            connection = DriverManager.getConnection(url,user,password);
            stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    //传入sql语句,递交执行
    /**
     * @param fetchMode : if is true, setFetchSize(Integer.MIN_VALUE)
     */
    public void deliverSql(String sql,boolean fetchMode){
        try {
            pst = connection.prepareStatement(sql);
            if(fetchMode) {
                pst.setFetchSize(Integer.MIN_VALUE);
                pst.setFetchDirection(ResultSet.FETCH_REVERSE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
