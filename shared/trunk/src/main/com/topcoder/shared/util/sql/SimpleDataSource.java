package com.topcoder.shared.util.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class SimpleDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;

    public SimpleDataSource(String jdbcDriverClassName, String url) throws ClassNotFoundException {
        this(jdbcDriverClassName, url, null, null);
    }

    public SimpleDataSource(String jdbcDriverClassName, String url, String username, String password)
            throws ClassNotFoundException {
        if (url == null) {
            throw new NullPointerException("The url cannot be null");
        }
        Class.forName(jdbcDriverClassName);
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public final Connection getConnection() throws SQLException {
        return getConnection(username, password);
    }

    public final Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public final PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    public final void setLogWriter(PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    public final int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    public final void setLoginTimeout(int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

}
