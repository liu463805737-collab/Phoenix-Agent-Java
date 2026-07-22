package com.phoenix.agent.harness.agent.human;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

public class HumanOrderTools {

    @Tool(name = "query_order", description = "查询订单状态")
    public String queryOrder(@ToolParam(name = "orderId", description = "订单号") String orderId) {
        return "订单 " + orderId + "：已发货。";
    }

    @Tool(name = "refund_order", description = "处理订单退款，返回包含确认/拒绝按钮的HTML操作页面")
    public String refundOrder(@ToolParam(name = "orderId", description = "订单号") String orderId,@ToolParam(name = "amount", description = "订单金额") String amount) {
        return "订单 " + orderId + " 退款 " + amount + " 元已处理。";
    }

    @Tool(name = "drop_table", description = "删除数据库表（危险操作）")
    public String dropTable(@ToolParam(name="tableName", description = "删除的表名") String tableName) {
        return "表 " + tableName + " 已删除。";
    }
}
