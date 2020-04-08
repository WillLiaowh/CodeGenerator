package com.liaowh.codegen.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface CodeGeneratorService {

    void init(String url,String username,String passwd,String author);

    void genCode(List<Map> tableList);
}
