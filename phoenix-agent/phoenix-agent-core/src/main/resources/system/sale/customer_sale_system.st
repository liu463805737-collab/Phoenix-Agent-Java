# Postgresql数据表结构说明 - 用于SQL自动生成

## 表1: oracle_onhand_date_info (Oracle库存日汇总表)
**用途**: 存储postgresql系统的库存日汇总数据，按事业部、组织类型、SKU维度进行库存统计分析

### 字段详细说明
| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | int | PRIMARY KEY, AUTO_INCREMENT | - | 主键，自增ID |
| onhand_date | date | NOT NULL | - | 库存日期，业务日期 |
| division | varchar(32) | NOT NULL | '' | 事业部，业务维度 |
| org_type | varchar(32) | NOT NULL | '' | 业务单位类型（如：工厂、仓库等） |
| sku_code | varchar(32) | NOT NULL | '' | SKU编码，商品唯一标识 |
| sku_name | varchar(100) | NOT NULL | '' | SKU名称，商品名称 |
| onhand_quantity | int | NOT NULL | 0 | 现有库存数量（整数） |
| onhand_amount | decimal(14,4) | NOT NULL | 0.0000 | 库存金额（精确到4位小数） |
| onhand_day | decimal(10,2) | NOT NULL | 0.00 | 库存天数（库存可支撑天数） |
| unit_cost_price | decimal(10,6) | NOT NULL | 0.000000 | 成本单价（精确到6位小数） |
| created_at | timestamp | NOT NULL | CURRENT_TIMESTAMP | 记录创建时间 |
| updated_at | timestamp | NOT NULL | CURRENT_TIMESTAMP | 记录更新时间（自动更新） |

### 典型查询场景
1. **按日期统计总库存**：查询某日所有事业部的库存总量和金额
2. **按事业部汇总**：统计各事业部的库存金额和数量
3. **按组织类型分析**：分析不同类型组织的库存分布
4. **SKU库存趋势**：查询特定SKU在不同日期的库存变化
5. **库存天数预警**：查询库存天数超过阈值（如>30天）的商品
6. **成本分析**：分析各SKU的单位成本变化

---

## 表2: oracle_sale_data_info (postgresql销售数据汇总表)
**用途**: 存储postgresql系统的销售汇总数据，按事业部、渠道、SKU维度进行销售统计分析

### 字段详细说明
| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | int | PRIMARY KEY, AUTO_INCREMENT | - | 主键，自增ID |
| ordered_date | date | NOT NULL | - | 订单日期，业务日期 |
| division | varchar(32) | NOT NULL | '' | 事业部，业务维度（有索引） |
| channel | varchar(32) | NOT NULL | '' | 渠道（如：线上、线下、分销等） |
| sku_code | varchar(32) | NOT NULL | '' | SKU编码，商品唯一标识 |
| sku_name | varchar(32) | NOT NULL | '' | SKU名称，商品名称 |
| ordered_quantity | int | NOT NULL | 0 | 销售数量（整数） |
| ordered_amount | decimal(10,2) | NOT NULL | 0.00 | 销售额（精确到2位小数） |
| unit_cost_price | decimal(10,6) | NOT NULL | 0.000000 | 成本单价（精确到6位小数） |
| ten_income | decimal(10,2) | NOT NULL | 0.00 | 吨重量（吨收入指标） |
| kg_convert_rate | decimal(10,6) | NOT NULL | 0.000000 | 转换单位（公斤转换率） |
| created_at | timestamp | NOT NULL | CURRENT_TIMESTAMP | 记录创建时间 |
| updated_at | timestamp | NOT NULL | CURRENT_TIMESTAMP | 记录更新时间（自动更新） |

### 索引信息
- **PRIMARY KEY**: `id`
- **index_division**: 针对`division`字段的普通索引，优化事业部查询
- **index_ordered_date**: 针对`ordered_date`字段的普通索引，优化日期查询
- **idx_ordered_date_division**: 复合索引，包含`(ordered_date, division, ten_income, ordered_amount, ordered_quantity, kg_convert_rate)`，优化多条件组合查询

### 典型查询场景
1. **按日期统计销售**：查询某日的总销售额和销售量
2. **按事业部分析**：统计各事业部的销售额和销量排行
3. **按渠道分析**：比较不同渠道的销售业绩
4. **SKU销售排行**：查询热销SKU Top N
5. **吨收入分析**：分析各维度的吨收入（ten_income）
6. **毛利率计算**：计算销售额 - 成本（ordered_quantity * unit_cost_price）
7. **成本分析**：分析各SKU的销售成本

---

## 两表关联分析

### 关联字段
- **SKU维度**: `sku_code` 是两表的共同字段，可进行商品维度关联
- **事业部维度**: `division` 是两表的共同字段，可进行组织维度关联
- **日期维度**: `onhand_date` 与 `ordered_date` 可进行日期关联

### 常用关联场景
1. **进销存分析**：关联库存表和销售表，分析销售与库存的关系
2. **库存周转分析**：结合销售数据计算库存周转率
3. **SKU综合看板**：同时展示某SKU的库存和销售情况
4. **事业部经营分析**：按事业部维度综合分析销售和库存

### 关联查询示例
```sql
-- 按事业部和SKU分析销售与库存
SELECT
    s.division,
    s.sku_code,
    s.sku_name,
    SUM(s.ordered_quantity) AS total_sales_qty,
    SUM(s.ordered_amount) AS total_sales_amount,
    AVG(o.onhand_quantity) AS avg_inventory_qty,
    AVG(o.onhand_amount) AS avg_inventory_amount,
    AVG(o.onhand_day) AS avg_inventory_day
FROM oracle_sale_data_info s
LEFT JOIN oracle_onhand_date_info o
    ON s.sku_code = o.sku_code
    AND s.division = o.division
    AND DATE(s.ordered_date) = o.onhand_date
WHERE s.ordered_date BETWEEN '2026-01-01' AND '2026-06-22'
GROUP BY s.division, s.sku_code, s.sku_name
HAVING SUM(s.ordered_quantity) > 0
ORDER BY total_sales_amount DESC