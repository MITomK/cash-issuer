package com.r3.corda.finance.cash.issuer.common.contracts

import com.r3.corda.finance.cash.issuer.common.*
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.r3.corda.finance.cash.issuer.common.states.BankAccountState
import com.r3.corda.finance.cash.issuer.common.types.BankAccountType
import com.r3.corda.finance.cash.issuer.common.types.UKAccountNumber
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.identity.AbstractParty
import java.util.*

class NodeTransactionCreateTests {
    private val ledgerServices = MockServices((listOf("com.r3.corda.finance.cash.issuer.common")))

    @Test
    fun mustIncludeAddCommand() {
    }

}