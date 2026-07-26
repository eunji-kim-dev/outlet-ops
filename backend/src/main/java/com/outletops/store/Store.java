// 매장 도메인 클래스가 위치하는 패키지
package com.outletops.store;

// JPA 엔티티와 테이블 매핑에 필요한 클래스
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 반복 코드를 줄이기 위한 Lombok 클래스
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이 클래스가 JPA에서 관리하는 엔티티임을 선언
@Entity

// Store 엔티티를 MySQL의 stores 테이블과 연결
@Table(name = "stores")

// 모든 필드의 Getter 메서드를 Lombok이 자동 생성
@Getter

// JPA가 엔티티를 생성할 때 사용할 기본 생성자를 생성
// 외부에서 함부로 호출하지 못하도록 접근 범위를 protected로 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    // 이 필드를 테이블의 기본 키로 지정
    @Id

    // MySQL의 AUTO_INCREMENT 방식으로 ID를 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null을 허용하지 않고, 중복을 막으며, 최대 길이를 100자로 제한
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // null을 허용하지 않고 최대 길이를 255자로 제한
    @Column(nullable = false, length = 255)
    private String address;

    // null을 허용하지 않는 매장 활성화 상태
    @Column(nullable = false)

    // 새로운 매장은 기본적으로 활성 상태로 생성
    private boolean active = true;

    // 애플리케이션에서 새로운 매장을 만들 때 사용하는 생성자
    // id는 데이터베이스가 자동 생성하므로 받지 않음
    // active는 기본값이 true이므로 받지 않음
    public Store(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
