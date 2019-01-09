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
        // TODO: Test if this allows more than one command of different types
        val command = tx.commands.requireSingleCommand<NostroTransactionContract.Commands>()

        requireThat {
            val groups = tx.groupStates { it: NostroTransactionState -> it.participants }
            val issuers = groups.first().outputs.first().participants
            "NostroTransactionStates must have only one list of participants" using (groups.size == 1)
            "Must have only one participant/issuer per NostroTransactionState" using (issuers.size == 1)
            "The participant must be the signer of the NostroTransactionState" using (command.signingParties.contains(issuers.first()))
        }

        when (command.value) {
            is Add -> {
                requireThat {
                    val out = tx.outputsOfType<NostroTransactionState>()
                    "The transaction must have no input states when adding new nostro transactions" using (tx.inputStates.isEmpty())
                    "The transaction must have at least one input state" using (tx.outputStates.isNotEmpty())
                    "All output states types must be of type NostroTransactionState" using (out.containsAll(tx.outputStates))

                    for (it in out) {
                        if (it.type == NostroTransactionStatus.MATCHED) throw IllegalArgumentException("Adding new NostroTransactions with status matched is prohibited")
                    }
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