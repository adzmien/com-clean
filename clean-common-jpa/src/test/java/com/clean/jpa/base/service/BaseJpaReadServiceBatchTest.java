package com.clean.jpa.base.service;

import com.clean.jpa.base.repository.BaseJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseJpaReadServiceBatchTest {

    @Test
    void forEachBatchBySpecification_iteratesInBatchesAndInvokesConsumerPerPage() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        List<TestEntity> batchOne = LongStream.rangeClosed(1, 1000).mapToObj(TestEntity::new).toList();
        List<TestEntity> batchTwo = LongStream.rangeClosed(1001, 2000).mapToObj(TestEntity::new).toList();
        List<TestEntity> batchThree = LongStream.rangeClosed(2001, 2200).mapToObj(TestEntity::new).toList();

        Page<TestEntity> pageOne = new PageImpl<>(
                batchOne,
                PageRequest.of(0, 1000, Sort.by("id").ascending()),
                2200
        );
        Page<TestEntity> pageTwo = new PageImpl<>(
                batchTwo,
                PageRequest.of(1, 1000, Sort.by("id").ascending()),
                2200
        );
        Page<TestEntity> pageThree = new PageImpl<>(
                batchThree,
                PageRequest.of(2, 1000, Sort.by("id").ascending()),
                2200
        );

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(pageOne, pageTwo, pageThree);

        List<List<Long>> consumed = new ArrayList<>();
        service.forEachBatchBySpecification(new DummyFilter("cfg"), 1000, consumed::add);

        assertThat(consumed).hasSize(3);
        assertThat(consumed.get(0)).hasSize(1000);
        assertThat(consumed.get(1)).hasSize(1000);
        assertThat(consumed.get(2)).hasSize(200);
        assertThat(consumed.get(0).get(0)).isEqualTo(1L);
        assertThat(consumed.get(2).get(199)).isEqualTo(2200L);
    }

    @Test
    void forEachBatch_withNullBatchSize_usesDefaultBatchSize() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.forEachBatch(null, batch -> {
        });

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(anySpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1000);
    }

    @Test
    void forEachBatch_withBatchSizeAboveMax_capsAt1000() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.forEachBatch(5000, batch -> {
        });

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(anySpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1000);
    }

    @Test
    void forEachBatch_withNonPositiveBatchSize_usesDefaultBatchSize() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(Page.empty(), Page.empty());

        service.forEachBatch(0, batch -> {
        });
        service.forEachBatch(-1, batch -> {
        });

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, times(2)).findAll(anySpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getAllValues()).hasSize(2);
        assertThat(pageableCaptor.getAllValues().get(0).getPageSize()).isEqualTo(1000);
        assertThat(pageableCaptor.getAllValues().get(1).getPageSize()).isEqualTo(1000);
    }

    @Test
    void forEachBatch_enforcesDeterministicIdAscendingSortForEachPage() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        Page<TestEntity> firstPage = new PageImpl<>(
                List.of(new TestEntity(1L)),
                PageRequest.of(0, 1000, Sort.by("id").ascending()),
                1001
        );
        Page<TestEntity> secondPage = new PageImpl<>(
                List.of(new TestEntity(2L)),
                PageRequest.of(1, 1000, Sort.by("id").ascending()),
                1001
        );

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(firstPage, secondPage);

        service.forEachBatch(1000, batch -> {
        });

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, times(2)).findAll(anySpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getAllValues()).hasSize(2);
        assertThat(pageableCaptor.getAllValues().get(0).getSort())
                .isEqualTo(Sort.by("id").ascending());
        assertThat(pageableCaptor.getAllValues().get(1).getSort())
                .isEqualTo(Sort.by("id").ascending());
    }

    @Test
    void findAllBySpecificationInBatches_aggregatesAllResultsWithFilter() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        Page<TestEntity> firstPage = new PageImpl<>(
                List.of(new TestEntity(10L), new TestEntity(11L)),
                PageRequest.of(0, 1000, Sort.by("id").ascending()),
                1001
        );
        Page<TestEntity> secondPage = new PageImpl<>(
                List.of(new TestEntity(12L)),
                PageRequest.of(1, 1000, Sort.by("id").ascending()),
                1001
        );

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(firstPage, secondPage);

        List<Long> result = service.findAllBySpecificationInBatches(new DummyFilter("cfg"), 1000);

        assertThat(result).containsExactly(10L, 11L, 12L);
        verify(repository, times(2)).findAll(anySpecification(), any(Pageable.class));
    }

    @Test
    void findAllInBatches_returnsEmptyAndConsumerNotInvokedWhenNoResults() {
        BaseJpaRepository<TestEntity, Long> repository = mockRepository();
        TestService service = new TestService(repository);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(Page.empty(), Page.empty());

        AtomicInteger consumerCalls = new AtomicInteger();
        service.forEachBatch(1000, batch -> consumerCalls.incrementAndGet());

        List<Long> result = service.findAllInBatches(1000);

        assertThat(consumerCalls.get()).isZero();
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static BaseJpaRepository<TestEntity, Long> mockRepository() {
        return mock(BaseJpaRepository.class);
    }

    @SuppressWarnings("unchecked")
    private static Specification<TestEntity> anySpecification() {
        return any(Specification.class);
    }

    private static final class TestService extends BaseJpaReadService<TestEntity, Long, Long> {
        private TestService(BaseJpaRepository<TestEntity, Long> repository) {
            super(repository, TestEntity::id, TestEntity.class, Set.of());
        }
    }

    private record TestEntity(Long id) {
    }

    private record DummyFilter(String key) {
    }
}
