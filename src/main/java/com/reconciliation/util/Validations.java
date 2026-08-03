package com.reconciliation.util;

import com.reconciliation.beans.InternalTxn;
import com.reconciliation.beans.ProcessorTxn;
import com.reconciliation.enums.CardFees;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.round;

public class Validations {
    public static boolean checkForFeeProb(ProcessorTxn pTxn, InternalTxn iTxn){
        CardFees feeType = CardFees.valueOf(pTxn.getCardType().trim().toUpperCase());
        double gross = iTxn.getGrossAmt().doubleValue();
        double fee =  gross * feeType.getInterchangeRate().doubleValue();
        fee += feeType.getInterchangeFlat().doubleValue();

        // debug purposes.
        if (fee > (pTxn.getInterchangeFee().doubleValue() + pTxn.getInterchangeFee().doubleValue())){
            return true;
        }
        return false;
    }


    public static boolean checkForAmountProblem(ProcessorTxn pTxn, InternalTxn iTxn){
        // Calculate expected fee based on internal gross.
        CardFees feeType = CardFees.valueOf(pTxn.getCardType().trim().toUpperCase());
        double intFee = iTxn.getGrossAmt().doubleValue() * feeType.getInterchangeRate().doubleValue() ;
        intFee += round(feeType.getInterchangeFlat().doubleValue());
        BigDecimal bIntFee = BigDecimal.valueOf(intFee).setScale(2, RoundingMode.HALF_UP);


        double procFee = iTxn.getGrossAmt().doubleValue() * CardFees.PROC.getInterchangeRate().doubleValue() +CardFees.PROC.getInterchangeFlat().doubleValue();
        BigDecimal bProcFee = BigDecimal.valueOf(procFee).setScale(2,RoundingMode.HALF_UP);
        double expectedSettleAmt = iTxn.getGrossAmt().doubleValue() - procFee;
        // debug purposes not rolled into return stmt.
        if (expectedSettleAmt != pTxn.getSettledAmt().doubleValue()){
            return true;
        }
        return false;
    }
}
