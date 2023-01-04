package com.example.project.pharmacy.entity;

import com.example.project.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "pharmacy")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 키 값 생성을 데이터베이스가 해준다.
    private Long id;

    private String pharmacyName;
    private String pharmacyAddress;
    private double latitude;
    private double longitude;

    public void changePharmacyAddress(String address) {
        this.pharmacyAddress = address;
    }
}

/*
* Dirty Checking : 엔터티의 상태 변경 검사...
* JPA 에서 트랜잭션이 끝나는 시점에 변화가 있는 모든 엔터티 객체를
* 데이터 베이스에 자동으로 반영해 준다.
* 1차 캐시에 스냅샷을 두고, 변경이 있다면 스냅샷과 비교하여 업데이트 쿼리를
* 쓰기 지연 SQL 저장소에 반영
* 주의 사항 :
* 1. 영속성 컨텍스트가 관리하는 엔터티에만 적용,
* 때문에 영속 상태가 아닐 경우 값을 변경해도 데이터베이스에 반영이 안됨
* 2. 트랜잭션이 없이 데이터 반영이 일어나지 않는다.
* */
