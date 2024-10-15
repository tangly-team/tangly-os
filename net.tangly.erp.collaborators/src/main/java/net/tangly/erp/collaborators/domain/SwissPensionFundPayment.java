package net.tangly.erp.collaborators.domain;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Builder
public record SwissPensionFundPayment (
    @NotNull int year,
    @NotNull BigDecimal compulsorilyPayment,
    BigDecimal extraPayment){

}
