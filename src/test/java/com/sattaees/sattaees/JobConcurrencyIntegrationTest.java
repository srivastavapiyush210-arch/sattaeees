package com.sattaees.sattaees;

import com.sattaees.sattaees.config.TestConfig;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class JobConcurrencyIntegrationTest {

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should detect optimistic locking conflict when two transactions update Worker concurrently")
    void optimisticLocking_ConcurrentWorkerUpdates_ThrowsOptimisticLockException() {
        // 1. Create a worker record
        Worker worker = Worker.builder()
                .name("Concurrency Test Worker")
                .email("concurrency@test.com")
                .password("password")
                .phoneNumber("+91 9123456780")
                .skill("Electrician")
                .experience(4)
                .city("Pune")
                .hourlyRate(50.0)
                .available(true)
                .build();

        Worker savedWorker = workerRepository.saveAndFlush(worker);
        Long workerId = savedWorker.getId();
        Long initialVersion = savedWorker.getVersion();

        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);

        // 2. Transaction 1 reads worker
        Worker workerInTx1 = tx1.execute(status -> workerRepository.findById(workerId).orElseThrow());

        // 3. Transaction 2 reads, modifies, and commits the same worker
        tx2.execute(status -> {
            Worker workerInTx2 = workerRepository.findById(workerId).orElseThrow();
            workerInTx2.setAvailable(false);
            workerRepository.saveAndFlush(workerInTx2);
            return null;
        });

        // Verify version was incremented by Transaction 2
        Worker updatedWorker = workerRepository.findById(workerId).orElseThrow();
        assertThat(updatedWorker.getVersion()).isGreaterThan(initialVersion);

        // 4. Transaction 1 attempts to save with stale version -> Must fail with ObjectOptimisticLockingFailureException!
        assertThatThrownBy(() -> {
            tx1.execute(status -> {
                workerInTx1.setName("Overwritten Name");
                workerRepository.saveAndFlush(workerInTx1);
                return null;
            });
        }).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
