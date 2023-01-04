package com.example.project.pharmacy.service;

import com.example.project.pharmacy.entity.Pharmacy;
import com.example.project.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRepositoryService {
    private final PharmacyRepository pharmacyRepository;

    // self invocation test
    public void bar(List<Pharmacy> pharmacyList) {
        log.info("bar CurrentTransactionName: "+ TransactionSynchronizationManager.getCurrentTransactionName());
        foo(pharmacyList);
    }

    // self invocation test
    @Transactional
    public void foo(List<Pharmacy> pharmacyList) {
        log.info("foo CurrentTransactionName: "+ TransactionSynchronizationManager.getCurrentTransactionName());
        pharmacyList.forEach(pharmacy -> {
            pharmacyRepository.save(pharmacy);
            throw new RuntimeException("error"); // RuntimeException 이 발생하면 롤백이 발생
        });
    }

     /* Transactional 이 포함된 메소드가 "호출"될 경우, 프록시 객체를 생성
     * 트랜잭션 생성 및 커밋 또는 롤백 후 트랜잭션을 닫는 부가적인 작업을 프록시 객체에게 위임
     * 따라서 메소드에 @Transaction 어노테이션만 선언하고 비지니스 로직에 집중할 수 있음..
     * Spring AOP 기반으로 하는 기능이므로 Self Invocation 문제가 생길 수 있음
     * 1. @Transactional 위치를 외부에서 호출하는 bar() 메소드로 이동
     * 2. 객체의 책임을 최대한 분리하여 외부 호출 하도록 리펙토링 */

    // read only test
    @Transactional(readOnly = true)
    public void startReadOnlyMethod(Long id) {
        pharmacyRepository.findById(id)
                .ifPresent(pharmacy -> pharmacy.changePharmacyAddress("서울 특별시 광진구"));
    }

    @Transactional
    public List<Pharmacy> saveAll(List<Pharmacy> pharmacyList) {
        if(CollectionUtils.isEmpty(pharmacyList)) return Collections.emptyList();
        return pharmacyRepository.saveAll(pharmacyList);
    }

    /* @Transactional(readOnly = true) : 읽기 전용으로 설정
     * 스냅샷, 더티 체킹 작업을 수행하지 않음 -> 성능 향상 대신 더티 체킹 불가
     * 우선 순위가 존재. 클래스 보다 메소드가 더 우선순위가 높음. */

    @Transactional
    public void updateAddress(Long id, String address) {
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if(Objects.isNull(entity)) {
            log.error("[PharmacyRepositoryService updateAddress] not found id: {}", id);
            return;
        }

        entity.changePharmacyAddress(address);
    }

    // test
    public void updateAddressWithoutTransaction(Long id, String address) {
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if(Objects.isNull(entity)) {
            log.error("[PharmacyRepositoryService updateAddress] not found id: {}", id);
            return;
        }

        entity.changePharmacyAddress(address);
    }

    @Transactional(readOnly = true)
    public List<Pharmacy> findAll() {
        return pharmacyRepository.findAll();
    }
}
