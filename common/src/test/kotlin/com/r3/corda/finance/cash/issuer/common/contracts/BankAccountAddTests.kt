package com.r3.corda.finance.cash.issuer.common.contracts

import com.r3.corda.finance.cash.issuer.common.*
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.r3.corda.finance.cash.issuer.common.states.BankAccountState
import com.r3.corda.finance.cash.issuer.common.types.BankAccountType
import com.r3.corda.finance.cash.issuer.common.types.UKAccountNumber
import java.util.*

class BankAccountAddTests {
    private val ledgerServices = MockServices((listOf("com.r3.corda.finance.cash.issuer.common")))

    @Test
    fun `must include add command`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID,  bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), DUMMY_COMMAND()) // Wrong type.
                this.fails()
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add()) // Correct type.
                this.verifies()
            }
        }
    }

    @Test
    fun `must have no input`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID,  bankAccount)
                output(BankAccountContract.CONTRACT_ID,  bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this `fails with` "No inputs should be consumed when issuing a new BankAccount."
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this.verifies()
            }
        }
    }

    @Test
    fun `must have only one output`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID,  bankAccount)
                output(BankAccountContract.CONTRACT_ID,  bankAccount.copy())
                command(listOf(CHARLIE.publicKey), BankAccountContract.Add())
                this `fails with` "Only one output state should be created when adding a BankAccount."
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this.verifies()
            }
        }
    }

    @Test
    fun `must have bankaccount output`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID, DUMMY_STATE())
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this.fails()
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this.verifies()
            }
        }
    }

    @Test
    fun `must be signed by owner`() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(CHARLIE.publicKey), BankAccountContract.Add())
                this `fails with` "The transaction is signed by the owner of the BankAccount."
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey), BankAccountContract.Add())
                this.verifies()
            }
        }
    }

    @Test
    fun `must be also signed by verifier when directly verified`() {
//    fun mustBeAlsoSignedByOwnerWhenDirectlyVerified() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val verifiedBankAccount = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID, verifiedBankAccount)
                command(listOf(ALICE.publicKey), BankAccountContract.Add())
                this `fails with` "The transaction is signed by the verifier of the BankAccount."
            }
            transaction {
                output(BankAccountContract.CONTRACT_ID, verifiedBankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Add())
                this.verifies()
            }
        }
    }
}