package server;

import java.sql.*;

public class TestServiceBase implements AuthServiceBase {
    private static Connection connection;
    private static Statement stmt;

//    public static void main(String[] args) {
//        try {
//            connect();
//            System.out.println("connect_ok");
//          System.out.println(getNicknameByLoginAndPassword("aaa", "aaa"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            disconnect();
//        }
//
//    }

    @Override
    public void connect() throws Exception {
        // загрузчик классов
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:login5.db");
        stmt = connection.createStatement();
    }

    @Override
    public void disconnect() {
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM auth");
        while (rs.next()) {
            if (rs.getString("login").equals(login) && rs.getString("password").equals(password))
                return rs.getString("nickname");
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM auth");
        while (rs.next()) {
            if (rs.getString("login").equals(login) && rs.getString("password").equals(password))
                return false;
        }
        stmt.executeUpdate("INSERT INTO auth (login, password, nickname) VALUES ('login','password','nickname');");
        return true;
    }

    public void exInsert() throws SQLException {
        stmt.executeUpdate("INSERT INTO auth (login, password) VALUES ('uuu','uuu');");
    }

}
