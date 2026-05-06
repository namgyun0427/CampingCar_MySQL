import java.io.*;
import java.sql.*;

public class SqlScriptExecutor {
    public static void executeScript(Connection conn, String scriptPath) throws SQLException {
        try (InputStream is = SqlScriptExecutor.class.getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                
                sb.append(line);
                if (line.endsWith(";")) {
                    executeStatement(conn, sb.toString().replace(";", ""));
                    sb.setLength(0);
                }
            }
            conn.commit();
        } catch (IOException ex) {
            conn.rollback();
            throw new SQLException("스크립트 파일 읽기 실패: " + ex.getMessage());
        }
    }

    private static void executeStatement(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
