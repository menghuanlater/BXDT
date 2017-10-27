package MySQLAssist;

import java.sql.*;

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
    public PreparedStatement pst = null;

    public DBConnect(){
        try{
            Class.forName(name);
            connection = DriverManager.getConnection(url,user,password);
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
            if(fetchMode)
                pst.setFetchSize(Integer.MIN_VALUE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //获得查询之后的结果集
    public ResultSet getResultSet(){
        if(pst!=null)
            try {
                return pst.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return null;
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
