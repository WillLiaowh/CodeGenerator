package com.liaowh.codegen.base;

import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于通用MyBatis Mapper插件的Service接口的实现
 */
public abstract class AbstractService<T> implements Service<T> {

  @Autowired
  protected mapper<T> Mapper;

  private Class<T> modelClass;    // 当前泛型真实类型的Class

  public AbstractService() {
    ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
    modelClass = (Class<T>) pt.getActualTypeArguments()[0];
  }

  public void save(T model) {
    Mapper.insertSelective(model);
  }

  public void save(List<T> models) {
    Mapper.insertList(models);
  }

  public void deleteById(Integer id) {
    Mapper.deleteByPrimaryKey(id);
  }

  public void deleteById(String id) {
    Mapper.deleteByPrimaryKey(id);
  }

  public void deleteByIds(String ids) {
    Mapper.deleteByIds(ids);
  }

  public void update(T model) {
    Mapper.updateByPrimaryKeySelective(model);
  }

  public T findById(Integer id) {
    return Mapper.selectByPrimaryKey(id);
  }


  public T findById(String id) {
    return Mapper.selectByPrimaryKey(id);
  }
  @Override
  public T findBy(String fieldName, Object value) throws TooManyResultsException {
    try {
      T model = modelClass.newInstance();
      Field field = modelClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(model, value);
      return Mapper.selectOne(model);
    } catch (ReflectiveOperationException e) {
      throw new ServiceException(e.getMessage(), e);
    }
  }

  public List<T> findByIds(String ids) {
    return Mapper.selectByIds(ids);
  }

  public List<T> findByCondition(Condition condition) {
    return Mapper.selectByCondition(condition);
  }

  public List<T> findAll() {
    return Mapper.selectAll();
  }

  @Override
  public T findByOrderId(String orderId) {
    try {
      Condition condition = new Condition(modelClass);
      condition.and().andEqualTo("orderId", orderId);
      List<T> list = Mapper.selectByCondition(condition);
      T model = modelClass.newInstance();
      if (!(null == list || list.size() < 1)) {
        model = list.get(0);
      }
      return model;
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  @Override
  public List<T> findByOrderIds(List<String> orderIds) {
    List<T> list = new ArrayList<>();
    if (null == orderIds || orderIds.size() == 0) return list;
    Condition condition = new Condition(modelClass);
    condition.and().andIn("orderId", orderIds);
    condition.setOrderByClause("create_time");
    list = Mapper.selectByCondition(condition);
    return list;
  }

  @Override
  public List<T> findByCond(T cond) {
    Condition condition = new Condition(modelClass);
    Map<String, Object> input = new HashMap<String, Object>();
    Field f[] = cond.getClass().getDeclaredFields();
    for (Field field : f) {
      //设置访问权限
      try{
        field.setAccessible(true);
        if (field.get(cond) == null || "".equals(field.get(cond))) {
          continue;
        }
        input.put(field.getName(), field.get(cond));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    condition.createCriteria().andAllEqualTo(input);
    condition.orderBy("id").desc();
    List<T> result = findByCondition(condition);
    return result;
  }
  @Override
  public List<T> findByCond(T cond,String orderKey,Boolean isDesc) {
    Condition condition = new Condition(modelClass);
    Map<String, Object> input = new HashMap<String, Object>();
    Field f[] = cond.getClass().getDeclaredFields();
    for (Field field : f) {
      //设置访问权限
      try{
        field.setAccessible(true);
        if (field.get(cond) == null || "".equals(field.get(cond))) {
          continue;
        }
        input.put(field.getName(), field.get(cond));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    condition.createCriteria().andAllEqualTo(input);
    if(isDesc){
      condition.orderBy(orderKey).desc();
    }else {
      condition.orderBy(orderKey);
    }
    List<T> result = findByCondition(condition);
    return result;
  }
}
