package com.camping.config;

import com.camping.model.*;
import com.camping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Configuration
@EnableMongoAuditing
@RequiredArgsConstructor
@Slf4j
public class MongoConfig {

    @Bean
    public CommandLineRunner seedDatabase(
            UserRepository userRepo,
            CampingCenterRepository campingRepo,
            ProductRepository productRepo,
            PasswordEncoder encoder) {
        return args -> {
            // Seed admin user
            if (!userRepo.existsByEmail("admin@camping.tn")) {
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("CampingTN")
                        .email("admin@camping.tn")
                        .password(encoder.encode("Admin@2024"))
                        .roles(new HashSet<>(Arrays.asList("ADMIN", "USER")))
                        .active(true)
                        .build();
                userRepo.save(admin);
                log.info("Admin user created: admin@camping.tn / Admin@2024");
            }

            // Seed camping centers from dataset
            if (campingRepo.count() == 0) {
                List<CampingCenter> centers = createCampingCenters();
                campingRepo.saveAll(centers);
                log.info("Seeded {} camping centers", centers.size());
            }

            // Seed products
            if (productRepo.count() == 0) {
                List<Product> products = createProducts();
                productRepo.saveAll(products);
                log.info("Seeded {} products", products.size());
            }
        };
    }

    private List<CampingCenter> createCampingCenters() {
        return Arrays.asList(
            CampingCenter.builder().nb(1).name("Centre El Hbibia").governorate("Manouba").region("District-Tunis")
                .yearCreated(1989).capacityBuildings(0).capacityTents(80).capacityTotal(80)
                .siteNature("foréstiére").latitude(36.8).longitude(9.76).rating(3.8).reviewCount(12)
                .description("Serene forest camping in the heart of Manouba")
                .amenities(Arrays.asList("Restrooms", "Fire Pits", "Parking")).build(),

            CampingCenter.builder().nb(3).name("Centre Chat Mami").governorate("Bizerte").region("Nord-Est")
                .phone("72447235").capacityBuildings(30).capacityTents(45).capacityTotal(75)
                .siteNature("foréstiére/ littoral").latitude(37.27).longitude(9.87).rating(4.2).reviewCount(34)
                .description("Beautiful coastal and forest site near Bizerte")
                .amenities(Arrays.asList("Beach Access", "Showers", "Restaurant", "Parking")).build(),

            CampingCenter.builder().nb(4).name("Centre Errimal").governorate("Bizerte").region("Nord-Est")
                .phone("72440819").capacityBuildings(95).capacityTents(0).capacityTotal(95)
                .siteNature("foréstiére/ littoral").latitude(37.15).longitude(9.91).rating(4.0).reviewCount(21)
                .description("Premium building accommodation in Bizerte coastal forest")
                .amenities(Arrays.asList("Swimming Pool", "Cafeteria", "WiFi", "AC")).build(),

            CampingCenter.builder().nb(7).name("Centre Beni Mtir").governorate("Jendouba").region("Nord-Ouest")
                .phone("78649200").capacityBuildings(100).capacityTents(0).capacityTotal(100)
                .siteNature("foréstiére/ désertique").latitude(36.54).longitude(8.68).rating(4.5).reviewCount(56)
                .description("Premium Beni Mtir forest retreat with mountain views")
                .amenities(Arrays.asList("Lake Access", "Hiking Trails", "BBQ", "Parking")).build(),

            CampingCenter.builder().nb(11).name("Centre Ain Bousaadia").governorate("Manouba").region("Nord-Ouest")
                .yearCreated(1989).capacityBuildings(32).capacityTents(100).capacityTotal(132)
                .siteNature("foréstiére/ désertique").latitude(36.83).longitude(9.71).rating(3.9).reviewCount(18)
                .description("Family-friendly forest camping site").build(),

            CampingCenter.builder().nb(12).name("Centre El Salloume").governorate("Sousse").region("Centre-Est")
                .yearCreated(1999).phone("73859001").capacityBuildings(30).capacityTents(100).capacityTotal(130)
                .siteNature("Littoral").latitude(35.83).longitude(10.64).rating(4.6).reviewCount(89)
                .description("Premium beachfront camping on the Mediterranean coast")
                .amenities(Arrays.asList("Private Beach", "Water Sports", "Restaurant", "Showers", "WiFi")).build(),

            CampingCenter.builder().nb(15).name("Centre El Douirat").governorate("Mahdia").region("Centre-Est")
                .yearCreated(2011).phone("73643815").capacityBuildings(80).capacityTents(0).capacityTotal(80)
                .siteNature("Littoral").latitude(35.50).longitude(11.07).rating(4.3).reviewCount(45)
                .description("Modern coastal camping center in Mahdia").build(),

            CampingCenter.builder().nb(16).name("Centre Erramela (Karekna)").governorate("Sfax").region("Centre-Est")
                .yearCreated(1986).phone("74481148").capacityBuildings(80).capacityTents(0).capacityTotal(80)
                .siteNature("Littoral").latitude(34.74).longitude(10.76).rating(3.7).reviewCount(29)
                .description("Coastal camping center in Sfax region").build(),

            CampingCenter.builder().nb(17).name("Centre Ain Selsla").governorate("Kasserine").region("Centre-Ouest")
                .yearCreated(1963).capacityBuildings(60).capacityTents(70).capacityTotal(130)
                .siteNature("foréstiére/ désertique").latitude(35.17).longitude(8.84).rating(4.1).reviewCount(37)
                .description("Historic Kasserine mountain camping with desert views")
                .amenities(Arrays.asList("Mountain Views", "Hiking", "Campfire", "Parking")).build(),

            CampingCenter.builder().nb(18).name("Centre El Cheaanbi").governorate("Kasserine").region("Centre-Ouest")
                .yearCreated(2008).capacityBuildings(36).capacityTents(50).capacityTotal(86)
                .siteNature("foréstiére/ désertique").latitude(35.13).longitude(8.73).rating(4.4).reviewCount(52)
                .description("Cheaanbi mountain range camping experience").build(),

            CampingCenter.builder().nb(20).name("Centre Marsa El Kssiba").governorate("Médenine").region("Sud-Est")
                .yearCreated(1987).phone("75719841").capacityBuildings(64).capacityTents(200).capacityTotal(264)
                .siteNature("Littoral").latitude(33.87).longitude(10.85).rating(4.7).reviewCount(112)
                .description("Largest coastal camping center in southern Tunisia — gateway to Djerba")
                .amenities(Arrays.asList("Private Beach", "Snorkeling", "Restaurant", "Bungalows", "WiFi")).build(),

            CampingCenter.builder().nb(21).name("Centre El Douirat").governorate("Tataouine").region("Sud-Est")
                .yearCreated(1996).capacityTents(0).capacityTotal(0)
                .siteNature("désertique").latitude(32.92).longitude(10.45).rating(4.8).reviewCount(76)
                .description("Authentic Saharan desert camping experience in Tataouine")
                .amenities(Arrays.asList("Desert Safari", "Camel Rides", "Star Gazing", "Berber Tents")).build(),

            CampingCenter.builder().nb(22).name("Centre Douz").governorate("Kébili").region("Sud-Ouest")
                .yearCreated(1990).phone("75473420").capacityBuildings(30).capacityTents(200).capacityTotal(230)
                .siteNature("désertique").latitude(33.46).longitude(8.97).rating(4.9).reviewCount(203)
                .description("Gateway to the Sahara — the most iconic desert camping in Tunisia")
                .amenities(Arrays.asList("Camel Trekking", "Sand Dunes", "Traditional Food", "Night Tours", "4x4 Safari")).build()
        );
    }

