package com.phoenix.data.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.phoenix.data.dto.ChatMessageDTO;
import com.phoenix.data.entity.ChatMessage;
import com.phoenix.data.entity.ChatSession;
import com.phoenix.data.service.chat.ChatMessageService;
import com.phoenix.data.service.chat.ChatSessionService;
import com.phoenix.data.service.chat.SessionTitleService;
import com.phoenix.data.util.ReportTemplateUtil;
import com.phoenix.data.vo.ApiResponse;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Chat Controller
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionService chatSessionService;

    private final ChatMessageService chatMessageService;

    private final SessionTitleService sessionTitleService;

    private final ReportTemplateUtil reportTemplateUtil;

    /**
     * Get session list for an agent
     */
    @GetMapping("/agent/{id}/sessions")
    public ReturnVo<List<ChatSession>> getAgentSessions(@PathVariable(value = "id") Integer id) {
        List<ChatSession> sessions = chatSessionService.findByAgentIdAndUserId(id, StpUtil.getLoginIdAsString());
        return ReturnVo.ok(sessions);
    }

//	/**
//	 * Get session list for an agent
//	 */
//	@GetMapping("/agent/{id}/sessions")
//	public ReturnVo<List<ChatSession>> getAgentSessions(@PathVariable(value = "id") Integer id) {
//		List<ChatSession> sessions = chatSessionService.findByAgentId(id);
//		return ReturnVo.ok(sessions);
//	}

    /**
     * Create a new session
     */
    @PostMapping("/agent/{id}/sessions")
    public ReturnVo<ChatSession> createSession(@PathVariable(value = "id") Integer id,
                                               @RequestBody(required = false) Map<String, Object> request) {
        String title = request != null ? (String) request.get("title") : null;
        ChatSession session = chatSessionService.createSession(id, title, StpUtil.getLoginIdAsString());
        return ReturnVo.ok(session);
    }

    /**
     * Clear all sessions for an agent
     */
    @DeleteMapping("/agent/{id}/sessions")
    public ReturnVo<ApiResponse> clearAgentSessions(@PathVariable(value = "id") Integer id) {
        chatSessionService.clearSessionsByAgentId(id);
        return ReturnVo.ok(ApiResponse.success("会话已清空"));
    }

    /**
     * Get message list for a session
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ReturnVo<List<ChatMessage>> getSessionMessages(@PathVariable(value = "sessionId") String sessionId) {
        List<ChatMessage> messages = chatMessageService.findBySessionId(sessionId);
        return ReturnVo.ok(messages);
    }

    /**
     * Save message to session
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ReturnVo<ChatMessage> saveMessage(@PathVariable(value = "sessionId") String sessionId,
                                             @RequestBody ChatMessageDTO request) {
        try {
            if (request == null) {
                return ReturnVo.error("参数不能为空！");
            }
            ChatMessage message = ChatMessage.builder()
                    .sessionId(sessionId)
                    .role(request.getRole())
                    .content(request.getContent())
                    .messageType(request.getMessageType())
                    .metadata(request.getMetadata())
                    .build();

            ChatMessage savedMessage = chatMessageService.saveMessage(message);

            // Update session activity time
            chatSessionService.updateSessionTime(sessionId);

            if (request.isTitleNeeded()) {
                sessionTitleService.scheduleTitleGeneration(sessionId, message.getContent());
            }

            return ReturnVo.ok(savedMessage);
        } catch (Exception e) {
            log.error("Save message error for session {}: {}", sessionId, e.getMessage(), e);
            return ReturnVo.error(e.getMessage());
        }
    }

    /**
     * 置顶/取消置顶会话
     */
    @PutMapping("/sessions/{sessionId}/pin")
    public ReturnVo<ApiResponse> pinSession(@PathVariable(value = "sessionId") String sessionId,
                                            @RequestParam(value = "isPinned") Boolean isPinned) {
        try {
            chatSessionService.pinSession(sessionId, isPinned);
            String message = isPinned ? "会话已置顶" : "会话已取消置顶";
            return ReturnVo.ok(ApiResponse.success(message));
        } catch (Exception e) {
            log.error("Pin session error for session {}: {}", sessionId, e.getMessage(), e);
            return ReturnVo.error("操作失败");
        }
    }

    /**
     * Rename session
     */
    @PutMapping("/sessions/{sessionId}/rename")
    public ReturnVo<ApiResponse> renameSession(@PathVariable(value = "sessionId") String sessionId,
                                               @RequestParam(value = "title") String title) {
        try {
            if (!StringUtils.hasText(title)) {
                return ReturnVo.error("标题不能为空");
            }

            chatSessionService.renameSession(sessionId, title.trim());
            return ReturnVo.ok(ApiResponse.success("会话已重命名"));
        } catch (Exception e) {
            log.error("Rename session error for session {}: {}", sessionId, e.getMessage(), e);
            return ReturnVo.error("重命名失败");
        }
    }

    /**
     * Delete a single session
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ReturnVo<ApiResponse> deleteSession(@PathVariable(value = "sessionId") String sessionId) {
        try {
            chatSessionService.deleteSession(sessionId);
            return ReturnVo.ok(ApiResponse.success("会话已删除"));
        } catch (Exception e) {
            log.error("Delete session error for session {}: {}", sessionId, e.getMessage(), e);
            return ReturnVo.error("删除失败");
        }
    }

    /**
     * Download HTML report
     */
    @PostMapping("/sessions/{sessionId}/reports/html")
    public ResponseEntity<byte[]> convertAndDownloadHtml(@PathVariable(value = "sessionId") String sessionId,
                                                         @RequestBody String content) {
        try {
            if (!StringUtils.hasText(content)) {
                return ResponseEntity.badRequest().build();
            }
            log.debug("Download HTML report for session {}", sessionId);
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append(reportTemplateUtil.getHeader());
            htmlContent.append(content);
            htmlContent.append(reportTemplateUtil.getFooter());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = "report_" + timestamp + ".html";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Download HTML report error for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
