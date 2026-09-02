package com.farmconnect.service;

import com.farmconnect.util.GeoDistanceCalculator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LocationService {

    // Common Indian district coordinate center points (latitude, longitude)
    private final Map<String, double[]> districtCoordinates = new HashMap<>();

    public LocationService() {
        initDistrictCoordinates();
    }

    private void initDistrictCoordinates() {
        // Tamil Nadu
        districtCoordinates.put("tiruvallur", new double[]{13.1438, 79.9080});
        districtCoordinates.put("chennai", new double[]{13.0827, 80.2707});
        districtCoordinates.put("kancheepuram", new double[]{12.8342, 79.7036});
        districtCoordinates.put("vellore", new double[]{12.9165, 79.1325});
        districtCoordinates.put("coimbatore", new double[]{11.0168, 76.9558});
        districtCoordinates.put("madurai", new double[]{9.9252, 78.1198});
        districtCoordinates.put("salem", new double[]{11.6643, 78.1460});
        districtCoordinates.put("tiruchirappalli", new double[]{10.7905, 78.7047});
        districtCoordinates.put("thanjavur", new double[]{10.7870, 79.1378});
        districtCoordinates.put("erode", new double[]{11.3410, 77.7172});
        districtCoordinates.put("tirunelveli", new double[]{8.7139, 77.7567});
        districtCoordinates.put("dharmapuri", new double[]{12.1211, 78.1582});
        districtCoordinates.put("krishnagiri", new double[]{12.5186, 78.2137});
        districtCoordinates.put("cuddalore", new double[]{11.7480, 79.7714});
        districtCoordinates.put("villupuram", new double[]{11.9401, 79.4861});
        districtCoordinates.put("dindigul", new double[]{10.3673, 77.9803});

        // Karnataka
        districtCoordinates.put("bengaluru", new double[]{12.9716, 77.5946});
        districtCoordinates.put("bangalore", new double[]{12.9716, 77.5946});
        districtCoordinates.put("mysuru", new double[]{12.2958, 76.6394});
        districtCoordinates.put("mysore", new double[]{12.2958, 76.6394});
        districtCoordinates.put("kolar", new double[]{13.1367, 78.1291});
        districtCoordinates.put("chikkaballapur", new double[]{13.4325, 77.7275});
        districtCoordinates.put("tumakuru", new double[]{13.3409, 77.1010});
        districtCoordinates.put("belagavi", new double[]{15.8497, 74.4977});
        districtCoordinates.put("hubballi", new double[]{15.3647, 75.1240});

        // Andhra Pradesh & Telangana
        districtCoordinates.put("chittoor", new double[]{13.2172, 79.1003});
        districtCoordinates.put("tirupati", new double[]{13.6288, 79.4192});
        districtCoordinates.put("nellore", new double[]{14.4426, 79.9865});
        districtCoordinates.put("vijayawada", new double[]{16.5062, 80.6480});
        districtCoordinates.put("guntur", new double[]{16.3067, 80.4365});
        districtCoordinates.put("visakhapatnam", new double[]{17.6868, 83.2185});
        districtCoordinates.put("hyderabad", new double[]{17.3850, 78.4867});
        districtCoordinates.put("kurnool", new double[]{15.8281, 78.0373});

        // Maharashtra & others
        districtCoordinates.put("pune", new double[]{18.5204, 73.8567});
        districtCoordinates.put("mumbai", new double[]{19.0760, 72.8777});
        districtCoordinates.put("nashik", new double[]{19.9975, 73.7898});
        districtCoordinates.put("nagpur", new double[]{21.1458, 79.0882});
        districtCoordinates.put("aurangabad", new double[]{19.8762, 75.3433});
        districtCoordinates.put("ahmednagar", new double[]{19.0952, 74.7496});
        districtCoordinates.put("delhi", new double[]{28.7041, 77.1025});
        districtCoordinates.put("jaipur", new double[]{26.9124, 75.7873});
        districtCoordinates.put("lucknow", new double[]{26.8467, 80.9462});
    }

    public double[] getCoordinates(String districtOrLocation) {
        if (districtOrLocation == null || districtOrLocation.isBlank()) {
            return new double[]{13.0827, 80.2707}; // Default fallback
        }
        String key = districtOrLocation.trim().toLowerCase();
        for (Map.Entry<String, double[]> entry : districtCoordinates.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new double[]{13.0827, 80.2707};
    }

    public double calculateDistanceBetween(String loc1, String loc2, Double lat1, Double lon1, Double lat2, Double lon2) {
        double pLat1 = (lat1 != null && lat1 != 0) ? lat1 : getCoordinates(loc1)[0];
        double pLon1 = (lon1 != null && lon1 != 0) ? lon1 : getCoordinates(loc1)[1];
        double pLat2 = (lat2 != null && lat2 != 0) ? lat2 : getCoordinates(loc2)[0];
        double pLon2 = (lon2 != null && lon2 != 0) ? lon2 : getCoordinates(loc2)[1];

        return GeoDistanceCalculator.calculateDistance(pLat1, pLon1, pLat2, pLon2);
    }
}
