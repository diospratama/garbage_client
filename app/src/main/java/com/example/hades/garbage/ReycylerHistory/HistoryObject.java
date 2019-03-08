package com.example.hades.garbage.ReycylerHistory;

public class HistoryObject {
    private String rideId;
    private String time;
    private String distance;
    private String costTotal;
    private String status_transaksi;



    private String profilImageUrl;



    public HistoryObject(String rideId, String time, String distance,String costTotal,String status_transaksi,String profilImageUrl){
        this.rideId = rideId;
        this.time = time;
        this.distance=distance;
        this.costTotal=costTotal;
        this.status_transaksi=status_transaksi;
        this.profilImageUrl=profilImageUrl;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() { return distance; }

    public void setDistance(String distance) { this.distance = distance;}

    public String getCostTotal() {return costTotal;}

    public void setCostTotal(String costTotal) {this.costTotal = costTotal;}

    public String getStatus_transaksi() {return status_transaksi; }

    public void setStatus_transaksi(String status_transaksi) {this.status_transaksi = status_transaksi;}

    public String getProfilImageUrl() {
        return profilImageUrl;
    }

    public void setProfilImageUrl(String profilImageUrl) {
        this.profilImageUrl = profilImageUrl;
    }


}
