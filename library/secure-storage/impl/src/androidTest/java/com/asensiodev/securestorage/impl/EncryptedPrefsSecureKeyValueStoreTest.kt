package com.asensiodev.securestorage.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedPrefsSecureKeyValueStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val fileName = "encrypted_prefs_secure_key_value_store_test"

    @Before
    fun setUp() {
        context.deleteSharedPreferences(fileName)
    }

    @After
    fun tearDown() {
        context.deleteSharedPreferences(fileName)
    }

    @Test
    fun givenMissingKeyWhenReadStringThenReturnsNull() {
        val store = createStore()

        store.readString("missing").shouldBeNull()
    }

    @Test
    fun givenValueWhenWriteStringThenReadStringReturnsValue() {
        val store = createStore()

        store.writeString("key", "value")

        store.readString("key") shouldBeEqualTo "value"
    }

    @Test
    fun givenExistingValueWhenWriteStringThenOverwritesValue() {
        val store = createStore()
        store.writeString("key", "first")

        store.writeString("key", "second")

        store.readString("key") shouldBeEqualTo "second"
    }

    @Test
    fun givenExistingValueWhenRemoveThenReadStringReturnsNull() {
        val store = createStore()
        store.writeString("key", "value")

        store.remove("key")

        store.readString("key").shouldBeNull()
    }

    @Test
    fun givenMultipleValuesWhenClearAllThenAllValuesAreRemoved() {
        val store = createStore()
        store.writeString("first", "one")
        store.writeString("second", "two")

        store.clearAll()

        store.readString("first").shouldBeNull()
        store.readString("second").shouldBeNull()
    }

    @Test
    fun givenPersistedValueWhenStoreIsRecreatedThenValueRemainsAvailable() {
        createStore().writeString("key", "value")

        val recreatedStore = createStore()

        recreatedStore.readString("key") shouldBeEqualTo "value"
    }

    @Test
    fun givenCorruptedEncryptedKeysetWhenStoreIsCreatedThenFileIsReset() {
        context
            .getSharedPreferences(fileName, Context.MODE_PRIVATE)
            .edit()
            .putString("__androidx_security_crypto_encrypted_prefs_key_keyset__", "invalid-keyset")
            .putString("existing", "lost-value")
            .commit()

        val store = createStore()

        store.readString("existing").shouldBeNull()
        store.writeString("new-key", "new-value")
        store.readString("new-key") shouldBeEqualTo "new-value"
    }

    private fun createStore() = EncryptedPrefsSecureKeyValueStore(context, fileName)
}
