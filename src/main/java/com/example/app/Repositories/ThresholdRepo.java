package com.example.app.Repositories;
import com.example.app.DTOs.UserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ThresholdRepo {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public ThresholdRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateThresholdData(long userId , UserInfoDTO userInfoDTO){
        String sql = "UPDATE users SET gas_threshold = ?, CO_threshold = ? WHERE id = ?";
        jdbcTemplate.update(sql,userInfoDTO.getGasThreshold(),userInfoDTO.getCOThreshold(),userId);
    }

    public List<Integer> getThresholdDataFromDB(String field ,long userId){
        try{
            String sql = "SELECT " + field + " FROM users WHERE id = " + userId;
            List<Integer> result =  jdbcTemplate.queryForList(sql,Integer.class);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
