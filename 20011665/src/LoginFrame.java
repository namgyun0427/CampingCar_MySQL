import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername = new JTextField(20);
    private JPasswordField txtPassword = new JPasswordField(20);
    private JRadioButton rbAdmin = new JRadioButton("관리자");
    private JRadioButton rbUser = new JRadioButton("일반 회원", true);

    public LoginFrame() {
        setTitle("캠핑카 예약 시스템 로그인");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("캠핑카 예약 시스템", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 102, 153));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        ButtonGroup group = new ButtonGroup();
        group.add(rbAdmin);
        group.add(rbUser);
        radioPanel.add(rbAdmin);
        radioPanel.add(rbUser);
        gbc.gridy = 1;
        mainPanel.add(radioPanel, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("아이디:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(txtPassword, gbc);

        JButton btnLogin = new JButton("로그인");
        btnLogin.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnLogin.setBackground(new Color(51, 102, 153));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(100, 35));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(btnLogin, gbc);

        // 관리자 라디오 버튼 선택 시 자동 로그인 처리
        rbAdmin.addItemListener(e -> {
            if (rbAdmin.isSelected()) {
                txtUsername.setText("root");
                txtPassword.setText("1234");
                txtUsername.setEnabled(false);
                txtPassword.setEnabled(false);
                // 자동 로그인 호출
                handleAdminLogin();
            }
        });

        rbUser.addItemListener(e -> {
            if (rbUser.isSelected()) {
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                txtUsername.setText("");
                txtPassword.setText("");
            }
        });

        btnLogin.addActionListener(e -> {
            if (rbAdmin.isSelected()) handleAdminLogin();
            else handleUserLogin();
        });

        add(mainPanel);
        setVisible(true);
    }

    private void handleAdminLogin() {
        try (Connection conn = DBConnection.getAdminConnection()) {
            new AdminMainFrame().setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "관리자 로그인 실패: " + ex.getMessage());
        }
    }

    private void handleUserLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }

        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM Customer WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                new UserMainFrame(username).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패: 아이디나 비밀번호를 확인하세요.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
