/*
 Navicat Premium Dump SQL

 Source Server         : 本地PG
 Source Server Type    : PostgreSQL
 Source Server Version : 180004 (180004)
 Source Host           : localhost:5432
 Source Catalog        : phoenix
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 180004 (180004)
 File Encoding         : 65001

 Date: 11/07/2026 13:54:38
*/


-- ----------------------------
-- Type structure for ghstore
-- ----------------------------
DROP TYPE IF EXISTS "public"."ghstore";
CREATE TYPE "public"."ghstore" (
  INPUT = "public"."ghstore_in",
  OUTPUT = "public"."ghstore_out",
  INTERNALLENGTH = VARIABLE,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."ghstore" OWNER TO "phoenix";

-- ----------------------------
-- Type structure for halfvec
-- ----------------------------
DROP TYPE IF EXISTS "public"."halfvec";
CREATE TYPE "public"."halfvec" (
  INPUT = "public"."halfvec_in",
  OUTPUT = "public"."halfvec_out",
  RECEIVE = "public"."halfvec_recv",
  SEND = "public"."halfvec_send",
  TYPMOD_IN = "public"."halfvec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."halfvec" OWNER TO "phoenix";

-- ----------------------------
-- Type structure for hstore
-- ----------------------------
DROP TYPE IF EXISTS "public"."hstore";
CREATE TYPE "public"."hstore" (
  INPUT = "public"."hstore_in",
  OUTPUT = "public"."hstore_out",
  RECEIVE = "public"."hstore_recv",
  SEND = "public"."hstore_send",
  INTERNALLENGTH = VARIABLE,
  STORAGE = extended,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."hstore" OWNER TO "phoenix";

-- ----------------------------
-- Type structure for sparsevec
-- ----------------------------
DROP TYPE IF EXISTS "public"."sparsevec";
CREATE TYPE "public"."sparsevec" (
  INPUT = "public"."sparsevec_in",
  OUTPUT = "public"."sparsevec_out",
  RECEIVE = "public"."sparsevec_recv",
  SEND = "public"."sparsevec_send",
  TYPMOD_IN = "public"."sparsevec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."sparsevec" OWNER TO "phoenix";

-- ----------------------------
-- Type structure for vector
-- ----------------------------
DROP TYPE IF EXISTS "public"."vector";
CREATE TYPE "public"."vector" (
  INPUT = "public"."vector_in",
  OUTPUT = "public"."vector_out",
  RECEIVE = "public"."vector_recv",
  SEND = "public"."vector_send",
  TYPMOD_IN = "public"."vector_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);
ALTER TYPE "public"."vector" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_agent_datasource_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_agent_datasource_id_seq";
CREATE SEQUENCE "public"."tbl_data_agent_datasource_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_agent_datasource_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_agent_datasource_tables_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_agent_datasource_tables_id_seq";
CREATE SEQUENCE "public"."tbl_data_agent_datasource_tables_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_agent_datasource_tables_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_agent_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_agent_id_seq";
CREATE SEQUENCE "public"."tbl_data_agent_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 5
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_agent_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_agent_knowledge_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_agent_knowledge_id_seq";
CREATE SEQUENCE "public"."tbl_data_agent_knowledge_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_agent_knowledge_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_agent_preset_question_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_agent_preset_question_id_seq";
CREATE SEQUENCE "public"."tbl_data_agent_preset_question_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_agent_preset_question_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_business_knowledge_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_business_knowledge_id_seq";
CREATE SEQUENCE "public"."tbl_data_business_knowledge_id_seq" 
INCREMENT 5
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_business_knowledge_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_categories_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_categories_id_seq";
CREATE SEQUENCE "public"."tbl_data_categories_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_categories_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_chat_message_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_chat_message_id_seq";
CREATE SEQUENCE "public"."tbl_data_chat_message_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_chat_message_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_datasource_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_datasource_id_seq";
CREATE SEQUENCE "public"."tbl_data_datasource_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_datasource_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_logical_relation_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_logical_relation_id_seq";
CREATE SEQUENCE "public"."tbl_data_logical_relation_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_logical_relation_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_model_config_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_model_config_id_seq";
CREATE SEQUENCE "public"."tbl_data_model_config_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_model_config_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_order_items_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_order_items_id_seq";
CREATE SEQUENCE "public"."tbl_data_order_items_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_order_items_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_orders_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_orders_id_seq";
CREATE SEQUENCE "public"."tbl_data_orders_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_orders_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_products_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_products_id_seq";
CREATE SEQUENCE "public"."tbl_data_products_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_products_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_semantic_model_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_semantic_model_id_seq";
CREATE SEQUENCE "public"."tbl_data_semantic_model_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_semantic_model_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_data_users_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_data_users_id_seq";
CREATE SEQUENCE "public"."tbl_data_users_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_data_users_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Sequence structure for tbl_privilege_group_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."tbl_privilege_group_id_seq";
CREATE SEQUENCE "public"."tbl_privilege_group_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
ALTER SEQUENCE "public"."tbl_privilege_group_id_seq" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for act_hi_procinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_procinst";
CREATE TABLE "public"."act_hi_procinst" (
  "ID_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "REV_" int4,
  "PROC_INST_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "BUSINESS_KEY_" varchar(255) COLLATE "pg_catalog"."default",
  "PROC_DEF_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "START_TIME_" timestamp(6),
  "END_TIME_" timestamp(6),
  "DURATION_" int8,
  "START_USER_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "START_ACT_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "END_ACT_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "SUPER_PROCESS_INSTANCE_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "DELETE_REASON_" varchar(4000) COLLATE "pg_catalog"."default",
  "TENANT_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "NAME_" varchar(255) COLLATE "pg_catalog"."default",
  "CALLBACK_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "CALLBACK_TYPE_" varchar(255) COLLATE "pg_catalog"."default",
  "REFERENCE_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "REFERENCE_TYPE_" varchar(255) COLLATE "pg_catalog"."default",
  "PROPAGATED_STAGE_INST_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "BUSINESS_STATUS_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_hi_procinst" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."act_hi_procinst"."ID_" IS '主键';
COMMENT ON COLUMN "public"."act_hi_procinst"."PROC_INST_ID_" IS '流程实例编号，关联任务表的外键';
COMMENT ON COLUMN "public"."act_hi_procinst"."BUSINESS_KEY_" IS '业务主键，业务单据号';
COMMENT ON COLUMN "public"."act_hi_procinst"."PROC_DEF_ID_" IS '流程定义ID，流程模板标识';
COMMENT ON COLUMN "public"."act_hi_procinst"."START_TIME_" IS '开始时间，流程发起时间';
COMMENT ON COLUMN "public"."act_hi_procinst"."END_TIME_" IS '结束时间 ， NULL 表示未结束
';
COMMENT ON COLUMN "public"."act_hi_procinst"."START_USER_ID_" IS '发起人工号，关联tbl_org_personal.code';
COMMENT ON COLUMN "public"."act_hi_procinst"."DELETE_REASON_" IS '终止原因，非NULL表示已终止';
COMMENT ON COLUMN "public"."act_hi_procinst"."NAME_" IS '流程实例名称，如"张三的请假申请"';
COMMENT ON COLUMN "public"."act_hi_procinst"."BUSINESS_STATUS_" IS '业务状态，自定义业务状态';
COMMENT ON TABLE "public"."act_hi_procinst" IS '历史流程实例表';

-- ----------------------------
-- Table structure for act_hi_taskinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_taskinst";
CREATE TABLE "public"."act_hi_taskinst" (
  "ID_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "REV_" int4,
  "PROC_DEF_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "TASK_DEF_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "TASK_DEF_KEY_" varchar(255) COLLATE "pg_catalog"."default",
  "PROC_INST_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "EXECUTION_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "SCOPE_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "SUB_SCOPE_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "SCOPE_TYPE_" varchar(255) COLLATE "pg_catalog"."default",
  "SCOPE_DEFINITION_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "PROPAGATED_STAGE_INST_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "STATE_" varchar(255) COLLATE "pg_catalog"."default",
  "NAME_" varchar(255) COLLATE "pg_catalog"."default",
  "PARENT_TASK_ID_" varchar(64) COLLATE "pg_catalog"."default",
  "DESCRIPTION_" varchar(4000) COLLATE "pg_catalog"."default",
  "OWNER_" varchar(255) COLLATE "pg_catalog"."default",
  "ASSIGNEE_" varchar(255) COLLATE "pg_catalog"."default",
  "START_TIME_" timestamp(6) NOT NULL,
  "IN_PROGRESS_TIME_" timestamp(6),
  "IN_PROGRESS_STARTED_BY_" varchar(255) COLLATE "pg_catalog"."default",
  "CLAIM_TIME_" timestamp(6),
  "CLAIMED_BY_" varchar(255) COLLATE "pg_catalog"."default",
  "SUSPENDED_TIME_" timestamp(6),
  "SUSPENDED_BY_" varchar(255) COLLATE "pg_catalog"."default",
  "END_TIME_" timestamp(6),
  "COMPLETED_BY_" varchar(255) COLLATE "pg_catalog"."default",
  "DURATION_" int8,
  "DELETE_REASON_" varchar(4000) COLLATE "pg_catalog"."default",
  "PRIORITY_" int4,
  "IN_PROGRESS_DUE_DATE_" timestamp(6),
  "DUE_DATE_" timestamp(6),
  "FORM_KEY_" varchar(255) COLLATE "pg_catalog"."default",
  "CATEGORY_" varchar(255) COLLATE "pg_catalog"."default",
  "TENANT_ID_" varchar(255) COLLATE "pg_catalog"."default",
  "LAST_UPDATED_TIME_" timestamp(6)
)
;
ALTER TABLE "public"."act_hi_taskinst" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."act_hi_taskinst"."ID_" IS '任务ID（主键）';
COMMENT ON COLUMN "public"."act_hi_taskinst"."TASK_DEF_KEY_" IS '任务节点标识，如"approveTask"';
COMMENT ON COLUMN "public"."act_hi_taskinst"."PROC_INST_ID_" IS '所属流程实例ID，关联act_hi_procinst';
COMMENT ON COLUMN "public"."act_hi_taskinst"."NAME_" IS '任务名称，如"部门经理审批"';
COMMENT ON COLUMN "public"."act_hi_taskinst"."ASSIGNEE_" IS '执行人工号，关联tbl_org_personal.code';
COMMENT ON COLUMN "public"."act_hi_taskinst"."START_TIME_" IS '任务创建时间';
COMMENT ON COLUMN "public"."act_hi_taskinst"."END_TIME_" IS '任务结束时间';
COMMENT ON COLUMN "public"."act_hi_taskinst"."COMPLETED_BY_" IS '完成人工号，关联tbl_org_personal.code';
COMMENT ON COLUMN "public"."act_hi_taskinst"."DURATION_" IS '任务耗时（毫秒）';
COMMENT ON TABLE "public"."act_hi_taskinst" IS '历史任务实例表';

-- ----------------------------
-- Table structure for tbl_agent_user_agent_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_agent_user_agent_info";
CREATE TABLE "public"."tbl_agent_user_agent_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "agent_sn" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "action_count" int8 DEFAULT 0,
  "last_date" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_agent_user_agent_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_agent_user_agent_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_agent_user_agent_info"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."tbl_agent_user_agent_info"."agent_sn" IS '智能体标识';
COMMENT ON COLUMN "public"."tbl_agent_user_agent_info"."action_count" IS '操作次数';
COMMENT ON COLUMN "public"."tbl_agent_user_agent_info"."last_date" IS '最后操作时间';
COMMENT ON TABLE "public"."tbl_agent_user_agent_info" IS '用户-智能体操作记录表';

-- ----------------------------
-- Table structure for tbl_agent_user_memory_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_agent_user_memory_info";
CREATE TABLE "public"."tbl_agent_user_memory_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "agent_sn" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "memory_type" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "content" text COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_agent_user_memory_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."agent_sn" IS '智能体标识';
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."memory_type" IS '记忆类型：PROFILE-用户画像，FACT-事实记忆';
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."content" IS '记忆内容（JSON或纯文本）';
COMMENT ON COLUMN "public"."tbl_agent_user_memory_info"."created_at" IS '创建时间';
COMMENT ON TABLE "public"."tbl_agent_user_memory_info" IS '用户记忆信息表';

-- ----------------------------
-- Table structure for tbl_agent_user_profile_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_agent_user_profile_info";
CREATE TABLE "public"."tbl_agent_user_profile_info" (
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_sn" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "profile_data" jsonb,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_agent_user_profile_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_agent_user_profile_info"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."tbl_agent_user_profile_info"."agent_sn" IS '智能体标识';
COMMENT ON COLUMN "public"."tbl_agent_user_profile_info"."profile_data" IS '画像数据（JSON格式）';
COMMENT ON COLUMN "public"."tbl_agent_user_profile_info"."updated_at" IS '更新时间';
COMMENT ON TABLE "public"."tbl_agent_user_profile_info" IS '用户画像信息表';

-- ----------------------------
-- Table structure for tbl_data_agent
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent";
CREATE TABLE "public"."tbl_data_agent" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 5
CACHE 1
),
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "avatar" text COLLATE "pg_catalog"."default",
  "status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'draft'::character varying,
  "api_key" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "api_key_enabled" int2 DEFAULT 0,
  "prompt" text COLLATE "pg_catalog"."default",
  "category" varchar(100) COLLATE "pg_catalog"."default",
  "admin_id" int8,
  "tags" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "type" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(40) COLLATE "pg_catalog"."default",
  "category_id" varchar(64) COLLATE "pg_catalog"."default",
  "order_num" int4
)
;
ALTER TABLE "public"."tbl_data_agent" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent"."name" IS '智能体名称';
COMMENT ON COLUMN "public"."tbl_data_agent"."description" IS '智能体描述';
COMMENT ON COLUMN "public"."tbl_data_agent"."avatar" IS '头像URL';
COMMENT ON COLUMN "public"."tbl_data_agent"."status" IS '状态：draft-待发布，published-已发布，offline-已下线';
COMMENT ON COLUMN "public"."tbl_data_agent"."api_key" IS '访问 API Key，格式 sk-xxx';
COMMENT ON COLUMN "public"."tbl_data_agent"."api_key_enabled" IS 'API Key 是否启用：0-禁用，1-启用';
COMMENT ON COLUMN "public"."tbl_data_agent"."prompt" IS '自定义Prompt配置';
COMMENT ON COLUMN "public"."tbl_data_agent"."category" IS '分类';
COMMENT ON COLUMN "public"."tbl_data_agent"."admin_id" IS '管理员ID';
COMMENT ON COLUMN "public"."tbl_data_agent"."tags" IS '标签，逗号分隔';
COMMENT ON COLUMN "public"."tbl_data_agent"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_agent"."type" IS '类型';
COMMENT ON COLUMN "public"."tbl_data_agent"."sn" IS '标识';
COMMENT ON COLUMN "public"."tbl_data_agent"."category_id" IS '分类id';
COMMENT ON COLUMN "public"."tbl_data_agent"."order_num" IS '排序号';
COMMENT ON TABLE "public"."tbl_data_agent" IS '智能体表';

-- ----------------------------
-- Table structure for tbl_data_agent_category
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent_category";
CREATE TABLE "public"."tbl_data_agent_category" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "pid" varchar(64) COLLATE "pg_catalog"."default",
  "sn" varchar(100) COLLATE "pg_catalog"."default",
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_data_agent_category" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent_category"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."pid" IS '父分类ID';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."sn" IS '分类编码';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."name" IS '分类名称';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."description" IS '分类描述';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_data_agent_category"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_data_agent_category" IS '智能体分类表';

-- ----------------------------
-- Table structure for tbl_data_agent_datasource
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent_datasource";
CREATE TABLE "public"."tbl_data_agent_datasource" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "agent_id" int4 NOT NULL,
  "datasource_id" int4 NOT NULL,
  "is_active" int2 DEFAULT 0,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_agent_datasource" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent_datasource"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource"."datasource_id" IS '数据源ID';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource"."is_active" IS '是否启用：0-禁用，1-启用';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_agent_datasource" IS '智能体数据源关联表';

-- ----------------------------
-- Table structure for tbl_data_agent_datasource_tables
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent_datasource_tables";
CREATE TABLE "public"."tbl_data_agent_datasource_tables" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "agent_datasource_id" int4 NOT NULL,
  "table_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_agent_datasource_tables" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent_datasource_tables"."agent_datasource_id" IS '智能体数据源ID';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource_tables"."table_name" IS '数据表名';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource_tables"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent_datasource_tables"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_agent_datasource_tables" IS '某个智能体某个数据源所选中的数据表';

-- ----------------------------
-- Table structure for tbl_data_agent_knowledge
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent_knowledge";
CREATE TABLE "public"."tbl_data_agent_knowledge" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "agent_id" int4 NOT NULL,
  "title" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "question" text COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default",
  "is_recall" int4 DEFAULT 1,
  "embedding_status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "error_msg" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "source_filename" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "file_path" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "file_size" int8,
  "file_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "splitter_type" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'token'::character varying,
  "created_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" int4 DEFAULT 0,
  "is_resource_cleaned" int4 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_data_agent_knowledge" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."agent_id" IS '关联的智能体ID';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."title" IS '知识的标题 (用户定义, 用于在UI上展示和识别)';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."type" IS '知识类型: DOCUMENT-文档, QA-问答, FAQ-常见问题';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."question" IS '问题 (仅当type为QA或FAQ时使用)';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."content" IS '知识内容 (对于QA/FAQ是答案; 对于DOCUMENT, 此字段通常为空)';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."is_recall" IS '业务状态: 1=召回, 0=非召回';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."embedding_status" IS '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."error_msg" IS '操作失败的错误信息';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."source_filename" IS '上传时的原始文件名';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."file_path" IS '文件在服务器上的物理存储路径';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."file_size" IS '文件大小 (字节)';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."file_type" IS '文件类型（pdf,md,markdown,doc等）';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."splitter_type" IS '分块策略类型：token, recursive, sentence, semantic';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."updated_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."is_deleted" IS '逻辑删除字段，0=未删除, 1=已删除';
COMMENT ON COLUMN "public"."tbl_data_agent_knowledge"."is_resource_cleaned" IS '0=物理资源（文件和向量）未清理, 1=物理资源已清理';
COMMENT ON TABLE "public"."tbl_data_agent_knowledge" IS '智能体知识源管理表 (支持文档、QA、FAQ)';

-- ----------------------------
-- Table structure for tbl_data_agent_preset_question
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_agent_preset_question";
CREATE TABLE "public"."tbl_data_agent_preset_question" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "agent_id" int4 NOT NULL,
  "question" text COLLATE "pg_catalog"."default" NOT NULL,
  "sort_order" int4 DEFAULT 0,
  "is_active" bool,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "account_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_data_agent_preset_question" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."question" IS '预设问题内容';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."sort_order" IS '排序顺序';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."is_active" IS '是否启用：0-禁用，1-启用';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_agent_preset_question"."account_id" IS '账号id';
COMMENT ON TABLE "public"."tbl_data_agent_preset_question" IS '智能体预设问题表';

-- ----------------------------
-- Table structure for tbl_data_business_knowledge
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_business_knowledge";
CREATE TABLE "public"."tbl_data_business_knowledge" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 5
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "business_term" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "synonyms" text COLLATE "pg_catalog"."default",
  "is_recall" int4 DEFAULT 1,
  "agent_id" int4 NOT NULL,
  "created_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "embedding_status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "error_msg" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_deleted" int4 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_data_business_knowledge" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."business_term" IS '业务名词';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."description" IS '描述';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."synonyms" IS '同义词，逗号分隔';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."is_recall" IS '是否召回：0-不召回，1-召回';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."agent_id" IS '关联的智能体ID';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."updated_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."embedding_status" IS '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."error_msg" IS '操作失败的错误信息';
COMMENT ON COLUMN "public"."tbl_data_business_knowledge"."is_deleted" IS '逻辑删除：0-未删除，1-已删除';
COMMENT ON TABLE "public"."tbl_data_business_knowledge" IS '业务知识表';

-- ----------------------------
-- Table structure for tbl_data_categories
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_categories";
CREATE TABLE "public"."tbl_data_categories" (
  "id" int4 NOT NULL DEFAULT nextval('tbl_data_categories_id_seq'::regclass),
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."tbl_data_categories" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_data_chat_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_chat_message";
CREATE TABLE "public"."tbl_data_chat_message" (
  "id" int8 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
),
  "session_id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "role" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "message_type" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'text'::character varying,
  "metadata" jsonb,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_chat_message" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_chat_message"."session_id" IS '会话ID';
COMMENT ON COLUMN "public"."tbl_data_chat_message"."role" IS '角色：user-用户，assistant-助手，system-系统';
COMMENT ON COLUMN "public"."tbl_data_chat_message"."content" IS '消息内容';
COMMENT ON COLUMN "public"."tbl_data_chat_message"."message_type" IS '消息类型：text-文本，sql-SQL查询，result-查询结果，error-错误';
COMMENT ON COLUMN "public"."tbl_data_chat_message"."metadata" IS '元数据（JSON格式）';
COMMENT ON COLUMN "public"."tbl_data_chat_message"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."tbl_data_chat_message" IS '聊天消息表';

-- ----------------------------
-- Table structure for tbl_data_chat_session
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_chat_session";
CREATE TABLE "public"."tbl_data_chat_session" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" int4 NOT NULL,
  "title" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '新对话'::character varying,
  "status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'active'::character varying,
  "is_pinned" bool,
  "user_id" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_chat_session" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_chat_session"."id" IS '会话ID（UUID）';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."title" IS '会话标题';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."status" IS '状态：active-活跃，archived-归档，deleted-已删除';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."is_pinned" IS '是否置顶：0-否，1-是';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_chat_session"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_chat_session" IS '聊天会话表';

-- ----------------------------
-- Table structure for tbl_data_datasource
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_datasource";
CREATE TABLE "public"."tbl_data_datasource" (
  "id" int4 NOT NULL GENERATED ALWAYS AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "host" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "port" int4 NOT NULL,
  "database_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "username" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "connection_url" varchar(1000) COLLATE "pg_catalog"."default",
  "status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'inactive'::character varying,
  "test_status" varchar(50) COLLATE "pg_catalog"."default" DEFAULT 'unknown'::character varying,
  "description" text COLLATE "pg_catalog"."default",
  "creator_id" int8,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_datasource" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_datasource"."name" IS '数据源名称';
COMMENT ON COLUMN "public"."tbl_data_datasource"."type" IS '数据源类型：mysql, postgresql';
COMMENT ON COLUMN "public"."tbl_data_datasource"."host" IS '主机地址';
COMMENT ON COLUMN "public"."tbl_data_datasource"."port" IS '端口号';
COMMENT ON COLUMN "public"."tbl_data_datasource"."database_name" IS '数据库名称';
COMMENT ON COLUMN "public"."tbl_data_datasource"."username" IS '用户名';
COMMENT ON COLUMN "public"."tbl_data_datasource"."password" IS '密码（加密存储）';
COMMENT ON COLUMN "public"."tbl_data_datasource"."connection_url" IS '完整连接URL';
COMMENT ON COLUMN "public"."tbl_data_datasource"."status" IS '状态：active-启用，inactive-禁用';
COMMENT ON COLUMN "public"."tbl_data_datasource"."test_status" IS '连接测试状态：success-成功，failed-失败，unknown-未知';
COMMENT ON COLUMN "public"."tbl_data_datasource"."description" IS '描述';
COMMENT ON COLUMN "public"."tbl_data_datasource"."creator_id" IS '创建者ID';
COMMENT ON COLUMN "public"."tbl_data_datasource"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_datasource"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_datasource" IS '数据源表';

-- ----------------------------
-- Table structure for tbl_data_logical_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_logical_relation";
CREATE TABLE "public"."tbl_data_logical_relation" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "datasource_id" int4 NOT NULL,
  "source_table_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "source_column_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "target_table_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "target_column_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "relation_type" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_deleted" int2 DEFAULT 0,
  "created_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_logical_relation" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."datasource_id" IS '关联的数据源ID';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."source_table_name" IS '主表名 (例如 t_order)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."source_column_name" IS '主表字段名 (例如 buyer_uid)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."target_table_name" IS '关联表名 (例如 t_user)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."target_column_name" IS '关联表字段名 (例如 id)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."relation_type" IS '关系类型: 1:1, 1:N, N:1 (辅助LLM理解数据基数，可选)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."description" IS '业务描述: 存入Prompt中帮助LLM理解 (例如: 订单表通过buyer_uid关联用户表id)';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."is_deleted" IS '逻辑删除: 0-未删除, 1-已删除';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_logical_relation"."updated_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_logical_relation" IS '逻辑外键配置表';

-- ----------------------------
-- Table structure for tbl_data_model_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_model_config";
CREATE TABLE "public"."tbl_data_model_config" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "provider" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "base_url" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "api_key" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "model_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "temperature" numeric(10,2) DEFAULT 0.00,
  "is_active" bool,
  "max_tokens" int4 DEFAULT 2000,
  "model_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'CHAT'::character varying,
  "completions_path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "embeddings_path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "created_time" timestamp(6),
  "updated_time" timestamp(6),
  "is_deleted" int4 DEFAULT 0,
  "proxy_enabled" bool,
  "proxy_host" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "proxy_port" int4,
  "proxy_username" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "proxy_password" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
ALTER TABLE "public"."tbl_data_model_config" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_model_config"."provider" IS '厂商标识 (方便前端展示回显，实际调用主要靠 baseUrl)';
COMMENT ON COLUMN "public"."tbl_data_model_config"."base_url" IS '关键配置';
COMMENT ON COLUMN "public"."tbl_data_model_config"."api_key" IS 'API密钥';
COMMENT ON COLUMN "public"."tbl_data_model_config"."model_name" IS '模型名称';
COMMENT ON COLUMN "public"."tbl_data_model_config"."temperature" IS '温度参数';
COMMENT ON COLUMN "public"."tbl_data_model_config"."is_active" IS '是否激活';
COMMENT ON COLUMN "public"."tbl_data_model_config"."max_tokens" IS '输出响应最大令牌数';
COMMENT ON COLUMN "public"."tbl_data_model_config"."model_type" IS '模型类型 (CHAT/EMBEDDING)';
COMMENT ON COLUMN "public"."tbl_data_model_config"."completions_path" IS 'Chat模型专用。附加到 Base URL 的路径。例如OpenAi的/v1/chat/completions';
COMMENT ON COLUMN "public"."tbl_data_model_config"."embeddings_path" IS '嵌入模型专用。附加到 Base URL 的路径。';
COMMENT ON COLUMN "public"."tbl_data_model_config"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_model_config"."updated_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_model_config"."is_deleted" IS '0=未删除, 1=已删除';
COMMENT ON COLUMN "public"."tbl_data_model_config"."proxy_enabled" IS '是否启用代理：0-禁用，1-启用';
COMMENT ON COLUMN "public"."tbl_data_model_config"."proxy_host" IS '代理主机地址';
COMMENT ON COLUMN "public"."tbl_data_model_config"."proxy_port" IS '代理端口';
COMMENT ON COLUMN "public"."tbl_data_model_config"."proxy_username" IS '代理用户名（可选）';
COMMENT ON COLUMN "public"."tbl_data_model_config"."proxy_password" IS '代理密码（可选）';
COMMENT ON TABLE "public"."tbl_data_model_config" IS '模型配置表';

-- ----------------------------
-- Table structure for tbl_data_order_items
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_order_items";
CREATE TABLE "public"."tbl_data_order_items" (
  "id" int4 NOT NULL DEFAULT nextval('tbl_data_order_items_id_seq'::regclass),
  "order_id" int4 NOT NULL,
  "product_id" int4 NOT NULL,
  "quantity" int4 NOT NULL,
  "unit_price" numeric(10,2) NOT NULL
)
;
ALTER TABLE "public"."tbl_data_order_items" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_data_product_categories
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_product_categories";
CREATE TABLE "public"."tbl_data_product_categories" (
  "product_id" int4 NOT NULL,
  "category_id" int4 NOT NULL
)
;
ALTER TABLE "public"."tbl_data_product_categories" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_data_semantic_model
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_semantic_model";
CREATE TABLE "public"."tbl_data_semantic_model" (
  "id" int4 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "agent_id" int4 NOT NULL,
  "datasource_id" int4 NOT NULL,
  "table_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "column_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "business_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "synonyms" text COLLATE "pg_catalog"."default",
  "business_description" text COLLATE "pg_catalog"."default",
  "column_comment" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "data_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "status" int2 NOT NULL DEFAULT 1,
  "created_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_data_semantic_model" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."agent_id" IS '关联的智能体ID';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."datasource_id" IS '关联的数据源ID';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."table_name" IS '关联的表名';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."column_name" IS '数据库中的物理字段名 (例如: csat_score)';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."business_name" IS '业务名/别名 (例如: 客户满意度分数)';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."synonyms" IS '业务名的同义词 (例如: 满意度,客户评分)';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."business_description" IS '业务描述 (用于向LLM解释字段的业务含义)';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."column_comment" IS '数据库中的物理字段的原始注释';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."data_type" IS '物理数据类型 (例如: int, varchar(20))';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."status" IS '0 停用 1 启用';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_semantic_model"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."tbl_data_semantic_model" IS '语义模型表';

-- ----------------------------
-- Table structure for tbl_data_user_prompt_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_data_user_prompt_config";
CREATE TABLE "public"."tbl_data_user_prompt_config" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "prompt_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" int4,
  "system_prompt" text COLLATE "pg_catalog"."default" NOT NULL,
  "enabled" bool DEFAULT true,
  "description" text COLLATE "pg_catalog"."default",
  "priority" int4 DEFAULT 0,
  "display_order" int4 DEFAULT 0,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_data_user_prompt_config" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."id" IS '配置ID（UUID）';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."name" IS '配置名称';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."prompt_type" IS 'Prompt类型（如report-generator, planner等）';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."agent_id" IS '关联的智能体ID，为空表示全局配置';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."system_prompt" IS '用户自定义系统Prompt内容';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."enabled" IS '是否启用该配置：0-禁用，1-启用';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."description" IS '配置描述';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."priority" IS '配置优先级，数字越大优先级越高';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."display_order" IS '配置显示顺序，数字越小越靠前';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_data_user_prompt_config"."creator" IS '创建者';
COMMENT ON TABLE "public"."tbl_data_user_prompt_config" IS '用户Prompt配置表';

-- ----------------------------
-- Table structure for tbl_platform_account_group_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_account_group_info";
CREATE TABLE "public"."tbl_platform_account_group_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "account_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "group_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "account_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_account_group_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."group_id" IS '组织ID';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."account_id" IS '账号ID';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."group_name" IS '组织名称';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."account_name" IS '账号名称';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_account_group_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_account_group_info" IS '账号-组织关联表';

-- ----------------------------
-- Table structure for tbl_platform_account_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_account_info";
CREATE TABLE "public"."tbl_platform_account_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "username" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "password" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "real_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "nick_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "birthday" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "email" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "phone" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "avatar_url" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "gender" varchar(10) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(10) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "third_party_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1,
  "dept_id" varchar(64) COLLATE "pg_catalog"."default",
  "dept_name" varchar(128) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_platform_account_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_account_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."code" IS '账号编码';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."username" IS '用户名';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."password" IS '密码（MD5加密）';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."real_name" IS '真实姓名';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."nick_name" IS '昵称';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."birthday" IS '生日';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."email" IS '邮箱';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."phone" IS '手机号';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."avatar_url" IS '头像地址';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."gender" IS '性别';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."status" IS '状态 0-禁用 1-启用';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."third_party_id" IS '第三方平台ID';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."dept_id" IS '部门ID';
COMMENT ON COLUMN "public"."tbl_platform_account_info"."dept_name" IS '部门名称';
COMMENT ON TABLE "public"."tbl_platform_account_info" IS '前台账号信息表';

-- ----------------------------
-- Table structure for tbl_platform_account_tenant_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_account_tenant_info";
CREATE TABLE "public"."tbl_platform_account_tenant_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "account_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_account_tenant_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."account_id" IS '账号ID';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_account_tenant_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_account_tenant_info" IS '用户租户关联表';

-- ----------------------------
-- Table structure for tbl_platform_dingtalk
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_dingtalk";
CREATE TABLE "public"."tbl_platform_dingtalk" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_code" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "user_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "userid" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "unionid" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "avatar" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_dingtalk" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."user_code" IS '用户编码';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."user_name" IS '用户名称';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."userid" IS '钉钉用户ID';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."unionid" IS '钉钉唯一标识';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."avatar" IS '头像地址';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_dingtalk"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_dingtalk" IS '钉钉用户信息表';

-- ----------------------------
-- Table structure for tbl_platform_group_agent_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_group_agent_info";
CREATE TABLE "public"."tbl_platform_group_agent_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "agent_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_group_agent_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."group_id" IS '用户组ID';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_group_agent_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_group_agent_info" IS '用户组-智能体关联表';

-- ----------------------------
-- Table structure for tbl_platform_group_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_group_info";
CREATE TABLE "public"."tbl_platform_group_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "sn" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2
)
;
ALTER TABLE "public"."tbl_platform_group_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_group_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."name" IS '组织名称';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."sn" IS '组织编码';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."description" IS '组描述';
COMMENT ON COLUMN "public"."tbl_platform_group_info"."status" IS '状态0为启用 1为禁用';
COMMENT ON TABLE "public"."tbl_platform_group_info" IS '组织信息表';

-- ----------------------------
-- Table structure for tbl_platform_platform_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_platform_info";
CREATE TABLE "public"."tbl_platform_platform_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "corpid" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "corpsecret" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "agentid" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_platform_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."type" IS '平台类型';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."name" IS '平台名称';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."corpid" IS '企业ID';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."corpsecret" IS '企业密钥';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."agentid" IS '应用AgentId';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_platform_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_platform_info" IS '三方平台信息表';

-- ----------------------------
-- Table structure for tbl_platform_tenant_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_platform_tenant_info";
CREATE TABLE "public"."tbl_platform_tenant_info" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "description" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "creator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updator" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "del_flag" int2 DEFAULT 1
)
;
ALTER TABLE "public"."tbl_platform_tenant_info" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."id" IS '主键（雪花算法ID）';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."sn" IS '租户编码';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."name" IS '租户名称';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."description" IS '租户描述';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."creator" IS '创建人';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."updator" IS '更新人';
COMMENT ON COLUMN "public"."tbl_platform_tenant_info"."del_flag" IS '删除标识 0-已删除 1-未删除';
COMMENT ON TABLE "public"."tbl_platform_tenant_info" IS '租户信息表';

-- ----------------------------
-- Table structure for tbl_privilege_acl
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_acl";
CREATE TABLE "public"."tbl_privilege_acl" (
  "id" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "release_id" varchar(200) COLLATE "pg_catalog"."default",
  "release_sn" varchar(10) COLLATE "pg_catalog"."default" DEFAULT 'role'::character varying,
  "system_sn" varchar(40) COLLATE "pg_catalog"."default",
  "module_id" varchar(200) COLLATE "pg_catalog"."default",
  "module_sn" varchar(40) COLLATE "pg_catalog"."default",
  "acl_state" varchar(100) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_privilege_acl" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_acl"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."release_id" IS '来源id';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."release_sn" IS '来源标识 role表示角色user 表示用户';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."system_sn" IS '系统标识';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."module_id" IS '模块id';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."module_sn" IS '模块标识';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_acl"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON TABLE "public"."tbl_privilege_acl" IS '权限访问控制表';

-- ----------------------------
-- Table structure for tbl_privilege_company
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_company";
CREATE TABLE "public"."tbl_privilege_company" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "pid" varchar(64) COLLATE "pg_catalog"."default",
  "cname" varchar(32) COLLATE "pg_catalog"."default",
  "ename" varchar(32) COLLATE "pg_catalog"."default",
  "idm_company_id" varchar(64) COLLATE "pg_catalog"."default",
  "short_name" varchar(120) COLLATE "pg_catalog"."default",
  "code" varchar(32) COLLATE "pg_catalog"."default",
  "descr" varchar(200) COLLATE "pg_catalog"."default",
  "status" int4 DEFAULT 1,
  "create_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "del_flag" int4 NOT NULL DEFAULT 0,
  "sort" int2
)
;
ALTER TABLE "public"."tbl_privilege_company" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_company"."pid" IS '父级id';
COMMENT ON COLUMN "public"."tbl_privilege_company"."cname" IS '公司中文名称';
COMMENT ON COLUMN "public"."tbl_privilege_company"."ename" IS '公司英文名称';
COMMENT ON COLUMN "public"."tbl_privilege_company"."idm_company_id" IS 'idm公司id';
COMMENT ON COLUMN "public"."tbl_privilege_company"."short_name" IS '公司简称';
COMMENT ON COLUMN "public"."tbl_privilege_company"."code" IS '公司code';
COMMENT ON COLUMN "public"."tbl_privilege_company"."descr" IS '描述';
COMMENT ON COLUMN "public"."tbl_privilege_company"."status" IS '状态 1启用 0禁用';
COMMENT ON COLUMN "public"."tbl_privilege_company"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_company"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_company"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_company"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_company"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_company"."sort" IS '顺序';
COMMENT ON TABLE "public"."tbl_privilege_company" IS '公司表';

-- ----------------------------
-- Table structure for tbl_privilege_department
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_department";
CREATE TABLE "public"."tbl_privilege_department" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "note" varchar(80) COLLATE "pg_catalog"."default",
  "pid" varchar(64) COLLATE "pg_catalog"."default",
  "order_no" int4,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 NOT NULL DEFAULT 0,
  "leader" int4 NOT NULL DEFAULT 0,
  "department_type" int4 NOT NULL,
  "status" int2 DEFAULT 0,
  "nature" int2 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_privilege_department" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_department"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_department"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."tbl_privilege_department"."name" IS '名称';
COMMENT ON COLUMN "public"."tbl_privilege_department"."code" IS '编号';
COMMENT ON COLUMN "public"."tbl_privilege_department"."note" IS '备注';
COMMENT ON COLUMN "public"."tbl_privilege_department"."pid" IS '父id';
COMMENT ON COLUMN "public"."tbl_privilege_department"."order_no" IS '排序号';
COMMENT ON COLUMN "public"."tbl_privilege_department"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_department"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_department"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_department"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_department"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_department"."leader" IS '是否是leader1:是 0：不是';
COMMENT ON COLUMN "public"."tbl_privilege_department"."department_type" IS '是否为自建部门 0-自建部门，1-idm同步部门';
COMMENT ON COLUMN "public"."tbl_privilege_department"."status" IS '部门状态，0启用、1禁用';
COMMENT ON COLUMN "public"."tbl_privilege_department"."nature" IS '部门性质：0部门、1组';
COMMENT ON TABLE "public"."tbl_privilege_department" IS '部门';

-- ----------------------------
-- Table structure for tbl_privilege_dictionary
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_dictionary";
CREATE TABLE "public"."tbl_privilege_dictionary" (
  "id" int8 NOT NULL,
  "code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "pcode" varchar(64) COLLATE "pg_catalog"."default",
  "system_sn" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "sn" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "order_no" int4,
  "del_flag" int2 NOT NULL DEFAULT 0,
  "creator_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_privilege_dictionary" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."code" IS '编码';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."name" IS '名称';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."pcode" IS '父编码';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."system_sn" IS '系统标识';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."sn" IS '标识';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."order_no" IS '排序号';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."creator_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_dictionary"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."tbl_privilege_dictionary" IS '字典表';

-- ----------------------------
-- Table structure for tbl_privilege_employee
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_employee";
CREATE TABLE "public"."tbl_privilege_employee" (
  "id" varchar(36) COLLATE "pg_catalog"."default" NOT NULL,
  "emp_code" varchar(255) COLLATE "pg_catalog"."default",
  "emp_name" varchar(255) COLLATE "pg_catalog"."default",
  "position_code" varchar(255) COLLATE "pg_catalog"."default",
  "job_grade_code" varchar(255) COLLATE "pg_catalog"."default",
  "leader_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "leader_user_name" varchar(255) COLLATE "pg_catalog"."default",
  "company_id" varchar(255) COLLATE "pg_catalog"."default",
  "company_name" varchar(255) COLLATE "pg_catalog"."default",
  "dept_id" varchar(255) COLLATE "pg_catalog"."default",
  "dept_name" varchar(255) COLLATE "pg_catalog"."default",
  "sex" int4,
  "status" int4 DEFAULT 3,
  "enable_flag" int4 DEFAULT 3,
  "service_date" timestamp(6),
  "leave_date" timestamp(6),
  "third_union_id" varchar(255) COLLATE "pg_catalog"."default",
  "third_open_id" varchar(255) COLLATE "pg_catalog"."default",
  "third_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "del_flag" int2 NOT NULL DEFAULT 0,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "mobile" varchar(255) COLLATE "pg_catalog"."default",
  "email" varchar(255) COLLATE "pg_catalog"."default",
  "is_dept_leader" varchar(255) COLLATE "pg_catalog"."default",
  "paths" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_privilege_employee" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_employee"."id" IS 'id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."emp_code" IS '工号';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."emp_name" IS '员工姓名';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."position_code" IS '岗位编码';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."job_grade_code" IS '职级编码';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."leader_user_id" IS '直属领导工号';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."leader_user_name" IS '直属领导姓名';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."company_name" IS '公司名称';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."dept_id" IS '部门id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."dept_name" IS '部门名称';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."sex" IS '0：保密 1：男 2：女 3：其他';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."status" IS '2离职3在职';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."enable_flag" IS '是否禁用';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."service_date" IS '入职时间';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."leave_date" IS '离职时间';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."third_union_id" IS '三方平台union_id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."third_open_id" IS '三方平台open_id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."third_user_id" IS '三方平台user_id';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."del_flag" IS '删除标识;0：未删除；1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."mobile" IS '手机号';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."email" IS '邮箱';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."is_dept_leader" IS '是否为部门负责人';
COMMENT ON COLUMN "public"."tbl_privilege_employee"."paths" IS '全路径';
COMMENT ON TABLE "public"."tbl_privilege_employee" IS '员工';

-- ----------------------------
-- Table structure for tbl_privilege_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_group";
CREATE TABLE "public"."tbl_privilege_group" (
  "id" int8 NOT NULL GENERATED BY DEFAULT AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
),
  "super_id" int8,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2 NOT NULL,
  "state" int2 NOT NULL,
  "introduction" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "creator_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 NOT NULL DEFAULT 0
)
;
ALTER TABLE "public"."tbl_privilege_group" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_group"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_group"."super_id" IS '上级ID';
COMMENT ON COLUMN "public"."tbl_privilege_group"."name" IS '数据组名称';
COMMENT ON COLUMN "public"."tbl_privilege_group"."type" IS '数据组类型';
COMMENT ON COLUMN "public"."tbl_privilege_group"."state" IS '数据组状态：0-开；1-关';
COMMENT ON COLUMN "public"."tbl_privilege_group"."introduction" IS '介绍';
COMMENT ON COLUMN "public"."tbl_privilege_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_group"."creator_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_group"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_group"."del_flag" IS '删除标识0：删除1：存在';
COMMENT ON TABLE "public"."tbl_privilege_group" IS '数据组合表';

-- ----------------------------
-- Table structure for tbl_privilege_login_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_login_log";
CREATE TABLE "public"."tbl_privilege_login_log" (
  "id" int8 NOT NULL,
  "operation_id" int8,
  "ip" varchar(45) COLLATE "pg_catalog"."default",
  "operation_username" varchar(32) COLLATE "pg_catalog"."default",
  "operation_person" varchar(32) COLLATE "pg_catalog"."default",
  "operation_content" varchar(128) COLLATE "pg_catalog"."default",
  "operation_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "del_flag" int4,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6)
)
;
ALTER TABLE "public"."tbl_privilege_login_log" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."id" IS '主键id';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."operation_id" IS '操作人id';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."ip" IS '访问ip';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."operation_username" IS '操作人的姓名';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."operation_person" IS '操作人姓名';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."operation_content" IS '操作内容';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."operation_time" IS '操作时间';
COMMENT ON COLUMN "public"."tbl_privilege_login_log"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON TABLE "public"."tbl_privilege_login_log" IS '登录日志';

-- ----------------------------
-- Table structure for tbl_privilege_module
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_module";
CREATE TABLE "public"."tbl_privilege_module" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default",
  "url" varchar(128) COLLATE "pg_catalog"."default",
  "sn" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "state" varchar(100) COLLATE "pg_catalog"."default",
  "component" varchar(120) COLLATE "pg_catalog"."default",
  "system_id" int8,
  "status" int4,
  "image" varchar(200) COLLATE "pg_catalog"."default",
  "order_no" int4,
  "is_show" int4 DEFAULT 1,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 DEFAULT 0,
  "pid" varchar(128) COLLATE "pg_catalog"."default",
  "category_id" int4,
  "type" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_privilege_module" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_module"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_module"."name" IS '名称';
COMMENT ON COLUMN "public"."tbl_privilege_module"."url" IS '链接';
COMMENT ON COLUMN "public"."tbl_privilege_module"."sn" IS '标识';
COMMENT ON COLUMN "public"."tbl_privilege_module"."state" IS '存放该模块有哪些权限值可选';
COMMENT ON COLUMN "public"."tbl_privilege_module"."system_id" IS '系统id';
COMMENT ON COLUMN "public"."tbl_privilege_module"."image" IS '图片路径';
COMMENT ON COLUMN "public"."tbl_privilege_module"."order_no" IS '模块的排序号';
COMMENT ON COLUMN "public"."tbl_privilege_module"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_module"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_module"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_module"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_module"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_module"."pid" IS '父模块id';
COMMENT ON COLUMN "public"."tbl_privilege_module"."category_id" IS '权限id';
COMMENT ON COLUMN "public"."tbl_privilege_module"."type" IS '0目录，1菜单';
COMMENT ON TABLE "public"."tbl_privilege_module" IS '模块表';

-- ----------------------------
-- Table structure for tbl_privilege_pvalue
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_pvalue";
CREATE TABLE "public"."tbl_privilege_pvalue" (
  "id" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "position" int4 NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "order_no" int4,
  "remark" varchar(200) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 NOT NULL DEFAULT 0,
  "system_id" int8
)
;
ALTER TABLE "public"."tbl_privilege_pvalue" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."position" IS '整型的位';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."name" IS '名称';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."order_no" IS '排序号';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."remark" IS '备注';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_pvalue"."system_id" IS '系统id';
COMMENT ON TABLE "public"."tbl_privilege_pvalue" IS '权限值表';

-- ----------------------------
-- Table structure for tbl_privilege_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_role";
CREATE TABLE "public"."tbl_privilege_role" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "sn" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "role_level" varchar(32) COLLATE "pg_catalog"."default",
  "note" varchar(1024) COLLATE "pg_catalog"."default",
  "valid_state" int4 DEFAULT 1,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamp(6),
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 NOT NULL DEFAULT 0,
  "company_id" int8,
  "system_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."tbl_privilege_role" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_role"."id" IS '主键';
COMMENT ON COLUMN "public"."tbl_privilege_role"."name" IS '名称';
COMMENT ON COLUMN "public"."tbl_privilege_role"."sn" IS '标识';
COMMENT ON COLUMN "public"."tbl_privilege_role"."role_level" IS '角色等级【数据字典获取】';
COMMENT ON COLUMN "public"."tbl_privilege_role"."note" IS '备注';
COMMENT ON COLUMN "public"."tbl_privilege_role"."valid_state" IS '有效状态（1：有效；0：失效）';
COMMENT ON COLUMN "public"."tbl_privilege_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_role"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_role"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_role"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_role"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."tbl_privilege_role"."system_id" IS '系统id';
COMMENT ON TABLE "public"."tbl_privilege_role" IS '角色表';

-- ----------------------------
-- Table structure for tbl_privilege_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_user";
CREATE TABLE "public"."tbl_privilege_user" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(80) COLLATE "pg_catalog"."default" NOT NULL,
  "real_name" varchar(20) COLLATE "pg_catalog"."default",
  "username" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(100) COLLATE "pg_catalog"."default",
  "tel" varchar(32) COLLATE "pg_catalog"."default",
  "phone" varchar(32) COLLATE "pg_catalog"."default",
  "mobile" varchar(32) COLLATE "pg_catalog"."default",
  "email" varchar(256) COLLATE "pg_catalog"."default",
  "image" bytea,
  "company_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "dept_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "it_user_id" varchar(255) COLLATE "pg_catalog"."default",
  "it_user_name" varchar(32) COLLATE "pg_catalog"."default",
  "is_leader" int4 DEFAULT 0,
  "sex" int4 DEFAULT 0,
  "address" varchar(100) COLLATE "pg_catalog"."default",
  "fax" varchar(15) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "fail_month" int4,
  "failure_time" timestamp(6),
  "acl_timestamp" int4,
  "pwd_ftime" timestamp(6),
  "pwd_init" int4 DEFAULT 0,
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 DEFAULT 0,
  "user_type" int4 DEFAULT 0,
  "employee_id" varchar(65) COLLATE "pg_catalog"."default",
  "status" int2 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_privilege_user" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_user"."id" IS '主键ID';
COMMENT ON COLUMN "public"."tbl_privilege_user"."code" IS '工号';
COMMENT ON COLUMN "public"."tbl_privilege_user"."real_name" IS '真实姓名';
COMMENT ON COLUMN "public"."tbl_privilege_user"."username" IS '用户名';
COMMENT ON COLUMN "public"."tbl_privilege_user"."password" IS '密码';
COMMENT ON COLUMN "public"."tbl_privilege_user"."tel" IS '电话';
COMMENT ON COLUMN "public"."tbl_privilege_user"."phone" IS '座机';
COMMENT ON COLUMN "public"."tbl_privilege_user"."mobile" IS '手机';
COMMENT ON COLUMN "public"."tbl_privilege_user"."email" IS '邮箱';
COMMENT ON COLUMN "public"."tbl_privilege_user"."image" IS '头像';
COMMENT ON COLUMN "public"."tbl_privilege_user"."company_id" IS '公司ID';
COMMENT ON COLUMN "public"."tbl_privilege_user"."dept_id" IS '部门ID';
COMMENT ON COLUMN "public"."tbl_privilege_user"."it_user_id" IS 'it用户ID';
COMMENT ON COLUMN "public"."tbl_privilege_user"."it_user_name" IS 'it用户姓名';
COMMENT ON COLUMN "public"."tbl_privilege_user"."is_leader" IS '是否是领导1:是  0:否';
COMMENT ON COLUMN "public"."tbl_privilege_user"."sex" IS '性别 0标示男 1标示女  2';
COMMENT ON COLUMN "public"."tbl_privilege_user"."address" IS '地址';
COMMENT ON COLUMN "public"."tbl_privilege_user"."fax" IS '传真';
COMMENT ON COLUMN "public"."tbl_privilege_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_user"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_user"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_user"."fail_month" IS '失效月数';
COMMENT ON COLUMN "public"."tbl_privilege_user"."failure_time" IS '失效时间';
COMMENT ON COLUMN "public"."tbl_privilege_user"."acl_timestamp" IS '权限时间戳';
COMMENT ON COLUMN "public"."tbl_privilege_user"."pwd_ftime" IS '密码失效日期';
COMMENT ON COLUMN "public"."tbl_privilege_user"."pwd_init" IS '初始密码是否已修改 1是0否';
COMMENT ON COLUMN "public"."tbl_privilege_user"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_user"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON COLUMN "public"."tbl_privilege_user"."user_type" IS '用户类型 0：自建用户 1：idm用户';
COMMENT ON COLUMN "public"."tbl_privilege_user"."employee_id" IS '人员id';
COMMENT ON COLUMN "public"."tbl_privilege_user"."status" IS '人员状态0启用、1禁用';
COMMENT ON TABLE "public"."tbl_privilege_user" IS '用户表';

-- ----------------------------
-- Table structure for tbl_privilege_user_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_privilege_user_role";
CREATE TABLE "public"."tbl_privilege_user_role" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default",
  "user_no" varchar(32) COLLATE "pg_catalog"."default",
  "role_id" varchar(64) COLLATE "pg_catalog"."default",
  "end_date" date,
  "valid_month" int4,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "create_by" varchar(32) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(32) COLLATE "pg_catalog"."default",
  "del_flag" int4 DEFAULT 0
)
;
ALTER TABLE "public"."tbl_privilege_user_role" OWNER TO "phoenix";
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."id" IS '主键ID';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."user_no" IS '用户工号';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."end_date" IS '截止日期';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."valid_month" IS '有效期';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."tbl_privilege_user_role"."del_flag" IS '删除标识：0：存在 1：删除';
COMMENT ON TABLE "public"."tbl_privilege_user_role" IS '用户角色关联表';

-- ----------------------------
-- Table structure for tbl_tmp_orders
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_tmp_orders";
CREATE TABLE "public"."tbl_tmp_orders" (
  "id" int4 NOT NULL DEFAULT nextval('tbl_data_orders_id_seq'::regclass),
  "user_id" int4 NOT NULL,
  "order_date" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "total_amount" numeric(10,2) NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT 'pending'::character varying
)
;
ALTER TABLE "public"."tbl_tmp_orders" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_tmp_products
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_tmp_products";
CREATE TABLE "public"."tbl_tmp_products" (
  "id" int4 NOT NULL DEFAULT nextval('tbl_data_products_id_seq'::regclass),
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "price" numeric(10,2) NOT NULL,
  "stock" int4 NOT NULL,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_tmp_products" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_tmp_users
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_tmp_users";
CREATE TABLE "public"."tbl_tmp_users" (
  "id" int4 NOT NULL DEFAULT nextval('tbl_data_users_id_seq'::regclass),
  "username" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "email" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
ALTER TABLE "public"."tbl_tmp_users" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_vector_store_combined
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_vector_store_combined";
CREATE TABLE "public"."tbl_vector_store_combined" (
  "id" text COLLATE "pg_catalog"."default" NOT NULL,
  "namespace" text COLLATE "pg_catalog"."default",
  "key_name" varchar(500) COLLATE "pg_catalog"."default",
  "value_json" text COLLATE "pg_catalog"."default",
  "created_at" timestamp(6),
  "updated_at" timestamp(6)
)
;
ALTER TABLE "public"."tbl_vector_store_combined" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_vector_store_rag
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_vector_store_rag";
CREATE TABLE "public"."tbl_vector_store_rag" (
  "id" uuid NOT NULL DEFAULT uuid_generate_v4(),
  "content" text COLLATE "pg_catalog"."default",
  "metadata" json,
  "embedding" "public"."vector"
)
;
ALTER TABLE "public"."tbl_vector_store_rag" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_vector_store_simple_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_vector_store_simple_data";
CREATE TABLE "public"."tbl_vector_store_simple_data" (
  "id" uuid NOT NULL DEFAULT uuid_generate_v4(),
  "content" text COLLATE "pg_catalog"."default",
  "metadata" json,
  "embedding" "public"."vector"
)
;
ALTER TABLE "public"."tbl_vector_store_simple_data" OWNER TO "phoenix";

-- ----------------------------
-- Table structure for tbl_vector_store_user_memory
-- ----------------------------
DROP TABLE IF EXISTS "public"."tbl_vector_store_user_memory";
CREATE TABLE "public"."tbl_vector_store_user_memory" (
  "id" uuid NOT NULL DEFAULT uuid_generate_v4(),
  "content" text COLLATE "pg_catalog"."default",
  "metadata" json,
  "embedding" "public"."vector"
)
;
ALTER TABLE "public"."tbl_vector_store_user_memory" OWNER TO "phoenix";

-- ----------------------------
-- Function structure for akeys
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."akeys"("public"."hstore");
CREATE FUNCTION "public"."akeys"("public"."hstore")
  RETURNS "pg_catalog"."_text" AS '$libdir/hstore', 'hstore_akeys'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."akeys"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_numeric, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_halfvec"(_numeric, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_int4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_halfvec"(_int4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_float4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_halfvec"(_float4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_float8, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_halfvec"(_float8, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_float4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_sparsevec"(_float4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_numeric, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_sparsevec"(_numeric, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_float8, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_sparsevec"(_float8, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_int4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_sparsevec"(_int4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_numeric, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_vector"(_numeric, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_int4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_vector"(_int4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_float4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_vector"(_float4, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_float8, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."array_to_vector"(_float8, int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for avals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."avals"("public"."hstore");
CREATE FUNCTION "public"."avals"("public"."hstore")
  RETURNS "pg_catalog"."_text" AS '$libdir/hstore', 'hstore_avals'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."avals"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."vector");
CREATE FUNCTION "public"."binary_quantize"("public"."vector")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."binary_quantize"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."halfvec");
CREATE FUNCTION "public"."binary_quantize"("public"."halfvec")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'halfvec_binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."binary_quantize"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."cosine_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."cosine_distance"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."cosine_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."cosine_distance"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for defined
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."defined"("public"."hstore", text);
CREATE FUNCTION "public"."defined"("public"."hstore", text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_defined'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."defined"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for delete
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."delete"("public"."hstore", text);
CREATE FUNCTION "public"."delete"("public"."hstore", text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_delete'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."delete"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for delete
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."delete"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."delete"("public"."hstore", "public"."hstore")
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_delete_hstore'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."delete"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for delete
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."delete"("public"."hstore", _text);
CREATE FUNCTION "public"."delete"("public"."hstore", _text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_delete_array'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."delete"("public"."hstore", _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for each
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."each"("hs" "public"."hstore", OUT "key" text, OUT "value" text);
CREATE FUNCTION "public"."each"(IN "hs" "public"."hstore", OUT "key" text, OUT "value" text)
  RETURNS SETOF "pg_catalog"."record" AS '$libdir/hstore', 'hstore_each'
  LANGUAGE c IMMUTABLE STRICT
  COST 1
  ROWS 1000;
ALTER FUNCTION "public"."each"("hs" "public"."hstore", OUT "key" text, OUT "value" text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for exist
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."exist"("public"."hstore", text);
CREATE FUNCTION "public"."exist"("public"."hstore", text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_exists'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."exist"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for exists_all
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."exists_all"("public"."hstore", _text);
CREATE FUNCTION "public"."exists_all"("public"."hstore", _text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_exists_all'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."exists_all"("public"."hstore", _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for exists_any
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."exists_any"("public"."hstore", _text);
CREATE FUNCTION "public"."exists_any"("public"."hstore", _text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_exists_any'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."exists_any"("public"."hstore", _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for fetchval
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."fetchval"("public"."hstore", text);
CREATE FUNCTION "public"."fetchval"("public"."hstore", text)
  RETURNS "pg_catalog"."text" AS '$libdir/hstore', 'hstore_fetchval'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."fetchval"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_compress
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_compress"(internal);
CREATE FUNCTION "public"."ghstore_compress"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'ghstore_compress'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_compress"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_consistent
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_consistent"(internal, "public"."hstore", int2, oid, internal);
CREATE FUNCTION "public"."ghstore_consistent"(internal, "public"."hstore", int2, oid, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'ghstore_consistent'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_consistent"(internal, "public"."hstore", int2, oid, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_decompress
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_decompress"(internal);
CREATE FUNCTION "public"."ghstore_decompress"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'ghstore_decompress'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_decompress"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_in"(cstring);
CREATE FUNCTION "public"."ghstore_in"(cstring)
  RETURNS "public"."ghstore" AS '$libdir/hstore', 'ghstore_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_in"(cstring) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_options
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_options"(internal);
CREATE FUNCTION "public"."ghstore_options"(internal)
  RETURNS "pg_catalog"."void" AS '$libdir/hstore', 'ghstore_options'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."ghstore_options"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_out"("public"."ghstore");
CREATE FUNCTION "public"."ghstore_out"("public"."ghstore")
  RETURNS "pg_catalog"."cstring" AS '$libdir/hstore', 'ghstore_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_out"("public"."ghstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_penalty
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_penalty"(internal, internal, internal);
CREATE FUNCTION "public"."ghstore_penalty"(internal, internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'ghstore_penalty'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_penalty"(internal, internal, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_picksplit
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_picksplit"(internal, internal);
CREATE FUNCTION "public"."ghstore_picksplit"(internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'ghstore_picksplit'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_picksplit"(internal, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_same
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_same"("public"."ghstore", "public"."ghstore", internal);
CREATE FUNCTION "public"."ghstore_same"("public"."ghstore", "public"."ghstore", internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'ghstore_same'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_same"("public"."ghstore", "public"."ghstore", internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ghstore_union
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ghstore_union"(internal, internal);
CREATE FUNCTION "public"."ghstore_union"(internal, internal)
  RETURNS "public"."ghstore" AS '$libdir/hstore', 'ghstore_union'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."ghstore_union"(internal, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for gin_consistent_hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gin_consistent_hstore"(internal, int2, "public"."hstore", int4, internal, internal);
CREATE FUNCTION "public"."gin_consistent_hstore"(internal, int2, "public"."hstore", int4, internal, internal)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'gin_consistent_hstore'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."gin_consistent_hstore"(internal, int2, "public"."hstore", int4, internal, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for gin_extract_hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gin_extract_hstore"("public"."hstore", internal);
CREATE FUNCTION "public"."gin_extract_hstore"("public"."hstore", internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'gin_extract_hstore'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."gin_extract_hstore"("public"."hstore", internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for gin_extract_hstore_query
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."gin_extract_hstore_query"("public"."hstore", internal, int2, internal, internal);
CREATE FUNCTION "public"."gin_extract_hstore_query"("public"."hstore", internal, int2, internal, internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'gin_extract_hstore_query'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."gin_extract_hstore_query"("public"."hstore", internal, int2, internal, internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec"("public"."halfvec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec"("public"."halfvec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_accum"(_float8, "public"."halfvec");
CREATE FUNCTION "public"."halfvec_accum"(_float8, "public"."halfvec")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'halfvec_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_accum"(_float8, "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_add"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_add"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_add"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_avg"(_float8);
CREATE FUNCTION "public"."halfvec_avg"(_float8)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_avg"(_float8) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_combine"(_float8, _float8);
CREATE FUNCTION "public"."halfvec_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_combine"(_float8, _float8) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_concat"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_concat"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_concat"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_eq"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_eq"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_eq"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ge"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_ge"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_ge"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_gt"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_gt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_gt"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_in"(cstring, oid, int4);
CREATE FUNCTION "public"."halfvec_in"(cstring, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_in"(cstring, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_le"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_le"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_le"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_lt"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_lt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_lt"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_mul"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_mul"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_mul"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ne"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_ne"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_ne"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_out"("public"."halfvec");
CREATE FUNCTION "public"."halfvec_out"("public"."halfvec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'halfvec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_out"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_recv"(internal, oid, int4);
CREATE FUNCTION "public"."halfvec_recv"(internal, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_recv"(internal, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_send"("public"."halfvec");
CREATE FUNCTION "public"."halfvec_send"("public"."halfvec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'halfvec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_send"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_sub"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_sub"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_sub"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_float4"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_float4"("public"."halfvec", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'halfvec_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_to_float4"("public"."halfvec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'halfvec_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_vector"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_vector"("public"."halfvec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'halfvec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_to_vector"("public"."halfvec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for halfvec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_typmod_in"(_cstring);
CREATE FUNCTION "public"."halfvec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."halfvec_typmod_in"(_cstring) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hamming_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hamming_distance"(bit, bit);
CREATE FUNCTION "public"."hamming_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'hamming_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hamming_distance"(bit, bit) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hnsw_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_bit_support"(internal);
CREATE FUNCTION "public"."hnsw_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_bit_support'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."hnsw_bit_support"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hnsw_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_halfvec_support"(internal);
CREATE FUNCTION "public"."hnsw_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."hnsw_halfvec_support"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hnsw_sparsevec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_sparsevec_support"(internal);
CREATE FUNCTION "public"."hnsw_sparsevec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_sparsevec_support'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."hnsw_sparsevec_support"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hnswhandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnswhandler"(internal);
CREATE FUNCTION "public"."hnswhandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'hnswhandler'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."hnswhandler"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hs_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hs_concat"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hs_concat"("public"."hstore", "public"."hstore")
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hs_concat"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hs_contained
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hs_contained"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hs_contained"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_contained'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hs_contained"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hs_contains
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hs_contains"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hs_contains"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_contains'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hs_contains"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore"(_text);
CREATE FUNCTION "public"."hstore"(_text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_from_array'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore"(_text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore"(text, text);
CREATE FUNCTION "public"."hstore"(text, text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_from_text'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."hstore"(text, text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore"(_text, _text);
CREATE FUNCTION "public"."hstore"(_text, _text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_from_arrays'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."hstore"(_text, _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore"(record);
CREATE FUNCTION "public"."hstore"(record)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_from_record'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."hstore"(record) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_cmp"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_cmp"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."int4" AS '$libdir/hstore', 'hstore_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_cmp"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_eq"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_eq"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_eq"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_ge"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_ge"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_ge"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_gt"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_gt"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_gt"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_hash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_hash"("public"."hstore");
CREATE FUNCTION "public"."hstore_hash"("public"."hstore")
  RETURNS "pg_catalog"."int4" AS '$libdir/hstore', 'hstore_hash'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_hash"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_hash_extended
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_hash_extended"("public"."hstore", int8);
CREATE FUNCTION "public"."hstore_hash_extended"("public"."hstore", int8)
  RETURNS "pg_catalog"."int8" AS '$libdir/hstore', 'hstore_hash_extended'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_hash_extended"("public"."hstore", int8) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_in"(cstring);
CREATE FUNCTION "public"."hstore_in"(cstring)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_in"(cstring) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_le"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_le"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_le"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_lt"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_lt"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_lt"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_ne"("public"."hstore", "public"."hstore");
CREATE FUNCTION "public"."hstore_ne"("public"."hstore", "public"."hstore")
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_ne"("public"."hstore", "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_out"("public"."hstore");
CREATE FUNCTION "public"."hstore_out"("public"."hstore")
  RETURNS "pg_catalog"."cstring" AS '$libdir/hstore', 'hstore_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_out"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_recv"(internal);
CREATE FUNCTION "public"."hstore_recv"(internal)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_recv"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_send"("public"."hstore");
CREATE FUNCTION "public"."hstore_send"("public"."hstore")
  RETURNS "pg_catalog"."bytea" AS '$libdir/hstore', 'hstore_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_send"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_subscript_handler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_subscript_handler"(internal);
CREATE FUNCTION "public"."hstore_subscript_handler"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/hstore', 'hstore_subscript_handler'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_subscript_handler"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_array
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_array"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_array"("public"."hstore")
  RETURNS "pg_catalog"."_text" AS '$libdir/hstore', 'hstore_to_array'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_array"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_json
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_json"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_json"("public"."hstore")
  RETURNS "pg_catalog"."json" AS '$libdir/hstore', 'hstore_to_json'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_json"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_json_loose
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_json_loose"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_json_loose"("public"."hstore")
  RETURNS "pg_catalog"."json" AS '$libdir/hstore', 'hstore_to_json_loose'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_json_loose"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_jsonb
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_jsonb"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_jsonb"("public"."hstore")
  RETURNS "pg_catalog"."jsonb" AS '$libdir/hstore', 'hstore_to_jsonb'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_jsonb"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_jsonb_loose
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_jsonb_loose"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_jsonb_loose"("public"."hstore")
  RETURNS "pg_catalog"."jsonb" AS '$libdir/hstore', 'hstore_to_jsonb_loose'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_jsonb_loose"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_to_matrix
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_to_matrix"("public"."hstore");
CREATE FUNCTION "public"."hstore_to_matrix"("public"."hstore")
  RETURNS "pg_catalog"."_text" AS '$libdir/hstore', 'hstore_to_matrix'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_to_matrix"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for hstore_version_diag
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hstore_version_diag"("public"."hstore");
CREATE FUNCTION "public"."hstore_version_diag"("public"."hstore")
  RETURNS "pg_catalog"."int4" AS '$libdir/hstore', 'hstore_version_diag'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."hstore_version_diag"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."inner_product"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."inner_product"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."inner_product"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for isdefined
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."isdefined"("public"."hstore", text);
CREATE FUNCTION "public"."isdefined"("public"."hstore", text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_defined'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."isdefined"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for isexists
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."isexists"("public"."hstore", text);
CREATE FUNCTION "public"."isexists"("public"."hstore", text)
  RETURNS "pg_catalog"."bool" AS '$libdir/hstore', 'hstore_exists'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."isexists"("public"."hstore", text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ivfflat_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_bit_support"(internal);
CREATE FUNCTION "public"."ivfflat_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_bit_support'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."ivfflat_bit_support"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ivfflat_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_halfvec_support"(internal);
CREATE FUNCTION "public"."ivfflat_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."ivfflat_halfvec_support"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for ivfflathandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflathandler"(internal);
CREATE FUNCTION "public"."ivfflathandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'ivfflathandler'
  LANGUAGE c VOLATILE
  COST 1;
ALTER FUNCTION "public"."ivfflathandler"(internal) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for jaccard_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."jaccard_distance"(bit, bit);
CREATE FUNCTION "public"."jaccard_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'jaccard_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."jaccard_distance"(bit, bit) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."l1_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l1_distance"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."l1_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l1_distance"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."l1_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l1_distance"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."l2_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_distance"("public"."halfvec", "public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."l2_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_distance"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."l2_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_distance"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."halfvec");
CREATE FUNCTION "public"."l2_norm"("public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_norm"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."sparsevec");
CREATE FUNCTION "public"."l2_norm"("public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_norm"("public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."sparsevec");
CREATE FUNCTION "public"."l2_normalize"("public"."sparsevec")
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_normalize"("public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."halfvec");
CREATE FUNCTION "public"."l2_normalize"("public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_normalize"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."vector");
CREATE FUNCTION "public"."l2_normalize"("public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."l2_normalize"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for populate_record
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."populate_record"(anyelement, "public"."hstore");
CREATE FUNCTION "public"."populate_record"(anyelement, "public"."hstore")
  RETURNS "pg_catalog"."anyelement" AS '$libdir/hstore', 'hstore_populate_record'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."populate_record"(anyelement, "public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for skeys
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."skeys"("public"."hstore");
CREATE FUNCTION "public"."skeys"("public"."hstore")
  RETURNS SETOF "pg_catalog"."text" AS '$libdir/hstore', 'hstore_skeys'
  LANGUAGE c IMMUTABLE STRICT
  COST 1
  ROWS 1000;
ALTER FUNCTION "public"."skeys"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for slice
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."slice"("public"."hstore", _text);
CREATE FUNCTION "public"."slice"("public"."hstore", _text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_slice_to_hstore'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."slice"("public"."hstore", _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for slice_array
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."slice_array"("public"."hstore", _text);
CREATE FUNCTION "public"."slice_array"("public"."hstore", _text)
  RETURNS "pg_catalog"."_text" AS '$libdir/hstore', 'hstore_slice_to_array'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."slice_array"("public"."hstore", _text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec"("public"."sparsevec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec"("public"."sparsevec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_in"(cstring, oid, int4);
CREATE FUNCTION "public"."sparsevec_in"(cstring, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_in"(cstring, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_out"("public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_out"("public"."sparsevec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'sparsevec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_out"("public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_recv"(internal, oid, int4);
CREATE FUNCTION "public"."sparsevec_recv"(internal, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_recv"(internal, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_send"("public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_send"("public"."sparsevec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'sparsevec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_send"("public"."sparsevec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'sparsevec_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'sparsevec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for sparsevec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_typmod_in"(_cstring);
CREATE FUNCTION "public"."sparsevec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."sparsevec_typmod_in"(_cstring) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."halfvec", int4, int4);
CREATE FUNCTION "public"."subvector"("public"."halfvec", int4, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."subvector"("public"."halfvec", int4, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."vector", int4, int4);
CREATE FUNCTION "public"."subvector"("public"."vector", int4, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."subvector"("public"."vector", int4, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for svals
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."svals"("public"."hstore");
CREATE FUNCTION "public"."svals"("public"."hstore")
  RETURNS SETOF "pg_catalog"."text" AS '$libdir/hstore', 'hstore_svals'
  LANGUAGE c IMMUTABLE STRICT
  COST 1
  ROWS 1000;
ALTER FUNCTION "public"."svals"("public"."hstore") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for tconvert
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."tconvert"(text, text);
CREATE FUNCTION "public"."tconvert"(text, text)
  RETURNS "public"."hstore" AS '$libdir/hstore', 'hstore_from_text'
  LANGUAGE c IMMUTABLE
  COST 1;
ALTER FUNCTION "public"."tconvert"(text, text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_generate_v1
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v1"();
CREATE FUNCTION "public"."uuid_generate_v1"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v1'
  LANGUAGE c VOLATILE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_generate_v1"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_generate_v1mc
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v1mc"();
CREATE FUNCTION "public"."uuid_generate_v1mc"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v1mc'
  LANGUAGE c VOLATILE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_generate_v1mc"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_generate_v3
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v3"("namespace" uuid, "name" text);
CREATE FUNCTION "public"."uuid_generate_v3"("namespace" uuid, "name" text)
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v3'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_generate_v3"("namespace" uuid, "name" text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_generate_v4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v4"();
CREATE FUNCTION "public"."uuid_generate_v4"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v4'
  LANGUAGE c VOLATILE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_generate_v4"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_generate_v5
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v5"("namespace" uuid, "name" text);
CREATE FUNCTION "public"."uuid_generate_v5"("namespace" uuid, "name" text)
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v5'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_generate_v5"("namespace" uuid, "name" text) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_nil
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_nil"();
CREATE FUNCTION "public"."uuid_nil"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_nil'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_nil"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_ns_dns
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_dns"();
CREATE FUNCTION "public"."uuid_ns_dns"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_dns'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_ns_dns"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_ns_oid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_oid"();
CREATE FUNCTION "public"."uuid_ns_oid"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_oid'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_ns_oid"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_ns_url
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_url"();
CREATE FUNCTION "public"."uuid_ns_url"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_url'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_ns_url"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for uuid_ns_x500
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_x500"();
CREATE FUNCTION "public"."uuid_ns_x500"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_x500'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."uuid_ns_x500"() OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector"("public"."vector", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector"("public"."vector", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_accum"(_float8, "public"."vector");
CREATE FUNCTION "public"."vector_accum"(_float8, "public"."vector")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_accum"(_float8, "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_add"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_add"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_add"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_avg"(_float8);
CREATE FUNCTION "public"."vector_avg"(_float8)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_avg"(_float8) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_cmp"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_cmp"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_cmp"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_combine"(_float8, _float8);
CREATE FUNCTION "public"."vector_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_combine"(_float8, _float8) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_concat"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_concat"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_concat"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."halfvec");
CREATE FUNCTION "public"."vector_dims"("public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_dims"("public"."halfvec") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."vector");
CREATE FUNCTION "public"."vector_dims"("public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_dims"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_eq"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_eq"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_eq"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ge"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_ge"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_ge"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_gt"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_gt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_gt"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_in"(cstring, oid, int4);
CREATE FUNCTION "public"."vector_in"(cstring, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_in"(cstring, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_l2_squared_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_l2_squared_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_l2_squared_distance"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_le"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_le"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_le"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_lt"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_lt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_lt"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_mul"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_mul"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_mul"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ne"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_ne"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_ne"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_negative_inner_product"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_negative_inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_negative_inner_product"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_norm"("public"."vector");
CREATE FUNCTION "public"."vector_norm"("public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_norm"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_out"("public"."vector");
CREATE FUNCTION "public"."vector_out"("public"."vector")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'vector_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_out"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_recv"(internal, oid, int4);
CREATE FUNCTION "public"."vector_recv"(internal, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_recv"(internal, oid, int4) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_send"("public"."vector");
CREATE FUNCTION "public"."vector_send"("public"."vector")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'vector_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_send"("public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_spherical_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_spherical_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_spherical_distance"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_sub"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_sub"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_sub"("public"."vector", "public"."vector") OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_float4"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_float4"("public"."vector", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'vector_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_to_float4"("public"."vector", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_halfvec"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_halfvec"("public"."vector", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'vector_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_to_halfvec"("public"."vector", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_sparsevec"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_sparsevec"("public"."vector", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'vector_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_to_sparsevec"("public"."vector", int4, bool) OWNER TO "phoenix";

-- ----------------------------
-- Function structure for vector_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_typmod_in"(_cstring);
CREATE FUNCTION "public"."vector_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;
ALTER FUNCTION "public"."vector_typmod_in"(_cstring) OWNER TO "phoenix";

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_agent_datasource_id_seq"
OWNED BY "public"."tbl_data_agent_datasource"."id";
SELECT setval('"public"."tbl_data_agent_datasource_id_seq"', 23, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_agent_datasource_tables_id_seq"
OWNED BY "public"."tbl_data_agent_datasource_tables"."id";
SELECT setval('"public"."tbl_data_agent_datasource_tables_id_seq"', 526, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_agent_id_seq"
OWNED BY "public"."tbl_data_agent"."id";
SELECT setval('"public"."tbl_data_agent_id_seq"', 22, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_agent_knowledge_id_seq"
OWNED BY "public"."tbl_data_agent_knowledge"."id";
SELECT setval('"public"."tbl_data_agent_knowledge_id_seq"', 19, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_agent_preset_question_id_seq"
OWNED BY "public"."tbl_data_agent_preset_question"."id";
SELECT setval('"public"."tbl_data_agent_preset_question_id_seq"', 45, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_business_knowledge_id_seq"
OWNED BY "public"."tbl_data_business_knowledge"."id";
SELECT setval('"public"."tbl_data_business_knowledge_id_seq"', 45, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."tbl_data_categories_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_chat_message_id_seq"
OWNED BY "public"."tbl_data_chat_message"."id";
SELECT setval('"public"."tbl_data_chat_message_id_seq"', 3804, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_datasource_id_seq"
OWNED BY "public"."tbl_data_datasource"."id";
SELECT setval('"public"."tbl_data_datasource_id_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_logical_relation_id_seq"
OWNED BY "public"."tbl_data_logical_relation"."id";
SELECT setval('"public"."tbl_data_logical_relation_id_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_model_config_id_seq"
OWNED BY "public"."tbl_data_model_config"."id";
SELECT setval('"public"."tbl_data_model_config_id_seq"', 6, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."tbl_data_order_items_id_seq"', 19, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."tbl_data_orders_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."tbl_data_products_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_data_semantic_model_id_seq"
OWNED BY "public"."tbl_data_semantic_model"."id";
SELECT setval('"public"."tbl_data_semantic_model_id_seq"', 8, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."tbl_data_users_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."tbl_privilege_group_id_seq"
OWNED BY "public"."tbl_privilege_group"."id";
SELECT setval('"public"."tbl_privilege_group_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table act_hi_procinst
-- ----------------------------
CREATE INDEX "ACT_IDX_HI_PRO_INST_END" ON "public"."act_hi_procinst" USING btree (
  "END_TIME_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "ACT_IDX_HI_PRO_I_BUSKEY" ON "public"."act_hi_procinst" USING btree (
  "BUSINESS_KEY_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "ACT_IDX_HI_PRO_SUPER_PROCINST" ON "public"."act_hi_procinst" USING btree (
  "SUPER_PROCESS_INSTANCE_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "PROC_INST_ID_" ON "public"."act_hi_procinst" USING btree (
  "PROC_INST_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_procinst
-- ----------------------------
ALTER TABLE "public"."act_hi_procinst" ADD CONSTRAINT "act_hi_procinst_pkey" PRIMARY KEY ("ID_");

-- ----------------------------
-- Indexes structure for table act_hi_taskinst
-- ----------------------------
CREATE INDEX "ACT_IDX_HI_TASK_INST_PROCINST" ON "public"."act_hi_taskinst" USING btree (
  "PROC_INST_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "ACT_IDX_HI_TASK_SCOPE" ON "public"."act_hi_taskinst" USING btree (
  "SCOPE_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "SCOPE_TYPE_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "ACT_IDX_HI_TASK_SCOPE_DEF" ON "public"."act_hi_taskinst" USING btree (
  "SCOPE_DEFINITION_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "SCOPE_TYPE_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "ACT_IDX_HI_TASK_SUB_SCOPE" ON "public"."act_hi_taskinst" USING btree (
  "SUB_SCOPE_ID_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "SCOPE_TYPE_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_taskinst
-- ----------------------------
ALTER TABLE "public"."act_hi_taskinst" ADD CONSTRAINT "act_hi_taskinst_pkey" PRIMARY KEY ("ID_");

-- ----------------------------
-- Primary Key structure for table tbl_agent_user_agent_info
-- ----------------------------
ALTER TABLE "public"."tbl_agent_user_agent_info" ADD CONSTRAINT "tbl_agent_user_agent_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_agent_user_memory_info
-- ----------------------------
ALTER TABLE "public"."tbl_agent_user_memory_info" ADD CONSTRAINT "tbl_agent_user_memory_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_agent_user_profile_info
-- ----------------------------
ALTER TABLE "public"."tbl_agent_user_profile_info" ADD CONSTRAINT "tbl_agent_user_profile_info_pkey" PRIMARY KEY ("user_id", "agent_sn");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent" ADD CONSTRAINT "tbl_data_agent_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent_category
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_category" ADD CONSTRAINT "tbl_data_agent_category_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table tbl_data_agent_datasource
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_datasource" ADD CONSTRAINT "tbl_data_agent_datasource_agent_id_datasource_id_key" UNIQUE ("agent_id", "datasource_id");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent_datasource
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_datasource" ADD CONSTRAINT "tbl_data_agent_datasource_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table tbl_data_agent_datasource_tables
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_datasource_tables" ADD CONSTRAINT "tbl_data_agent_datasource_tab_agent_datasource_id_table_nam_key" UNIQUE ("agent_datasource_id", "table_name");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent_datasource_tables
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_datasource_tables" ADD CONSTRAINT "tbl_data_agent_datasource_tables_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent_knowledge
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_knowledge" ADD CONSTRAINT "tbl_data_agent_knowledge_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_agent_preset_question
-- ----------------------------
ALTER TABLE "public"."tbl_data_agent_preset_question" ADD CONSTRAINT "tbl_data_agent_preset_question_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_business_knowledge
-- ----------------------------
ALTER TABLE "public"."tbl_data_business_knowledge" ADD CONSTRAINT "tbl_data_business_knowledge_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_categories
-- ----------------------------
ALTER TABLE "public"."tbl_data_categories" ADD CONSTRAINT "tbl_data_categories_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_chat_message
-- ----------------------------
ALTER TABLE "public"."tbl_data_chat_message" ADD CONSTRAINT "tbl_data_chat_message_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_chat_session
-- ----------------------------
ALTER TABLE "public"."tbl_data_chat_session" ADD CONSTRAINT "tbl_data_chat_session_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_datasource
-- ----------------------------
ALTER TABLE "public"."tbl_data_datasource" ADD CONSTRAINT "tbl_data_datasource_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_logical_relation
-- ----------------------------
ALTER TABLE "public"."tbl_data_logical_relation" ADD CONSTRAINT "tbl_data_logical_relation_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_model_config
-- ----------------------------
ALTER TABLE "public"."tbl_data_model_config" ADD CONSTRAINT "tbl_data_model_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_order_items
-- ----------------------------
ALTER TABLE "public"."tbl_data_order_items" ADD CONSTRAINT "tbl_data_order_items_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_product_categories
-- ----------------------------
ALTER TABLE "public"."tbl_data_product_categories" ADD CONSTRAINT "tbl_data_product_categories_pkey" PRIMARY KEY ("product_id", "category_id");

-- ----------------------------
-- Primary Key structure for table tbl_data_semantic_model
-- ----------------------------
ALTER TABLE "public"."tbl_data_semantic_model" ADD CONSTRAINT "tbl_data_semantic_model_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_data_user_prompt_config
-- ----------------------------
ALTER TABLE "public"."tbl_data_user_prompt_config" ADD CONSTRAINT "tbl_data_user_prompt_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_account_group_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_account_group_info" ADD CONSTRAINT "tbl_phoenix_platform_account_group_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_account_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_account_info" ADD CONSTRAINT "tbl_phoenix_platform_account_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_account_tenant_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_account_tenant_info" ADD CONSTRAINT "tbl_platform_user_tenant_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_dingtalk
-- ----------------------------
ALTER TABLE "public"."tbl_platform_dingtalk" ADD CONSTRAINT "tbl_platform_dingtalk_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_group_agent_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_group_agent_info" ADD CONSTRAINT "tbl_platform_group_agent_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_group_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_group_info" ADD CONSTRAINT "tbl_phoenix_platform_group_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_platform_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_platform_info" ADD CONSTRAINT "tbl_platform_platform_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_platform_tenant_info
-- ----------------------------
ALTER TABLE "public"."tbl_platform_tenant_info" ADD CONSTRAINT "tbl_platform_tenant_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_privilege_acl
-- ----------------------------
CREATE UNIQUE INDEX "idx_tbl_privilege_acl_release_module" ON "public"."tbl_privilege_acl" USING btree (
  "release_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "module_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_privilege_acl
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_acl" ADD CONSTRAINT "tbl_privilege_acl_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_privilege_company
-- ----------------------------
CREATE UNIQUE INDEX "idx_tbl_privilege_company_code" ON "public"."tbl_privilege_company" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_privilege_company
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_company" ADD CONSTRAINT "tbl_privilege_company_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_privilege_department
-- ----------------------------
CREATE UNIQUE INDEX "idx_tbl_privilege_department_code" ON "public"."tbl_privilege_department" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_privilege_department
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_department" ADD CONSTRAINT "tbl_privilege_department_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_dictionary
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_dictionary" ADD CONSTRAINT "tbl_privilege_dictionary_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_employee
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_employee" ADD CONSTRAINT "tbl_privilege_employee_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_group
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_group" ADD CONSTRAINT "tbl_privilege_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_login_log
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_login_log" ADD CONSTRAINT "tbl_privilege_login_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_module
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_module" ADD CONSTRAINT "tbl_privilege_module_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_privilege_pvalue
-- ----------------------------
CREATE INDEX "idx_tbl_privilege_pvalue_name" ON "public"."tbl_privilege_pvalue" USING btree (
  "name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_tbl_privilege_pvalue_position" ON "public"."tbl_privilege_pvalue" USING btree (
  "position" "pg_catalog"."int4_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_privilege_pvalue
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_pvalue" ADD CONSTRAINT "tbl_privilege_pvalue_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_role
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_role" ADD CONSTRAINT "tbl_privilege_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_privilege_user
-- ----------------------------
CREATE UNIQUE INDEX "idx_tbl_privilege_user_code" ON "public"."tbl_privilege_user" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "idx_tbl_privilege_user_username" ON "public"."tbl_privilege_user" USING btree (
  "username" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_privilege_user
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_user" ADD CONSTRAINT "tbl_privilege_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_privilege_user_role
-- ----------------------------
ALTER TABLE "public"."tbl_privilege_user_role" ADD CONSTRAINT "tbl_privilege_user_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_tmp_orders
-- ----------------------------
ALTER TABLE "public"."tbl_tmp_orders" ADD CONSTRAINT "tbl_data_orders_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_tmp_products
-- ----------------------------
ALTER TABLE "public"."tbl_tmp_products" ADD CONSTRAINT "tbl_data_products_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_tmp_users
-- ----------------------------
ALTER TABLE "public"."tbl_tmp_users" ADD CONSTRAINT "tbl_data_users_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table tbl_vector_store_combined
-- ----------------------------
ALTER TABLE "public"."tbl_vector_store_combined" ADD CONSTRAINT "tbl_vector_store_combined_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_vector_store_rag
-- ----------------------------
CREATE INDEX "tbl_vector_store_rag_index" ON "public"."tbl_vector_store_rag" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_vector_store_rag
-- ----------------------------
ALTER TABLE "public"."tbl_vector_store_rag" ADD CONSTRAINT "tbl_vector_store_rag_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_vector_store_simple_data
-- ----------------------------
CREATE INDEX "tbl_vector_store_simple_data_index" ON "public"."tbl_vector_store_simple_data" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_vector_store_simple_data
-- ----------------------------
ALTER TABLE "public"."tbl_vector_store_simple_data" ADD CONSTRAINT "tbl_vector_store_simple_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table tbl_vector_store_user_memory
-- ----------------------------
CREATE INDEX "tbl_vector_store_user_memory_index" ON "public"."tbl_vector_store_user_memory" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table tbl_vector_store_user_memory
-- ----------------------------
ALTER TABLE "public"."tbl_vector_store_user_memory" ADD CONSTRAINT "tbl_vector_store_user_memory_pkey" PRIMARY KEY ("id");
