package com.vani.week4.backend.post.entity;

import com.vani.week4.backend.comment.entity.Comment;
import com.vani.week4.backend.global.ErrorCode;
import com.vani.week4.backend.global.exception.UnauthorizedException;
import com.vani.week4.backend.interaction.entity.Like;
import com.vani.week4.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post {
    @Id
    @Column(length = 26)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id" )
    private User user;

    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PostContent postContent;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Column(length = 100)
    private String title;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PostStatus postStatus;

    @Builder
    private Post(String id, User user, String title) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.commentCount = 0;
        this.likeCount = 0;
        this.postStatus = PostStatus.ACTIVE;
    }

    // PostContent 설정 메서드
    public void setPostContent(PostContent postContent) {
        this.postContent = postContent;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateModifiedDate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
    public void incrementCommentCount() {
        this.commentCount++;
    }
    public void decreaseCommentCount() { this.commentCount--; }
    public void updateLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void validateOwner(User requester) {
        if (!this.user.getId().equals(requester.getId())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }
    }
}
