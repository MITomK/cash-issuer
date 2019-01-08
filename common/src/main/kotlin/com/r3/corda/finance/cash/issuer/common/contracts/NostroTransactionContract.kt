package com.r3.corda.finance.cash.issuer.common.contracts

import com.r3.corda.finance.cash.issuer.common.states.NostroTransactionState
import com.r3.corda.finance.cash.issuer.common.types.NostroTransactionStatus
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class NostroTransactionContract : Contract {

    companion object {
        @JvmStatic
        val CONTRACT_ID = "com.r3.corda.finance.cash.issuer.common.contracts.NostroTransactionContract"
    }

    interface Commands : CommandData
    class Add : Commands
    class Match : Commands

    // TODO: Contract code not implemented for demo.
    override fun verify(tx: LedgerTransaction) {
        // A transaction must have only one command of type add or match
        val command = tx.commands.requireSingleCommand<NostroTransactionContract.Commands>()

        when (command.value) {
            is Add -> {
                requireThat {
                    "The transaction must have no input states when adding new nostro transactions" using (tx.inputStates.isEmpty())
                    "The transaction must have at least one input state" using (tx.outputStates.isNotEmpty())
                    "All output states types must be of type NostroTransactionState" using (tx.outputsOfType<NostroTransactionState>().containsAll(tx.outputStates))
                }

            }

            is Match -> {
                val input = tx.inputsOfType<NostroTransactionState>()
                val output = tx.outputsOfType<NostroTransactionState>()

                requireThat {
                    "The transaction must have at least one input state of type NostroTransactionState" using (input.isNotEmpty())
                    "The transaction must have at least one output state NostroTransactionState" using (output.isNotEmpty())
                }

                val groups = tx.groupStates { it: NostroTransactionState -> it.linearId }
                for ((inputs, outputs, _) in groups) {
                    requireThat {
                        "One nostro transaction state must be consumed and one must be produced" using (inputs.size == 1 && outputs.size == 1)

                        val out = outputs.single()

                        "The NostroTransactionState output must not be unmatched" using (out.status != NostroTransactionStatus.UNMATCHED)
                    }

                }

            }

            else -> throw IllegalArgumentException("Unrecognised command")
        }
    }

}