import type { PresetQuestion } from '#/api/core/agent';
import {
  getPresetQuestionsApi,
  batchSavePresetQuestionsApi,
  deletePresetQuestionApi,
} from '#/api/core/agent';

export type { PresetQuestion };

export interface PresetQuestionDTO {
  question: string;
  isActive?: boolean;
}

const presetQuestionService = {
  async list(agentId: number) {
    return getPresetQuestionsApi(agentId);
  },

  async delete(agentId: number, questionId: number) {
    try {
      await deletePresetQuestionApi(agentId, questionId);
      return true;
    } catch {
      return false;
    }
  },

  async batchSave(agentId: number, questions: PresetQuestionDTO[]) {
    try {
      await batchSavePresetQuestionsApi(agentId, questions);
      return true;
    } catch {
      return false;
    }
  },
};

export default presetQuestionService;
