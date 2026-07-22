<!--
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
-->

<template>
  <el-aside
    class="chat-session-sidebar"
    style="flex-shrink: 0; min-width: 0; width: 300px; overflow: hidden;"
  >
    <!-- 顶部操作栏 -->
    <div class="sidebar-header">
      <div class="header-controls">
        <el-avatar size="large" style="margin:0 auto;font-size:20px;font-weight:600;color:#fff;background:#2f6bff">
          {{ agent.name?.charAt(0) || 'A' }}
        </el-avatar>
      </div>
      <div class="new-session-section">
        <el-button
          type="primary"
          @click="createNewSession"
          class="new-session-btn"
        >
          + 新建会话
        </el-button>
        <el-button type="danger" @click="clearAllSessions">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>

    <el-divider style="margin: 0" />

    <!-- 会话列表 -->
    <div class="session-list" style="margin-top: 20px">
      <div
        v-for="session in sessions"
        :key="session.id"
        :class="[
          'session-item',
          {
            active: handleGetCurrentSession()?.id === session.id,
            pinned: session.isPinned,
          },
        ]"
        @click="handleSelectSession(session)"
      >
        <div class="session-header">
          <span
            class="session-title"
            @dblclick="startEditSessionTitle(session)"
            v-if="!session.editing"
          >
            {{ session.title || '新会话' }}
          </span>
          <el-input
            v-else
            v-model="session.editingTitle"
            size="small"
            @blur="saveSessionTitle(session)"
            @keyup.enter="saveSessionTitle(session)"
            @keyup.esc="cancelEditSessionTitle(session)"
            ref="sessionTitleInputRef"
          />
          <div class="session-actions">
            <el-button
              type="text"
              size="small"
              @click.stop="startEditSessionTitle(session)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="text"
              size="small"
              @click.stop="togglePinSession(session)"
            >
              <el-icon>
                <StarFilled v-if="session.isPinned" />
                <Star v-else />
              </el-icon>
            </el-button>
            <el-button
              type="text"
              size="small"
              @click.stop="deleteSession(session)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="session-time">
          {{ formatTime(session.updateTime || session.createTime) }}
        </div>
      </div>
    </div>
  </el-aside>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ElButton,
  ElIcon,
  ElMessage,
  ElMessageBox,
  ElTooltip,
  ElAvatar,
  ElDivider,
  ElInput,
} from 'element-plus';
import type { Agent } from '#/api/core/agent';
import {
  getAgentSessionsApi,
  createSessionApi,
  deleteSessionApi,
  pinSessionApi,
  renameSessionApi,
  clearAgentSessionsApi,
} from '#/api/core/chat';
import type { ChatSession } from '#/api/core/chat';
import {
  Plus,
  Delete,
  Star,
  StarFilled,
  Edit,
} from '@element-plus/icons-vue';

// 扩展ChatSession接口以包含编辑相关属性
interface ExtendedChatSession extends ChatSession {
  editing?: boolean;
  editingTitle?: string;
}

interface SessionUpdateEvent {
  type: string;
  sessionId: string;
  title: string;
}

