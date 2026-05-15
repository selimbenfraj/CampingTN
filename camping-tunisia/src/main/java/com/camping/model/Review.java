package com.camping.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;

    private String campingCenterId;
    private String userId;
    private String userName;
    private String userAvatar;

    private int rating; // 1-5
    private String title;
    private String content;
    private List<String> images;

    private int likes;
    private boolean verified; // booked through platform

    @CreatedDate
    private LocalDateTime createdAt;
}
