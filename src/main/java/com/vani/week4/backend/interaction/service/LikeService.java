package com.vani.week4.backend.interaction.service;

import com.vani.week4.backend.global.ErrorCode;
import com.vani.week4.backend.global.exception.PostNotFoundException;
import com.vani.week4.backend.interaction.entity.Like;
import com.vani.week4.backend.interaction.entity.UserPostLikeId;
import com.vani.week4.backend.interaction.repository.LikeRepository;
import com.vani.week4.backend.post.entity.Post;
import com.vani.week4.backend.post.repository.PostRepository;
import com.vani.week4.backend.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 좋아요 관련 로직을 처리하는 클래스
 * Redis를 사용하여 좋아요 수를 캐싱, 스케줄러로 DB와 동기화
 * @author vani
 * @since 10/15/25
 */
@Slf4j
@Service
public class LikeService {
    private static final String LIKE_COUNT_KEY_PREFIX = "post:like:";

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> likesRedisTemplate;

    protected LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            @Qualifier("likesRedisTemplate")RedisTemplate<String, Object> template
        ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.likesRedisTemplate = template;
    }

    //TODO 레디스와 DB 동기화 오류 문제 있음 캐시가 비었을 경우 다시 DB에서 가져와서 쓰는 로직이 없음
    /**
     * 게시글의 좋아요를 토글
     * 이미 좋아요 했다면 취소, 좋아요하지 않았다면 좋아요합니다.
     * 수는 Redis에 캐싱, 스캐줄러를 통해 DB와 동기화
     * */
    @Transactional
    public void toggleLike(User user, String postId){

        String userId = user.getId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
        log.info("좋아요 처리중 {} ", userId);
        //이미 좋아요 했다면 삭제, 안했으면 좋아요
        //레디스에 카운트 캐싱
        //키는 텍스트로 가독성 향상
        if (likeRepository.existsById(new UserPostLikeId(userId, postId))){
            likeRepository.deleteById(new UserPostLikeId(userId, postId));
            likesRedisTemplate.opsForValue().decrement(LIKE_COUNT_KEY_PREFIX + postId);
        } else {
            likeRepository.save(new Like(user, post));
            likesRedisTemplate.opsForValue().increment(LIKE_COUNT_KEY_PREFIX + postId);
        }
    }

    /**
     * Redis에서 좋아요 수를 조회하고 없다면 DB에서 로드합니다.
     */
    @Transactional
    public Integer getLikeCount(String postId){
        log.info("🚨 [Before] Redis 단건 조회 호출! postId={}", postId);
        String redisKey = LIKE_COUNT_KEY_PREFIX + postId;
        Object value = likesRedisTemplate.opsForValue().get(redisKey);
        if (value == null){
            //DB에서 조회 후 Redis에 캐싱
            int count = likeRepository.countByUserPostLikeIdPostId(postId);
            likesRedisTemplate.opsForValue().set(redisKey,count);
            return count;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * Redis에서 좋아요 수를 배치로 조회하고 없다면 DB에서 배치로 로드합니다.
     * */
    public Map<String, Integer> getLikeCountsBatch(List<String> postIds) {
        if (postIds.isEmpty()){
            return new HashMap<>();
        }
        log.info("✅ [After] Redis 배치(Pipeline) 조회 호출! 대상={}개", postIds.size());
        List<String> keys = postIds.stream()
                .map(id -> LIKE_COUNT_KEY_PREFIX + id)
                .toList();

        // MGET으로 한 번에 불러옴
        List<Object> values = likesRedisTemplate.opsForValue().multiGet(keys);

        Map<String, Integer> result = new HashMap<>();
        List<String> missingPostIds = new ArrayList<>();

        // 있는 건 담고, 없는건 missing
        for (int i = 0; i < postIds.size(); i++) {
            if(values.get(i) != null) {
                result.put(postIds.get(i), Integer.parseInt(values.get(i).toString()));
            } else {
                missingPostIds.add(postIds.get(i));
            }
        }

        if (!missingPostIds.isEmpty()) {
            List<Object[]> dbResults = likeRepository.countByPostIdsIn(missingPostIds);

            Map<String, Integer> dbMap = new HashMap<>();
            for (Object[] row : dbResults) {
                String id = (String) row[0];
                Integer count = ((Number) row[1]).intValue();
                dbMap.put(id, count);
                result.put(id, count);
            }

            // DB 조회결과에도 없으면 좋아요 0개
            for (String id : missingPostIds){
                result.putIfAbsent(id, 0);
                dbMap.putIfAbsent(id, 0);
            }

            Map<String, String> updates = new HashMap<>();
            dbMap.forEach((id, count) -> updates.put(LIKE_COUNT_KEY_PREFIX + id, count.toString()));
            likesRedisTemplate.opsForValue().multiSet(updates);
        }
        return result;
    }
}
