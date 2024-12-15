package com.auticuro.firmwallet.storage.auticuro;

import com.auticuro.firmwallet.common.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RocksDBEventStore implements EventStore {
    private final String dbPath;
    private final ObjectMapper objectMapper;
    private RocksDB db;
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    
    public RocksDBEventStore(@Value("${rocksdb.event.path}") String dbPath, ObjectMapper objectMapper) {
        this.dbPath = dbPath;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            // Ensure directory exists
            File directory = new File(dbPath);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Failed to create directory: " + dbPath);
                }
            }
            log.info("RocksDB event store path: {}", dbPath);

            RocksDB.loadLibrary();
            Options options = new Options()
                    .setCreateIfMissing(true)
                    .setWriteBufferSize(64 * 1024 * 1024)
                    .setMaxWriteBufferNumber(4)
                    .setTargetFileSizeBase(64 * 1024 * 1024);

            db = RocksDB.open(options, dbPath);

            // Initialize sequence number
            byte[] lastSeqBytes = db.get("last_sequence".getBytes());
            if (lastSeqBytes != null) {
                sequenceNumber.set(Long.parseLong(new String(lastSeqBytes)));
            }
        } catch (Exception e) {
            log.error("Failed to initialize RocksDB", e);
            throw new RuntimeException("Could not initialize RocksDB at path: " + dbPath, e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (db != null) {
            db.close();
        }
    }
    
    @Override
    public void append(Event event) {
        try {
            long sequence = sequenceNumber.incrementAndGet();
            event.setSequence(sequence);
            
            // Convert event to JSON
            byte[] eventBytes = objectMapper.writeValueAsBytes(event);
            
            // Store event
            db.put(String.valueOf(sequence).getBytes(), eventBytes);
            
            // Update last sequence
            db.put("last_sequence".getBytes(), String.valueOf(sequence).getBytes());
            
            log.debug("Appended event with sequence {}: {}", sequence, event);
        } catch (Exception e) {
            log.error("Failed to append event", e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<Event> getEvents(long fromSequence, int maxCount) {
        List<Event> events = new ArrayList<>();
        try {
            RocksIterator iterator = db.newIterator();
            iterator.seek(String.valueOf(fromSequence).getBytes());
            
            int count = 0;
            while (iterator.isValid() && count < maxCount) {
                byte[] valueBytes = iterator.value();
                Event event = objectMapper.readValue(valueBytes, Event.class);
                events.add(event);
                
                iterator.next();
                count++;
            }
            
            iterator.close();
            log.debug("Retrieved {} events from sequence {}", events.size(), fromSequence);
        } catch (Exception e) {
            log.error("Failed to get events", e);
            throw new RuntimeException(e);
        }
        return events;
    }
    
    @Override
    public long getLastSequence() {
        return sequenceNumber.get();
    }
    
    @Override
    public void deleteEventsUpTo(long sequence) {
        try {
            RocksIterator iterator = db.newIterator();
            iterator.seekToFirst();
            
            while (iterator.isValid()) {
                String key = new String(iterator.key());
                long eventSequence = Long.parseLong(key);
                if (eventSequence > sequence) {
                    break;
                }
                
                db.delete(iterator.key());
                iterator.next();
            }
            
            iterator.close();
            log.debug("Deleted events up to sequence {}", sequence);
        } catch (Exception e) {
            log.error("Failed to delete events", e);
            throw new RuntimeException(e);
        }
    }
}
