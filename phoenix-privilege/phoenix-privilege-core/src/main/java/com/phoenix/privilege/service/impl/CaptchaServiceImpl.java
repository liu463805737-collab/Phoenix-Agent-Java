package com.phoenix.privilege.service.impl;

import com.phoenix.privilege.enums.CaptchaVerifyResult;
import com.phoenix.privilege.service.CaptchaService;
import com.phoenix.privilege.vo.CaptchaVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class CaptchaServiceImpl implements CaptchaService {

	private static final int WIDTH = 130;

	private static final int HEIGHT = 42;

	private static final int CODE_LENGTH = 4;

	private static final long EXPIRE_SECONDS = 60;

	private static final String REDIS_KEY_PREFIX = "captcha:";

	private final StringRedisTemplate redisTemplate;

	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	private static final Random RANDOM = new Random();

	public CaptchaServiceImpl(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public CaptchaVO generate() {
		String code = generateCode();
		String key = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, code, EXPIRE_SECONDS, TimeUnit.SECONDS);
		String imageBase64 = drawImage(code);
		return CaptchaVO.builder().captchaKey(key).captchaImage("data:image/png;base64," + imageBase64).build();
	}

	@Override
	public CaptchaVerifyResult verify(String captchaKey, String captchaCode) {
		if (captchaKey == null || captchaCode == null) {
			return CaptchaVerifyResult.INVALID;
		}
		String redisKey = REDIS_KEY_PREFIX + captchaKey;
		String expected = redisTemplate.opsForValue().get(redisKey);
		if (expected == null) {
			return CaptchaVerifyResult.EXPIRED;
		}
		redisTemplate.delete(redisKey);
		if (expected.equalsIgnoreCase(captchaCode.trim())) {
			return CaptchaVerifyResult.VALID;
		}
		return CaptchaVerifyResult.INVALID;
	}

	private String generateCode() {
		StringBuilder sb = new StringBuilder(CODE_LENGTH);
		for (int i = 0; i < CODE_LENGTH; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}

	private String drawImage(String code) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 26));
			for (int i = 0; i < CODE_LENGTH; i++) {
				g.setColor(randomColor());
				int x = 15 + i * 28 + RANDOM.nextInt(8);
				int y = 30 + RANDOM.nextInt(8);
				double angle = (RANDOM.nextDouble() - 0.5) * 0.6;
				g.rotate(angle, x, y);
				g.drawString(String.valueOf(code.charAt(i)), x, y);
				g.rotate(-angle, x, y);
			}
			for (int i = 0; i < 8; i++) {
				g.setColor(randomColor());
				int x1 = RANDOM.nextInt(WIDTH);
				int y1 = RANDOM.nextInt(HEIGHT);
				int x2 = RANDOM.nextInt(WIDTH);
				int y2 = RANDOM.nextInt(HEIGHT);
				g.drawLine(x1, y1, x2, y2);
			}
			g.setColor(Color.LIGHT_GRAY);
			for (int i = 0; i < 40; i++) {
				int x = RANDOM.nextInt(WIDTH);
				int y = RANDOM.nextInt(HEIGHT);
				g.fillRect(x, y, 2, 2);
			}
			g.dispose();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
		catch (Exception e) {
			if (g != null)
				g.dispose();
			throw new RuntimeException("生成验证码失败", e);
		}
	}

	private Color randomColor() {
		int r = 50 + RANDOM.nextInt(180);
		int g = 50 + RANDOM.nextInt(180);
		int b = 50 + RANDOM.nextInt(180);
		return new Color(r, g, b);
	}

}
