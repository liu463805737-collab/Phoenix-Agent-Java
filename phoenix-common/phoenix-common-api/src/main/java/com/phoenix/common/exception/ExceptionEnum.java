package com.phoenix.common.exception;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    // 通用
    SUCCESS(200, "success"),
    FAIL(500, "fail"),
    PARAM_IS_NULL(400, "参数不能为空"),
    DATA_NOT_FOUND(404, "数据不存在"),

    // 用户
    USER_NOT_LOGIN_EXCEPTION(401, "用户未登录"),
    USER_ERROR_EXIST_EXCEPTION(1001, "用户不存在"),
    USER_IS_DISABLE_EXCEPTION(1002, "用户已被禁用"),
    USERNAME_DUPLICATE_EXCEPTION(1003, "用户名重复"),
    USERCODE_DUPLICATE_EXCEPTION(1004, "工号重复"),
    USER_DELETE_ERROR(1005, "该用户为IDM用户，不可删除"),
    USER_PASSWORD_ERROR(1006, "密码错误"),

    // 图形验证码
    IMAGE_NOT_EXIST_EXCEPTION(2001, "图形不存在"),
    IMAGE_CREATE_EXIST_EXCEPTION(2002, "图形创建失败"),
    IMAGE_NOT_EXIST_OR_EXPIRE_EXCEPTION(2003, "图形验证码不存在或已过期"),
    IMAGE_VERIFICATION_EXCEPTION(2004, "图形验证码校验失败"),

    // 公司
    COMPANYCODE_DUPLICATE_ERROR(3001, "公司编码重复"),
    CUSTOMER_CODE_NOT_NULL(3002, "客户编码不能为空"),
    PARENT_COMPANY_NOT_EXIST(3003, "上级公司不存在"),
    HAS_CHILD_COMPANY(3004, "存在子公司，无法删除"),

    // 系统
    SYSTEM_NOT_EXIST_EXCEPTION(4001, "系统不存在"),

    // 模块
    MODULE_NOT_EXIST_EXCEPTION(5001, "模块不存在"),

    // 权限
    DANGQIAN_YONGHU_MEIYOUQUANXIAN(6001, "当前用户没有权限"),
    ACCOUNT_ID_NULL(6002, "账号ID不能为空"),

    // 文件
    FILE_NOT_EXIST(7001, "文件不存在"),

    // 系统配置
    CONFIG_KEY_NOT_NULL(8001, "配置Key不能为空"),

    // 参数
    INPUT_PARAMETER_EXCEPTION(9001, "输入参数异常"),
    NO_QUERY_POSITION_EXCEED_ERROR(9002, "没有查询权限"),

    // 模块&系统匹配
    MODULE_AND_SYSTEM_NOT_MATCH(10001, "模块与系统不匹配"),

    // 部门
    DEPARTMENTCODE_DUPLICATE_ERROR(11001, "部门编码重复"),
    DELETE_DEPMENT_ERROR(11002, "该部门存在子部门，无法删除"),
    ISBIND_USER_ERROR(11003, "该部门已绑定用户，无法删除"),

    // 数据组
    GROUP_NULL_EXCEPTION(12001, "数据组不存在"),
    GROUP_NOT_EXIST(12002, "数据组已被删除"),
    GROUP_BIND_EXCEED_ERROR(12003, "送奶员只能绑定一个奶站"),
    SUPER_NAME_REPEAT_EXCEPTION(12004, "名称不能与上级名称重复"),
    SUPER_NULL_EXCEPTION(12005, "上级数据不存在"),
    SUPER_ID_REPEAT_EXCEPTION(12006, "不能成为自己的上级"),
    SUPER_ID_LOWER_THAN_ITSELF_EXCEPTION(12007, "下级不能成为上级"),
    USER_NOT_EXIST_EXCEPTION(12008, "用户不存在"),
    SYSTEM_NULL_EXCEPTION(12009, "系统不存在"),


    USER_NOT_PERMISSION__EXCEPTION(403, "你无权限操作,请联系管理员！"),
    PARAMETER_REPETITION_EXCEPTION(328, "参数重复!"),
    SYSTEM_NOT_PERMISSION_EXCEPTION(336, "系统标识不存在或没有系统操作权限！"),
    SYSTEMSN_NOT_EXIST_EXCEPTION(336, "SystemSn不存在!"),

    GROUP_NAME_VERIFY_EXCEPTION(501, "数据组名称已存在，请重新输入！"),
    USER_ID_AND_USERNAME_ALL_EMPTY(400, "userId和username不能同时为空"),
    ROlE_DELFAIL(324, "删除失败，当前角色已分配人员"),

    // 分类
    CATE_NAME_NULL(13001, "分类名称不能为空"),
    CATE_NAME_EXIST(13002, "分类名称已存在"),
    CATE_SAVE_FAILED(13003, "分类保存失败"),
    CATE_UPDATE_FAILED(13004, "分类更新失败"),
    CATE_DELETE_FAILED(13005, "分类删除失败"),
    CATE_HAS_CHILDREN(13006, "该分类存在子分类，无法删除"),
    CATE_HAS_API(13007, "该分类下存在接口，无法删除"),
    SYSTEM_ERROR(13008, "系统内部错误"),

    // 账号
    ACCOUNT_NOT_EXIST(15001, "账号不存在"),
    ACCOUNT_STATUS_NOT_ALLOW_DELETE(15002, "账号状态不允许删除"),
    USER_EXIT(15003, "用户已存在"),
    ACCOUNT_EXIT(15004, "账号已存在"),
    COMPANY_ID_NOT_EMPTY(15005, "公司ID不能为空"),
    PARENT_DEPT_NOT_EXIST(15006, "父部门不存在"),
    DEPARTMENT_NOT_FOUND(15007, "部门不存在"),
    PARENT_DEPT_IS_CHILD(15008, "父部门不能是自己的子部门"),

    // 上传
    UPLOAD_FILE_NULL(14001, "上传文件不能为空"),
    UPLOAD_FILE_EXTEND_ERROR(14002, "文件格式不正确"),

    // 业务
    BUSINESS_ERROR(16001, "业务异常"),
    BUSINESS_ERROR_CONFIG(16002, "配置异常"),
    BUSINESS_ERROR_KEY(16003, "KEY异常"),
    BUSINESS_ERROR_NAME(16004, "名称异常"),
    BUSINESS_ERROR_SEQ_CODE(16005, "编码异常"),
    PARAM_ERROR(16006, "参数异常"),
    PARAM_ERROR_CODE(16007, "参数CODE异常"),
    PARAM_ERROR_CONFIGKEY(16008, "配置KEY异常"),
    PARAM_ERROR_CONFIGNAME(16009, "配置名称异常"),
    PARAM_ERROR_CONFIGVALUE(16010, "配置值异常"),
    PARAM_ERROR_NAME(16011, "参数名称异常"),
    PARAM_ERROR_OFFICE_SEQ_CODE(16012, "办公室编码异常"),
    OPERATE_FAILED(16013, "操作失败"),
    UPDATE_FAILED(16014, "更新失败"),
    STATUS_IS_INVALID(16015, "状态无效"),
    ID_IS_EMPTY(16016, "ID不能为空"),
    DATA_ALREADY_DELETED(16017, "数据已被删除"),
    ADD_EMPLOYEE_ERROW(16018, "添加员工失败"),
    DELETE_EMPLOYEE_ERROW(16019, "删除员工失败"),
    URL_PARAM_IS_NULL(16020, "URL参数不能为空"),

    // 角色
    ROLE_NAME_EMPTY(17001, "角色名称不能为空"),
    ROLE_ID_NULL(17002, "角色ID不能为空"),
    ROLE_NOT_EXIST(17003, "角色不存在"),
    ROLE_ALREADY_DELETED(17004, "角色已被删除"),
    ROLE_NOT_FOUND_ERROR(17005, "角色未找到"),
    ROLE_STATUS_UPDATE_ERROR(17006, "角色状态更新失败"),
    ROLE_UPDATE_FOUND_ERROR(17007, "角色更新失败"),

    // 部门
    DEPARTMENT_NOT_EXIST(18001, "部门不存在"),
    DEPARTMENT_HAS_CHILD(18002, "部门存在子部门"),
    DEPARTMENT_HAS_EMPLOYEE(18003, "部门存在员工"),
    DEPARTMENT_HAS_USER(18004, "部门存在用户"),
    DEPARTMENT_UPDATE_ERROR(18005, "部门更新失败"),
    DEPT_NAME_NOT_EMPTY(18006, "部门名称不能为空"),
    DEPT_NAME_DUPLICATE_ERROR(18007, "部门名称重复"),
    CODE_DUPLICATE_ERROR(18008, "编码重复"),
    NAME_DUPLICATE_ERROR(18009, "名称重复"),
    NAME_POSITION_ERROR(18010, "位置名称错误"),
    CATE_ID_DUPLICATE(18011, "分类ID重复"),

    // 模块
    MODULENAME_DUPILCATE_EXCEPTION(19001, "模块名称重复"),
    MODULESN_DUPILCATE_EXCEPTION(19002, "模块标识重复"),

    // 客户端
    CLIENT_ID_NULL(20001, "客户端ID不能为空"),
    CLIENT_NAME_NULL(20002, "客户端名称不能为空"),
    CLIENT_NAME_EXIST(20003, "客户端名称已存在"),
    CLIENT_NOT_EXIST(20004, "客户端不存在"),
    CLIENT_DELETED(20005, "客户端已被删除"),
    CLIENT_NOT_EXIST_OR_DELETED(20006, "客户端不存在或已被删除"),

    // 岗位
    JOB_GRADE_NAME_NULL(21001, "职级名称不能为空"),
    JOB_GRADE_NAME_DUPLICATE(21002, "职级名称重复"),
    OFFICE_SEQ_CODE_DUPLICATE_ERROR(21003, "办公室编码重复"),
    PROFESSIONAL_NAME_NULL(21004, "专业名称不能为空"),
    PROFESSIONAL_NAME_EXIST(21005, "专业名称已存在"),
    PVALUE_DUPLICATED(21006, "PVALUE重复"),
    PVALUE_DUPLICATED_NAME(21007, "PVALUE名称重复"),
    PVALUE_DUPLICATED_ORDERNO(21008, "PVALUE排序重复"),

    // 用户
    USER_NOT_BIND_GROUP_EXCEPTION(22001, "用户未绑定数据组"),
    USER_ROLE_COMPANYID_ERROR(22002, "用户角色公司ID错误"),
    COMPANYCODE_DUPLICATE_ERRORW(22003, "公司编码重复"),
    COMPANYCODE_UPDATE_ERRORW(22004, "公司编码更新失败"),
    COMPANY_DATAUNITNAME_ALREADYBIND_ERROR(22005, "数据单元名称已被绑定"),
    DATAUNITNAME_EXIST_ERROR(22006, "数据单元名称已存在"),
    PASSWORDS_ENTERD_INCORRECT(22007, "两次输入的密码不一致"),
    NO_QUERY_MODULE_EXCEED_ERROR(22008, "请至少选择一个查询权限"),
    DEPORTMENT_DELETE_ERROR(22009, "IDM同步的部门不允许删除"),
    ;

    private final int code;
    private final String message;

    ExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
