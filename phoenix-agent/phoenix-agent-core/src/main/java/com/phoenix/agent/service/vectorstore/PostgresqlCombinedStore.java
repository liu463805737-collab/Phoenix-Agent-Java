package com.phoenix.agent.service.vectorstore;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.graph.store.NamespaceListRequest;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.StoreSearchRequest;
import com.alibaba.cloud.ai.graph.store.StoreSearchResult;
import com.alibaba.cloud.ai.graph.store.stores.BaseStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.agent.model.CombinedStore;
import com.phoenix.agent.service.store.CombinedStoreService;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class PostgresqlCombinedStore extends BaseStore {
    private final CombinedStoreService storeService;

    private final ObjectMapper objectMapper;

    private final String tableName;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor with default table name.
     *
     * @param storeService database data source
     */
    public PostgresqlCombinedStore(CombinedStoreService storeService) {
        this(storeService, "tbl_vector_store_combined");
    }

    /**
     * Constructor with custom table name.
     *
     * @param storeService database data source
     * @param tableName    table name
     */
    public PostgresqlCombinedStore(CombinedStoreService storeService, String tableName) {
        this.storeService = storeService;
        this.tableName = tableName;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        initializeTable();
    }

    @Override
    public void putItem(StoreItem item) {
        validatePutItem(item);

        lock.writeLock().lock();
        try {
            String itemId = createItemId(item.getNamespace(), item.getKey());
            String namespaceJson = objectMapper.writeValueAsString(item.getNamespace());
            String valueJson = objectMapper.writeValueAsString(item.getValue());
            var combinedStore = CombinedStore.builder().id(itemId)
                    .namespace(namespaceJson)
                    .keyName(item.getKey())
                    .valueJson(valueJson)
                    .createdAt(new Timestamp(item.getCreatedAt()))
                    .updatedAt(new Timestamp(item.getUpdatedAt())).build();
            storeService.saveOrUpdate(combinedStore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store item in database", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<StoreItem> getItem(List<String> namespace, String key) {
        validateGetItem(namespace, key);
        lock.readLock().lock();
        try {
            String itemId = createItemId(namespace, key);
            CombinedStore combinedStore = storeService.getById(itemId);
            if (combinedStore != null){
                return Optional.of(resultSetToStoreItem(combinedStore));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve item from database", e);
        } finally {
            lock.readLock().unlock();
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteItem(List<String> namespace, String key) {
        validateDeleteItem(namespace, key);

        lock.writeLock().lock();
        try {
            String itemId = createItemId(namespace, key);
            return storeService.removeById(itemId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete item from database", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public StoreSearchResult searchItems(StoreSearchRequest searchRequest) {
        validateSearchItems(searchRequest);

        lock.readLock().lock();
        try {
            List<StoreItem> allItems = getAllItems();

            // Apply filters
            List<StoreItem> filteredItems = allItems.stream()
                    .filter(item -> matchesSearchCriteria(item, searchRequest))
                    .collect(Collectors.toList());

            // Sort items
            if (!searchRequest.getSortFields().isEmpty()) {
                filteredItems.sort(createComparator(searchRequest));
            }

            long totalCount = filteredItems.size();

            // Apply pagination
            int offset = searchRequest.getOffset();
            int limit = searchRequest.getLimit();

            if (offset >= filteredItems.size()) {
                return StoreSearchResult.of(Collections.emptyList(), totalCount, offset, limit);
            }

            int endIndex = Math.min(offset + limit, filteredItems.size());
            List<StoreItem> resultItems = filteredItems.subList(offset, endIndex);

            return StoreSearchResult.of(resultItems, totalCount, offset, limit);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> listNamespaces(NamespaceListRequest namespaceRequest) {
        validateListNamespaces(namespaceRequest);

        lock.readLock().lock();
        try {
            Set<String> namespaceSet = new HashSet<>();
            List<String> prefixFilter = namespaceRequest.getNamespace();

            List<StoreItem> allItems = getAllItems();

            for (StoreItem item : allItems) {
                List<String> itemNamespace = item.getNamespace();

                // Check if namespace starts with prefix filter
                if (!prefixFilter.isEmpty() && !startsWithPrefix(itemNamespace, prefixFilter)) {
                    continue;
                }

                // Generate all possible namespace paths up to maxDepth
                int maxDepth = namespaceRequest.getMaxDepth();
                int depth = (maxDepth == -1) ? itemNamespace.size() : Math.min(maxDepth, itemNamespace.size());

                for (int i = 1; i <= depth; i++) {
                    String namespacePath = String.join("/", itemNamespace.subList(0, i));
                    namespaceSet.add(namespacePath);
                }
            }

            List<String> namespaces = new ArrayList<>(namespaceSet);
            Collections.sort(namespaces);

            // Apply pagination
            int offset = namespaceRequest.getOffset();
            int limit = namespaceRequest.getLimit();

            if (offset >= namespaces.size()) {
                return Collections.emptyList();
            }

            int endIndex = Math.min(offset + limit, namespaces.size());
            return namespaces.subList(offset, endIndex);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        storeService.remove(QueryWrapper.create());
    }

    @Override
    public long size() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return storeService.count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Initialize database table.
     */
    private void initializeTable() {
        storeService.initTable();
    }

    /**
     * Create item ID from namespace and key.
     *
     * @param namespace namespace
     * @param key       key
     * @return item ID
     */
    private String createItemId(List<String> namespace, String key) {
        return createStoreKey(namespace, key);
    }

    /**
     * Get all items from database.
     *
     * @return list of all items
     */
    private List<StoreItem> getAllItems()  {
        List<StoreItem> items = new ArrayList<>();
        List<CombinedStore> combinedStores = storeService.list();
        try {
            if (CollUtil.isNotEmpty(combinedStores)) {
                for (CombinedStore store : combinedStores) {
                    items.add(resultSetToStoreItem(store));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all items from database", e);
        }
        return items;
    }

    /**
     * Convert ResultSet to StoreItem.
     *
     * @param store result set
     * @return StoreItem
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    private StoreItem resultSetToStoreItem(CombinedStore store) throws Exception {
        String namespaceJson = store.getNamespace();
        String key = store.getKeyName();
        String valueJson = store.getValueJson();
        Timestamp createdAt = store.getCreatedAt();
        Timestamp updatedAt = store.getUpdatedAt();
        List<String> namespace = objectMapper.readValue(namespaceJson, List.class);
        Map<String, Object> value = objectMapper.readValue(valueJson, Map.class);
        return new StoreItem(namespace, key, value, createdAt.getTime(), updatedAt.getTime());
    }
}
