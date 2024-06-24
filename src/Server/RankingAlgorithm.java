package src.Server;

import src.H_U_R.Hotel;
import src.H_U_R.Review;

public class RankingAlgorithm {

    private static final double LAMBDA = 0.01;

    public static double calculateRanking(Hotel hotel) {

        long now = System.currentTimeMillis();
        double currencyScore = 0;

        for(Review review : hotel.getReviews()) {
            double daysSinceReview = (now - review.getDate()) / 1000 / 60 / 60 / 24;
            currencyScore += review.getGlobalScore() * Math.exp(-LAMBDA * daysSinceReview);
        }

        double mediumScore = calculateMediumScore(hotel);
        double quantityScore = Math.log(hotel.getTotalRatings() + 1);

        hotel.setRate(mediumScore);
        hotel.setRankingRate(currencyScore * mediumScore * quantityScore);

        return currencyScore * mediumScore * quantityScore;
    }

    /**
     * Calculate the medium score of a hotel
     * @param hotel the hotel to calculate the medium score
     * @return the medium score of the hotel
     */
    public static double calculateMediumScore(Hotel hotel) {
        double totalScore = 0;
        int reviewCount = hotel.getReviews().size();
        for(Review review : hotel.getReviews()) {
            double reviewScore = review.getGlobalScore();
            reviewScore += review.getCleaningScore();
            reviewScore += review.getPositionScore();
            reviewScore += review.getServicesScore();
            reviewScore += review.getQualityScore();
            totalScore += reviewScore / 5;
        }
        return totalScore / reviewCount;
    }
    
}

