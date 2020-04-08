package com.liaowh.codegen.controller;

import com.liaowh.codegen.base.Result;
import com.liaowh.codegen.base.ResultGenerator;
import com.liaowh.codegen.service.CodeGeneratorService;
import com.liaowh.codegen.service.DatabaseInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeGeneratorController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    @Autowired
    private DatabaseInfoService databaseInfoService;

    @Autowired
    private Environment env;

    @RequestMapping(value="/genCode")
    public Result GenMenuCode(){
        String jdbcUrl = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String author = "Liaowh";
        codeGeneratorService.init(jdbcUrl,username,password,author);
        try{
            codeGeneratorService.genCode(databaseInfoService.listTable());
        }
        catch (Exception e){
            logger.error("生成代码失败:"+e.getCause().getMessage());
            return ResultGenerator.genFailResult("生成代码失败");
        }

        return ResultGenerator.genSuccessResult();
    }

}
