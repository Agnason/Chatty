package server;

import java.sql.*;

public class AuthServiceBase {

    private Connection connection;
    private  Statement stmt;
    private  PreparedStatement psInsert;



    public void batchTable() throws SQLException {
        long begin = System.currentTimeMillis();
        connection.setAutoCommit(false);
        for (int i = 0; i < 1000; i++) {
            psInsert.setString(1, "Bob" + i);
            psInsert.setInt(2, i * 15 % 100);
            psInsert.addBatch();
        }
        psInsert.executeBatch();
        connection.setAutoCommit(true);
        long end = System.currentTimeMillis();
        System.out.printf("Time: %d ms", end - begin);
    }


    public void fillTable() throws SQLException {
        long begin = System.currentTimeMillis();
        connection.setAutoCommit(false);
        for (int i = 0; i < 1000; i++) {
            psInsert.setString(1, "Bob" + i);
            psInsert.setInt(2, i * 15 % 100);
            psInsert.executeUpdate();
        }
        connection.setAutoCommit(true);
        long end = System.currentTimeMillis();
        System.out.printf("Time: %d ms", end - begin);
    }

    public void prepareAllStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO students (name, score) VALUES (?,?);");
    }

    // CRUD create read update delete
    public void exSelect() throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT login, password FROM auth;");
        while (rs.next()) {
            System.out.println(rs.getString("name") + " " + rs.getInt("score"));
        }
        rs.close();
    }


    public void clearTable() throws SQLException {
        stmt.executeUpdate("DELETE FROM students;");
    }

    public void exDelete() throws SQLException {
        stmt.executeUpdate("DELETE FROM students WHERE score ==100;");
    }

    public void exUpdate() throws SQLException {
        stmt.executeUpdate("UPDATE students SET score =100 WHERE score >100;");
    }

    public void exInsert() throws SQLException {
        stmt.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob4',70);");
    }

    public void connect() throws Exception {
        // загрузчик классов
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:login.db");
        stmt = connection.createStatement();
    }

    public void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}



