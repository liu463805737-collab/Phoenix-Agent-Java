package com.phoenix.agent.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AudioTranscriptionService {

    String transcribeAudio(MultipartFile audioFile) throws IOException;
}
