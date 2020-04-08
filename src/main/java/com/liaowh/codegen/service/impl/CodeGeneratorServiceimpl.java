package com.liaowh.codegen.service.impl;

import com.google.common.base.CaseFormat;
import com.liaowh.codegen.service.CodeGeneratorService;
import com.liaowh.codegen.service.DatabaseInfoService;
import freemarker.template.TemplateExceptionHandler;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class CodeGeneratorServiceimpl implements CodeGeneratorService {

    private static final String JDBC_DIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    //REFERENCE
    private static final String PROJECT_PACKAGE = "com.liaowh.codegen";
    private static final String MODEL_PACKAGE = PROJECT_PACKAGE + ".model";//Model所在包
    private static final String MAPPER_PACKAGE = PROJECT_PACKAGE + ".mapper";//Mapper所在包
    private static final String SERVICE_PACKAGE = PROJECT_PACKAGE + ".service";//Service所在包
    private static final String SERVICE_IMPL_PACKAGE = PROJECT_PACKAGE + ".service.impl";//ServiceImpl所在包
    private static final String CONTROLLER_PACKAGE = PROJECT_PACKAGE + ".controller";//controller所在包
    private static final String BASE_PACKAGE = PROJECT_PACKAGE + ".base";

    //PATH
    private static final String PROJECT_PATH = "/Users/liaowh/CMProject/CodeGenerator";//项目在硬盘上的基础路径
    private static final String TEMPLATE_FILE_PATH = PROJECT_PATH + "/src/main/resources/template";//模板位置
    private static final String JAVA_PATH = "/src/main/java"; //java文件路径
    private static final String RESOURCES_PATH = "/src/main/resources";//资源文件路径
    private static final String PACKAGE_SERVICE_PATH = packageConvertPath(SERVICE_PACKAGE);//生成的Service存放路径
    private static final String PACKAGE_SERVICE_IMPL_PATH = packageConvertPath(SERVICE_IMPL_PACKAGE);//生成的ServiceImpl实现存放路径
    private static final String PACKAGE_CONTROLLER_PATH = packageConvertPath(CONTROLLER_PACKAGE);//生成的Controller存放路径

    private static final String MAPPER_INTERFACE_REFERENCE = BASE_PACKAGE + ".mapper";//Mapper插件基础接口的完全限定名

    private static final String DATE = new SimpleDateFormat("yyyy/MM/dd").format(new Date());//@date

    @Autowired
    private DatabaseInfoService databaseInfoService;

    protected String jdbcUrl;
    protected String jdbcUsername;
    protected String jdbcPasswd;
    protected String author;

    @Override
    public void init(String url,String username,String passwd,String author) {
        this.jdbcUrl = url;
        this.jdbcPasswd = passwd;
        this.jdbcUsername = username;
        this.author = author;
    }

    /**
     * 通过数据表名称生成代码，Model 名称通过解析数据表名称获得，下划线转大驼峰的形式。
     * 如输入表名称 "t_user_detail" 将生成 TUserDetail、TUserDetailMapper、TUserDetailService ...
     */

    @Override
    public void genCode(List<Map> tableList) {
        for(Map<String,String> map : tableList){
            String tableName = map.get("value");
            genModelAndMapper(tableName);
            genService(tableName);
            genController(tableName);
            genVueViewList(tableName);
            genVueViewEdit(tableName);
            genVueApi(tableName);
        }
    }


    public void genModelAndMapper(String tableName) {
        Context context = new Context(ModelType.FLAT);
        context.setId("Potato");
        context.setTargetRuntime("MyBatis3Simple");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "`");
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "`");

        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(jdbcUrl);
        jdbcConnectionConfiguration.setUserId(jdbcUsername);
        jdbcConnectionConfiguration.setPassword(jdbcPasswd);
        jdbcConnectionConfiguration.setDriverClass(JDBC_DIVER_CLASS_NAME);
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        pluginConfiguration.addProperty("mappers", MAPPER_INTERFACE_REFERENCE);
        context.addPluginConfiguration(pluginConfiguration);

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(PROJECT_PATH + JAVA_PATH);
        javaModelGeneratorConfiguration.setTargetPackage(MODEL_PACKAGE);
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetProject(PROJECT_PATH + RESOURCES_PATH);
        sqlMapGeneratorConfiguration.setTargetPackage("mapper");
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetProject(PROJECT_PATH + JAVA_PATH);
        javaClientGeneratorConfiguration.setTargetPackage(MAPPER_PACKAGE);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(tableName);
        tableConfiguration.setGeneratedKey(new GeneratedKey("id", "Mysql", true, null));
        context.addTableConfiguration(tableConfiguration);

        List<String> warnings;
        MyBatisGenerator generator;
        try {
            Configuration config = new Configuration();
            config.addContext(context);
            config.validate();

            boolean overwrite = true;
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            warnings = new ArrayList<String>();
            generator = new MyBatisGenerator(config, callback, warnings);
            generator.generate(null);
        } catch (Exception e) {
            throw new RuntimeException("生成Model和Mapper失败", e);
        }

        if (generator.getGeneratedJavaFiles().isEmpty() || generator.getGeneratedXmlFiles().isEmpty()) {
            throw new RuntimeException("生成Model和Mapper失败：" + warnings);
        }
        System.out.println(tableName + ".java 生成成功");
        System.out.println(tableName + "mapper.java 生成成功");
        System.out.println(tableName + "mapper.xml 生成成功");
    }

    public void genService(String tableName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", author);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", tableNameConvertLowerCamel(tableName));

            data.put("basePath", BASE_PACKAGE);
            data.put("modelPackage", MODEL_PACKAGE);
            data.put("mapperPackage", MAPPER_PACKAGE);
            data.put("servicePackage", SERVICE_PACKAGE);
            data.put("serviceImplPackage", SERVICE_IMPL_PACKAGE);

            File file = new File(PROJECT_PATH + JAVA_PATH + PACKAGE_SERVICE_PATH + modelNameUpperCamel + "Service.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("service.ftl").process(data,
                    new FileWriter(file));
            System.out.println(modelNameUpperCamel + "Service.java 生成成功");

            File file1 = new File(PROJECT_PATH + JAVA_PATH + PACKAGE_SERVICE_IMPL_PATH + modelNameUpperCamel + "ServiceImpl.java");
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            cfg.getTemplate("service-impl.ftl").process(data,
                    new FileWriter(file1));
            System.out.println(modelNameUpperCamel + "ServiceImpl.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Service失败", e);
        }
    }

    public void genController(String tableName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", author);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            data.put("baseRequestMapping", modelNameConvertMappingPath(modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePath", BASE_PACKAGE);
            data.put("modelPackage", MODEL_PACKAGE);
            data.put("mapperPackage", MAPPER_PACKAGE);
            data.put("servicePackage", SERVICE_PACKAGE);
            data.put("controllerPackage",CONTROLLER_PACKAGE);

            File file = new File(PROJECT_PATH + JAVA_PATH + PACKAGE_CONTROLLER_PATH + modelNameUpperCamel + "Controller.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));

            System.out.println(modelNameUpperCamel + "Controller.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Controller失败", e);
        }

    }

    private void genVueViewList(String tableName) {
        List<Map> tableColumnList = databaseInfoService.listTableColumn(tableName);
        try {
            freemarker.template.Configuration cfg = getConfiguration();
            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", author);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            String modelNameLowerCamel = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel);
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", modelNameLowerCamel);
            data.put("tableColumnList",tableColumnList);
            data.put("searchItemList",tableColumnList);
            data.put("editPath",tableNameConvertVuePath(tableName));
            File file = new File(System.getProperty("user.dir") + "/webapp/src/modules/"
                    + tableNameConvertUpperCamel(tableName) + "/views/" + modelNameLowerCamel + "List.vue");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("vueViewList.ftl").process(data, new FileWriter(file));
            System.out.println(modelNameUpperCamel + ".vue 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("vueViewList", e);
        }
    }
    private void genVueViewEdit(String tableName) {
        List<Map> tableColumnList = databaseInfoService.listTableColumn(tableName);
        try {
            freemarker.template.Configuration cfg = getConfiguration();
            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", author);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            String modelNameLowerCamel = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel);
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", modelNameLowerCamel);
            data.put("tableColumnList",tableColumnList);
            File file = new File(System.getProperty("user.dir") + "/webapp/src/modules/"
                    + tableNameConvertUpperCamel(tableName) + "/views/" + modelNameLowerCamel + "Edit.vue");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("vueViewEdit.ftl").process(data, new FileWriter(file));
            System.out.println(modelNameUpperCamel + ".vue 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("vueViewEdit", e);
        }
    }
    private void genVueApi(String tableName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();
            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", author);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            String modelNameLowerCamel = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel);
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", modelNameLowerCamel);
            data.put("baseRequestMapping", modelNameConvertMappingPath(modelNameUpperCamel));
            File file = new File(System.getProperty("user.dir")  + "/webapp/src/modules/"
                    + tableNameConvertUpperCamel(tableName) + "/api/" + modelNameLowerCamel + ".js");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("vueApi.ftl").process(data, new FileWriter(file));

            System.out.println(modelNameUpperCamel + ".js 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("vueApi", e);
        }
    }


    private static freemarker.template.Configuration getConfiguration() throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FILE_PATH));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return cfg;
    }

    private static String tableNameConvertLowerCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName.toLowerCase());
    }

    private static String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());

    }

    private static String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();//兼容使用大写的表名
        return "/" + (tableName.contains("_") ? tableName.replaceAll("_", "") : tableName);
    }

    private static String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }

    private static String tableNameConvertVuePath(String tableName){
        tableName = tableName.toLowerCase();//兼容使用大写的表名
        return (tableName.contains("_") ? tableName.replaceAll("_", "-") : tableName);
    }
}
