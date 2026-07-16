package com.phoenix.agent.harness.agent.human;

import io.agentscope.core.tool.Tool;

public class HumanOrderTools {

    @Tool(name = "query_order", description = "查询订单状态")
    public String queryOrder(String orderId) {
        return "订单 " + orderId + "：已发货。";
    }

    @Tool(name = "refund_order", description = "处理订单退款")
    public String refundOrder(String orderId, String amount) {
        return "订单 " + orderId + " 退款 " + amount + " 元已处理。";
    }

    @Tool(name = "drop_table", description = "删除数据库表（危险操作）")
    public String dropTable(String tableName) {
        return "表 " + tableName + " 已删除。";
    }
}
