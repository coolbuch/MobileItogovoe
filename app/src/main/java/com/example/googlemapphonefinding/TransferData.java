package com.example.googlemapphonefinding;

public class TransferData
{
    private String uuid;
    private double lat, lng;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getUuid() {
        return uuid;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
