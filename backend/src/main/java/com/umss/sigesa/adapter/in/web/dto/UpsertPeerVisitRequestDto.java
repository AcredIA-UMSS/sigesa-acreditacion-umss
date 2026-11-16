package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpsertPeerVisitRequestDto {

    private LocalDate startsOn;
    private LocalDate endsOn;
    private String venue;
    private String room;
    private String status;
    private String notes;
    private List<PeerVisitItemRequestDto> items = new ArrayList<>();

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PeerVisitItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<PeerVisitItemRequestDto> items) {
        this.items = items;
    }
}
