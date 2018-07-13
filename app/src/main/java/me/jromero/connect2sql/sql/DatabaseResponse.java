package me.jromero.connect2sql.sql;

import java.sql.Statement;

public class DatabaseResponse {
    private boolean mSuccessful;
    private String mErrorText;
    private String mSqlState;
    private Statement mStatement;

    public static DatabaseResponse success(Statement stmt) {
        DatabaseResponse response = new DatabaseResponse();
        response.mSuccessful = true;
        response.mStatement = stmt;
        return response;
    }

    public static DatabaseResponse error(String errorText) {
        return error(errorText, null);
    }

    public static DatabaseResponse error(String errorText, String sqlState) {
        DatabaseResponse response = new DatabaseResponse();
        response.mSuccessful = false;
        response.mErrorText = errorText;
        response.mSqlState = sqlState;
        return response;
    }

    private DatabaseResponse() {
    }

    public boolean isSuccessful() {
        return mSuccessful;
    }

    public String getErrorText() {
        return mErrorText;
    }

    public String getSqlState() {
        return mSqlState;
    }

    public Statement getStatement() {
        return mStatement;
    }
}
