package com.phoenix.agent.service.impl;

import com.phoenix.agent.service.AudioTranscriptionService;
import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AudioTranscriptionServiceImpl implements AudioTranscriptionService {

    private final AiModelRegistry aiModelRegistry;

    @Override
    public String transcribeAudio(MultipartFile audioFile) {
        TranscriptionModel transcriptionModel = aiModelRegistry.getTranscriptionModel();
        // 2. 封装请求
        Resource audioResource = audioFile.getResource();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource);
        // 3. 调用模型
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        if (response.getResult() != null) {
            return response.getResult().getOutput();
        }
        return "";
    }
}
