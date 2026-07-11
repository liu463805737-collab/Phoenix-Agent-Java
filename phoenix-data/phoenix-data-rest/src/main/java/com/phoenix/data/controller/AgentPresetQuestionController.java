package com.phoenix.data.controller;

import com.phoenix.data.entity.AgentPresetQuestion;
import com.phoenix.data.service.agent.AgentPresetQuestionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能体预设问题控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentPresetQuestionController {

    private final AgentPresetQuestionService presetQuestionService;

    @GetMapping("/{agentId}/{accountId}/preset-questions")
    public ResponseEntity<List<AgentPresetQuestion>> getPresetQuestions(@PathVariable(value = "agentId") Long agentId, @PathVariable(value = "accountId") String accountId) {
        try {
            List<AgentPresetQuestion> questions = presetQuestionService.findAllByAgentIdAndAccountId(agentId, accountId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting preset questions for agent {}", agentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get preset question list of agent
     */
    @GetMapping("/{agentId}/preset-questions")
    public ResponseEntity<List<AgentPresetQuestion>> getPresetQuestions(@PathVariable(value = "agentId") Long agentId) {
        try {
            List<AgentPresetQuestion> questions = presetQuestionService.findAllByAgentId(agentId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting preset questions for agent {}", agentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Batch save preset questions of agent
     */
    @PostMapping("/{agentId}/preset-questions")
    public ResponseEntity<Map<String, String>> savePresetQuestions(@PathVariable(value = "agentId") Long agentId,
                                                                   @RequestBody List<Map<String, Object>> questionsData) {
        try {
            List<AgentPresetQuestion> questions = questionsData.stream().map(data -> {
                AgentPresetQuestion question = new AgentPresetQuestion();
                question.setQuestion((String) data.get("question"));
                Object isActiveObj = data.get("isActive");
                if (isActiveObj instanceof Boolean) {
                    question.setIsActive((Boolean) isActiveObj);
                } else if (isActiveObj != null) {
                    question.setIsActive(Boolean.parseBoolean(isActiveObj.toString()));
                } else {
                    question.setIsActive(true);
                }
                return question;
            }).toList();
            presetQuestionService.batchSave(agentId, questions);
            return ResponseEntity.ok(Map.of("message", "预设问题保存成功"));
        } catch (Exception e) {
            log.error("Error saving preset questions for agent {}", agentId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "保存预设问题失败: " + e.getMessage()));
        }
    }

    /**
     * Delete preset question
     */
    @DeleteMapping("/{agentId}/preset-questions/{questionId}")
    public ResponseEntity<Map<String, String>> deletePresetQuestion(@PathVariable(value = "agentId") Long agentId,
                                                                    @PathVariable Long questionId) {
        try {
            presetQuestionService.deleteById(questionId);
            return ResponseEntity.ok(Map.of("message", "预设问题删除成功"));
        } catch (Exception e) {
            log.error("Error deleting preset question {} for agent {}", questionId, agentId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "删除预设问题失败: " + e.getMessage()));
        }
    }

}
