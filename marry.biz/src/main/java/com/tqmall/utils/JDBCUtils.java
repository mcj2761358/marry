package com.tqmall.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by Minutch on 2015-05-31.
 */
public class JDBCUtils {

    private static String driverName = "com.mysql.jdbc.Driver";  //加载JDBC驱动
    private static String dbURL = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull";  //连接服务器和数据库
    private static String userName = "root";  //默认用户名
    private static String userPwd = "";  //密码

    static {
        try {
            Class.forName(driverName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConn(){
        Connection dbConn =null;
        try {
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbConn;
    }

    public static void closeStmt(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeRs(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeStmtAndRs(Statement stmt,ResultSet rs){
        closeStmt(stmt);
        closeRs(rs);
    }
}
