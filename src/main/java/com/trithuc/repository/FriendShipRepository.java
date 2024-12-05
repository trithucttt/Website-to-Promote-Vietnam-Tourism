package com.trithuc.repository;

import com.trithuc.model.Friendship;
import com.trithuc.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendShipRepository extends JpaRepository<Friendship,Long> {
    @Query("SELECT f.id.friendId FROM Friendship f WHERE f.user.id = :userId AND f.friendState = 'ACCEPTED'")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.id.friendId FROM Friendship f WHERE f.user.id = :userId AND f.friendState = 'REQUEST'")
    List<Long> findFriendIdsByUserIdAndAndFriendState_Request(@Param("userId") Long userId);



    boolean existsById(FriendshipId friendshipId);

    Optional<Friendship> findById(FriendshipId friendshipId);
}
