package com.BeatUp.BackEnd.Community.Post.repository;

import com.BeatUp.BackEnd.Community.Post.dto.PostSearchCondition;
import com.BeatUp.BackEnd.Community.Post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCustomRepository {
    List<Post> searchPosts(PostSearchCondition condition);
    Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable);
}
