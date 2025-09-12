package com.trip.model;

public class TripProposal {
    private int id;
    private String title;
    private String stage;
    private boolean isArchived;
    private Integer finalizedPlaceId;
    
    public TripProposal() {}
    
    public TripProposal(int id, String title, String stage, boolean isArchived, Integer finalizedPlaceId) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.isArchived = isArchived;
        this.finalizedPlaceId = finalizedPlaceId;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
    
    public Integer getFinalizedPlaceId() { return finalizedPlaceId; }
    public void setFinalizedPlaceId(Integer finalizedPlaceId) { this.finalizedPlaceId = finalizedPlaceId; }
}
