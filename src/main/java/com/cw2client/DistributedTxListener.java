package com.cw2client;

public interface DistributedTxListener {
    void onGlobalCommit();
    void onGlobalAbort();
}
