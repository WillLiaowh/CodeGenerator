package com.liaowh.codegen.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DatabaseInfoMapper {
    @Select("select table_name as value from information_schema.TABLES where TABLE_SCHEMA=(select database())")
    List<Map> listTable();

    @Select("select column_comment as columnComment,column_key as columnKey,column_name as columnName," +
            "column_type as columnType,is_nullable as isNullable,privileges from information_schema.COLUMNS " +
            "where TABLE_SCHEMA = (select database()) and TABLE_NAME=#{tableName}")
    List<Map> listTableColumn(String tableName);
}
