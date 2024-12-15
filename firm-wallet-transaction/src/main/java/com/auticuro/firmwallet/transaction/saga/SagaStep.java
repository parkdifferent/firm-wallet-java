package com.auticuro.firmwallet.transaction.saga;

import com.auticuro.firmwallet.saga.SagaInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Consumer;

@Data
@EqualsAndHashCode(callSuper = true)
public class SagaStep extends SagaInstance.SagaStep {
    private String stepId;

    public SagaStep(String stepId, Consumer<SagaInstance> action, Consumer<SagaInstance> compensation) {
        super();
        this.stepId = stepId;
        setAction(action);
        setCompensation(compensation);
    }
}
