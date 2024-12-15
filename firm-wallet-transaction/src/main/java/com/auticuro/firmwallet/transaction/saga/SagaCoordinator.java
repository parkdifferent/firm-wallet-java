package com.auticuro.firmwallet.transaction.saga;

import com.auticuro.firmwallet.saga.SagaInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    public void executeSaga(SagaInstance saga) {
        try {
            executeSteps(saga);
        } catch (Exception e) {
            compensateSteps(saga);
            throw e;
        }
    }

    private void executeSteps(SagaInstance saga) {
        for (SagaInstance.SagaStep step : saga.getSteps()) {
            try {
                step.getAction().accept(saga);
                step.setExecuted(true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute step: " + step.getStepId(), e);
            }
        }
    }

    private void compensateSteps(SagaInstance saga) {
        List<SagaInstance.SagaStep> reversedSteps = saga.getSteps();
        Collections.reverse(reversedSteps);

        for (SagaInstance.SagaStep step : reversedSteps) {
            if (step.isExecuted() && step.getCompensation() != null) {
                try {
                    step.getCompensation().accept(saga);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to compensate step: " + step.getStepId(), e);
                }
            }
        }
    }
}
