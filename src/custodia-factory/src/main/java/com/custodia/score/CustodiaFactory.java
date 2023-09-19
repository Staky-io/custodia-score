package com.custodia.score;

import com.custodia.score.util.EnumerableSet;

import score.Address;

import score.Context;
import score.ObjectReader;
import score.ObjectWriter;

import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.VarDB;
import score.BranchDB;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;

public class CustodiaFactory {
    private final VarDB<String> contractData = Context.newVarDB("contractData", String.class);
    private final BranchDB<Address, EnumerableSet> memberships = Context.newBranchDB("memberships", Address.class);
    private final EnumerableSet<Address> contracts = new EnumerableSet<>("contracts", Address.class);
    
    public CustodiaFactory(String _data) {
        _setCustodiaContractByteCode(_data);
    }
    
    protected void checkOwnerOrThrow() {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(1, "Not contract owner");
        }
    }

    @External(readonly=true)
    public String getCustodiaContract() {
        return contractData.get();
    }

    @External
    public void setCustodiaContractByteCode(String _data) {
        checkOwnerOrThrow();
        _setCustodiaContractByteCode(_data);
    }

    @External
    public void deployCustodia(String _data, Address[] _owners, BigInteger _required) {
        byte [] code = contractData.get().getBytes();
        Address newScore = Context.deploy(code, _owners, _required, Context.getAddress());
        var length = _owners.length;
        for (var i = 0; i < length; i++) {
            memberships.at(newScore).add(_owners[i]);
        }
        ContractDeployed(newScore);
    }

    @External
    public void addOwner(Address _score, Address _owner) {
        if (!Context.getCaller().equals(_score)) {
            Context.revert(1, "Can only be called by the score itself");
        }
        memberships.at(_score).add(_owner);
    }

    @External
    public void removeOwner(Address _score, Address _owner) {
        if (!Context.getCaller().equals(_score)) {
            Context.revert(1, "Can only be called by the score itself");
        }
        memberships.at(_score).remove(_owner);
    }

    private void _setCustodiaContractByteCode(String _data){
        contractData.set(_data);
    }

    @EventLog(indexed=1)
    public void ContractDeployed(Address _receiver) {}
}
