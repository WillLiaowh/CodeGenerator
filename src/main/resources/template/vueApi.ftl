import request from '@/utils/request'

class ${modelNameUpperCamel}Api {
  query(data) {
    return request({
      url: '${baseRequestMapping}/list',
      method: 'get',
      data
    })
  }

  queryByCond(data, pageSize, pageNum) {
    return request({
      url: `${baseRequestMapping}/queryByCond?pageSize=${r'${pageSize}'}&pageNum=${r'${pageNum}'}`,
      method: 'post',
      data
    })
  }
  add(data) {
    return request({
      url: '${baseRequestMapping}/add',
      method: 'post',
      data
    })
  }
  update(data) {
    return request({
      url: '${baseRequestMapping}/update',
      method: 'post',
      data
    })
  }
  delete(data) {
    return request({
      url: '${baseRequestMapping}/delete',
      method: 'post',
      data
    })
  }
}

export default new ${modelNameUpperCamel}Api()
