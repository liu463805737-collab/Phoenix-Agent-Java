package com.phoenix.data.service.code.impls;

import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.service.code.CodePoolExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运行Python任务的容器池
 *
 * @author vlsmb
 * @since 2025/7/12
 */
@Slf4j
public abstract class AbstractCodePoolExecutorService implements CodePoolExecutorService {

	// Record core container status
	protected final ConcurrentHashMap<String, CodePoolExecutorService.State> coreContainerState;

	// Record temporary container status
	protected final ConcurrentHashMap<String, CodePoolExecutorService.State> tempContainerState;

	// Record Future for temporary container destruction
	protected final ConcurrentHashMap<String, Future<?>> tempContainerRemoveFuture;

	// Task queue (temporarily store tasks when containers are full)
	protected final ArrayBlockingQueue<FutureTask<CodePoolExecutorService.TaskResponse>> taskQueue;

	// Ready core containers
	protected final ArrayBlockingQueue<String> readyCoreContainer;

	// Ready temporary containers
	protected final ArrayBlockingQueue<String> readyTempContainer;

	// Current number of core containers
	protected final AtomicInteger currentCoreContainerSize;

	// Current number of temporary containers
	protected final AtomicInteger currentTempContainerSize;

	// Thread pool, running temporarily stored tasks
	protected final ExecutorService consumerThreadPool;

	// Configuration properties
	protected final CodeExecutorProperties properties;

