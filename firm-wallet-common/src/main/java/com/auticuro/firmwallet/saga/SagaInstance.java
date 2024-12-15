package com.auticuro.firmwallet.saga;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class SagaInstance {
    private String sagaId;
    private String transactionId;
    private List<SagaStep> steps = new ArrayList<>();
    private String status;
    private String error;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String currentStepId;
    private String compensatingStepId;

    @Data
    public static class SagaStep {
        private String stepId;
        private Consumer<SagaInstance> action;
        private Consumer<SagaInstance> compensation;
        private boolean executed;
        private String status;
        private String error;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public SagaStep() {
            this.status = "CREATED";
            this.executed = false;
            this.startTime = LocalDateTime.now();
        }

        public Consumer<SagaInstance> getAction() {
            return action;
        }

        public void setAction(Consumer<SagaInstance> action) {
            this.action = action;
        }

        public Consumer<SagaInstance> getCompensation() {
            return compensation;
        }

        public void setCompensation(Consumer<SagaInstance> compensation) {
            this.compensation = compensation;
        }

        public boolean isExecuted() {
            return executed;
        }

        public void setExecuted(boolean executed) {
            this.executed = executed;
        }
    }
}
