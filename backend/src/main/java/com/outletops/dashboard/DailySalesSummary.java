// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 일별 매출 집계가 참조할 매장 엔티티
import com.outletops.store.Store;

// JPA 엔티티와 연관관계 매핑에 필요한 클래스
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// 금액과 집계 날짜를 표현하기 위한 클래스
import java.math.BigDecimal;
import java.time.LocalDate;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 매장별 하루 매출 집계 결과를 나타내는 JPA 엔티티
@Entity

// 같은 날짜와 매장 조합은 한 번만 저장되도록 유니크 제약조건 설정
@Table(
        name = "daily_sales_summaries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_sales_summary_date_store",
                columnNames = {"sales_date", "store_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySalesSummary {

    // daily_sales_summaries 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 집계 대상 매장
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 집계 대상 영업일
    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    // 해당 날짜와 매장의 총매출
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    // 해당 날짜와 매장의 전체 판매 수량
    @Column(nullable = false)
    private long totalQuantity;

    // 해당 날짜와 매장의 판매 거래 건수
    @Column(nullable = false)
    private long transactionCount;

    // 처음 집계 결과를 생성할 때 사용하는 생성자
    public DailySalesSummary(
            Store store,
            LocalDate salesDate,
            BigDecimal totalRevenue,
            long totalQuantity,
            long transactionCount
    ) {
        this.store = store;
        this.salesDate = salesDate;
        update(totalRevenue, totalQuantity, transactionCount);
    }

    // 같은 날짜를 다시 집계할 경우 기존 집계값 갱신
    public void update(
            BigDecimal totalRevenue,
            long totalQuantity,
            long transactionCount
    ) {
        this.totalRevenue = totalRevenue;
        this.totalQuantity = totalQuantity;
        this.transactionCount = transactionCount;
    }
}
