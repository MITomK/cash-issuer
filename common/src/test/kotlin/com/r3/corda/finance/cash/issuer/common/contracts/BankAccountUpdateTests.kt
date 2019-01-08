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
//    mustIncludeUpdateCommand() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                output(BankAccountContract.CONTRACT_ID,  bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), DUMMY_COMMAND())
                this.fails()
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update()) // Correct type.
                this.verifies()
            }
        }
    }

    @Test
    fun `must have one input and one output`() {
//        fun mustHaveOneInputAndOneOutput() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)

        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                input(BankAccountContract.CONTRACT_ID, DUMMY_STATE())
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
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
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name 1"))
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name 2"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "An Update BankAccount transaction must create one output state."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must update the same account`() {
//    fun mustUpdateSameAccount() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(linearId = UniqueIdentifier("OTHER")))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "Only the same BankAccount can be updated."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must change the account`() {
//    fun mustChangeAccount() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount)
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "Something has to be changed."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

    @Test
    fun `must not change an already vertified account`() {
//    fun mustNotChangeAlreadyVerifiedAccount() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val bankAccountVerified = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccountVerified)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "Verified BankAccounts could not be further changed."
            }
        }
    }

    @Test
    fun `must be signed by verifier when verification occurs`() {
//    fun mustBeSignedByVerifierOnVerification() {
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
//    fun mustOnlyChangeVerifiyFlag() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        val bankAccountVerified = bankAccount.copy(verified = true)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccountVerified.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey, BOB.publicKey), BankAccountContract.Update())
                this `fails with` "On verification only the verify state can be changed."
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
//    fun mustBeVerifiedByOwnerOnNormalUpdate() {
        val bankAccount = BankAccountState(ALICE.party, BOB.party, "1234", "Dummy Account", UKAccountNumber("10000000000014"), Currency.getInstance("USD"), BankAccountType.COLLATERAL)
        ledgerServices.ledger {
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(BOB.publicKey), BankAccountContract.Update())
                this `fails with` "The transaction is signed by the owner of the BankAccount."
            }
            transaction {
                input(BankAccountContract.CONTRACT_ID, bankAccount)
                output(BankAccountContract.CONTRACT_ID, bankAccount.copy(accountName = "New Name"))
                command(listOf(ALICE.publicKey), BankAccountContract.Update())
                this.verifies()
            }
        }
    }

}