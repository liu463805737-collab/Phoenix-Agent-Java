package com.phoenix.data.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型检查视图对象，用于检查聊天模型和嵌入模型的就绪状态
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelCheckVo {

	boolean chatModelReady;

	boolean embeddingModelReady;

	boolean ready;

}
