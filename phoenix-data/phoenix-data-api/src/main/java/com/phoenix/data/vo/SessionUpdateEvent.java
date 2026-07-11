package com.phoenix.data.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 会话更新事件对象
 */
@Value
@Builder
public class SessionUpdateEvent {

	public static final String TYPE_TITLE_UPDATED = "title-updated";

	String type;

	String sessionId;

	String title;

	/**
	 * 创建标题更新事件
	 * @param sessionId 会话ID
	 * @param title 新标题
	 * @return SessionUpdateEvent实例
	 */
	public static SessionUpdateEvent titleUpdated(String sessionId, String title) {
		return SessionUpdateEvent.builder().type(TYPE_TITLE_UPDATED).sessionId(sessionId).title(title).build();
	}

}
