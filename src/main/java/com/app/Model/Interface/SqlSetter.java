package com.app.Model.Interface;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlSetter {
    void set(PreparedStatement ps) throws SQLException;
}
