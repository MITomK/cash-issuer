package com.r3.corda.finance.cash.issuer.common.contracts

import com.r3.corda.finance.cash.issuer.common.states.BankAccountState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction

class BankAccountContract : Contract {

    companion object {
        @JvmStatic
        val CONTRACT_ID = "com.r3.corda.finance.cash.issuer.common.contracts.BankAccountContract"
    }

    interface Commands : CommandData
    class Add : Commands
    class Update : Commands

    override fun verify(tx: LedgerTransaction) {
        //  CURRENTLY WE ALLOW ONLY ONE UPDATED BANKACCOUNTSTATE AND NO BATCH
        val command = tx.commands.requireSingleCommand<BankAccountContract.Commands>()
        when (command.value) {
            is Add -> {
                "No inputs should be consumed when issuing a new BankAccount." using (tx.inputs.isEmpty())
                "Only one output state should be created when adding a BankAccount." using (tx.outputs.size == 1)
                val bankAccountOutput = tx.outputsOfType<BankAccountState>().single()
                "The transaction is signed by the owner of the BankAccount." using (bankAccountOutput.owner.owningKey in command.signers)
                if (bankAccountOutput.verified) {
                    "The transaction is signed by the verifier of the BankAccount." using (bankAccountOutput.verifier.owningKey in command.signers)
                }
            }
            // we currently interpret the Update-command more in the sense of a verify command
            // this means: an update can only be done on the verified status from false to true
            // the update can only be done once
            // later deletions, lockings, etc. of a bank account must be considered separately
            is Update -> {
                "An Update BankAccount transaction must consume one input state." using (tx.inputs.size == 1)
                "An Update BankAccount transaction must create one output state." using (tx.outputs.size == 1)

                val bankAccountInput = tx.inputsOfType<BankAccountState>().single()
                val bankAccountOutput = tx.outputsOfType<BankAccountState>().single()

                // IF AN INPUT STATE IS VERIFIED WE DO NOT ALLOW ANY CHANGES ANY MORE
                if (bankAccountInput.verified) {
                    throw IllegalArgumentException("Verified BankAccounts could not be further changed.")
                } else {
                    "Only the verify status must be changed." using (bankAccountInput == bankAccountOutput.copy(verified = false))
                    "The account must be verified." using (bankAccountOutput.verified == true)
                    "The transaction is signed by the verifier of the BankAccount." using (bankAccountOutput.verifier.owningKey in command.signers)
                }
            }
            else -> throw IllegalArgumentException("Unrecognised command")
        }
    }

}