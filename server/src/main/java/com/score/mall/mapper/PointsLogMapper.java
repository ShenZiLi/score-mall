package com.score.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.score.mall.entity.PointsLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PointsLogMapper extends BaseMapper<PointsLog> {

    @Select("SELECT DATE(create_time) d, SUM(change_points) pts FROM points_log " +
            "WHERE create_time >= #{startDate} AND user_id IS NOT NULL " +
            "GROUP BY DATE(create_time) ORDER BY d")
    List<Map<String, Object>> dailyPointsTrend(@Param("startDate") String startDate);

    @Select("SELECT type, SUM(ABS(change_points)) pts FROM points_log " +
            "WHERE create_time >= #{startDate} GROUP BY type")
    List<Map<String, Object>> pointsTypeStat(@Param("startDate") String startDate);
}
