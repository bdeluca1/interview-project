package com.reconciliation.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TxnPair {
    public TxnPair(InternalTxn internal, ProcessorTxn processor){
        this.internal = internal;
        this.processor = processor;
    }

    private InternalTxn internal;
    private ProcessorTxn processor;
}
