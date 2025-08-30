package com.BeatUp.BackEnd.Concert.repository;


import com.BeatUp.BackEnd.Concert.dto.ConcertSearchCondition;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.BeatUp.BackEnd.Concert.entity.QConcert.concert;
import static com.BeatUp.BackEnd.common.util.QueryDslUtils.*;

@Repository
@RequiredArgsConstructor
public class ConcertCustomRepsoitoryImpl implements ConcertCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Concert> searchConcerts(ConcertSearchCondition condition){
        return queryFactory
                .selectFrom(concert)
                .where(
                        queryContains(condition.getQuery()),
                        betweenDates(concert.startAt, condition.getStartDate(), condition.getEndDate()),
                        regionEq(condition.getRegion()),
                        genreEq(condition.getGenre())
                )
                .orderBy(concert.startAt.asc())
                .fetch();
    }

    @Override
    public Page<Concert> searchConcerts(ConcertSearchCondition condition, Pageable pageable){
        List<Concert> content = queryFactory
                .selectFrom(concert)
                .where(
                        queryContains(condition.getQuery()),
                        betweenDates(concert.startAt, condition.getStartDate(), condition.getEndDate()),
                        regionEq(condition.getRegion()),
                        genreEq(condition.getGenre())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getConcertOrderSpecifier(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(concert.count())
                .from(concert)
                .where(
                        queryContains(condition.getQuery()),
                        betweenDates(concert.startAt, condition.getStartDate(), condition.getEndDate()),
                        regionEq(condition.getRegion()),
                        genreEq(condition.getGenre())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> getConcertOrderSpecifier(Pageable pageable) {
        return null;
    }


    // 동적 조건 메서드들
    private BooleanExpression queryContains(String query){
        if(!hasText(query)) return null;
        return concert.name.lower().contains(query.toLowerCase())
                .or(concert.venue.lower().contains(query.toLowerCase()));
    }

    private BooleanExpression regionEq(String region){
        return eqlfNotNull(concert.venue, region);
    }

    private BooleanExpression genreEq(String genre){
        return eqlfNotNull(concert.genre, genre);
    }
}
