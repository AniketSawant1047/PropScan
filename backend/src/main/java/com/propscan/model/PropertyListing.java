package com.propscan.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalId;

    private String source;
    private String sourceDisplayName;

    private String title;

    @Column(length = 2000)
    private String description;

    // Normalized price in INR
    private Long priceInr;
    private String rawPrice;

    // Normalized area in sq ft
    private Double areaSqft;
    private String rawArea;

    private Integer bhk;
    private String rawBhk;

    private String propertyType;
    private String locality;
    private String city;

    @Column(length = 500)
    private String address;

    private String project;
    private String developer;
    private String floor;
    private Integer totalFloors;
    private String facing;
    private String furnishing;

    private Boolean parking;
    private Boolean lift;
    private Boolean gym;
    private Boolean swimmingPool;
    private Boolean security;

    @ElementCollection
    @CollectionTable(name = "listing_amenities", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "amenity")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "listing_images", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "image_url", length = 1000)
    @Builder.Default
    private List<String> images = new ArrayList<>();

    private Double latitude;
    private Double longitude;

    @Column(length = 1000)
    private String listingUrl;

    private LocalDate postedDate;
    private Boolean readyToMove;
    private String possession;
    private String agentName;
    private String agentPhone;

    // Derived/computed fields
    private Double pricePerSqft;

    // Group assignment
    private String groupId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
