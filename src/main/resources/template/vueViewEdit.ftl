<template>
    <div>
        <el-form :model="currentRecord"
                 ref="editForm"
                 :rules="formRules">
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
        <#list tableColumnList as tableColumn>
        <el-form-item label="${tableColumn.columnComment}"
                      prop="${dashedToCamel(tableColumn.columnName)}"
                      label-width="80px">
            <el-col :span="22">
                <el-input v-model="currentRecord.${dashedToCamel(tableColumn.columnName)}"
                          :disabled="this.mode=='details'" placeholder="请输入${tableColumn.columnComment}"></el-input>
            </el-col>
        </el-form-item>
        </#list>
    <el-form-item align="center">
        <el-col :span="24">
            <el-button type="primary"
                       @click.native.prevent="handleSubmit">
                <i class="fa fa-save"></i> 保存
            </el-button>
        </el-col>
    </el-form-item>
    </el-form>
    </div>
</template>

<script>
    import {Message} from 'element-ui'
    import ${modelNameUpperCamel}Api from '../api/${modelNameLowerCamel}'

    export default {
        name: '${modelNameUpperCamel}Edit',
        props: {
            mode: {
                type: String
            },
            record: {
                type: Object
            }
        },
        data() {
            return {
                currentRecord: this.record,
                formRules: {}
            }
        },
        watch: {
            record: function (oldValue, newValue) {
                this.currentRecord = this.record
                // this.getDetail()
            }
        },
        methods: {

            handleSubmit() {
                this.$refs['editForm'].validate(valid => {
                    if (valid) {
                        let newRecord = Object.assign({}, this.currentRecord)
                        if (this.mode === 'add') {
                            ${modelNameUpperCamel}Api.add(newRecord).then(rspData => {
                                if (rspData.data.code === 200) {
                                    Message.success('新增成功')
                                }
                                this.$emit('after-edit')
                            })
                        }
                        else if (this.mode === 'details') {
                            this.$emit('after-edit')
                        }
                        else {
                            ${modelNameUpperCamel}Api.update(newRecord).then(rspData => {
                                if (rspData.data.code === 200) {
                                    Message.success('修改成功')
                                }
                                this.$emit('after-edit')
                            })
                        }
                    } else {
                        return false
                    }
                })
            },
            resetFields() {
                this.$refs['editForm'].resetFields()
            }
        }
    }
</script>
