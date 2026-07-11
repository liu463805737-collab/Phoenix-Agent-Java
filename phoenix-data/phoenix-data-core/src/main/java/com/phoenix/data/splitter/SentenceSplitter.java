package com.phoenix.data.splitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 句子分块器
 *
 * @author zihenzzz
 * @since 2025/1/3
 */
@Slf4j
public class SentenceSplitter extends TextSplitter {

	/**
	 * 正则说明： 1. ([^。！？；.!?;\n]+) : 非分隔符内容 2. (?:[。！？；.!?;]|\n+) : 分隔符（标点或换行） 3.
	 * ["'）\)\\]]* : 可能跟随的后引号/括号 4. \\s* : 尾随空白（将被 trim 掉）
	 */
	private static final Pattern SENTENCE_PATTERN = Pattern
		.compile("([^。！？；.!?;\n]+(?:[。！？；.!?;]|\\n+)[\"'）\\)\\]]*\\s*)");

	private static final int DEFAULT_CHUNK_SIZE = 1000;

	private static final int DEFAULT_SENTENCE_OVERLAP = 1;

	private final int chunkSize;

	private final int sentenceOverlap;

	// 私有构造器 & Builder
	/**
	 * 私有构造方法，使用Builder创建实例
	 * @param builder Builder对象
	 */
	private SentenceSplitter(Builder builder) {
		this.chunkSize = builder.chunkSize > 0 ? builder.chunkSize : DEFAULT_CHUNK_SIZE;
		this.sentenceOverlap = builder.sentenceOverlap >= 0 ? builder.sentenceOverlap : DEFAULT_SENTENCE_OVERLAP;
	}

	/**
	 * 创建Builder实例
	 * @return Builder对象
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * SentenceSplitter的构建器
	 */
	public static class Builder {

		private int chunkSize = DEFAULT_CHUNK_SIZE;

		private int sentenceOverlap = DEFAULT_SENTENCE_OVERLAP;

		/**
		 * 设置每个块的最大字符数
		 * @param chunkSize 块大小
		 * @return Builder实例
		 */
		public Builder withChunkSize(int chunkSize) {
			this.chunkSize = chunkSize;
			return this;
		}

		/**
		 * 设置句子重叠数
		 * @param overlap 重叠句子数
		 * @return Builder实例
		 */
		public Builder withSentenceOverlap(int overlap) {
			this.sentenceOverlap = overlap;
			return this;
		}

		/**
		 * 构建SentenceSplitter实例
		 * @return SentenceSplitter对象
		 */
		public SentenceSplitter build() {
			return new SentenceSplitter(this);
		}

	}

	/**
	 * 对文本进行句子级分块
	 * @param text 输入文本
	 * @return 句子列表
	 */
	@Override
	protected List<String> splitText(String text) {
		return extractSentences(text);
	}

	/**
	 * 对文档列表应用句子分块
	 * @param documents 原始文档列表
	 * @return 分块后的文档列表
	 */
	@Override
	public List<Document> apply(List<Document> documents) {
		if (CollectionUtils.isEmpty(documents))
			return new ArrayList<>();
		List<Document> result = new ArrayList<>();
		for (Document doc : documents) {
			if (StringUtils.hasText(doc.getText())) {
				result.addAll(splitDocument(doc));
			}
		}
		log.info("Split {} documents into {} chunks", documents.size(), result.size());
		return result;
	}

	/**
	 * 对单个文档进行句子级分块
	 * @param document 原始文档
	 * @return 分块后的文档列表
	 */
	private List<Document> splitDocument(Document document) {
		List<String> sentences = extractSentences(document.getText());
		if (sentences.isEmpty())
			return List.of(document);

		List<Document> result = new ArrayList<>();
		List<String> currentChunk = new ArrayList<>();
		int currentSize = 0;

		for (String sentence : sentences) {
			int sentenceLen = sentence.length();

			// 1. 处理巨型句子 (单句本身超长)
			if (sentenceLen > this.chunkSize) {
				if (!currentChunk.isEmpty()) {
					flushChunk(result, currentChunk, document);
					// 巨型句子打断了上下文，Overlap 意义不大，直接清空
					currentChunk.clear();
					currentSize = 0;
				}

				// 切分巨型句子
				List<String> parts = splitLongSentence(sentence);
				for (String part : parts) {
					// 检查 part 是否能放入 (通常 splitLongSentence 保证了 part <= chunkSize)
					if (currentSize + part.length() > this.chunkSize && !currentChunk.isEmpty()) {
						flushChunk(result, currentChunk, document);
						handleOverlap(currentChunk);
						currentSize = calculateSize(currentChunk);
					}
					currentChunk.add(part);
					currentSize += part.length();
				}
				continue;
			}

			// 2. 普通句子处理
			if (currentSize + sentenceLen > this.chunkSize && !currentChunk.isEmpty()) {
				flushChunk(result, currentChunk, document);
				handleOverlap(currentChunk);
				currentSize = calculateSize(currentChunk);
			}

			currentChunk.add(sentence);
			currentSize += sentenceLen;
		}

		// 处理剩余
		if (!currentChunk.isEmpty()) {
			flushChunk(result, currentChunk, document);
		}

		return result;
	}

