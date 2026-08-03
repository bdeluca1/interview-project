package com.reconciliation.controller;

import com.reconciliation.beans.InternalTxn;
import com.reconciliation.beans.ProcessorTxn;
import com.reconciliation.beans.TxnPair;
import com.reconciliation.converters.CsvToJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.reconciliation.converters.JsonToList;
import com.reconciliation.util.Validations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Locale.filter;
import static java.util.spi.ToolProvider.findFirst;

@RestController
@RequestMapping("/api")
public class BaseRestController {

    private final CsvToJson csvToJson;
    private final JsonToList jsonToList;

    public BaseRestController(CsvToJson csvToJson, JsonToList jsonToList) {
        this.csvToJson = csvToJson;
        this.jsonToList = jsonToList;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

//    @GetMapping("/reconcile")
//    public List<TxnPair> reconcile(){
//        return null;
//    }
    @GetMapping("/csv-json")
    public List<TxnPair> csvJson() throws IOException {
        List<TxnPair> salesPairs = new ArrayList<>();
        List<TxnPair> refundPairs = new ArrayList<>();
        List<TxnPair> duplicates = new ArrayList<>();
        List<TxnPair> amtProblemPairs = new ArrayList<>();
        List<TxnPair> feeProblemPairs = new ArrayList<>();
        List<TxnPair> salesMerchRefEmPairs = new ArrayList<>();
        List<TxnPair> refundMerchEmPairs = new ArrayList<>();


        List<InternalTxn> internalPure = csvToJson.convertCsvToJson();
        System.out.println(internalPure.size()  + " size of internal list");
        List<InternalTxn> internal = new ArrayList<>();
        internal.addAll(internalPure);
        List<ProcessorTxn> processorPure = jsonToList.convert();
        System.out.println("size of processor list " + processorPure.size());
        List<ProcessorTxn>processor =new ArrayList<>();
        processor.addAll(processorPure);

        // Start with finding clean matches and dups
        for (InternalTxn iTxn : internal) {
            //Try to find clean match for Sales
            List<ProcessorTxn> match = processor.stream()
                    .filter(processorTxn ->
                            Objects.equals(iTxn.getMerchRef(), processorTxn.getMerchRef())
                                    && Objects.equals(iTxn.getType(), "SALE")
                                    && processorTxn.getSettledAmt() != null
                                    && processorTxn.getSettledAmt().doubleValue() > 0
                    )
                    .toList();
            //Find Dups
            if (!match.isEmpty() ) {
                Set<String> seen = new HashSet<>();
                // rename temp ?  maybe for clarity
                for (ProcessorTxn pTxn : match) {
                    String key = pTxn.getMerchRef()
                            + "|" + pTxn.getCardLast4()
                            + "|" + pTxn.getSettledAmt()
                            + "|" + pTxn.getSettleDate();

                    if (!seen.add(key)) {
                        duplicates.add(new TxnPair(iTxn, pTxn));
                        processorPure.remove(pTxn);

                    } else {
                        // Check amounts 
                        if (Validations.checkForAmountProblem(pTxn, iTxn)) {
                            amtProblemPairs.add(new TxnPair(iTxn, pTxn));

                        } else if (Validations.checkForFeeProb(pTxn, iTxn)) {
                            // There is a fee problem
                            feeProblemPairs.add(new TxnPair(iTxn, pTxn));
                        } else {
                            // Yay clean recon
                            salesPairs.add(new TxnPair(iTxn, pTxn));
                        }

                        processorPure.remove(pTxn);
                    }
                }
                internalPure.remove(iTxn);
            }
        }

        // Find refunds
        System.out.println("InternalPure " + internalPure.size());
        internal.clear();
        internal.addAll(internalPure);
        processor.clear();
        processor.addAll(processorPure);
        for (InternalTxn txn : internal){
            //Try to find clean match for Refunds
            Optional<ProcessorTxn> match = processor.stream()
                    .filter(processorTxn ->
                            Objects.equals(txn.getMerchRef(), processorTxn.getMerchRef())
                                    && Objects.equals(txn.getType(), "REFUND")
                                    && processorTxn.getSettledAmt() != null
                                    && processorTxn.getSettledAmt().doubleValue() < 0
                    )
                    .findFirst();
            if (match.isPresent()){
                refundPairs.add(new TxnPair(txn, match.get()));
                internalPure.remove(txn);
                processorPure.remove(match.get());
            }
        }


        // Try to match items without a merch ref from processor
        // settlement + fees must equal our gross solidly
        // I expect manual exception processing will have to occur
        // Seems too risky w/o merch ref.
        internal.clear();
        internal.addAll(internalPure);
        processor.clear();
        processor.addAll(processorPure);
        for (InternalTxn iTxn :internal){
            Optional<ProcessorTxn> match = processor.stream()
                    .filter(pTxn ->
                            Objects.equals(iTxn.getCardLast4(), pTxn.getCardLast4())
                            && Objects.equals(iTxn.getMerchId(), pTxn.getMerchId())
                            && iTxn.getGrossAmt().compareTo(pTxn.getSettledAmt()
                                            .add(pTxn.getProcessorFee())
                                            .add(pTxn.getInterchangeFee())
                                        ) == 0
                    )
                    .findFirst();
            if (match.isPresent()){
                if(iTxn.getType().equalsIgnoreCase("SALE")){
                    salesMerchRefEmPairs.add(new TxnPair(iTxn, match.get()));
                }else{
                    // Assuming Refund -- not good? not sure probably not
                    refundMerchEmPairs.add(new TxnPair(iTxn, match.get()));
                }
                internalPure.remove(iTxn);
                processorPure.remove(match.get());
            }
        }

        System.out.println("SalesPair size: " + salesPairs.size());
        System.out.println("Refund Size: "  + refundPairs.size());
        System.out.println("internalPureSize: " + internalPure.size());
        System.out.println("processor size: " + processorPure.size());
        System.out.println("dups shown in sale:  " + duplicates.size());
        System.out.println("amount discrepancy:  " + amtProblemPairs.size());
        System.out.println("fee discrepancy:  " + feeProblemPairs.size());
        System.out.println("sales no merch ref: " + salesMerchRefEmPairs.size());
        System.out.println("ref no merch ref: " + refundMerchEmPairs.size());


        return null;
    }
}
