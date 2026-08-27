package id.my.alan.minikasir.domain.usecase;

import id.my.alan.minikasir.data.repository.SyncRepository;

/**
 * UseCase for triggering background synchronization of pending transactions.
 */
public class SyncTransactionsUseCase {

    private final SyncRepository syncRepository;

    public SyncTransactionsUseCase(SyncRepository syncRepository) {
        this.syncRepository = syncRepository;
    }

    public SyncRepository.SyncResult execute() {
        return syncRepository.syncPendingTransactions();
    }
}
