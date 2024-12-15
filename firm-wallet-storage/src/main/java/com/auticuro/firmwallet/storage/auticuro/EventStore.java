package com.auticuro.firmwallet.storage.auticuro;

import com.auticuro.firmwallet.common.event.Event;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public interface EventStore {
    /**
     * Append a new event to the store
     * @param event The event to append
     */
    void append(Event event);
    
    /**
     * Get events from a specific sequence number
     * @param fromSequence The sequence number to start from
     * @param maxCount Maximum number of events to return
     * @return List of events
     */
    List<Event> getEvents(long fromSequence, int maxCount);
    
    /**
     * Get the last sequence number in the store
     * @return The last sequence number
     */
    long getLastSequence();
    
    /**
     * Delete all events up to and including the given sequence number
     * @param sequence The sequence number up to which events should be deleted
     */
    void deleteEventsUpTo(long sequence);
}