    private List<Product> createProducts() {
        return Arrays.asList(
            // Tents
            Product.builder().name("Desert Pro 4 Tent").category("TENT").price(289.00).rentalPricePerDay(25.00)
                .description("4-person ultralight tent, UV-resistant, perfect for desert camping")
                .brand("TrailMaster").stockSale(15).stockRental(8).rating(4.8).reviewCount(64)
                .weight("2.1kg").material("Ripstop Nylon").recommendedFor(Arrays.asList("DESERT", "COASTAL"))
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Forest Shelter 3P").category("TENT").price(199.00).rentalPricePerDay(18.00)
                .description("3-person tent with excellent rain protection for forest environments")
                .brand("WoodCraft").stockSale(20).stockRental(12).rating(4.5).reviewCount(42)
                .weight("3.2kg").material("Polyester").recommendedFor(Arrays.asList("FOREST", "COASTAL"))
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Sahara Dome 2P").category("TENT").price(159.00).rentalPricePerDay(15.00)
                .description("Compact 2-person tent, breathable mesh design for hot climates")
                .brand("SaharaGear").stockSale(25).stockRental(15).rating(4.3).reviewCount(38)
                .recommendedFor(Arrays.asList("DESERT")).availableForSale(true).availableForRental(true).active(true).build(),

            // Sleeping Bags
            Product.builder().name("Berber Warmth Sleeping Bag").category("SLEEPING_BAG").price(89.00).rentalPricePerDay(8.00)
                .description("Rated -5°C, ideal for desert nights — surprisingly cold!")
                .brand("NightRest").stockSale(30).stockRental(20).rating(4.6).reviewCount(55)
                .weight("900g").recommendedFor(Arrays.asList("DESERT", "FOREST"))
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Summer Coastal Bag").category("SLEEPING_BAG").price(59.00).rentalPricePerDay(6.00)
                .description("Lightweight +15°C bag, breathable for coastal warm nights")
                .brand("NightRest").stockSale(40).stockRental(25).rating(4.2).reviewCount(31)
                .recommendedFor(Arrays.asList("COASTAL")).availableForSale(true).availableForRental(true).active(true).build(),

            // Cooking
            Product.builder().name("Camp Chef Stove Set").category("COOKING").price(75.00).rentalPricePerDay(10.00)
                .description("2-burner portable gas stove with windshield, perfect for group cooking")
                .brand("CampChef").stockSale(18).stockRental(10).rating(4.7).reviewCount(89)
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Mess Kit Pro").category("COOKING").price(35.00).rentalPricePerDay(4.00)
                .description("Complete 4-person mess kit — plates, cups, utensils")
                .brand("CampChef").stockSale(50).stockRental(30).rating(4.4).reviewCount(67)
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("40L Insulated Cooler Box").category("COOKING").price(85.00).rentalPricePerDay(12.00)
                .description("Keeps ice 48h — essential for desert trips")
                .brand("IcePro").stockSale(12).stockRental(8).rating(4.5).reviewCount(44)
                .availableForSale(true).availableForRental(true).active(true).build(),

            // Navigation
            Product.builder().name("Trail GPS Navigator").category("NAVIGATION").price(199.00).rentalPricePerDay(20.00)
                .description("Rugged GPS with Tunisia offline maps preloaded")
                .brand("NavPro").stockSale(8).stockRental(5).rating(4.9).reviewCount(28)
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Silva Compass + Map Kit").category("NAVIGATION").price(29.00).rentalPricePerDay(3.00)
                .description("Professional compass with topographic map of Tunisia regions")
                .brand("Silva").stockSale(35).stockRental(20).rating(4.6).reviewCount(52)
                .availableForSale(true).availableForRental(true).active(true).build(),

            // Safety
            Product.builder().name("First Aid Kit Outdoor Pro").category("SAFETY").price(45.00).rentalPricePerDay(5.00)
                .description("120-piece first aid kit for outdoor emergencies")
                .brand("SafeHike").stockSale(25).stockRental(15).rating(4.8).reviewCount(73)
                .availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Emergency Survival Kit").category("SAFETY").price(65.00).rentalPricePerDay(8.00)
                .description("Emergency whistle, fire starter, mylar blanket, multi-tool")
                .brand("SafeHike").stockSale(20).stockRental(12).rating(4.7).reviewCount(45)
                .availableForSale(true).availableForRental(true).active(true).build(),

            // Hydration
            Product.builder().name("Desert 3L Water Bladder").category("HYDRATION").price(35.00).rentalPricePerDay(4.00)
                .description("BPA-free hydration pack, insulated sleeve for desert heat")
                .brand("HydroTrail").stockSale(40).stockRental(25).rating(4.5).reviewCount(61)
                .recommendedFor(Arrays.asList("DESERT")).availableForSale(true).availableForRental(true).active(true).build(),

            Product.builder().name("Water Purification Filter").category("HYDRATION").price(55.00).rentalPricePerDay(7.00)
                .description("Filters up to 100,000L — essential for forest spring water")
                .brand("PureFlow").stockSale(15).stockRental(10).rating(4.9).reviewCount(38)
                .recommendedFor(Arrays.asList("FOREST")).availableForSale(true).availableForRental(true).active(true).build(),

            // Clothing
            Product.builder().name("Desert Sun Protection Kit").category("CLOTHING").price(49.00)
                .description("UV shirt, sun hat, neck gaiter — complete sun protection set")
                .brand("SunShield").stockSale(30).stockRental(0).rating(4.4).reviewCount(29)
                .recommendedFor(Arrays.asList("DESERT")).availableForSale(true).availableForRental(false).active(true).build(),

            Product.builder().name("Headlamp 800 Lumen").category("SAFETY").price(39.00).rentalPricePerDay(4.00)
                .description("Rechargeable 800-lumen headlamp with red night vision mode")
                .brand("LightPro").stockSale(35).stockRental(20).rating(4.7).reviewCount(82)
                .availableForSale(true).availableForRental(true).active(true).build()
        );
    }
}
