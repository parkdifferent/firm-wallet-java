package com.auticuro.firmwallet.storage.auticuro;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RocksDBStore {
    private RocksDB db;
    private final String dbPath;
    private final List<ColumnFamilyHandle> columnFamilyHandles;
    private WriteOptions writeOptions;
    private ReadOptions readOptions;

    public RocksDBStore(@Value("${rocksdb.path}") String dbPath) {
        this.dbPath = dbPath;
        this.columnFamilyHandles = new ArrayList<>();
    }

    @PostConstruct
    public void init() throws RocksDBException {
        RocksDB.loadLibrary();
        
        File dir = new File(dbPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            DBOptions options = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true);

            writeOptions = new WriteOptions();
            writeOptions.setSync(true);
            
            readOptions = new ReadOptions();
            readOptions.setVerifyChecksums(true);

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));

            db = RocksDB.open(options, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            log.info("RocksDB initialized at {}", dbPath);
        } catch (RocksDBException e) {
            log.error("Failed to initialize RocksDB", e);
            throw e;
        }
    }

    @PreDestroy
    public void close() {
        if (writeOptions != null) {
            writeOptions.close();
        }
        if (readOptions != null) {
            readOptions.close();
        }
        columnFamilyHandles.forEach(ColumnFamilyHandle::close);
        if (db != null) {
            db.close();
        }
        log.info("RocksDB closed");
    }

    public void put(String key, byte[] value) throws RocksDBException {
        db.put(writeOptions, key.getBytes(), value);
    }

    public Optional<byte[]> get(String key) throws RocksDBException {
        byte[] value = db.get(readOptions, key.getBytes());
        return Optional.ofNullable(value);
    }

    public void delete(String key) throws RocksDBException {
        db.delete(writeOptions, key.getBytes());
    }

    public void snapshot(String path) throws RocksDBException {
        Checkpoint checkpoint = Checkpoint.create(db);
        checkpoint.createCheckpoint(path);
    }

    public void recover(String path) throws RocksDBException, IOException {
        // Close the current database connection
        close();

        // Move the checkpoint to the database path, replacing existing files
        Path sourcePath = Paths.get(path);
        Path targetPath = Paths.get(dbPath);

        // Ensure the target directory exists
        Files.createDirectories(targetPath.getParent());

        // Move the files, replacing existing files
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Reinitialize the database
        init();

    }
}
