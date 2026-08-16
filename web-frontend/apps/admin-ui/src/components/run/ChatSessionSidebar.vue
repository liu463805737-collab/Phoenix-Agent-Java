<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRoute } from 'vue-router';
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
import {
  Plus,
  Delete,
  Star,
  StarFilled,
  Edit,
  DArrowLeft,
  DArrowRight,
} from '@element-plus/icons-vue';
import type { Agent } from '#/api/core/agent';
import type { ChatSession } from '#/api/core/chat';
import {
  getAgentSessionsApi,
  createSessionApi,
  deleteSessionApi,
  pinSessionApi,
  renameSessionApi,
  clearAgentSessionsApi,
} from '#/api/core/chat';

defineOptions({ name: 'ChatSessionSidebar' });

const props = defineProps<{
  agent: Agent;
  currentSession: ChatSession | null;
}>();

const emit = defineEmits<{
  (e: 'select-session', session: ChatSession | null): void;
  (e: 'delete-session-state', sessionId: string): void;
}>();

interface ExtendedChatSession extends ChatSession {
  editing?: boolean;
  editingTitle?: string;
}

interface SessionUpdateEvent {
  type: string;
  sessionId: string;
  title: string;
}

const sessions = ref<ExtendedChatSession[]>([]);
const collapsed = ref(false);
const sessionEventSource = ref<{ close: () => void } | null>(null);

let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let isComponentActive = true;

const route = useRoute();
const agentId = computed(() => route.params.id as string);

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
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

const handleTitleUpdate = (eventData: SessionUpdateEvent) => {
  if (!eventData?.sessionId) return;
  const target = sessions.value.find(
    (session) => session.id === eventData.sessionId,
  );
  if (target) {
    target.title = eventData.title;
    target.editingTitle = eventData.title;
  }
  if (props.currentSession?.id === eventData.sessionId && props.currentSession) {
    props.currentSession.title = eventData.title;
  }
};

const connectSessionStream = () => {
  clearReconnectTimer();
  const currentAgentId = agentId.value;
  if (!currentAgentId) return;
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
      if (!reader) throw new Error('No reader available');
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split('\n');
        buffer = parts.pop() || '';
        for (const line of parts) {
          parseLine(line);
        }
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return;
      console.error('会话推送连接异常:', error);
      sessionEventSource.value = null;
      if (isComponentActive) {
        reconnectTimer = setTimeout(() => connectSessionStream(), 3000);
      }
    }
  };

  doFetch();
  sessionEventSource.value = { close: () => controller.abort() };
};

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

const cancelEditSessionTitle = (session: ExtendedChatSession) => {
  session.editing = false;
};

const loadSessions = async () => {
  try {
    sessions.value = (await getAgentSessionsApi(
      Number.parseInt(agentId.value),
    )) as ExtendedChatSession[];
    if (sessions.value.length > 0) {
      if (sessions.value[0]) emit('select-session', sessions.value[0]);
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
    emit('select-session', newSession);
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
    emit('delete-session-state', session.id);
    sessions.value = sessions.value.filter(
      (s: ChatSession) => s.id !== session.id,
    );
    if (props.currentSession?.id === session.id) {
      emit('select-session', null);
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
      emit('delete-session-state', session.id);
    });
    sessions.value = [];
    emit('select-session', null);
    ElMessage.success('所有会话已清空');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空会话失败');
      console.error('清空会话失败:', error);
    }
  }
};

const handleSelectSession = (session: ChatSession) => {
  emit('select-session', session);
};

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
</script>

<template>
  <el-aside
    class="chat-session-sidebar"
    :class="{ collapsed }"
    :style="{ width: collapsed ? '52px' : '300px' }"
  >
    <div v-if="collapsed" class="sidebar-collapsed">
      <el-avatar size="small" style="font-size:14px;font-weight:600;color:#fff;background:#2f6bff;flex-shrink:0">
        {{ agent.name?.charAt(0) || 'A' }}
      </el-avatar>
      <el-tooltip content="展开侧栏" placement="right">
        <el-button
          text
          class="collapse-toggle-btn"
          @click="collapsed = false"
        >
          <el-icon style="font-size:18px"><DArrowRight /></el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <template v-else>
      <div class="sidebar-header">
        <div class="header-controls">
          <span class="header-spacer" />
          <el-avatar size="large" style="font-size:20px;font-weight:600;color:#fff;background:#2f6bff;flex-shrink:0">
            {{ agent.name?.charAt(0) || 'A' }}
          </el-avatar>
          <el-tooltip content="收起侧栏" placement="bottom">
            <el-button
              text
              class="collapse-toggle-btn"
              @click="collapsed = true"
            >
              <el-icon style="font-size:18px"><DArrowLeft /></el-icon>
            </el-button>
          </el-tooltip>
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

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="[
            'session-item',
            {
              active: currentSession?.id === session.id,
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
    </template>
  </el-aside>
</template>

<style scoped>
.chat-session-sidebar {
  overflow: hidden;
  background-color: white;
  border-right: 1px solid #e8e8e8;
  transition: width 0.25s ease;
}

.chat-session-sidebar.collapsed {
  overflow: visible;
}

.sidebar-collapsed {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
}

.collapse-toggle-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #666;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.collapse-toggle-btn:hover {
  background-color: #f0f0f0;
  color: #333;
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

.sidebar-header {
  padding: 16px 12px 4px;
}

.header-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-spacer {
  width: 28px;
  flex-shrink: 0;
}

.session-list {
  max-height: calc(100vh - 200px);
  padding: 12px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  margin-bottom: 4px;
  cursor: pointer;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.session-item:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
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
