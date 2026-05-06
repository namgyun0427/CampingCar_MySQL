import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultSetToTableModel {
    private List<Object[]> data = new ArrayList<>();
    private String[] columnNames;

    public ResultSetToTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i-1] = metaData.getColumnName(i);
        }

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i-1] = rs.getObject(i);
            }
            data.add(row);
        }
    }

    public Object[][] getData() {
        return data.toArray(new Object[0][]);
    }

    public String[] getColumnNames() {
        return columnNames;
    }
}
