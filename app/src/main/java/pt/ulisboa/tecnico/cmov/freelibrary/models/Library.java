package pt.ulisboa.tecnico.cmov.freelibrary.models;

public class Library {
    public int id;
    public String name;
    public double latitude;
    public double longitude;

    public String imageUrl;
    public int distanceFromCurrentLocation;

    public Library(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public String getName() {
        return name;
    }

    public String getImageUrl() { return "https://librarist.evandro.pt" + imageUrl; }

    public int getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public void setDistanceFromCurrentLocation(int distanceFromCurrentLocation) {
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
    }
}