export default defineComponent({
  name: 'ChatSessionSidebar',
  components: {
    ElButton,
    ElIcon,
    ElTooltip,
    ElAvatar,
    ElDivider,
    ElInput,
    Plus,
    Delete,
    Star,
    StarFilled,
    Edit,
  },
  props: {
    agent: {
      type: Object as PropType<Agent>,
      required: true,
    },
    handleSetCurrentSession: {
      type: Function as PropType<
        (session: ChatSession | null) => Promise<void>
      >,
      required: true,
    },
    handleGetCurrentSession: {
      type: Function as PropType<() => ChatSession | null>,
      required: true,
    },
    handleSelectSession: {
      type: Function as PropType<(session: ChatSession) => Promise<void>>,
      required: true,
    },
    handleDeleteSessionState: {
      type: Function as PropType<(sessionId: string) => void>,
      required: true,
    },
  },
  setup(props) {
    const sessions = ref<ExtendedChatSession[]>([]);
    const sessionEventSource = ref<{ close: () => void } | null>(null);
    let reconnectTimer: number | null = null;
    let isComponentActive = true;

    const router = useRouter();
    const route = useRoute();

    const formatTime = (time: Date | string | undefined) => {
      if (!time) return '';
      const date = new Date(time);
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const diffDays = Math.floor(
        (today.getTime() - date.getTime()) / 86_400_000,
      );

      if (diffDays < 0) {
        return date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
        });
      }
      if (diffDays === 0) {
        return date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
        });
      }
      if (diffDays === 1) {
        return '昨天';
      }
      if (date.getFullYear() === now.getFullYear()) {
        return `${date.getMonth() + 1}月${date.getDate()}日`;
      }
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
    };

    const clearReconnectTimer = () => {
      if (reconnectTimer) {
        window.clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
    };

    const handleTitleUpdate = (eventData: SessionUpdateEvent) => {
      if (!eventData?.sessionId) {
        return;
      }
      const target = sessions.value.find(
        (session) => session.id === eventData.sessionId,
      );
      if (target) {
        target.title = eventData.title;
        target.editingTitle = eventData.title;
      }
      const current = props.handleGetCurrentSession();
      if (current && current.id === eventData.sessionId) {
        current.title = eventData.title;
      }
    };

    const connectSessionStream = () => {
      clearReconnectTimer();
      const currentAgentId = agentId.value;
      if (!currentAgentId) {
        return;
      }
      if (sessionEventSource.value) {
        sessionEventSource.value.close();
      }

      const controller = new AbortController();
      let buffer = '';
      let currentEvent = '';
      let currentData = '';

      const dispatchEvent = () => {
        if (currentEvent === 'title-updated' && currentData) {
          try {
            const data = JSON.parse(currentData) as SessionUpdateEvent;
            handleTitleUpdate(data);
          } catch (error) {
            console.error('解析会话标题更新失败', error);
          }
        }
        currentEvent = '';
        currentData = '';
      };

      const parseLine = (line: string) => {
        if (line === '') {
          dispatchEvent();
        } else if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim();
        } else if (line.startsWith('data:')) {
          currentData = line.slice(5).trim();
        }
      };

      const doFetch = async () => {
        try {
          const token = localStorage.getItem('phoenix-token');
          const response = await fetch(
            `/api/api/agent/${currentAgentId}/sessions/stream`,
            {
              headers: {
                'phoenix-token': token || '',
                Accept: 'text/event-stream',
              },
              signal: controller.signal,
            },
          );
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          const reader = response.body?.getReader();
          const decoder = new TextDecoder();
          if (!reader) {
            throw new Error('No reader available');
          }
          while (true) {
            const { done, value } = await reader.read();
            if (done) {
              break;
            }
            buffer += decoder.decode(value, { stream: true });
            const parts = buffer.split('\n');
            buffer = parts.pop() || '';
            for (const line of parts) {
              parseLine(line);
            }
          }
        } catch (error: any) {
          if (error.name === 'AbortError') {
            return;
          }
          console.error('会话推送连接异常:', error);
          sessionEventSource.value = null;
          if (isComponentActive) {
            reconnectTimer = window.setTimeout(
              () => connectSessionStream(),
              3000,
            );
          }
        }
      };

      doFetch();
      sessionEventSource.value = { close: () => controller.abort() };
    };

    // 开始编辑会话标题
    const startEditSessionTitle = (session: ExtendedChatSession) => {
      session.editing = true;
      session.editingTitle = session.title || '新会话';
      nextTick(() => {
        const input = document.querySelector(
          '.el-input__inner',
        ) as HTMLInputElement;
        if (input) {
          input.focus();
          input.select();
        }
      });
    };

    // 保存会话标题
    const saveSessionTitle = async (session: ExtendedChatSession) => {
      if (!session.editingTitle || session.editingTitle.trim() === '') {
        ElMessage.warning('会话标题不能为空');
        return;
      }

      const newTitle = session.editingTitle.trim();
      if (newTitle === session.title) {
        session.editing = false;
        return;
      }

      try {
        await renameSessionApi(session.id, newTitle);
        session.title = newTitle;
        session.editing = false;
        ElMessage.success('会话标题已更新');
      } catch (error) {
        ElMessage.error('更新会话标题失败');
        console.error('更新会话标题失败:', error);
      }
    };

    // 取消编辑会话标题
    const cancelEditSessionTitle = (session: ExtendedChatSession) => {
      session.editing = false;
    };

    // 计算属性
    const agentId = computed(() => route.params.id as string);

    // 方法
    const goBack = () => {
      router.push(`/agent/${agentId.value}`);
    };

    const loadSessions = async () => {
      try {
        sessions.value = (await getAgentSessionsApi(
          Number.parseInt(agentId.value),
        )) as ExtendedChatSession[];
        // 默认选择第一个会话或创建新会话
        if (sessions.value.length > 0) {
          if (sessions.value[0])
            await props.handleSelectSession(sessions.value[0]);
        } else {
          await createNewSession();
        }
      } catch (error) {
        ElMessage.error('加载会话列表失败');
        console.error('加载会话列表失败:', error);
      }
    };

    const createNewSession = async () => {
      try {
        const newSession = await createSessionApi(
          Number.parseInt(agentId.value),
          '新会话',
        );
        if (!newSession) return;
        sessions.value.unshift(newSession);
        await props.handleSelectSession(newSession);
        ElMessage.success('新会话创建成功');
      } catch (error) {
        ElMessage.error('创建会话失败');
        console.error('创建会话失败:', error);
      }
    };

    const togglePinSession = async (session: ChatSession) => {
      try {
        await pinSessionApi(session.id, !session.isPinned);
        session.isPinned = !session.isPinned;
        ElMessage.success(session.isPinned ? '会话已置顶' : '会话已取消置顶');
      } catch (error) {
        ElMessage.error('操作失败');
        console.error('置顶会话失败:', error);
      }
    };

    const deleteSession = async (session: ChatSession) => {
      try {
        await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          confirmButtonType: 'danger',
        });
        await deleteSessionApi(session.id);
        props.handleDeleteSessionState(session.id);
        sessions.value = sessions.value.filter(
          (s: ChatSession) => s.id !== session.id,
        );
        if (props.handleGetCurrentSession() == session) {
          await props.handleSetCurrentSession(null);
        }
        ElMessage.success('会话删除成功');
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除会话失败');
          console.error('删除会话失败:', error);
        }
      }
    };

    const clearAllSessions = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要清空所有会话吗？此操作不可恢复。',
          '确认清空',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          },
        );
        await clearAgentSessionsApi(Number.parseInt(agentId.value));
        sessions.value.forEach((session: ChatSession) => {
          props.handleDeleteSessionState(session.id);
        });
        sessions.value = [];
        await props.handleSetCurrentSession(null);
        ElMessage.success('所有会话已清空');
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('清空会话失败');
          console.error('清空会话失败:', error);
        }
      }
    };

    // 生命周期
    onMounted(async () => {
      connectSessionStream();
      await loadSessions();
    });

    onUnmounted(() => {
      isComponentActive = false;
      clearReconnectTimer();
      if (sessionEventSource.value) {
        sessionEventSource.value.close();
        sessionEventSource.value = null;
      }
    });

    return {
      sessions,
      formatTime,
      goBack,
      createNewSession,
      togglePinSession,
      deleteSession,
      clearAllSessions,
      startEditSessionTitle,
      saveSessionTitle,
      cancelEditSessionTitle,
    };
  },
});
</script>

<style scoped>
.chat-session-sidebar {
  overflow: hidden;
  background-color: white;
  border-right: 1px solid #e8e8e8;
}

.new-session-section {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.new-session-section .new-session-btn {
  flex: 1;
}

/* 左侧边栏样式 */
.sidebar-header {
  padding: 12px;
}

.header-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 会话列表样式 */
.session-list {
  max-height: calc(100vh - 200px);
  padding: 0 12px 12px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 10px;
  margin-bottom: 4px;
  cursor: pointer;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.session-item:hover {
  background-color: #f8fbff;
  border-color: #409eff;
}

.session-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.session-item.pinned {
  border-left: 4px solid #e6a23c;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
}

.session-actions {
  display: flex;
  flex-shrink: 0;
  gap: 0;
}

.session-actions .el-button {
  min-height: auto;
  padding: 4px 2px;
}

.session-time {
  font-size: 11px;
  color: #909399;
}


</style>
