package javaex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class App {
    private JFrame frame;
    private JTextField postTitleField;
    private JTextArea postContentArea;
    private JList<String> postList;
    private DefaultListModel<String> listModel;
    private ArrayList<Post> posts;
    private String currentUser;
    private JPanel loginPanel;
    private JPanel communityPanel;
    private JPanel signUpPanel;

    // 파일 경로
    private final String userFile = "A:\\javacode\\javaex\\DataFile\\user.txt";
    private final String postFile = "A:\\javacode\\javaex\\DataFile\\data.txt";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                App window = new App();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public App() {
        posts = new ArrayList<>();
        loadPosts();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("대학교 커뮤니티 앱");
        frame.setBounds(100, 100, 500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new CardLayout());
        frame.setLocationRelativeTo(null);

        // 로그인 화면
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        
        JLabel idLabel = new JLabel("아이디");
        JTextField idField = new JTextField(7);

        JLabel pwLabel = new JLabel("비밀번호");
        JPasswordField passwordField = new JPasswordField(7);

        JButton loginButton = new JButton("로그인");
        loginButton.addActionListener(e -> {
            String userId = idField.getText();
            String password = new String(passwordField.getPassword());
            if (validateLogin(userId, password)) {
                currentUser = userId;
                showCommunityPanel();
            } else {
                JOptionPane.showMessageDialog(frame, "아이디나 비밀번호가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton signUpButton = new JButton("회원가입");
        signUpButton.addActionListener(e -> showSignUpPanel());

        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(pwLabel);
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(signUpButton);

        // 회원가입 화면
        signUpPanel = new JPanel();
        signUpPanel.setLayout(new GridBagLayout());

        JLabel newIdLabel = new JLabel("아이디");
        JTextField newIdField = new JTextField(7);

        JLabel newPwLabel = new JLabel("비밀번호");
        JPasswordField newPasswordField = new JPasswordField(7);

        JButton registerButton = new JButton("회원가입");
        registerButton.addActionListener(e -> {
            String newId = newIdField.getText();
            String newPassword = new String(newPasswordField.getPassword());
            if (registerUser(newId, newPassword)) {
                JOptionPane.showMessageDialog(frame, "회원가입 성공", "회원가입", JOptionPane.INFORMATION_MESSAGE);
                showLoginPanel();
            } else {
                JOptionPane.showMessageDialog(frame, "이미 존재하는 아이디입니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        signUpPanel.add(newIdLabel);
        signUpPanel.add(newIdField);
        signUpPanel.add(newPwLabel);
        signUpPanel.add(newPasswordField);
        signUpPanel.add(registerButton);

        // 커뮤니티 화면
        communityPanel = new JPanel();
        communityPanel.setLayout(new BorderLayout());

        // 게시글 작성 폼
        JPanel postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));

        JLabel postTitleLabel = new JLabel("제목");
        postTitleField = new JTextField();
        postTitleField.setColumns(30);

        JLabel postContentLabel = new JLabel("내용");
        postContentArea = new JTextArea(5, 30);
        JScrollPane contentScrollPane = new JScrollPane(postContentArea);

        JButton postButton = new JButton("게시글 작성");
        postButton.addActionListener(e -> createPost());

        postPanel.add(postTitleLabel);
        postPanel.add(postTitleField);
        postPanel.add(postContentLabel);
        postPanel.add(contentScrollPane);
        postPanel.add(postButton);

        // 게시글 목록
        listModel = new DefaultListModel<>();
        postList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(postList);

        postList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = postList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Post clickedPost = posts.get(index);
                        showPostDetails(clickedPost);
                    }
                }
            }
        });

        JButton refreshButton = new JButton("목록 갱신");
        refreshButton.addActionListener(e -> refreshPostList());

        JButton deleteButton = new JButton("게시글 삭제");
        deleteButton.addActionListener(e -> deletePost());

        // 커뮤니티 화면에 게시글 목록 추가
        communityPanel.add(postPanel, BorderLayout.NORTH);
        communityPanel.add(listScrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(refreshButton);
        bottomPanel.add(deleteButton);
        communityPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(loginPanel, "login");
        frame.getContentPane().add(signUpPanel, "signup");
        frame.getContentPane().add(communityPanel, "community");

        // 로그인 화면 표시
        showLoginPanel();
    }

    private boolean validateLogin(String userId, String password) {
        // user.txt 파일에서 아이디와 비밀번호 검증
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials[0].equals(userId) && credentials[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean registerUser(String userId, String password) {
        // user.txt 파일에 새로운 사용자 추가
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials[0].equals(userId)) {
                    return false;  // 이미 존재하는 아이디
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile, true))) {
            writer.write(userId + "," + password + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showLoginPanel() {
        CardLayout cardLayout = (CardLayout) frame.getContentPane().getLayout();
        cardLayout.show(frame.getContentPane(), "login");
    }

    private void showSignUpPanel() {
        CardLayout cardLayout = (CardLayout) frame.getContentPane().getLayout();
        cardLayout.show(frame.getContentPane(), "signup");
    }

    private void showCommunityPanel() {
        CardLayout cardLayout = (CardLayout) frame.getContentPane().getLayout();
        cardLayout.show(frame.getContentPane(), "community");
    }

    private void createPost() {
        String title = postTitleField.getText().trim();
        String content = postContentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "제목과 내용을 모두 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 게시글 추가
        Post post = new Post(title, content);
        posts.add(post);
        savePosts();
        clearForm();
        JOptionPane.showMessageDialog(frame, "게시글이 작성되었습니다.");
    }

    private void clearForm() {
        postTitleField.setText("");
        postContentArea.setText("");
    }

    private void refreshPostList() {
        listModel.clear();
        for (Post post : posts) {
            listModel.addElement(post.getTitle());
        }
    }

    private void showPostDetails(Post post) {
        new PostDetailsWindow(post, this);  // App 인스턴스를 전달
    }


    private void loadPosts() {
        // data.txt에서 게시글 로드 (좋아요 수와 댓글 포함)
        try (BufferedReader reader = new BufferedReader(new FileReader(postFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] postData = line.split(",", 4);
                if (postData.length == 4) {
                    String title = postData[0];
                    String content = postData[1];
                    int likes = Integer.parseInt(postData[2]);
                    ArrayList<String> comments = new ArrayList<>();
                    if (!postData[3].isEmpty()) {
                        String[] commentArray = postData[3].split(";");
                        for (String comment : commentArray) {
                            comments.add(comment);
                        }
                    }
                    posts.add(new Post(title, content, likes, comments));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void savePosts() {
        // data.txt에 게시글 저장 (좋아요 수와 댓글 포함)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(postFile))) {
            for (Post post : posts) {
                writer.write(post.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePost() {
        // 게시글 삭제 처리
        int index = postList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(frame, "삭제할 게시글을 선택하세요.", "삭제 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String password = JOptionPane.showInputDialog(frame, "비밀번호를 입력하세요.");
        if ("1235".equals(password)) {
            posts.remove(index);
            savePosts();
            refreshPostList();
            JOptionPane.showMessageDialog(frame, "게시글이 삭제되었습니다.");
        } else {
            JOptionPane.showMessageDialog(frame, "잘못된 비밀번호입니다.", "삭제 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class Post {
    private String title;
    private String content;
    private int likes;
    private ArrayList<String> comments;

    public Post(String title, String content, int likes, ArrayList<String> comments) {
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    public Post(String title, String content) {
        this(title, content, 0, new ArrayList<>());
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void addLike() {
        likes++;
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    // 게시글 저장을 위한 toString() 메서드
    @Override
    public String toString() {
        StringBuilder commentsStr = new StringBuilder();
        for (String comment : comments) {
            commentsStr.append(comment).append(";");
        }
        return title + "," + content + "," + likes + "," + commentsStr.toString();
    }
}



class PostDetailsWindow {
    private JFrame detailsFrame;
    private JTextArea postDetailsArea;
    private JButton likeButton;
    private JTextArea commentArea;
    private JButton commentButton;
    private JButton viewCommentsButton;
    private App app;  // App 인스턴스 추가

    public PostDetailsWindow(Post post, App app) {
        this.app = app; // App 인스턴스를 받음
        detailsFrame = new JFrame(post.getTitle());
        detailsFrame.setBounds(200, 200, 400, 300);
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 게시글 내용과 좋아요 수 표시
        postDetailsArea = new JTextArea(post.getContent() + "\n\n좋아요: " + post.getLikes());
        postDetailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(postDetailsArea);

        // 좋아요 버튼
        likeButton = new JButton("좋아요");
        likeButton.addActionListener(e -> {
            post.addLike();
            postDetailsArea.setText(post.getContent() + "\n\n좋아요: " + post.getLikes());
            app.savePosts(); // 좋아요 수가 변경될 때마다 게시글 저장
        });

        // 댓글 입력 필드
        commentArea = new JTextArea(3, 30);
        JScrollPane commentScrollPane = new JScrollPane(commentArea);

        // 댓글 작성 버튼
        commentButton = new JButton("댓글 작성");
        commentButton.addActionListener(e -> {
            String comment = commentArea.getText().trim();
            if (!comment.isEmpty()) {
                post.addComment(comment);
                commentArea.setText("");
                JOptionPane.showMessageDialog(detailsFrame, "댓글이 작성되었습니다.");
                app.savePosts(); // 댓글 추가 시 게시글 저장
            }
        });

        // 댓글 보기 버튼
        viewCommentsButton = new JButton("댓글 보기");
        viewCommentsButton.addActionListener(e -> {
            showComments1(post);
        });

        // 하단 패널에 좋아요, 댓글 입력, 댓글 작성 버튼 추가
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(likeButton, BorderLayout.NORTH);
        bottomPanel.add(commentScrollPane, BorderLayout.CENTER);
        bottomPanel.add(commentButton, BorderLayout.SOUTH);

        // 게시글 내용과 하단 버튼을 메인 프레임에 추가
        detailsFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        detailsFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        detailsFrame.getContentPane().add(viewCommentsButton, BorderLayout.EAST);

        detailsFrame.setVisible(true);
    }

    // 댓글을 볼 수 있는 새 창
    private void showComments1(Post post) {
        JFrame commentFrame = new JFrame("댓글 보기");
        commentFrame.setBounds(250, 250, 300, 200);
        commentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea commentArea = new JTextArea();
        commentArea.setEditable(false);
        for (String comment : post.getComments()) {
            commentArea.append(comment + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(commentArea);
        commentFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        commentFrame.setVisible(true);
    }



    // 댓글을 볼 수 있는 새 창
    private void showComments(Post post) {
        JFrame commentFrame = new JFrame("댓글 보기");
        commentFrame.setBounds(250, 250, 300, 200);
        commentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea commentArea = new JTextArea();
        commentArea.setEditable(false);
        for (String comment : post.getComments()) {
            commentArea.append(comment + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(commentArea);
        commentFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        commentFrame.setVisible(true);
    }
}

