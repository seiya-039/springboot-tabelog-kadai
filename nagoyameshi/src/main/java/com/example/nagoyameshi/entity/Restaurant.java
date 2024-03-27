package com.example.nagoyameshi.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "image")
	private String image;

	@Column(name = "description")
	private String description;

	@Column(name = "lowest_price")
	private Integer lowestPrice;

	@Column(name = "highest_price")
	private Integer highestPrice;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "address")
	private String address;

	@Column(name = "opening_time")
	private LocalTime openingTime;

	@Column(name = "closing_time")
	private LocalTime closingTime;

	@Column(name = "seating_capacity")
	private Integer seatingCapacity;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;

	@OneToMany(mappedBy = "restaurant")
	private List<CategoryRestaurant> categoryRestaurants;

	@OneToMany(mappedBy = "restaurant")
	private List<Review> reviews;

	@Transient // このアノテーションにより、Hibernateがこのフィールドをデータベースカラムとして扱わないようになる  
	public List<Category> getCategories() {
		if (!categoryRestaurants.isEmpty()) {
			return categoryRestaurants.stream()
					.map(CategoryRestaurant::getCategory)
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@Transient
	public Double getAverageScore() {
		if (!reviews.isEmpty()) {
			double totalScore = 0.0;
			int reviewCount = 0;

			for (Review review : reviews) {
				totalScore += review.getScore();
				reviewCount++;
			}

			// 小数点第3位を四捨五入する
			BigDecimal bd = new BigDecimal(totalScore / reviewCount);
			bd = bd.setScale(2, RoundingMode.HALF_UP);

			return bd.doubleValue();
		}

		return 0.0;
	}

	// 平均評価を四捨五入して0.5刻みで返す（星評価用）
	@Transient
	public Double getRoundedAverageScore() {
		return Math.round(getAverageScore() * 2.0) / 2.0;
	}

	// 平均評価のフォーマットを整えて返す
	@Transient
	public String getFormattedAverageScore() {
		double averageScore = getAverageScore();
		String averageScoreStr = String.format("%.2f", averageScore);

		if (averageScoreStr.endsWith(".00")) {
			// 末尾が ".00" の場合、整数部分だけ返す
			return averageScoreStr.substring(0, averageScoreStr.length() - 3);
		} else if (averageScoreStr.endsWith("0")) {
			// 末尾が "0" の場合、小数点第1位まで返す
			return averageScoreStr.substring(0, averageScoreStr.length() - 1);
		} else {
			// それ以外はそのまま返す
			return averageScoreStr;
		}
	}
}
