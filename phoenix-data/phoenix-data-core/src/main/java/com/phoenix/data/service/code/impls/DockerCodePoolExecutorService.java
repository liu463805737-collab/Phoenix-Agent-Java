package com.phoenix.data.service.code.impls;

import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.service.code.CodePoolExecutorService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

/**
 * 运行Python任务的容器池（Docker实现类）
 *
 * @author vlsmb
 * @since 2025/7/12
 */
@Slf4j
public class DockerCodePoolExecutorService extends AbstractCodePoolExecutorService implements CodePoolExecutorService {

	private final DockerClient dockerClient;

	private final boolean isRemote;

	private final ConcurrentHashMap<String, Path> containerTempPath;

	/**
	 * 构造 Docker 代码池执行器，初始化 Docker 客户端并拉取镜像
	 */
	public DockerCodePoolExecutorService(CodeExecutorProperties properties) {
		super(properties);
		// Initialize DockerClient
		String dockerHost = this.getDockerHostForCurrentOS(properties.getHost());
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
			.withDockerHost(dockerHost)
			.withDockerTlsVerify(false)
			.build();
		this.dockerClient = this.createDockerClientWithFallback(config);
		this.isRemote = this.checkIsRemote(properties.getHost());
		log.info("Docker Code Pool initialized. Mode: {}",
				this.isRemote ? "Remote (Copy Files)" : "Local (Bind Mounts)");

		this.containerTempPath = new ConcurrentHashMap<>();

		// Check if image exists locally
		boolean imageExists = this.dockerClient.listImagesCmd()
			.withImageNameFilter(properties.getImageName())
			.exec()
			.stream()
			.anyMatch(image -> Arrays.asList(image.getRepoTags()).contains(properties.getImageName()));

		if (!imageExists) {
			// Pull image
			try {
				this.dockerClient.pullImageCmd(properties.getImageName())
					.exec(new PullImageResultCallback())
					.awaitCompletion();
				log.info("pull image {} success", properties.getImageName());
			}
			catch (Exception e) {
				log.error("pull image {} error", properties.getImageName(), e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 根据当前操作系统自动选择 Docker Host 地址
	 * @return Docker Host URI
	 */
	private String getDockerHostForCurrentOS(String dockerHost) {
		// If configuration object has value, directly use configuration object's value
		if (StringUtils.hasText(dockerHost)) {
			return dockerHost;
		}
		String osName = System.getProperty("os.name").toLowerCase();
		log.info("Detected operating system: {}", osName);

		if (osName.contains("win")) {
			// Windows系统
			log.info("Using Windows Docker configuration");
			// On Windows, try TCP connection first, more stable
			return "tcp://localhost:2375";
		}
		else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
			// Linux/Unix系统
			log.info("Using Linux/Unix Docker configuration");
			return "unix:///var/run/docker.sock";
		}
		else if (osName.contains("mac")) {
			// macOS系统
			log.info("Using macOS Docker configuration");
			return "unix:///var/run/docker.sock";
		}
		else {
			// Unknown system, use default value from configuration file
			log.warn("Unknown operating system: {}, using default docker host", osName);
			return "unix:///var/run/docker.sock";
		}
	}

	/**
	 * 检查 Docker Host 是否为远程地址
	 */
	private boolean checkIsRemote(String host) {
		if (!StringUtils.hasText(host)) {
			// Empty host means using defaults which are local (unix socket or npipe or
			// localhost)
			return false;
		}
		try {
			URI uri = URI.create(host);
			String scheme = uri.getScheme();
			if ("unix".equalsIgnoreCase(scheme) || "npipe".equalsIgnoreCase(scheme)) {
				return false;
			}
			// 处理TCP协议
			if ("tcp".equalsIgnoreCase(scheme)) {
				String h = uri.getHost();
				// 本地地址包括：localhost、127.0.0.1、::1（IPv6本地回环）
				boolean isLocalTcp = "localhost".equalsIgnoreCase(h) || "127.0.0.1".equals(h) || "::1".equals(h);
				return !isLocalTcp; // 不是本地TCP地址则为远程
			}
			return false;
		}
		catch (Exception e) {
			log.warn("Failed to parse Docker host URI: {}, assuming local.", host);
			return false;
		}
	}

	/**
	 * 创建 Docker 客户端，支持多连接方式的回退机制
	 * @param config Docker 客户端配置
	 * @return DockerClient 实例
	 */
	private DockerClient createDockerClientWithFallback(DockerClientConfig config) {
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			// Windows System: Try the configured host first, then fallbacks
			List<String> windowsHosts = new ArrayList<>();
			// 1. Priority: The host from configuration (which might be user-provided or
			// auto-detected)
			if (StringUtils.hasText(String.valueOf(config.getDockerHost()))) {
				windowsHosts.add(String.valueOf(config.getDockerHost()));
			}
			// 2. Fallback: Standard Windows Docker Desktop named pipe
			windowsHosts.add("npipe://./pipe/docker_engine");
			// 3. Fallback: Localhost TCP (common setting)
			windowsHosts.add("tcp://localhost:2375");

			for (String dockerHost : windowsHosts) {
				try {
					log.info("Attempting to connect to Docker using: {}", dockerHost);

					DockerClientConfig testConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
						.withDockerHost(dockerHost)
						.withDockerTlsVerify(false)
						.build();

					ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
						.dockerHost(testConfig.getDockerHost())
						.sslConfig(testConfig.getSSLConfig())
						.build();

					DockerClient dockerClient = DockerClientImpl.getInstance(testConfig, httpClient);

					// Test if connection is normal
					dockerClient.pingCmd().exec();
					log.info("Successfully connected to Docker using: {}", dockerHost);
					return dockerClient;

				}
				catch (Exception e) {
					log.warn("Failed to connect using {}: {}", dockerHost, e.getMessage());
				}
			}

			// If all Windows connection methods fail
			throw new RuntimeException(
					"Failed to connect to Docker on Windows. Please ensure Docker Desktop is running and either:\n"
							+ "1. Enable 'Expose daemon on tcp://localhost:2375 without TLS' in Docker Desktop settings, or\n"
							+ "2. Ensure Docker Desktop's named pipe is available");

		}
		else {
			// Linux/Unix/macOS系统：使用标准Unix socket
			try {
				ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
					.dockerHost(config.getDockerHost())
					.sslConfig(config.getSSLConfig())
					.build();

				DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
				dockerClient.pingCmd().exec(); // Test connection
				log.info("Successfully connected to Docker using: {}", config.getDockerHost());
				return dockerClient;

			}
			catch (Exception e) {
				throw new RuntimeException("Failed to connect to Docker on " + osName + ": " + e.getMessage()
						+ "\nPlease ensure Docker is running and accessible at: " + config.getDockerHost());
			}
		}
	}

	/**
	 * 创建容器的 HostConfig（内存限制、CPU、网络等）
	 */
	private HostConfig createHostConfig(Path tempDir) {
		HostConfig config = newHostConfig().withMemory(this.properties.getLimitMemory() * 1024L * 1024L)
			.withCpuCount(this.properties.getCpuCore())
			.withCapDrop(Capability.ALL)
			.withAutoRemove(false)
			.withTmpFs(Map.of("/tmp", ""))
			.withNetworkMode(this.properties.getNetworkMode());

		if (!this.isRemote) {
			List<Bind> binds = new ArrayList<>();
			binds.add(new Bind(tempDir.resolve("script.py").toAbsolutePath().toString(), new Volume("/app/script.py"),
					AccessMode.ro));
			binds.add(new Bind(tempDir.resolve("requirements.txt").toAbsolutePath().toString(),
					new Volume("/app/requirements.txt"), AccessMode.ro));
			binds.add(new Bind(tempDir.resolve("input_data.txt").toAbsolutePath().toString(),
					new Volume("/app/input_data.txt"), AccessMode.ro));
			config.withBinds(binds.toArray(new Bind[0]));
		}
		return config;
	}

	/**
	 * 清理同名容器资源
	 */
	private void cleanupExistingResources(String containName) {
		try {
			// Try to delete container with same name
			dockerClient.removeContainerCmd(containName).withForce(true).exec();
			log.info("Removed existing container: {}", containName);
		}
		catch (Exception e) {
			log.warn("Failed to remove container {}: {}", containName, e.getMessage());
		}
	}

	/**
	 * 创建新的 Docker 容器
	 */
	@Override
	protected String createNewContainer() throws Exception {
		String containerName = this.generateContainerName();
		// First clean up possibly existing container with same name
		this.cleanupExistingResources(containerName);

		// Generate temporary directory and files
		Path tempDir = Files.createTempDirectory(containerName);
		Files.createFile(tempDir.resolve("requirements.txt"));
		Files.createFile(tempDir.resolve("script.py"));
		Files.createFile(tempDir.resolve("input_data.txt"));

		// Create container
		HostConfig hostConfig = this.createHostConfig(tempDir);
		String cmd = this.buildExecutionCommand(tempDir);

		CreateContainerResponse container = dockerClient.createContainerCmd(properties.getImageName())
			.withName(containerName)
			.withWorkingDir("/app")
			.withHostConfig(hostConfig)
			.withCmd("sh", "-c", cmd)
			.exec();
		String containerId = container.getId();
		// Save temporary directory object
		this.containerTempPath.put(containerId, tempDir);
		return containerId;
	}

	/**
	 * 在指定 Docker 容器中执行任务
	 */
	@Override
	protected TaskResponse execTaskInContainer(TaskRequest request, String containerId) {
		// Get temporary directory object
		Path tempDir = this.containerTempPath.get(containerId);
		if (tempDir == null) {
			log.error("Container '{}' does not exist work dir", containerId);
			return TaskResponse.exception("Container '" + containerId + "' does not exist work dir");
		}

		try {
			// 1. Prepare files
			this.writeContextFiles(tempDir, request);
			this.uploadFilesIfRemote(containerId, tempDir);

			// 2. Start container and wait
			dockerClient.startContainerCmd(containerId).exec();
			dockerClient.waitContainerCmd(containerId)
				.start()
				.awaitCompletion(this.properties.getContainerTimeout(), TimeUnit.SECONDS);

			// 3. Fetch logs
			LogResult logs = this.fetchExecutionLogs(containerId, tempDir);
			String stdout = logs.stdout;
			String stderr = logs.stderr;

			// 4. Check exit code
			InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
			int exitCode = Objects.requireNonNull(inspectResponse.getState().getExitCodeLong()).intValue();
			if (exitCode != 0) {
				String errorMessage = "Docker exit code " + exitCode + ". Stderr: " + stderr + ". Stdout: " + stdout;
				log.error("Error executing Docker container {}: {}", containerId, errorMessage);
				return TaskResponse.failure(stdout, stderr);
			}
			return TaskResponse.success(stdout);
		}
		catch (Exception e) {
			log.error("Error executing task in container: {}", e.getMessage());
			return TaskResponse.exception(e.getMessage());
		}
	}

	// --- Helper Methods ---

	/**
	 * 构建容器执行命令
	 */
	private String buildExecutionCommand(Path tempDir) {
		return String.format(
				"if [ -s requirements.txt ]; then pip3 install --no-cache-dir -r requirements.txt > /dev/null; fi && timeout -s SIGKILL %s python3 -u script.py < input_data.txt",
				properties.getCodeTimeout());
	}

	/**
	 * 将代码、依赖和输入写入临时文件
	 */
	private void writeContextFiles(Path tempDir, TaskRequest request) throws IOException {
		Files.write(tempDir.resolve("script.py"),
				StringUtils.hasText(request.code()) ? request.code().getBytes() : "".getBytes());
		Files.write(tempDir.resolve("requirements.txt"),
				StringUtils.hasText(request.requirement()) ? request.requirement().getBytes() : "".getBytes());
		Files.write(tempDir.resolve("input_data.txt"),
				StringUtils.hasText(request.input()) ? request.input().getBytes() : "".getBytes());
	}

	/**
	 * 远程模式下将文件上传到容器
	 */
	private void uploadFilesIfRemote(String containerId, Path tempDir) {
		if (!this.isRemote) {
			return;
		}
		String[] files = { "script.py", "requirements.txt", "input_data.txt" };
		for (String file : files) {
			dockerClient.copyArchiveToContainerCmd(containerId)
				.withHostResource(tempDir.resolve(file).toString())
				.withRemotePath("/app/")
				.exec();
		}
	}

	/**
	 * 日志结果记录
	 */
	private record LogResult(String stdout, String stderr) {
	}

	/**
	 * 获取容器执行日志（限制最大 5MB）
	 */
	private LogResult fetchExecutionLogs(String containerId, Path tempDir) throws InterruptedException {
		StringBuilder stdoutBuilder = new StringBuilder();
		StringBuilder stderrBuilder = new StringBuilder();

		final int MAX_LOG_SIZE = 5 * 1024 * 1024; // 5MB limit
		dockerClient.logContainerCmd(containerId)
			.withStdOut(true)
			.withStdErr(true)
			.exec(new ResultCallback.Adapter<Frame>() {
				@Override
				public void onNext(Frame item) {
					String payload = new String(item.getPayload(), StandardCharsets.UTF_8);
					if (item.getStreamType() == StreamType.STDOUT) {
						appendWithLimit(stdoutBuilder, payload, MAX_LOG_SIZE);
					}
					else if (item.getStreamType() == StreamType.STDERR) {
						appendWithLimit(stderrBuilder, payload, MAX_LOG_SIZE);
					}
				}
			})
			.awaitCompletion();

		return new LogResult(stdoutBuilder.toString(), stderrBuilder.toString());
	}

	/**
	 * 追加日志内容并限制总大小
	 */
	private void appendWithLimit(StringBuilder builder, String payload, int limit) {
		if (builder.length() < limit) {
			builder.append(payload);
		}
		else if (builder.length() == limit) {
			builder.append("\n...[Output truncated due to size limit]...");
			builder.append(" "); // Prevent re-entry
		}
	}

	/**
	 * 停止 Docker 容器
	 */
	@Override
	protected void stopContainer(String containerId) throws Exception {
		try {
			this.dockerClient.stopContainerCmd(containerId).exec();
			log.info("Successfully stopped container: {}", containerId);
		}
		catch (Exception e) {
			log.warn("Failed to stop container: {}, message: {}", containerId, e.getMessage());
		}
	}

	/**
	 * 删除 Docker 容器并清理临时目录
	 */
	@Override
	protected void removeContainer(String containerId) throws Exception {
		try {
			this.dockerClient.removeContainerCmd(containerId).withForce(true).exec();
			Path tempDir = containerTempPath.get(containerId);
			if (tempDir != null) {
				this.clearTempDir(tempDir);
			}
			containerTempPath.remove(containerId);
			log.info("Successfully removed container: {}", containerId);
		}
		catch (Exception e) {
			log.warn("Failed to remove container: {}, message: {}", containerId, e.getMessage());
		}
	}

	/**
	 * 关闭容器池，清理所有容器并关闭 Docker 客户端
	 */
	@Override
	protected void shutdownPool() throws Exception {
		try {
			super.shutdownPool();
			this.dockerClient.close();
		}
		catch (IOException ignored) {

		}
	}

}
