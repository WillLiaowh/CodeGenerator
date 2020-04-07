<template>
    <div class="container">
        <el-card class="search-container">
            <el-form :inline="true" :model="searchFrom" class="demo-form-inline">
                <#function dashedToCamel(s)>
                <#return s
                ?replace('(^_+)|(_+$)', '', 'r')
                ?replace('\\_+(\\w)?', ' $1', 'r')
                ?replace('([A-Z])', ' $1', 'r')
                ?capitalize
                ?replace(' ' , '')
                ?uncap_first
                >
            </#function>
            <#list searchItemList as searchItem>
            <#if searchItem_index lt 3>
            <el-form-item
                    label="${searchItem.columnComment}"
                    prop="${dashedToCamel(searchItem.columnName)}"
            <#if searchItem_index != 0>label-width="80px"
            </#if>>
            <el-col :span="22">
                <el-input :rows="5" v-model="searchFrom.${dashedToCamel(searchItem.columnName)}" clearable type="text"
                          placeholder="请输入${searchItem.columnComment}"/>
            </el-col>
            </el-form-item>
            </#if>
            </#list>
<el-form-item>
    <el-button type="primary" @click="handleQuery">查询</el-button>
</el-form-item>
<el-form-item>
    <el-button type="primary" @click="handleAdd">新增</el-button>
</el-form-item>
</el-form>
</el-card>

<el-card class="table-container">
    <el-table
            :data="tableData.list"
            :header-row-class-name="'table-head-th'"
            row-key="id"
            fit
            highlight-current-row>
        <#list tableColumnList as tableColumn>
        <el-table-column
                key="${dashedToCamel(tableColumn.columnName)}"
                show-overflow-tooltip
                prop="${dashedToCamel(tableColumn.columnName)}"
                label="${tableColumn.columnComment}"/>
        </#list>
    <!-- 表格操作列 -->
    <el-table-column label="操作">
        <template slot-scope="scope">
            <el-button-group>
                <el-button
                        size="mini"
                        title="查看"
                        type="primary"
                        @click="handleDetail(scope.row)">
                    <i class="fa fa-eye"/>
                </el-button>
                <el-button
                        size="mini"
                        title="编辑"
                        type="primary"
                        @click="handleEdit(scope.row)">
                    <i class="fa fa-pencil"/>
                </el-button>
                <el-button
                        size="mini"
                        title="删除"
                        type="danger"
                        @click="handleDel(scope.row)">
                    <i class="fa fa-trash-o"/>
                </el-button>
            </el-button-group>
        </template>
    </el-table-column>
    </el-table>
    <!-- 分页信息 -->
    <div class="pagination-container">
        <el-pagination background
                       layout="total, prev, pager, next, jumper"
                       @current-change="handlePageChange"
                       @size-change="handleSizeChange"
                       :current-page="pageNum"
                       :total="tableData.total">
        </el-pagination>
    </div>
</el-card>

<!-- 新增/编辑对话框 -->
<el-dialog :title="mode==='add'?'新增':'编辑'"
           :visible.sync="showEditDialog"
           :before-close="handleDialogClose">
    <${editPath}-edit :mode="mode"
    ref="editForm"
    :record="currentRecord"
    @after-edit="handleAfterEdit"/>
</el-dialog>
</div>
</template>

<script>
    import {Message} from 'element-ui'
    import ${modelNameUpperCamel}Api from '../api/${modelNameLowerCamel}'
    import ${modelNameLowerCamel}Edit from './${modelNameLowerCamel}Edit'

    export default {
    	name: '${modelNameUpperCamel}List',
        components: {
            ${modelNameLowerCamel}Edit
        },
        data() {
            return {
                /* 搜索请求数据*/
                searchFrom: {
                },
                pageSize: 10,
                pageNum: 1,
                // 表格数据
                tableData: {
                    results: []
                },
                mode: 'edit',
                // 是否显示添加/编辑对话框
                showEditDialog: false,
                // 当前处理行
                currentRecord: {}
            }
        },
        mounted() {
            this.init()
        },
        methods: {
            // 页面初始化
            init() {
                this.handleQuery()
            },
            handleQuery() {
                this.pageNum = 1
                this.query()
            },
            query() {
                return new Promise((resolve, reject) => {
                    const param = Object.assign({}, this.searchFrom)
                    ${modelNameUpperCamel}Api
                        .queryByCond(param, this.pageSize, this.pageNum)
                        .then(response => {
                            const data = response.data
                            if (data.code === 200) {
                                const data = response.data.data
                                this.tableData = data
                            } else {
                                Message.error(data.message)
                                reject()
                            }
                        })
                        .catch(error => {
                            Message.error(error)
                            reject(error)
                        })
                })
            },
            handleAdd() {
                this.mode = 'add'
                this.currentRecord = {}
                this.showEditDialog = true
            },
            handleDetail(row) {
                this.mode = 'details'
                this.currentRecord = Object.assign({}, row)
                this.showEditDialog = true
            },
            handleEdit(row) {
                this.mode = 'edit'
                this.currentRecord = Object.assign({}, row)
                this.showEditDialog = true
            },

            // 处理翻页事件
            handlePageChange(currentPage) {
                this.pageNum = currentPage
                this.query()
            },
            // 处理页面总条数事件
            handleSizeChange(val) {
                this.pageSize = val;
                this.query()
            },
            // 处理对话框关闭事件
            handleDialogClose(done) {
                this.$refs.editForm.resetFields()
                done()
            },
            handleDel(row) {
                this.$confirm('确定删除吗？', '警告', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                    .then(() => {
                        const param = Object.assign({}, row)
                        ${modelNameUpperCamel}Api
                            .delete(param)
                            .then(response => {
                                const data = response.data
                                if (data.code === 200) {
                                    this.$message({
                                        message: '删除成功',
                                        type: 'success'
                                    })
                                    this.handleQuery()
                                } else {
                                    Message.error(data.message)
                                }
                            })
                            .catch(error => {
                                Message.error(error)
                            })
                    })
                    .catch(() => {
                    })
            },
            // 处理编辑对话框关闭事件
            handleAfterEdit() {
                this.showEditDialog = false
                this.$refs.editForm.resetFields()
                this.query()
            }
        }
    }
</script>
<style>
    .container {
        margin: 20px;
    }

    .table-container {
        margin-top: 20px;
    }
    .pagination-container {
        margin-top: 30px;
        text-align: right;
    }
</style>

