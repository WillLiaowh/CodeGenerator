package com.liaowh.codegen.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface DatabaseInfoService {
    /**
     * @Author lizhitao
     * @Description 根据表名列出字段
     **/
    List<Map> listTableColumn(String tableName);
    /**
     * @Author lizhitao
     * @Description 列出所有表的表名
     **/
    List<Map> listTable();
}
