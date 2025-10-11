package com.BeatUp.BackEnd.Concert.repository;


import com.BeatUp.BackEnd.Concert.dto.ConcertSearchCondition;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.BeatUp.BackEnd.Concert.entity.QConcert.concert;

@Repository
@RequiredArgsConstructor
public class ConcertCustomRepositoryImpl implements ConcertCustomRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Concert> searchConcerts(ConcertSearchCondition condition){
        return queryFactory
                .selectFrom(concert)
                .where(buildSearchConditions(condition))
                .orderBy(buildOrderSpecifier(condition))
                .fetch();
    }

    @Override
    public Page<Concert> searchConcerts(ConcertSearchCondition condition, Pageable pageable){
        BooleanBuilder conditions = buildSearchConditions(condition);

        JPAQuery<Concert> query = queryFactory
                .selectFrom(concert)
                .where(conditions);

        long total = queryFactory
                .select(concert.count())
                .from(concert)
                .where(conditions)
                .fetchOne();

        List<Concert> content = query
                .orderBy(buildOrderSpecifier(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }


    private BooleanBuilder buildSearchConditions(ConcertSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();

        // 통합 검색어 (이름, 장소, 출연진)
        if(StringUtils.hasText(condition.getQuery())){
            builder.and(
                    concert.name.containsIgnoreCase(condition.getQuery())
                            .or(concert.venue.containsIgnoreCase(condition.getQuery()))
                            .or(concert.cast.containsIgnoreCase(condition.getQuery()))
            );
        }

        // 날짜 조건
        if(condition.getDate() != null){
            builder.and(
                    concert.startDate.loe(condition.getDate())
                            .and(concert.endDate.isNull().or(concert.endDate.goe(condition.getDate())))
            );
        }else if(condition.getStartDate() != null && condition.getEndDate() != null){
            builder.and(concert.startDate.between(condition.getStartDate(), condition.getEndDate()));
        }

        // 장르
        if(StringUtils.hasText(condition.getGenre())){
            builder.and(concert.genre.eq(condition.getGenre()));
        }

        // 상태
        if(StringUtils.hasText(condition.getState())){
            builder.and(concert.state.eq(condition.getState()));
        }

        // 지역
        if(StringUtils.hasText(condition.getArea())){
            builder.and(concert.area.containsIgnoreCase(condition.getArea()));
        }

        // 데이터 출처
        if(condition.getDataSource() != null){
            builder.and(concert.dataSource.eq(condition.getDataSource()));
        }

        // 플래그 조건들
        if(condition.getIsOpenRun() != null){
            builder.and(concert.isOpenRun.eq(condition.getIsOpenRun()));
        }

        if(condition.getIsChildPerformance() != null){
            builder.and(concert.isChildPerformance.eq(condition.getIsChildPerformance()));
        }

        if(condition.getIsDaehakro() != null){
            builder.and(concert.isDaehakro.eq(condition.getIsDaehakro()));
        }

        if(condition.getIsFestival() != null){
            builder.and(concert.isFestival.eq(condition.getIsFestival()));
        }


        return builder;

    }


    private OrderSpecifier<?>[] buildOrderSpecifier(ConcertSearchCondition condition){
        if(!StringUtils.hasText(condition.getSortBy())){
            return new OrderSpecifier[]{
                    concert.startDate.asc().nullsLast(),
                    concert.createdAt.desc()
            };
        }

        Order direction = "desc".equalsIgnoreCase(condition.getSortDirection()) ? Order.DESC : Order.ASC;

        return switch (condition.getSortBy().toLowerCase()){
            case "name" -> new OrderSpecifier[]{new OrderSpecifier<>(direction, concert.name)};
            case "startdate" -> new OrderSpecifier[]{new OrderSpecifier<>(direction, concert.startDate)};
            case "createdat" -> new OrderSpecifier[]{new OrderSpecifier<>(direction, concert.createdAt)};
            default -> new OrderSpecifier[]{concert.startDate.asc().nullsLast(), concert.createdAt.desc()};
        };

    }



}
