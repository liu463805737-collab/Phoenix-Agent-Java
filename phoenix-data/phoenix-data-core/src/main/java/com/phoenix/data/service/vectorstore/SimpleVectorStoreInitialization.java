//package com.phoenix.data.service.vectorstore;
//
//import com.phoenix.data.properties.DataAgentProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.vectorstore.SimpleVectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
///**
// * 向量存储初始化服务，负责应用启动时加载持久化的向量数据，关闭时保存向量数据到本地文件。
// *
// * @author David Yu
// */
//@Slf4j
//@RequiredArgsConstructor
//public class SimpleVectorStoreInitialization implements ApplicationRunner, DisposableBean {
//	@Autowired
//	@Qualifier("simpleVectorStore")
//	private PgVectorStore vectorStore;
//	@Autowired
//	private DataAgentProperties properties;
//
//	public void load() {
//		File file = new File(properties.getVectorStore().getFilePath());
//
//		if (!file.exists()) {
//			log.info("No locally serialized vector database file was found.");
//			return;
//		}
//
//		try {
//			vectorStore.load(file);
//		}
//		catch (Throwable throwable) {
//			log.error("Failed to load the locally serialized vector database file.", throwable);
//		}
//	}
//
//	public void save() {
//		log.info("Serialize the vector database to a local file.");
//		Path path = Paths.get(properties.getVectorStore().getFilePath());
//
//		try {
//			Files.createDirectories(path.getParent());
//
//			if (!Files.exists(path)) {
//				Files.createFile(path);
//			}
//
//			vectorStore.save(path.toFile());
//		}
//		catch (Throwable t) {
//			log.error("An exception occurred while serializing the vector database to a local file.", t);
//		}
//	}
//
//	@Override
//	public void run(ApplicationArguments args) throws Exception {
//		this.load();
//	}
//
//	@Override
//	public void destroy() {
//		this.save();
//	}
//
//}
