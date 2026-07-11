package com.phoenix.agent.hook;

import cn.hutool.core.map.MapUtil;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 结合长期记忆的钩子
 */

public abstract class AbstractCombinedDbHook extends MessagesModelHook {

    @Override
    public String getName() {
        return "combined_db";
    }

    /**
     * 智能体的标识作为命名空间
     *
     * @return
     */
    public abstract List<String> namespace(String agentId, String userId);

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        Optional<Object> userIdOpt = config.metadata("userId");
        if (userIdOpt.isEmpty()) {
            return new AgentCommand(previousMessages);
        }
        String userId = (String) userIdOpt.get();
        Optional<Object> agentIdOpt = config.metadata("agentId");
        if (agentIdOpt.isEmpty()) {
            return new AgentCommand(previousMessages);
        }
        String agentId = (String) agentIdOpt.get();

        Store store = config.store();
        // 从长期记忆加载
        Optional<StoreItem> profileOpt = store.getItem(this.namespace(agentId, userId), userId);
        if (profileOpt.isEmpty()) {
            return new AgentCommand(previousMessages);
        }
        Map<String, Object> profile = profileOpt.get().getValue();
        String contextInfo = String.format("登录人画像信息：姓名 %s, 工号：%s, 职业: %s ",
                profile.get("name"), profile.get("userCode"), profile.get("occupation"));
        profile.remove("name");
        profile.remove("userCode");
        profile.remove("occupation");
        if (MapUtil.isNotEmpty(profile)) {
            StringBuilder profileInfo = new StringBuilder(", 偏好：");
            profile.forEach((s, o) -> {
                profileInfo.append(o.toString()).append(" ");
            });
            contextInfo = contextInfo + profileInfo.toString() + "\n";
        }

        // 查找是否已存在 SystemMessage
        SystemMessage existingSystemMessage = null;
        int systemMessageIndex = -1;
        for (int i = 0; i < previousMessages.size(); i++) {
            Message msg = previousMessages.get(i);
            if (msg instanceof SystemMessage) {
                existingSystemMessage = (SystemMessage) msg;
                systemMessageIndex = i;
                break;
            }
        }
        // 如果找到 SystemMessage，更新它；否则创建新的
        SystemMessage enhancedSystemMessage;
        if (existingSystemMessage != null) {
            // 更新现有的 SystemMessage
            enhancedSystemMessage = new SystemMessage(
                    existingSystemMessage.getText() + "\n\n" + contextInfo
            );
        } else {
            // 创建新的 SystemMessage
            enhancedSystemMessage = new SystemMessage(contextInfo);
        }

        // 构建新的消息列表
        List<Message> newMessages = new ArrayList<>();
        if (systemMessageIndex >= 0) {
            // 如果找到了 SystemMessage，替换它
            for (int i = 0; i < previousMessages.size(); i++) {
                if (i == systemMessageIndex) {
                    newMessages.add(enhancedSystemMessage);
                } else {
                    newMessages.add(previousMessages.get(i));
                }
            }
        } else {
            // 如果没有找到 SystemMessage，在开头添加新的
            newMessages.add(enhancedSystemMessage);
            newMessages.addAll(previousMessages);
        }
        // 使用 REPLACE 策略替换所有消息
        return new AgentCommand(newMessages, UpdatePolicy.REPLACE);
    }
}
