package com.propscan.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String groupId;

    private String canonicalTitle;
    private String canonicalAddress;
    private String canonicalLocality;
    private String canonicalCity;
    private String canonicalProject;
    private String canonicalDeveloper;

    private Integer canonicalBhk;
    private Double canonicalAreaSqft;
    private Double canonicalLatitude;
    private Double canonicalLongitude;

    // Best price across all sources
    private Long bestPriceInr;
    private String bestPriceSource;

    // Highest price across all sources
    private Long highestPriceInr;
    private String highestPriceSource;

    private Double matchConfidence;

    @ElementCollection
    @CollectionTable(name = "group_sources", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "source_name")
    @Builder.Default
    private List<String> sources = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "group_listing_ids", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "listing_id")
    @Builder.Default
    private List<Long> listingIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "group_images", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "image_url", length = 1000)
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
