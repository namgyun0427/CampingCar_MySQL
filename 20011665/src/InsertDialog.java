import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsertDialog extends JDialog {
    private boolean saved = false;
    private JTextField[] inputFields;
    private String[] columnNames;
    private String[] columnTypes;

    public InsertDialog(JFrame parent, String tableName) {
        super(parent, "데이터 입력 - " + tableName, true);
        setSize(500, 600);
        setLocationRelativeTo(parent);

        
        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color veryLightGray = new Color(245, 245, 245);

        getContentPane().setBackground(veryLightGray);

        try (Connection conn = DBConnection.getAdminConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1")) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            columnNames = new String[columnCount];
            columnTypes = new String[columnCount];
            
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = meta.getColumnName(i + 1);
                columnTypes[i] = meta.getColumnTypeName(i + 1);
            }

            // 입력 패널
            JPanel inputPanel = new JPanel(new GridBagLayout());
            inputPanel.setBackground(Color.WHITE);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            inputFields = new JTextField[columnCount];

            for (int i = 0; i < columnCount; i++) {
                gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
                JLabel label = new JLabel(columnNames[i] + " (" + columnTypes[i] + "):");
                label.setForeground(darkGray);
                inputPanel.add(label, gbc);

                gbc.gridx = 1; gbc.weightx = 1;
                inputFields[i] = new JTextField(20);
                inputPanel.add(inputFields[i], gbc);
            }

            // 예시 데이터 제공 버튼
            JButton exampleBtn = new JButton("예시 데이터 채우기");
            exampleBtn.setBackground(Color.LIGHT_GRAY);
            exampleBtn.setForeground(darkGray);
            exampleBtn.setFocusPainted(false);
            exampleBtn.addActionListener(e -> fillExampleData(tableName));

            // 버튼 패널
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.setBackground(veryLightGray);
            
            JButton saveBtn = new JButton("저장");
            JButton cancelBtn = new JButton("취소");

            saveBtn.setBackground(blueColor);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setPreferredSize(new Dimension(80, 35));
            saveBtn.setFocusPainted(false);

            cancelBtn.setBackground(Color.GRAY);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setPreferredSize(new Dimension(80, 35));
            cancelBtn.setFocusPainted(false);

            btnPanel.add(exampleBtn);
            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);

            saveBtn.addActionListener(e -> saveData(tableName));
            cancelBtn.addActionListener(e -> {
                saved = false;
                dispose();
            });

            setLayout(new BorderLayout(10, 10));
            add(new JScrollPane(inputPanel), BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "테이블 정보를 불러오는 중 오류 발생: " + ex.getMessage());
            dispose();
        }
    }

    private void fillExampleData(String tableName) {
        
        switch (tableName) {
            case "Customer":
                if (inputFields.length >= 10) {
                    inputFields[0].setText("CUST0013");
                    inputFields[1].setText("user13");
                    inputFields[2].setText("pass13");
                    inputFields[3].setText("13-56-789012-34");
                    inputFields[4].setText("홍길동");
                    inputFields[5].setText("서울시 강남구");
                    inputFields[6].setText("010-1111-2222");
                    inputFields[7].setText("hong@test.com");
                    inputFields[8].setText("2023-12-01");
                    inputFields[9].setText("가족형");
                }
                break;
            case "Company":
                if (inputFields.length >= 6) {
                    inputFields[0].setText("13");
                    inputFields[1].setText("테스트캠핑카");
                    inputFields[2].setText("서울시 강남구");
                    inputFields[3].setText("02-1111-2222");
                    inputFields[4].setText("홍관리");
                    inputFields[5].setText("test@company.com");
                }
                break;
            case "Camper":
                if (inputFields.length >= 10) {
                    inputFields[0].setText("13");
                    inputFields[1].setText("테스트 캠핑카");
                    inputFields[2].setText("서울999테9999");
                    inputFields[3].setText("4");
                    inputFields[4].setText("test.jpg");
                    inputFields[5].setText("테스트용 캠핑카");
                    inputFields[6].setText("100000.00");
                    inputFields[7].setText("1");
                    inputFields[8].setText("2023-12-01");
                    inputFields[9].setText("Available");
                }
                break;
        }
    }

    private void saveData(String tableName) {
        List<String> values = new ArrayList<>();
        
        for (int i = 0; i < inputFields.length; i++) {
            String val = inputFields[i].getText().trim();
            
            if (val.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    columnNames[i] + " 값을 입력하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
                inputFields[i].requestFocus();
                return;
            }

            // 데이터 타입에 따른 값 검증
            try {
                if (isNumericType(columnTypes[i])) {
                    // 숫자 타입 검증
                    if (columnTypes[i].toLowerCase().contains("decimal") || 
                        columnTypes[i].toLowerCase().contains("double")) {
                        Double.parseDouble(val);
                    } else {
                        Integer.parseInt(val);
                    }
                    values.add(val);
                } else if (isDateType(columnTypes[i])) {
                    // 날짜 타입 검증 (간단한 형태만)
                    if (!val.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)");
                    }
                    values.add("'" + val + "'");
                } else {
                    // 문자열 타입 - 작은따옴표 이스케이핑
                    values.add("'" + val.replace("'", "''") + "'");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    columnNames[i] + "에는 숫자를 입력하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
                inputFields[i].requestFocus();
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, 
                    columnNames[i] + ": " + e.getMessage(),
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
                inputFields[i].requestFocus();
                return;
            }
        }

        String valuesStr = String.join(", ", values);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "다음 데이터를 입력하시겠습니까?\n\n" + valuesStr,
            "입력 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getAdminConnection()) {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    String sql = "INSERT INTO " + tableName + " VALUES (" + valuesStr + ")";
                    stmt.executeUpdate(sql);
                    conn.commit();
                    saved = true;
                    JOptionPane.showMessageDialog(this, "데이터가 성공적으로 입력되었습니다.");
                    dispose();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg.contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(this, 
                        "중복된 값입니다. 고유한 값을 입력하세요.",
                        "제약 조건 위반", JOptionPane.ERROR_MESSAGE);
                } else if (errorMsg.contains("cannot be null")) {
                    JOptionPane.showMessageDialog(this, 
                        "필수 값이 누락되었습니다.",
                        "제약 조건 위반", JOptionPane.ERROR_MESSAGE);
                } else if (errorMsg.contains("foreign key constraint")) {
                    JOptionPane.showMessageDialog(this, 
                        "참조하는 데이터가 존재하지 않습니다.",
                        "제약 조건 위반", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "입력 실패: " + errorMsg,
                        "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private boolean isNumericType(String type) {
        String lowerType = type.toLowerCase();
        return lowerType.contains("int") || lowerType.contains("decimal") || 
               lowerType.contains("double") || lowerType.contains("float") ||
               lowerType.contains("numeric");
    }

    private boolean isDateType(String type) {
        String lowerType = type.toLowerCase();
        return lowerType.contains("date") || lowerType.contains("time");
    }

    public boolean isSaved() {
        return saved;
    }
}
