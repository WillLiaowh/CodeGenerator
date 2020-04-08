package com.liaowh.codegen.service.impl;

import com.liaowh.codegen.mapper.DatabaseInfoMapper;
import com.liaowh.codegen.service.DatabaseInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class DatabaseInfoServiceImpl implements DatabaseInfoService {
    @Resource
    private DatabaseInfoMapper databaseInfoMapper;

    @Override
    public List<Map> listTableColumn(String tableName){
        return databaseInfoMapper.listTableColumn(tableName);
    }

    @Override
    public List<Map> listTable(){
        return databaseInfoMapper.listTable();
    }
}
