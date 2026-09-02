package com.farmconnect.service;

import com.farmconnect.entity.MarketPrice;
import com.farmconnect.repository.MarketPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Service
public class MarketPriceService {

    private static final Logger log = LoggerFactory.getLogger(MarketPriceService.class);

    private final MarketPriceRepository marketPriceRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${farmconnect.market.api.url:https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070}")
    private String marketApiUrl;

    @Value("${farmconnect.market.api.key:}")
    private String marketApiKey;

    public MarketPriceService(MarketPriceRepository marketPriceRepository) {
        this.marketPriceRepository = marketPriceRepository;
    }

    public List<MarketPrice> getAllMarketPrices() {
        return marketPriceRepository.findAllByOrderByDateDescCreatedAtDesc();
    }

    public List<MarketPrice> searchMarketPrices(String cropName, String district, String state) {
        return marketPriceRepository.searchMarketPrices(cropName, district, state);
    }

    public Optional<MarketPrice> getLatestPriceForCrop(String cropName) {
        return marketPriceRepository.findFirstByCropNameIgnoreCaseOrderByDateDesc(cropName);
    }

    @Transactional
    public MarketPrice saveMarketPrice(MarketPrice marketPrice) {
        return marketPriceRepository.save(marketPrice);
    }

    /**
     * Compares a farmer's expected crop price with prevailing market modal price.
     */
    public Map<String, Object> compareWithMarketPrice(String cropName, Double expectedPrice) {
        Map<String, Object> result = new HashMap<>();
        Optional<MarketPrice> latest = getLatestPriceForCrop(cropName);

        if (latest.isEmpty() || expectedPrice == null) {
            result.put("hasComparison", false);
            result.put("message", "No market price data currently available for " + cropName + ".");
            return result;
        }

        MarketPrice mp = latest.get();
        double marketPrice = mp.getPrice();
        double diff = expectedPrice - marketPrice;
        double diffPercent = (diff / marketPrice) * 100.0;

        result.put("hasComparison", true);
        result.put("cropName", cropName);
        result.put("marketPrice", marketPrice);
        result.put("expectedPrice", expectedPrice);
        result.put("marketName", mp.getMarketName());
        result.put("district", mp.getDistrict());
        result.put("date", mp.getDate());
        result.put("source", mp.getSource());
        result.put("difference", Math.round(diff * 10.0) / 10.0);
        result.put("differencePercent", Math.round(diffPercent * 10.0) / 10.0);

        if (Math.abs(diff) <= 1.5) {
            result.put("analysis", "Competitive with current market rates.");
            result.put("badgeClass", "bg-success");
        } else if (diff > 0) {
            result.put("analysis", "?" + Math.round(diff * 10.0) / 10.0 + "/kg higher than prevailing market price (" + mp.getMarketName() + ").");
            result.put("badgeClass", "bg-warning text-dark");
        } else {
            result.put("analysis", "?" + Math.round(Math.abs(diff) * 10.0) / 10.0 + "/kg lower than market price. Very attractive for buyers!");
            result.put("badgeClass", "bg-info text-dark");
        }

        return result;
    }

    /**
     * Attempts to refresh market price data from the configured external Government API.
     * If external API is unreachable or key is unset, handles error gracefully without fabricating data.
     */
    public boolean refreshFromExternalSource() {
        if (marketApiKey == null || marketApiKey.isBlank()) {
            log.info("External market API key is not configured. Retaining currently stored database records.");
            return false;
        }

        try {
            String url = marketApiUrl + "?api-key=" + marketApiKey + "&format=json&limit=50";
            log.info("Fetching market prices from external endpoint: {}", marketApiUrl);
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("records")) {
                List<?> records = (List<?>) response.get("records");
                for (Object item : records) {
                    if (item instanceof Map<?, ?> map) {
                        String commodity = String.valueOf(map.get("commodity"));
                        String market = String.valueOf(map.get("market"));
                        String district = String.valueOf(map.get("district"));
                        String state = String.valueOf(map.get("state"));
                        String modalPriceStr = String.valueOf(map.get("modal_price"));

                        try {
                            double modalPrice = Double.parseDouble(modalPriceStr);
                            // Convert quintal to kg if price is > 500
                            double pricePerKg = modalPrice > 500 ? modalPrice / 100.0 : modalPrice;

                            MarketPrice mp = new MarketPrice(
                                    commodity,
                                    market,
                                    district,
                                    state,
                                    pricePerKg,
                                    pricePerKg * 0.9,
                                    pricePerKg * 1.1,
                                    "kg",
                                    LocalDate.now(),
                                    "Data.gov.in Agmarknet API"
                            );
                            marketPriceRepository.save(mp);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                return true;
            }
        } catch (Exception ex) {
            log.warn("External Market API request failed: {}", ex.getMessage());
        }
        return false;
    }
}
