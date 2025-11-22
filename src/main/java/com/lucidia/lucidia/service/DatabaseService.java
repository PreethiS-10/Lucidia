package com.lucidia.lucidia.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.lucidia.lucidia.model.DreamEntry;
import com.lucidia.lucidia.util.DatabaseConfig;

public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {}

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void initialize() throws SQLException {
        connection = DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
        System.out.println("Connected to PostgreSQL database!");
    }

    public int saveDreamEntry(DreamEntry dream) throws SQLException {
        String sql = "INSERT INTO dream_entries (user_id, dream_text, dream_date, sleep_quality, lucidity_level) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, dream.getUserId());
            stmt.setString(2, dream.getDreamText());
            stmt.setDate(3, Date.valueOf(dream.getDreamDate()));
            stmt.setInt(4, dream.getSleepQuality());
            stmt.setInt(5, dream.getLucidityLevel());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating dream entry failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    dream.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating dream entry failed, no ID obtained.");
                }
            }
        }
    }

    public List<DreamEntry> getAllDreams(int userId) throws SQLException {
        return getDreamEntries(null, "All Dreams", userId);
    }

    public List<DreamEntry> getDreamEntries(String searchText, String filter, int userId) throws SQLException {
        List<DreamEntry> dreams = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM dream_entries WHERE user_id = ?"
        );
        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);

        // Add search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND dream_text ILIKE ?");
            parameters.add("%" + searchText + "%");
        }

        // Add time filter
        if (filter != null) {
            switch (filter) {
                case "Last 7 Days":
                    sql.append(" AND dream_date >= CURRENT_DATE - INTERVAL '7 days'");
                    break;
                case "Last 30 Days":
                    sql.append(" AND dream_date >= CURRENT_DATE - INTERVAL '30 days'");
                    break;
                case "High Lucidity":
                    sql.append(" AND lucidity_level >= 3");
                    break;
                case "Vivid Dreams":
                    sql.append(" AND sleep_quality >= 8");
                    break;
                // "All Dreams" - no additional filter
            }
        }

        sql.append(" ORDER BY dream_date DESC");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DreamEntry dream = new DreamEntry();
                dream.setId(rs.getInt("id"));
                dream.setUserId(rs.getInt("user_id"));
                dream.setDreamText(rs.getString("dream_text"));
                dream.setDreamDate(rs.getDate("dream_date").toLocalDate());
                dream.setSleepQuality(rs.getInt("sleep_quality"));
                dream.setLucidityLevel(rs.getInt("lucidity_level"));
                dreams.add(dream);
            }
        }
        return dreams;
    }

    public boolean deleteDreamEntry(int dreamId) throws SQLException {
        String sql = "DELETE FROM dream_entries WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dreamId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Method to check if a dream exists
    public boolean dreamExists(int dreamId) throws SQLException {
        String sql = "SELECT 1 FROM dream_entries WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dreamId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}