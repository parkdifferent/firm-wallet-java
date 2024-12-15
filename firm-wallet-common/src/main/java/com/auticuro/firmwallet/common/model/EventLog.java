package com.auticuro.firmwallet.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Base64;

@Data
@Entity
@Table(name = "event_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EventLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String eventId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(columnDefinition = "BLOB")
    private byte[] eventData;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private int version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.createdAt = timestamp;
    }

    public byte[] toByteArray() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
            return Base64.getEncoder().encode(baos.toByteArray());
        } catch (IOException e) {
            log.error("Failed to serialize EventLog", e);
            throw new RuntimeException("Failed to serialize EventLog", e);
        }
    }

    public static EventLog fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(bytes));
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (EventLog) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to deserialize EventLog", e);
            throw new RuntimeException("Failed to deserialize EventLog", e);
        }
    }
}
