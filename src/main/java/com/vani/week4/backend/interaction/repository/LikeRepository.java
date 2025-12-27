package com.vani.week4.backend.interaction.repository;

import com.vani.week4.backend.interaction.entity.Like;
import com.vani.week4.backend.interaction.entity.UserPostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author vani
 * @since 10/15/25
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, UserPostLikeId> {
    // 좋아요 존재 여부 확인
    boolean existsById(UserPostLikeId id);

    boolean existsByUserIdAndPostId(String userId, String postId);

    int countByUserPostLikeIdPostId(String postId);

    @Query("SELECT l.post.id, COUNT (l) FROM Like l WHERE l.post.id IN :postIds GROUP BY l.post.id")
    List<Object[]> countByPostIdsIn(@Param("postIds") List<String> postIds);
}
