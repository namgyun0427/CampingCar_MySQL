import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/camping_db";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC 드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC 드라이버 로드 실패: " + e.getMessage());
        }
    }
    
    public static Connection getAdminConnection() throws SQLException {
        return DriverManager.getConnection(URL, "root", "1234");
    }
    
    public static Connection getUserConnection() throws SQLException {
        return DriverManager.getConnection(URL, "user1", "user1");
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("연결 종료 오류: " + e.getMessage());
            }
        }
    }
}
