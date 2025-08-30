package com.BeatUp.BackEnd.Community.Post.repository;

import com.BeatUp.BackEnd.Community.Post.dto.PostSearchCondition;
import com.BeatUp.BackEnd.Community.Post.entity.Post;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.BeatUp.BackEnd.Community.Post.entity.QPost.post;
import static com.BeatUp.BackEnd.common.util.QueryDslUtils.*;

@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPosts(PostSearchCondition condition){
        return queryFactory
                .selectFrom(post)
                .where(
                        statusFilter(condition.getStatus()),
                        concertIdEq(condition.getConcertId()),
                        queryContains(condition.getQuery()),
                        authorIdEq(condition.getAuthorId()),
                        betweenDates(post.createdAt, condition.getCreatedAfter(), condition.getCreatedBefore())
                )
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable){
        List<Post> content = queryFactory
                .selectFrom(post)
                .where(buildConditions(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(buildConditions(condition));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 동적 조건 메서드들
    private BooleanExpression[] buildConditions(PostSearchCondition condition){
        return new BooleanExpression[]{
                statusFilter(condition.getStatus()),
                concertIdEq(condition.getConcertId()),
                queryContains(condition.getQuery()),
                authorIdEq(condition.getAuthorId()),
                betweenDates(post.createdAt, condition.getCreatedAfter(), condition.getCreatedBefore())
        };
    }


    private BooleanExpression statusFilter(String status){
        return hasText(status) ? post.status.eq(status) : post.status.ne("DELETED");
    }

    private BooleanExpression concertIdEq(UUID concertId){
        return eqlfNotNull(post.concertId, concertId);
    }

    private BooleanExpression queryContains(String query){
        if(!hasText(query)) return null;
        return post.title.lower().contains(query.toLowerCase())
                .or(post.content.lower().contains(query.toLowerCase()));
    }

    private BooleanExpression authorIdEq(UUID authorId){
        return eqlfNotNull(post.authorId, authorId);
    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable){
        if(pageable.getSort().isEmpty()){
            return post.createdAt.desc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()){
            case "createdAt" -> new OrderSpecifier<>(direction, post.createdAt);
            case "title" -> new OrderSpecifier<>(direction, post.title);
            case "updatedAt" -> new OrderSpecifier<>(direction, post.updatedAt);
            default -> post.createdAt.desc();
        };
    }

}
