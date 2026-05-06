import javax.swing.*;
import java.awt.*;

public class UserMainFrame extends JFrame {
    private String username;

    public UserMainFrame(String username) {
        this.username = username;
        setTitle(username + "님 환영합니다");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel welcomeLabel = new JLabel(username + "님 환영합니다!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(51, 102, 153));

        JButton btnSearch = new JButton("캠핑카 검색/대여");
        JButton btnRental = new JButton("내 대여 관리");
        JButton btnRepair = new JButton("정비 신청");
        JButton btnLogout = new JButton("로그아웃");

        Font btnFont = new Font("맑은 고딕", Font.BOLD, 16);
        Color btnColor = new Color(51, 102, 153);
        
        JButton[] buttons = {btnSearch, btnRental, btnRepair, btnLogout};
        for (JButton btn : buttons) {
            btn.setFont(btnFont);
            btn.setBackground(btnColor);
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(200, 50));
        }

        btnLogout.setBackground(new Color(51, 102, 153));

        btnSearch.addActionListener(e -> new CamperRentalFrame(username).setVisible(true));
        btnRental.addActionListener(e -> new RentalManageFrame(username).setVisible(true));
        btnRepair.addActionListener(e -> new RepairRequestFrame(username).setVisible(true));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        panel.add(welcomeLabel);
        panel.add(btnSearch);
        panel.add(btnRental);
        panel.add(btnRepair);
        panel.add(new JLabel());
        panel.add(btnLogout);

        add(panel);
    }
}