	/**
	 * 构造容器池执行器，初始化容器状态管理、任务队列及线程池，并注册关闭钩子
	 */
	public AbstractCodePoolExecutorService(CodeExecutorProperties properties) {
		this.properties = properties;
		this.coreContainerState = new ConcurrentHashMap<>();
		this.tempContainerState = new ConcurrentHashMap<>();
		this.tempContainerRemoveFuture = new ConcurrentHashMap<>();
		this.taskQueue = new ArrayBlockingQueue<>(properties.getTaskQueueSize());
		this.readyCoreContainer = new ArrayBlockingQueue<>(properties.getCoreContainerNum());
		this.readyTempContainer = new ArrayBlockingQueue<>(properties.getTempContainerNum());
		this.consumerThreadPool = new ThreadPoolExecutor(properties.getCoreThreadSize(), properties.getMaxThreadSize(),
				properties.getKeepThreadAliveTime(), TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(properties.getThreadQueueSize()));
		this.currentCoreContainerSize = new AtomicInteger(0);
		this.currentTempContainerSize = new AtomicInteger(0);
		// Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down container pool executor...");
			try {
				this.shutdownPool();
			}
			catch (Exception ignored) {
			}
		}));
	}

	/**
	 * 创建新的容器
	 * @return 容器ID
	 */
	protected abstract String createNewContainer() throws Exception;

	/**
	 * 在指定容器ID的容器运行任务
	 * @param request 任务请求对象
	 * @param containerId 容器ID
	 * @return 运行结果对象
	 */
	protected abstract TaskResponse execTaskInContainer(TaskRequest request, String containerId);

	/**
	 * 停止指定容器
	 * @param containerId 容器ID
	 */
	protected abstract void stopContainer(String containerId) throws Exception;

	/**
	 * 删除指定容器
	 * @param containerId 容器ID
	 */
	protected abstract void removeContainer(String containerId) throws Exception;

	/**
	 * 关闭容器池，停止所有容器并清理资源
	 */
	protected void shutdownPool() throws Exception {
		// Shutdown thread pool
		this.consumerThreadPool.shutdownNow();
		// Stop and delete all containers
		this.tempContainerState.keySet().forEach(id -> this.removeContainerAndState(id, false, true));
		this.coreContainerState.keySet().forEach(id -> this.removeContainerAndState(id, true, true));
		this.tempContainerState.clear();
		this.coreContainerState.clear();
		this.tempContainerRemoveFuture.clear();
		this.readyCoreContainer.clear();
		this.readyTempContainer.clear();
		this.taskQueue.clear();
	}

	/**
	 * 删除容器并清理状态
	 * @param containerId 容器 ID
	 * @param isCore 是否为核心容器
	 * @param isForce 是否强制停止
	 */
	private void removeContainerAndState(String containerId, boolean isCore, boolean isForce) {
		try {
			if (isCore) {
				// Remove core container
				State state = this.coreContainerState.replace(containerId, State.REMOVING);
				if (state == State.RUNNING) {
					if (isForce) {
						this.stopContainer(containerId);
					}
					else {
						throw new RuntimeException("Container is still Running!");
					}
				}
				this.removeContainer(containerId);
				this.coreContainerState.remove(containerId);
				this.currentCoreContainerSize.decrementAndGet();
				log.info("Core Container {} has been removed successfully", containerId);
			}
			else {
				// Remove temporary container
				State state = this.tempContainerState.replace(containerId, State.REMOVING);
				if (state == State.RUNNING) {
					if (isForce) {
						this.stopContainer(containerId);
					}
					else {
						throw new RuntimeException("Container is still Running!");
					}
				}
				this.removeContainer(containerId);
				this.tempContainerState.remove(containerId);
				this.tempContainerRemoveFuture.remove(containerId);
				this.currentTempContainerSize.decrementAndGet();
				log.info("Temp Container {} has been removed successfully", containerId);
			}
		}
		catch (Exception e) {
			log.error("Error when trying to remove a container, containerId: {}, info: {}", containerId, e.getMessage(),
					e);
		}
	}

	/**
	 * 注册定时删除临时容器的任务
	 */
	private Future<?> registerRemoveTempContainer(String containerId) {
		return consumerThreadPool.submit(() -> {
			try {
				Thread.sleep(this.properties.getKeepThreadAliveTime() * 1000L * 60L);
				if (this.tempContainerState.get(containerId) == State.RUNNING) {
					throw new InterruptedException("Container " + containerId + " is already running");
				}
			}
			catch (InterruptedException e) {
				// Cancel temporary container destruction thread due to container running
				log.debug("Interrupted while waiting for temp container to be removed, info: {}", e.getMessage());
				return;
			}
			this.removeContainerAndState(containerId, false, false);
		});
	}

	/**
	 * 使用核心容器执行任务
	 */
	private TaskResponse useCoreContainer(String containerId, TaskRequest request) {
		try {
			// Execute task
			this.coreContainerState.replace(containerId, State.RUNNING);
			TaskResponse resp = this.execTaskInContainer(request, containerId);
			// 如果运行代码任务时出现了异常，认为容器损坏，执行容器清除，并将当前任务放进队列里重新执行
			if (!resp.isSuccess() && !resp.executionSuccessButResultFailed()) {
				log.error("use core container failed, {}", resp.exceptionMsg());
				this.coreContainerState.replace(containerId, State.REMOVING);
				this.removeContainerAndState(containerId, true, true);
				return this.pushTaskQueue(request);
			}
			this.coreContainerState.replace(containerId, State.READY);
			// Put back into blocking queue
			this.readyCoreContainer.add(containerId);
			// Run tasks in task queue if any
			this.popTaskQueue();
			return resp;
		}
		catch (Exception e) {
			log.error("use core container failed, {}", e.getMessage(), e);
			return TaskResponse.exception(e.getMessage());
		}
	}

	/**
	 * 使用临时容器执行任务
	 */
	private TaskResponse useTempContainer(String containerId, TaskRequest request) {
		try {
			Future<?> future = this.tempContainerRemoveFuture.remove(containerId);
			// Cancel temporary container destruction thread
			if (future != null) {
				if (future.isDone()) {
					// Container is destroyed, reselect usage strategy
					log.debug("reselect strategy: {} ...", request.toString());
					return this.runTask(request);
				}
				future.cancel(true);
			}
			// Execute task
			this.tempContainerState.replace(containerId, State.RUNNING);
			TaskResponse resp = this.execTaskInContainer(request, containerId);
			// 如果运行代码任务时出现了异常，认为容器损坏，执行容器清除，并将当前任务放进队列里重新执行
			if (!resp.isSuccess() && !resp.executionSuccessButResultFailed()) {
				log.error("use temp container failed, {}", resp.exceptionMsg());
				this.tempContainerState.replace(containerId, State.REMOVING);
				this.removeContainerAndState(containerId, false, true);
				return this.pushTaskQueue(request);
			}
			this.tempContainerState.replace(containerId, State.READY);
			// Put back into blocking queue
			this.readyTempContainer.add(containerId);
			// Recreate temporary container destruction thread
			this.tempContainerRemoveFuture.put(containerId, this.registerRemoveTempContainer(containerId));
			// Run tasks in task queue if any
			this.popTaskQueue();
			return resp;
		}
		catch (Exception e) {
			log.error("use temp container failed, {}", e.getMessage(), e);
			return TaskResponse.exception(e.getMessage());
		}
	}

	/**
	 * 创建并使用核心容器执行任务
	 */
	private TaskResponse createAndUseCoreContainer(TaskRequest request) {
		String containerId;
		try {
			containerId = this.createNewContainer();
		}
		catch (Exception e) {
			log.error("create new container failed, {}", e.getMessage(), e);
			return TaskResponse.exception(e.getMessage());
		}
		// Record newly added container
		this.coreContainerState.put(containerId, State.READY);
		// Use container
		return this.useCoreContainer(containerId, request);
	}

	/**
	 * 创建并使用临时容器执行任务
	 */
	private TaskResponse createAndUseTempContainer(TaskRequest request) {
		String containerId;
		try {
			containerId = this.createNewContainer();
		}
		catch (Exception e) {
			log.error("create new container failed, {}", e.getMessage(), e);
			return TaskResponse.exception(e.getMessage());
		}
		// Record newly added container
		this.tempContainerState.put(containerId, State.READY);
		// Use container
		return this.useTempContainer(containerId, request);
	}

	/**
	 * 将任务推入队列等待执行
	 */
	private TaskResponse pushTaskQueue(TaskRequest request) throws ExecutionException, InterruptedException {
		FutureTask<CodePoolExecutorService.TaskResponse> ft = new FutureTask<>(() -> {
			log.info("Execute tasks in the BlockingQueue {} ...", request.toString());
			return this.runTask(request);
		});
		this.taskQueue.put(ft);

		if (!this.readyCoreContainer.isEmpty() || !this.readyTempContainer.isEmpty()) {
			this.popTaskQueue();
		}

		return ft.get();
	}

	/**
	 * 从任务队列取出任务并提交到线程池执行
	 */
	private void popTaskQueue() {
		FutureTask<CodePoolExecutorService.TaskResponse> future = this.taskQueue.poll();
		if (future == null) {
			return;
		}
		log.info("Get task from the BlockingQueue ...");
		this.consumerThreadPool.submit(future);
	}

	/**
	 * 运行任务，依次尝试使用空闲核心容器、空闲临时容器、创建新容器，最终进入任务队列等待
	 */
	@Override
	public TaskResponse runTask(TaskRequest request) {
		// Use available core container
		String freeCoreId = this.readyCoreContainer.poll();
		if (freeCoreId != null) {
			log.debug("Use free core container to run task {} ...", request.toString());
			return this.useCoreContainer(freeCoreId, request);
		}

		// Use available temporary container
		String freeTempId = this.readyTempContainer.poll();
		if (freeTempId != null) {
			log.debug("Use free temp container to run task {} ...", request.toString());
			return this.useTempContainer(freeTempId, request);
		}

		// Create new core container
		int currentCore;
		boolean useCoreContainer = true;
		do {
			currentCore = this.currentCoreContainerSize.get();
			if (currentCore >= properties.getCoreContainerNum()) {
				useCoreContainer = false;
				break;
			}
		}
		while (!this.currentCoreContainerSize.compareAndSet(currentCore, currentCore + 1));
		if (useCoreContainer) {
			log.debug("Create new core container to run task {} ...", request.toString());
			return this.createAndUseCoreContainer(request);
		}

		// Create new temporary container
		int currentTemp;
		boolean useTempContainer = true;
		do {
			currentTemp = this.currentTempContainerSize.get();
			if (currentTemp >= properties.getTempContainerNum()) {
				useTempContainer = false;
				break;
			}
		}
		while (!this.currentTempContainerSize.compareAndSet(currentTemp, currentTemp + 1));
		if (useTempContainer) {
			log.debug("Create new temp container to run task {} ...", request.toString());
			return this.createAndUseTempContainer(request);
		}

		// Put into task queue to wait
		try {
			log.debug("push task into BlockingQueue: {} ...", request.toString());
			return this.pushTaskQueue(request);
		}
		catch (Exception e) {
			log.error("An exception occurred while executing the task: {}", e.getMessage(), e);
			return TaskResponse.exception(e.getMessage());
		}
	}

	/**
	 * 递归删除临时目录
	 */
	protected void clearTempDir(Path tempDir) {
		try {
			Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
				@NotNull
				@Override
				public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
						throws IOException {
					Files.delete(file);
					return super.visitFile(file, attrs);
				}

				@NotNull
				@Override
				public FileVisitResult postVisitDirectory(@NotNull Path dir, @Nullable IOException exc)
						throws IOException {
					if (exc != null)
						throw exc;
					Files.delete(dir);
					return super.postVisitDirectory(dir, exc);
				}
			});
			log.info("Temp directory has been deleted.");
		}
		catch (Exception e) {
			log.warn("Exception in clean temp directory: {}", e.getMessage());
		}
	}

	/**
	 * 生成唯一的容器名称
	 */
	protected String generateContainerName() {
		return this.properties.getContainerNamePrefix() + "_" + System.currentTimeMillis() + "_"
				+ Thread.currentThread().getName();
	}

}
