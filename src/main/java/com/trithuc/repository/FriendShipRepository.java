package com.trithuc.repository;

import com.trithuc.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendShipRepository extends JpaRepository<Friendship,Long> {
    @Query("SELECT f.id.friendId FROM Friendship f WHERE f.user.id = :userId")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);
}
