package com.camping.service;

import com.camping.dto.BudgetDTO;
import com.camping.model.BudgetPrediction;
import com.camping.repository.BudgetPredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetPredictionService {

    private final BudgetPredictionRepository predictionRepository;

    // Base daily costs per person in TND
    private static final Map<String, Double> BASE_ACCOMMODATION_COSTS = Map.of(
        "BUILDING_COASTAL", 35.0,
        "BUILDING_FOREST", 28.0,
        "BUILDING_DESERT", 32.0,
        "TENT_COASTAL", 18.0,
        "TENT_FOREST", 15.0,
        "TENT_DESERT", 12.0
    );

    private static final Map<String, Double> GOVERNORATE_COST_INDEX = new HashMap<>() {{
        put("Tunis", 1.3); put("Ariana", 1.2); put("Ben Arous", 1.2); put("Manouba", 1.0);
        put("Bizerte", 1.1); put("Nabeul", 1.15); put("Zaghouan", 0.95); put("Beja", 0.9);
        put("Jendouba", 0.88); put("Le Kef", 0.87); put("Siliana", 0.85); put("Sousse", 1.2);
        put("Monastir", 1.2); put("Mahdia", 1.1); put("Sfax", 1.05); put("Kairouan", 0.9);
        put("Kasserine", 0.85); put("Sidi Bouzid", 0.83); put("Gabès", 0.95); put("Médenine", 1.0);
        put("Tataouine", 0.95); put("Gafsa", 0.88); put("Tozeur", 1.0); put("Kébili", 0.92);
    }};

    public BudgetDTO.PredictionResponse predict(BudgetDTO.PredictionRequest req, String userId) {
        String season = getSeason(req.getStartDate() != null ? req.getStartDate() : LocalDate.now());
        double seasonMultiplier = getSeasonMultiplier(season, req.getSiteType());
        double govIndex = GOVERNORATE_COST_INDEX.getOrDefault(req.getGovernorate(), 1.0);

        // Accommodation cost
        String accKey = req.getAccommodationType() + "_" + req.getSiteType();
        double baseAccom = BASE_ACCOMMODATION_COSTS.getOrDefault(accKey, 20.0);
        double accommodationTotal = baseAccom * req.getNumberOfPersons() * req.getNumberOfDays() * govIndex * seasonMultiplier;

        // Food cost (30-45 TND/person/day depending on region)
        double foodPerDay = 35.0 * govIndex;
        double foodTotal = foodPerDay * req.getNumberOfPersons() * req.getNumberOfDays();

        // Transport (estimate based on distance from Tunis)
        double transportCost = estimateTransport(req.getGovernorate(), req.getNumberOfPersons());

        // Equipment rental estimate (if they need basic kit)
        double equipmentCost = estimateEquipment(req.getSiteType(), req.getNumberOfDays(), req.getNumberOfPersons());

        // Misc (entrance fees, activities)
        double miscCost = estimateMisc(req.getSiteType(), req.getNumberOfDays(), req.getNumberOfPersons());

        double total = accommodationTotal + foodTotal + transportCost + equipmentCost + miscCost;
        double variation = total * 0.15;

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Accommodation", Math.round(accommodationTotal * 100.0) / 100.0);
        breakdown.put("Food & Water", Math.round(foodTotal * 100.0) / 100.0);
        breakdown.put("Transport", Math.round(transportCost * 100.0) / 100.0);
        breakdown.put("Equipment", Math.round(equipmentCost * 100.0) / 100.0);
        breakdown.put("Activities & Misc", Math.round(miscCost * 100.0) / 100.0);

        List<String> essential = getEssentialItems(req.getSiteType());
        List<String> optional = getOptionalItems(req.getSiteType(), season);
        List<String> tips = getTips(req.getSiteType(), season, req.getGovernorate());
        String weatherAlert = getWeatherAlert(req.getSiteType(), season);

        // Simulate weather for the region
        double[] weather = getTypicalWeather(req.getGovernorate(), season);

        BudgetDTO.PredictionResponse response = BudgetDTO.PredictionResponse.builder()
                .governorate(req.getGovernorate())
                .siteType(req.getSiteType())
                .numberOfPersons(req.getNumberOfPersons())
                .numberOfDays(req.getNumberOfDays())
                .predictedBudget(Math.round(total * 100.0) / 100.0)
                .budgetMin(Math.round((total - variation) * 100.0) / 100.0)
                .budgetMax(Math.round((total + variation) * 100.0) / 100.0)
                .budgetBreakdown(breakdown)
                .essentialItems(essential)
                .optionalItems(optional)
                .tips(tips)
                .weatherAlert(weatherAlert)
                .weatherSummary(getWeatherSummary(req.getGovernorate(), season))
                .temperature(weather[0])
                .humidity(weather[1])
                .season(season)
                .build();

        // Save prediction
        if (userId != null) {
            BudgetPrediction saved = BudgetPrediction.builder()
                    .userId(userId)
                    .governorate(req.getGovernorate())
                    .siteType(req.getSiteType())
                    .numberOfPersons(req.getNumberOfPersons())
                    .numberOfDays(req.getNumberOfDays())
                    .startDate(req.getStartDate())
                    .season(season)
                    .accommodationType(req.getAccommodationType())
                    .predictedBudget(response.getPredictedBudget())
                    .budgetMin(response.getBudgetMin())
                    .budgetMax(response.getBudgetMax())
                    .budgetBreakdown(breakdown)
                    .essentialItems(essential)
                    .optionalItems(optional)
                    .tips(tips)
                    .temperature(weather[0])
                    .humidity(weather[1])
                    .build();
            predictionRepository.save(saved);
        }

        return response;
    }

    private String getSeason(LocalDate date) {
        Month m = date.getMonth();
        if (m == Month.DECEMBER || m == Month.JANUARY || m == Month.FEBRUARY) return "WINTER";
        if (m == Month.MARCH || m == Month.APRIL || m == Month.MAY) return "SPRING";
        if (m == Month.JUNE || m == Month.JULY || m == Month.AUGUST) return "SUMMER";
        return "AUTUMN";
    }

    private double getSeasonMultiplier(String season, String siteType) {
        if ("COASTAL".equals(siteType)) {
            return switch (season) {
                case "SUMMER" -> 1.5;
                case "SPRING", "AUTUMN" -> 1.1;
                default -> 0.85;
            };
        }
        if ("DESERT".equals(siteType)) {
            return switch (season) {
                case "WINTER", "SPRING", "AUTUMN" -> 1.3;
                case "SUMMER" -> 0.7; // very hot, fewer tourists
                default -> 1.0;
            };
        }
        return switch (season) {
            case "SPRING", "AUTUMN" -> 1.2;
            case "SUMMER" -> 1.1;
            default -> 0.9;
        };
    }

    private double estimateTransport(String governorate, int persons) {
        Map<String, Double> distances = Map.ofEntries(
            Map.entry("Tunis", 0.0), Map.entry("Ariana", 15.0), Map.entry("Manouba", 20.0),
            Map.entry("Ben Arous", 25.0), Map.entry("Bizerte", 65.0), Map.entry("Nabeul", 80.0),
            Map.entry("Beja", 110.0), Map.entry("Jendouba", 160.0), Map.entry("Le Kef", 175.0),
            Map.entry("Sousse", 140.0), Map.entry("Monastir", 160.0), Map.entry("Mahdia", 200.0),
            Map.entry("Sfax", 270.0), Map.entry("Kasserine", 250.0), Map.entry("Gabès", 360.0),
            Map.entry("Médenine", 430.0), Map.entry("Tataouine", 500.0), Map.entry("Tozeur", 450.0),
            Map.entry("Kébili", 430.0), Map.entry("Gafsa", 360.0)
        );
        double dist = distances.getOrDefault(governorate, 150.0);
        // 0.35 TND/km fuel cost, shared among persons (assuming car)
        double carCost = dist * 2 * 0.35; // round trip
        return carCost + (persons > 4 ? 50 : 0); // extra if need 2 cars
    }

    private double estimateEquipment(String siteType, int days, int persons) {
        double base = switch (siteType) {
            case "DESERT" -> 45.0; // need more specialized gear
            case "FOREST" -> 30.0;
            case "COASTAL" -> 25.0;
            default -> 30.0;
        };
        return base * persons + (days > 3 ? days * 5.0 : 0);
    }

    private double estimateMisc(String siteType, int days, int persons) {
        double baseActivity = switch (siteType) {
            case "DESERT" -> 80.0; // camel rides, quad, etc.
            case "COASTAL" -> 60.0; // water sports, boat trips
            case "FOREST" -> 30.0; // entrance fees, guided hikes
            default -> 40.0;
        };
        return (baseActivity + days * 10.0) * Math.min(persons, 4); // group discount effect
    }

    private List<String> getEssentialItems(String siteType) {
        List<String> common = Arrays.asList("First Aid Kit", "Water Bottles (2L/person)", "Headlamp + Batteries",
                "Sunscreen SPF50+", "Insect Repellent", "Sleeping Bag", "Fire Lighter");
        List<String> siteSpecific = switch (siteType) {
            case "DESERT" -> Arrays.asList("Extra Water (5L/person/day)", "UV Protection Clothing",
                    "Sand Goggles", "Compass/GPS", "Emergency Whistle", "Salt Tablets");
            case "COASTAL" -> Arrays.asList("Waterproof Bag", "Snorkel Set", "Life Jacket",
                    "After-Sun Lotion", "Flip Flops");
            case "FOREST" -> Arrays.asList("Rain Poncho", "Hiking Boots", "Trekking Poles",
                    "Map of Forest Trails", "Bear Spray", "Water Purification Tablets");
            default -> List.of();
        };
        List<String> all = new ArrayList<>(common);
        all.addAll(siteSpecific);
        return all;
    }

    private List<String> getOptionalItems(String siteType, String season) {
        List<String> items = new ArrayList<>(Arrays.asList("Portable Speaker", "Camera/GoPro",
                "Hammock", "Portable Charger/Power Bank", "Star Map App"));
        if ("SUMMER".equals(season)) items.addAll(Arrays.asList("Portable Fan", "Cooling Towel", "Extra Sunglasses"));
        if ("WINTER".equals(season)) items.addAll(Arrays.asList("Thermal Underwear", "Hand Warmers", "Hot Water Flask"));
        if ("DESERT".equals(siteType)) items.addAll(Arrays.asList("Sand Anchor for Tent", "Camel Backpack", "Night Vision Binoculars"));
        if ("COASTAL".equals(siteType)) items.addAll(Arrays.asList("Fishing Rod", "Kayak", "Paddleboard"));
        return items;
    }

    private List<String> getTips(String siteType, String season, String gov) {
        List<String> tips = new ArrayList<>();
        tips.add("Book your camping center at least 2 weeks in advance during peak season");
        tips.add("Always inform someone about your route and expected return time");
        if ("DESERT".equals(siteType)) {
            tips.add("Desert temperatures drop to 5–10°C at night — always bring a warm sleeping bag");
            tips.add("Never go alone in the desert — always camp with at least one other person");
            tips.add("Carry 5L of water per person per day minimum");
            if ("SUMMER".equals(season)) tips.add("⚠️ Avoid desert camping in July–August — extreme heat danger!");
        }
        if ("COASTAL".equals(siteType) && "SUMMER".equals(season)) {
            tips.add("Coastal sites fill up quickly in summer — book 1 month ahead");
            tips.add("Jellyfish season: check local alerts before swimming");
        }
        if ("FOREST".equals(siteType)) {
            tips.add("Tick season is March–October — use insect repellent and check after hikes");
            tips.add("Forest fires are common in summer — never leave campfire unattended");
        }
        tips.add("Download the offline map of " + gov + " before leaving — connectivity may be limited");
        return tips;
    }

    private String getWeatherAlert(String siteType, String season) {
        if ("DESERT".equals(siteType) && "SUMMER".equals(season))
            return "🔴 EXTREME HEAT WARNING: Desert temperatures exceed 45°C in summer. Not recommended for families with children or elderly.";
        if ("COASTAL".equals(siteType) && "WINTER".equals(season))
            return "🟡 ROUGH SEAS ADVISORY: Winter Mediterranean can be rough. Avoid open water activities.";
        if ("FOREST".equals(siteType) && "SUMMER".equals(season))
            return "🟠 FIRE RISK: High fire risk in summer forests. Follow all fire safety rules strictly.";
        return null;
    }

    private String getWeatherSummary(String gov, String season) {
        return "Typical " + season.toLowerCase() + " weather in " + gov +
               ": " + getTypicalDescription(gov, season);
    }

    private String getTypicalDescription(String gov, String season) {
        boolean isSouth = List.of("Kébili", "Tataouine", "Tozeur", "Gafsa", "Médenine").contains(gov);
        boolean isNorth = List.of("Bizerte", "Beja", "Jendouba", "Tunis", "Ariana").contains(gov);
        if (isSouth && "SUMMER".equals(season)) return "extremely hot and dry, 40–48°C. Desert conditions.";
        if (isSouth && "WINTER".equals(season)) return "mild days (15–20°C), cold nights (5–10°C). Perfect for desert trips.";
        if (isNorth && "SUMMER".equals(season)) return "hot Mediterranean, 28–35°C. Mild breezes.";
        if (isNorth && "WINTER".equals(season)) return "mild and rainy, 10–18°C. Pack waterproofs.";
        return "moderate conditions suitable for outdoor activities.";
    }

    private double[] getTypicalWeather(String gov, String season) {
        boolean isSouth = List.of("Kébili", "Tataouine", "Tozeur", "Gafsa", "Médenine").contains(gov);
        double temp, humidity;
        if (isSouth) {
            temp = switch (season) { case "SUMMER" -> 44.0; case "WINTER" -> 15.0; case "SPRING" -> 28.0; default -> 25.0; };
            humidity = switch (season) { case "SUMMER" -> 20.0; case "WINTER" -> 45.0; default -> 30.0; };
        } else {
            temp = switch (season) { case "SUMMER" -> 32.0; case "WINTER" -> 13.0; case "SPRING" -> 22.0; default -> 20.0; };
            humidity = switch (season) { case "SUMMER" -> 65.0; case "WINTER" -> 80.0; default -> 70.0; };
        }
        return new double[]{temp, humidity};
    }
}
