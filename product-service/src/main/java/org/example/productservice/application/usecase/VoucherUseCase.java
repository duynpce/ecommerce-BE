package org.example.productservice.application.usecase;

import org.example.productservice.application.command.SaveVoucherCommand;
import org.example.productservice.application.command.VoucherLine;
import org.example.productservice.domain.model.Voucher;
import org.example.productservice.domain.model.VoucherApplication;

import java.util.List;
import java.util.UUID;

public interface VoucherUseCase {
    Voucher create(SaveVoucherCommand command, UUID actorId, boolean admin);
    Voucher update(UUID id, SaveVoucherCommand command, UUID actorId, boolean admin);
    Voucher findById(UUID id);
    List<Voucher> findManageable(UUID actorId, boolean admin);
    List<VoucherApplication> findAvailable(List<VoucherLine> lines);
    void delete(UUID id, UUID actorId, boolean admin);
    VoucherApplication preview(String code, List<VoucherLine> lines);
    void consume(VoucherApplication application);
}
