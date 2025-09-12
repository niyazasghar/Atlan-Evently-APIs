package atlan.evently.atlan.analytics.view;

// src/main/java/com/evently/analytics/view/EventStatsView.java

public class EventStatsView {
    private Long eventId;
    private String eventName;
    private int capacity;
    private long totalBookings;
    private double capacityUtilization;

    public EventStatsView(Long eventId, String eventName, int capacity, long totalBookings, double capacityUtilization) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.capacity = capacity;
        this.totalBookings = totalBookings;
        this.capacityUtilization = capacityUtilization;
    }

    public Long getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public int getCapacity() { return capacity; }
    public long getTotalBookings() { return totalBookings; }
    public double getCapacityUtilization() { return capacityUtilization; }
}
