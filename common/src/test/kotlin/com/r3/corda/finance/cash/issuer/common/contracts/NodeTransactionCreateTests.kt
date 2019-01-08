package com.r3.corda.finance.cash.issuer.common.contracts

import com.r3.corda.finance.cash.issuer.common.*
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.r3.corda.finance.cash.issuer.common.states.BankAccountState
import com.r3.corda.finance.cash.issuer.common.states.NodeTransactionState
import com.r3.corda.finance.cash.issuer.common.types.BankAccountType
import com.r3.corda.finance.cash.issuer.common.types.NodeTransactionStatus
import com.r3.corda.finance.cash.issuer.common.types.NodeTransactionType
import com.r3.corda.finance.cash.issuer.common.types.UKAccountNumber
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant
import java.util.*

class NodeTransactionCreateTests {
    private val ledgerServices = MockServices((listOf("com.r3.corda.finance.cash.issuer.common")))

    @Test
    fun `must include create command`() {
//    fun mustIncludeCreateCommand() {
        val amountTransfer = AmountTransfer(
                quantityDelta = 50,
                token = Currency.getInstance("GBP"),
                source = ALICE.party,
                destination = BOB.party
        )
        val nodeTransaction = NodeTransactionState(amountTransfer, Instant.now(), listOf(ALICE.party), "Some notes", NodeTransactionType.ISSUANCE)
        ledgerServices.ledger {
            transaction {
                output(NodeTransactionContract.CONTRACT_ID, nodeTransaction)
                command(listOf(ALICE.publicKey, BOB.publicKey), DUMMY_COMMAND())
                this.verifies()
            }
            transaction {
                output(NodeTransactionContract.CONTRACT_ID, nodeTransaction)
                command(listOf(ALICE.publicKey, BOB.publicKey), NodeTransactionContract.Create())
                this.verifies()
            }
        }
    }

}
