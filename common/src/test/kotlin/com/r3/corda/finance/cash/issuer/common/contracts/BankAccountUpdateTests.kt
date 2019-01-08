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
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.finance.POUNDS
import java.util.*

class BankAccountUpdateTests {
    private val ledgerServices = MockServices((listOf("com.r3.corda.finance.cash.issuer.common")))

    @Test
    fun `must include update command`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID,  bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), DUMMY_COMMAND())
                this.fails()
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update()) // Correct type.
                this.verifies()
            }
        }
    }

    @Test
    fun `must have one input and one output`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)

        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                input(BankAccountContract.CONTRACT_ID, DUMMY_STATE())
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "An Update BankAccount transaction must consume one input state."
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey),BankAccountContract.Update())
                this `fails with` "An Update BankAccount transaction must consume one input state."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey),BankAccountContract.Update())
                this `fails with` "An Update BankAccount transaction must create one output state."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "An Update BankAccount transaction must create one output state."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must not change an already vertified account`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val bankAccountVerified = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "Verified BankAccounts could not be further changed."
            }
        }
    }

    @Test
    fun `must be signed by verifier`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val bankAccountVerified = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                command(listOf(ALICE.publicKey), BankAccountContract.Update())
                this `fails with` "The transaction is signed by the verifier of the BankAccount."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must change only verification flag`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val bankAccountVerified = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with`  "Only the verify status must be changed."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must be verified by owner when not verificated`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(ALICE.publicKey), BankAccountContract.Update())
                this `fails with` "The transaction is signed by the verifier of the BankAccount."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(verified = true))
                command(listOf(BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

}