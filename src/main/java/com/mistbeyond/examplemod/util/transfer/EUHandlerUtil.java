package com.mistbeyond.examplemod.util.transfer;

import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class EUHandlerUtil {
    private EUHandlerUtil() {
    }

    public static EUTransferInfo move(
            @Nullable EUEnergyHandler from,
            @Nullable EUEnergyHandler to,
            long amount,
            @Nullable TransactionContext transaction
    ) {
        if (from == null || to == null || amount == 0) return EUTransferInfo.ZERO;
        try (Transaction actualTransaction = Transaction.open(transaction)) {
            EUTransferInfo maxExtracted;
            try (Transaction simulation = Transaction.open(actualTransaction)) {
                maxExtracted = to.insertWith(from.extractWith(from.getVoltageTier(), amount, simulation), simulation);
            }
            var inserted = to.insertWith(maxExtracted, actualTransaction);
            if (!inserted.equals(from.extractWith(inserted, actualTransaction))) {
                return EUTransferInfo.ZERO;
            }
            actualTransaction.commit();
            return inserted;
        }
    }
}