	/**
	 * 核心方法：将当前 Buffer 生成 Document，并添加到结果集
	 */
	private void flushChunk(List<Document> result, List<String> chunkSentences, Document originalDoc) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < chunkSentences.size(); i++) {
			String s = chunkSentences.get(i);
			// 智能拼接：如果前一句不是中文，且当前句不是中文，才加空格
			// 或者简单点：只要不是中文就加空格。这里用严谨判断。
			if (i > 0 && !isChinese(s)) {
				content.append(" ");
			}
			content.append(s);
		}

		String text = content.toString();
		Document chunkDoc = new Document(text);
		if (originalDoc.getMetadata() != null) {
			chunkDoc.getMetadata().putAll(originalDoc.getMetadata());
		}
		chunkDoc.getMetadata().put("chunk_index", result.size());
		chunkDoc.getMetadata().put("chunk_size", text.length());
		chunkDoc.getMetadata().put("splitter_type", "sentence");

		result.add(chunkDoc);
	}

	/**
	 * 处理 Overlap：保留 list 的最后 N 个元素，修改 list 本身
	 */
	/**
	 * 处理块之间的句子重叠，保留最后N个句子作为下一块的起始
	 * @param currentChunk 当前块句子列表（会被修改）
	 */
	private void handleOverlap(List<String> currentChunk) {
		if (this.sentenceOverlap > 0 && currentChunk.size() > this.sentenceOverlap) {
			List<String> overlap = new ArrayList<>(
					currentChunk.subList(currentChunk.size() - this.sentenceOverlap, currentChunk.size()));
			currentChunk.clear();
			currentChunk.addAll(overlap);
		}
		else {
			currentChunk.clear();
		}
	}

	/**
	 * 计算句子列表的总字符数
	 * @param chunk 句子列表
	 * @return 总字符数
	 */
	private int calculateSize(List<String> chunk) {
		return chunk.stream().mapToInt(String::length).sum();
	}

	/**
	 * 从文本中提取句子列表
	 * @param text 输入文本
	 * @return 句子列表
	 */
	private List<String> extractSentences(String text) {
		List<String> sentences = new ArrayList<>();
		Matcher matcher = SENTENCE_PATTERN.matcher(text);
		int lastEnd = 0;

		while (matcher.find()) {
			String sentence = matcher.group(1).trim();
			if (StringUtils.hasText(sentence)) {
				sentences.add(sentence);
			}
			lastEnd = matcher.end();
		}

		if (lastEnd < text.length()) {
			String remaining = text.substring(lastEnd).trim();
			if (StringUtils.hasText(remaining)) {
				// 如果剩余部分本身就超大，也需要切
				if (remaining.length() > this.chunkSize) {
					sentences.addAll(splitLongSentence(remaining));
				}
				else {
					sentences.add(remaining);
				}
			}
		}
		return sentences;
	}

	/**
	 * 分割超长句子
	 */
	/**
	 * 分割超长句子，按字符数切分并优化英文单词边界
	 * @param sentence 超长句子
	 * @return 切分后的文本列表
	 */
	private List<String> splitLongSentence(String sentence) {
		List<String> result = new ArrayList<>();
		int i = 0;
		int len = sentence.length();

		while (i < len) {
			int end = Math.min(i + this.chunkSize, len);

			// 优化：只有当截断点在字符串中间，且是 ASCII 字符时才尝试回溯
			if (end < len && isAsciiLetter(sentence.charAt(end))) {
				int adjustedEnd = end;
				// 限制回溯范围 (例如最大回溯 50 个字符)，防止极端情况退回太多
				int minEnd = Math.max(i, end - 50);

				while (adjustedEnd > minEnd && isAsciiLetter(sentence.charAt(adjustedEnd))) {
					adjustedEnd--;
				}

				// 只有找到了合适的分割点（非字母数字）才更新 end
				if (adjustedEnd > minEnd && adjustedEnd < end) {
					end = adjustedEnd;
				}
			}

			result.add(sentence.substring(i, end));

			// 下一次的起点，必须是这一次的终点
			i = end;
		}
		return result;
	}

	// 判断是否为 ASCII 字母或数字 (用于英文单词边界判断)
	/**
	 * 判断字符是否为ASCII字母或数字（用于英文单词边界判断）
	 * @param c 字符
	 * @return 是否为ASCII字母或数字
	 */
	private boolean isAsciiLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	}

	// 判断是否包含汉字 (用于拼接空格判断)
	/**
	 * 判断字符串是否包含汉字（用于拼接空格判断）
	 * @param str 输入字符串
	 * @return 是否包含汉字
	 */
	private boolean isChinese(String str) {
		if (str == null || str.isEmpty())
			return false;
		// 简单判断首字符是否为汉字即可满足大部分拼接场景
		int codePoint = str.codePointAt(0);
		return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
	}

}
