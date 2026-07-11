package com.phoenix.platform.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.phoenix.data.entity.AgentPresetQuestion;
import com.phoenix.data.service.agent.AgentPresetQuestionService;
import com.phoenix.platform.dto.front.PresetQuestionAddDTO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/front")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentPresetQuestionFrontController {

    private final AgentPresetQuestionService presetQuestionService;

    @GetMapping("/{agentId}/preset-questions")
    public ReturnVo<List<AgentPresetQuestion>> getPresetQuestions(@PathVariable(value = "agentId") Long agentId) {
        try {
            List<AgentPresetQuestion> questions = presetQuestionService.findAllByAgentId(agentId);
            return ReturnVo.ok(questions);
        }
        catch (Exception e) {
            log.error("Error getting preset questions for agent {}", agentId, e);
            return ReturnVo.fail("Error getting preset questions for agent " + agentId);
        }
    }

    @PostMapping("/addPresetQuestion")
    public ReturnVo<AgentPresetQuestion> addPresetQuestion(
            @RequestBody PresetQuestionAddDTO dto) {
        try {
            String accountId = StpUtil.getLoginIdAsString();
            AgentPresetQuestion question = new AgentPresetQuestion();
            question.setAgentId(dto.getAgentId());
            question.setAccountId(accountId);
            question.setQuestion(dto.getQuestion());
            question.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            question.setIsActive(true);
            presetQuestionService.create(question);
            return ReturnVo.ok(question);
        }
        catch (Exception e) {
            log.error("添加预设问题失败，原因 {}", dto.getAgentId(), e);
            return ReturnVo.fail("添加预设问题失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletePresetQuestion/{questionId}")
    public ReturnVo<Void> deletePresetQuestion(@PathVariable Long questionId) {
        try {
            String accountId = StpUtil.getLoginIdAsString();
            AgentPresetQuestion existing = presetQuestionService.getById(questionId);
            if (existing == null) {
                return ReturnVo.fail("预设问题不存在");
            }
            if (!accountId.equals(existing.getAccountId())) {
                return ReturnVo.fail("无权删除该预设问题");
            }
            presetQuestionService.deleteById(questionId);
            return ReturnVo.ok();
        }
        catch (Exception e) {
            log.error("删除预设问题失败，原因{}", e.getMessage());
            return ReturnVo.fail("删除预设问题失败: " + e.getMessage());
        }
    }
}
