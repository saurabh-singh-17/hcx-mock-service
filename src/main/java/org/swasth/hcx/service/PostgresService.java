package org.swasth.hcx.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.hcx.exception.ClientException;

import java.sql.*;

//@Service
public class PostgresService {

    @Value("${postgres.url}")
    private String url;

    @Value("${postgres.user}")
    private String user;

    @Value("${postgres.password}")
    private String password;
    private Connection connection;
    private Statement statement;

    public PostgresService(String url, String user, String password) throws ClientException {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private void initializeConnection() throws ClientException {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }

    public Connection getConnection() throws ClientException, SQLException {
        if (connection == null || connection.isClosed()) {
            initializeConnection();
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public boolean execute(String query) throws ClientException, SQLException {
        try (Connection conn = getConnection()) {
            statement = conn.createStatement();
            return statement.execute(query);
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        } finally {
            close();
        }
    }

    public ResultSet executeQuery(String query) throws ClientException, SQLException {
        try (Connection conn = getConnection()) {
            statement = conn.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        } finally {
            close();
        }
    }

//    public void addBatch(String query) throws ClientException, SQLException {
//        Connection conn = getConnection();
//        try (Statement statement = conn.createStatement()) {
//            statement.addBatch(query);
//        } catch (Exception e) {
//            throw new ClientException("Error while performing database operation: " + e.getMessage());
//        }
//    }
//
//    public int[] executeBatch() throws ClientException {
//        try {
//            return statement.executeBatch();
//        } catch (Exception e) {
//            throw new ClientException("Error while performing database operation: " + e.getMessage());
//        }
//    }

//    public boolean isHealthy() {
//        try {
//            Connection conn = getConnection();
//            conn.close();
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

}
