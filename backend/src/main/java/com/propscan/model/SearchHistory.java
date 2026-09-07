package com.propscan.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 1000)
    private String naturalLanguageQuery;

    private String city;
    private String locality;
    private String propertyType;
    private Integer bhk;
    private Long maxBudget;
    private Long minBudget;
    private Boolean parkingRequired;
    private Integer maxCommuteMinutes;

    private Integer resultsFound;
    private Integer groupsFound;

    @Column(name = "searched_at")
    @Builder.Default
    private LocalDateTime searchedAt = LocalDateTime.now();
}
