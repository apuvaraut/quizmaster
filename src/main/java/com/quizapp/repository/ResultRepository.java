package com.quizapp.repository;

import com.quizapp.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByUserEmailOrderByTakenAtDesc(String email);

    // Total score per user, highest first
    @Query("select r.user.name, sum(r.score) from Result r group by r.user.id, r.user.name order by sum(r.score) desc")
    List<Object[]> leaderboard();
}
