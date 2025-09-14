package atlan.evently.atlan.analytics.pro;

public interface VenueAggregateProjection {
    String getVenue();
    long getConfirmedCount();
    long getCancelledCount();
    int getTotalCapacity();
    double getUtilizationPercent();
}