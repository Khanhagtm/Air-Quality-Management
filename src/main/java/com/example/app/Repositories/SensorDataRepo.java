package com.example.app.Repositories;

import com.example.app.Models.DailyStatistics;
import com.example.app.Models.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class SensorDataRepo {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public SensorDataRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void insertSensorData(String serialNumber, double value, int type) {
        String sql = "INSERT INTO sensor_datas (serial_number, sensor_value, sensor_type) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, serialNumber);
                preparedStatement.setDouble(2, value);
                preparedStatement.setInt(3, type);
                return preparedStatement;
            });
            System.out.println("Sensor data inserted successfully.");
        } catch (Exception e) {
            System.out.println("Error inserting sensor data: " + e.getMessage());
        }
    }

    public List<SensorData> getSensorDataBySerialNumber(String serialNumber) {
        String sql = "SELECT * FROM sensor_datas WHERE serial_number = ? ORDER BY record_time DESC LIMIT 17280";
        return jdbcTemplate.query(sql, new Object[]{serialNumber}, (rs, rowNum) -> new SensorData(
                rs.getString("serial_number"),
                rs.getTimestamp("record_time").toLocalDateTime(),
                rs.getDouble("sensor_value"),
                rs.getInt("sensor_type")
        ));
    }

    public Double findMaxValueByTypeAndSerialNumberAndDate(int type, String serialNumber, LocalDateTime now) {
        LocalDateTime oneDayBefore = now.minusDays(1);
        String sql = "SELECT MAX(sensor_value) FROM sensor_datas WHERE sensor_type = ? AND serial_number = ? AND record_time BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{type, serialNumber, oneDayBefore, now}, Double.class);
    }

    public Double findMinValueByTypeAndSerialNumberAndDate(int type, String serialNumber, LocalDateTime now) {
        LocalDateTime oneDayBefore = now.minusDays(1);
        String sql = "SELECT MIN(sensor_value) FROM sensor_datas WHERE sensor_type = ? AND serial_number = ? AND record_time BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{type, serialNumber, oneDayBefore, now}, Double.class);
    }

    public Double findAvgValueByTypeAndSerialNumberAndDate(int type, String serialNumber, LocalDateTime now) {
        LocalDateTime oneDayBefore = now.minusDays(1);
        String sql = "SELECT AVG(sensor_value) FROM sensor_datas WHERE sensor_type = ? AND serial_number = ? AND record_time BETWEEN ? AND ?";
        Double avgValue = jdbcTemplate.queryForObject(sql, new Object[]{type, serialNumber, oneDayBefore, now}, Double.class);

        // Làm tròn đến 1 chữ số thập phân
        if (avgValue != null) {
            BigDecimal bd = new BigDecimal(avgValue).setScale(1, RoundingMode.HALF_UP);
            avgValue = bd.doubleValue();
        }

        return avgValue;
    }

    public void insertDailyStatistics(String serialNumber, LocalDate date, int sensorType, double maxValue, double minValue, double avgValue) {
        String sql = "INSERT INTO daily_statistics (serial_number, date, sensor_type, max_value, min_value, avg_value) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, serialNumber);
                preparedStatement.setDate(2, java.sql.Date.valueOf(date));
                preparedStatement.setInt(3, sensorType);
                preparedStatement.setDouble(4, maxValue);
                preparedStatement.setDouble(5, minValue);
                preparedStatement.setDouble(6, avgValue);
                return preparedStatement;
            });
            System.out.println("Daily statistics inserted successfully.");
        } catch (Exception e) {
            System.out.println("Error inserting daily statistics: " + e.getMessage());
        }
    }

    public List<String> findAllSerialNumbers() {
        String sql = "SELECT DISTINCT serial_number FROM users";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<DailyStatistics> findStatisticsBySerialNumberAndDateRange(String serialNumber, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM daily_statistics WHERE serial_number = ? AND date BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new Object[]{serialNumber, startDate, endDate}, (rs, rowNum) -> new DailyStatistics(
                rs.getLong("id"),
                rs.getString("serial_number"),
                rs.getDate("date").toLocalDate(),
                rs.getInt("sensor_type"),
                rs.getDouble("max_value"),
                rs.getDouble("min_value"),
                rs.getDouble("avg_value")
        ));
    }

    public void updateDeviceInformation(String serialNumber, String newIp ,Timestamp recordIpTime) {
        String sql = "UPDATE device_informations SET current_ip = ? , record_ip_time = ? WHERE serial_number = ?";
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, newIp);
                preparedStatement.setTimestamp(2, recordIpTime);
                preparedStatement.setString(3, serialNumber);
                return preparedStatement;
            });
            System.out.println("Update IP address successfully.");
        } catch (Exception e) {
            System.out.println("Error update IP address: " + e.getMessage());
        }
    }

    public long getRecordIpTime(String serialNumber) {
        String sql = "SELECT record_ip_time FROM device_informations WHERE serial_number = ?";

        try {
            Timestamp result = jdbcTemplate.queryForObject(sql, new Object[]{serialNumber}, (rs, rowNum) -> rs.getTimestamp("record_ip_time"));
            if (result != null) {
                return result.getTime();
            }
        } catch (EmptyResultDataAccessException e) {
            // Handle the case when no rows are found
            System.out.println("No record found for serial number: " + serialNumber);
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Return a default value or handle it as you see fit
    }

    public void bulkInsertSensorData(List<Map<String, Object>> data) {
        String sql = "INSERT INTO sensor_datas (serial_number, sensor_type , sensor_value , record_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, data, data.size(), (ps, argument) -> {
            ps.setString(1, (String) argument.get("serialNumber"));
            ps.setInt(2, (int) argument.get("parameterType"));
            ps.setDouble(3, (Double) argument.get("value"));
            ps.setTimestamp(4, (Timestamp) argument.get("timestamp"));
        });
        System.out.println("INSERT DATA DONE!.........");
    }

    public void deleteDataOlderThanOneDay() {
        String sql = "DELETE FROM sensor_datas WHERE record_time < ?";
        String cutoff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        jdbcTemplate.update(sql, cutoff);
    }

    public List<SensorData> getSensorDataWithSamplingFrequency(String serialNumber, LocalDateTime startTime, LocalDateTime endTime, int intervalInSeconds) {
        String query = "SELECT t1.* " +
                "FROM sensor_datas t1 " +
                "JOIN (" +
                "  SELECT id, ROW_NUMBER() OVER (PARTITION BY sensor_type ORDER BY record_time) AS rn " +
                "  FROM sensor_datas " +
                "  WHERE serial_number = ? AND record_time BETWEEN ? AND ? " +
                ") t2 ON t1.id = t2.id " +
                "WHERE (t2.rn - 1) % ? = 0 " +
                "ORDER BY t1.record_time";

        return jdbcTemplate.query(query, new Object[]{serialNumber, startTime, endTime, intervalInSeconds / 5}, new SensorDataRowMapper());
    }


    private static class SensorDataRowMapper implements RowMapper<SensorData> {
        @Override
        public SensorData mapRow(ResultSet rs, int rowNum) throws SQLException {
            SensorData sensorData = new SensorData();
            sensorData.setId(rs.getLong("id"));
            sensorData.setSerialNumber(rs.getString("serial_number"));
            sensorData.setRecordTime(rs.getTimestamp("record_time").toLocalDateTime());
            sensorData.setSensorValue(rs.getDouble("sensor_value"));
            sensorData.setSensorType(rs.getInt("sensor_type"));
            return sensorData;
        }
    }
}
