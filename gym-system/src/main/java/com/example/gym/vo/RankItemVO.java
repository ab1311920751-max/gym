package com.example.gym.vo;

import lombok.Data;

/**
 * 通用排行项 VO，字段名与 ECharts {name, value} 对齐。
 * 供课程热度排行和课程分类分布共用。
 */
@Data
public class RankItemVO {
    private String name;
    private long value;
}
